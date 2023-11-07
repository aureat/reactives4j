package reactives4j.core;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactives4j.util.ReactiveUtil;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Context {

    /**
     * Counter used to generate unique names for contexts.
     */
    private static int counter = 0;

    /**
     * Runtime associated with the current context.
     */
    @Getter(AccessLevel.PUBLIC)
    private final Runtime runtime = Runtime.create(this);

    /**
     * Flag indicating whether the context is active or disposed.
     */
    @Getter(AccessLevel.PUBLIC)
    private boolean active = true;

    /**
     * Unique identifier for the current context.
     */
    @Getter(AccessLevel.PUBLIC)
    private String name;

    /**
     * Type of the runtime service associated with the current context.
     */
    private ServiceType serviceType = ServiceType.Synced;

    /**
     * Thread associated with the current context.
     */
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private Thread thread;

    /**
     * Runtime service associated with the current context.
     */
    private ExecutorService service;

    /**
     * Creates a new context and initializes it.
     * The context is automatically destroyed when the runtime is disposed.
     *
     * @return a new context
     */
    @Synchronized
    @Contract("-> new")
    public static @NotNull Context create() {
        var context = new Context();
        context.name = "" + counter++;
        context.logDebug("Creating a new reactive context.");
        return context;
    }

    @Synchronized
    @Contract("_ -> new")
    public static @NotNull Context create(String name) {
        var context = create();
        context.name = name;
        return context;
    }

    /**
     * Creates a runtime service with a dedicated thread.
     *
     * @see Thread.Builder.OfPlatform
     */
    public Context withDedicatedService() {
        serviceType = ServiceType.Dedicated;
        service = Executors.newSingleThreadExecutor(r -> {
            thread = new Thread(r);
            return thread;
        });
        active = true;
        return this;
    }

    /**
     * Creates a runtime service with a dedicated virtual thread.
     *
     * @see Thread.Builder.OfVirtual
     */
    public Context withDedicatedVirtualService() {
        serviceType = ServiceType.Virtual;
        service = Executors.newFixedThreadPool(1, r -> {
            thread = Thread.ofVirtual().factory().newThread(r);
            return thread;
        });
        active = true;
        return this;
    }

    /**
     * Creates a runtime service with the provided executor service.
     *
     * @see Executors
     * @see ExecutorService
     */
    public Context withProvidedService(Function<Context, ExecutorService> provider) {
        serviceType = ServiceType.Provided;
        service = provider.apply(this);
        active = true;
        return this;
    }

    public boolean isSynced() {
        return serviceType == ServiceType.Synced;
    }

    /**
     * Give a custom name to the runtime context.
     * This is useful for debugging purposes.
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Checks if the runtime service associated with the current context is destroyed.
     * Returns true if the runtime service is terminated or shutdown.
     */
    public boolean isShutdown() {
        if (isSynced()) {
            logWarn("Checking the status of a synced runtime service. Use isActive() instead.");
            return active;
        }
        return service.isTerminated() || service.isShutdown();
    }

    /**
     * Checks if the current thread is the reactive thread associated with the current context.
     */
    public boolean isReactiveThread() {
        return Thread.currentThread() == thread;
    }

    /**
     * Takes an inner value and returns a reactive and mutable value.
     * <p> The inner value can be retrieved using {@link Reactive#get()} or equivalent methods inside a reactive closure.
     * Read (retrieve) operations are tracked. </p>
     * <p> The value can also be updated using {@link Reactive#set(Object)} or equivalent methods.
     * Write (update) operations trigger associated effects. </p>
     *
     * @param value inner value
     * @param <T>   type of the inner value
     * @return the new reactive value
     * @see Reactive
     */
    @Contract("_ -> new")
    public <T> @NotNull Reactive<T> reactive(T value) {
        return Reactive.create(this, value);
    }

    /**
     * Creates a new trigger. Triggers are used to track operations
     * and trigger updates without holding a value.
     *
     * @return the new trigger
     * @see Trigger
     */
    @Contract("-> new")
    public @NotNull Trigger trigger() {
        return Trigger.create(this);
    }

    /**
     * @see #memo(Supplier, boolean)
     */
    @Contract("_ -> new")
    public <T> @NotNull Memo<T> memo(@NotNull Supplier<T> fx) {
        return Memo.create(this, fx, false);
    }

    /**
     * Takes a getter function and returns a readonly reactive value.
     * <p> The value can be retrieved using {@link Reactive#get()} or equivalent methods inside a reactive closure.
     * Just like {@link #reactive(Object)}, read (retrieve) operations on memos are also tracked. </p>
     *
     * @param fx   getter function
     * @param lazy if true, the memo is not evaluated immediately
     * @param <T>  type of the inner value
     * @return the new memo
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Memo<T> memo(@NotNull Supplier<T> fx, boolean lazy) {
        return Memo.create(this, fx, lazy);
    }

    /**
     * Takes a reactive closure and returns an effect handle.
     * Effects are used to trigger side effects without holding a value.
     * Effects are queued to run once immediately after creation,
     * and re-run whenever their dependencies change.
     *
     * @param fx   reactive closure
     * @param lazy if true, the effect is not evaluated immediately
     * @return the new effect
     */
    @Contract("_, _ -> new")
    public @NotNull Handle effect(@NotNull Runnable fx, boolean lazy) {
        return Effect.create(this, fx, lazy);
    }

    /**
     * @see #effect(Runnable, boolean)
     */
    @Contract("_ -> new")
    public @NotNull Handle effect(@NotNull Runnable fx) {
        return Effect.create(this, fx, true);
    }

    /**
     * @see #watchEffect(Reactive, Consumer, boolean)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Reactive<T> rx, @NotNull Consumer<T> fx) {
        return WatchEffect.create(this, rx, (value, _old) -> fx.accept(value));
    }

    /**
     * Watch on a reactive value that does not track the old value.
     *
     * @param immediate if true, the watch is evaluated immediately
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    @Contract("_, _, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Reactive<T> rx, @NotNull Consumer<T> fx, boolean immediate) {
        return WatchEffect.create(this, rx, (value, _old) -> fx.accept(value), immediate);
    }

    /**
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Reactive<T> rx, @NotNull BiConsumer<T, T> fx) {
        return WatchEffect.create(this, rx, fx);
    }

    /**
     * Takes a reactive value and a function, and returns a watch handle.
     * Watches explicitly declare their dependency and run only when their dependency changes.
     * You can also pass in {@code immediate = true} to run the watch immediately after creation.
     * This variant tracks the old value of the reactive value.
     * For a variant that does not track the old value, see {@link #watchEffect(Reactive, Consumer, boolean)}.
     *
     * @param <T>       type of the reactive value
     * @param rx        reactive value
     * @param fx        reactive closure
     * @param immediate if true, the watch is evaluated immediately
     */
    @Contract("_, _, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Reactive<T> rx, @NotNull BiConsumer<T, T> fx, boolean immediate) {
        return WatchEffect.create(this, rx, fx, immediate);
    }

    /**
     * @see #watchEffect(Reactive, Consumer, boolean)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Memo<T> rx, @NotNull Consumer<T> fx) {
        return WatchEffect.create(this, rx, (value, _old) -> fx.accept(value));
    }

    /**
     * @see #watchEffect(Reactive, Consumer, boolean)
     */
    @Contract("_, _, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Memo<T> rx, @NotNull Consumer<T> fx, boolean immediate) {
        return WatchEffect.create(this, rx, (value, _old) -> fx.accept(value), immediate);
    }

    /**
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Memo<T> rx, @NotNull BiConsumer<T, T> fx) {
        return WatchEffect.create(this, rx, fx);
    }

    /**
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    @Contract("_, _, _ -> new")
    public <T> @NotNull Handle watchEffect(@NotNull Memo<T> rx, @NotNull BiConsumer<T, T> fx, boolean immediate) {
        return WatchEffect.create(this, rx, fx, immediate);
    }

    /**
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    @Contract("_, _ -> new")
    public @NotNull Handle watchEffect(@NotNull Trigger rx, @NotNull Runnable fx) {
        BiConsumer<Void, Void> f = (_1, _2) -> fx.run();
        return WatchEffect.create(this, rx, f);
    }

    /**
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    @Contract("_, _, _ -> new")
    public @NotNull Handle watchEffect(@NotNull Trigger rx, @NotNull Runnable fx, boolean immediate) {
        BiConsumer<Void, Void> f = (_1, _2) -> fx.run();
        return WatchEffect.create(this, rx, f, immediate);
    }

    /**
     * @see #watchEffect(Reactive, Consumer, boolean)
     */
    public <T> @NotNull Handle watchEffect(@NotNull Resource<T> rx, @NotNull Consumer<T> fx) {
        return watchEffect(rx.value(), fx);
    }

    /**
     * @see #watchEffect(Reactive, Consumer, boolean)
     */
    public <T> @NotNull Handle watchEffect(@NotNull Resource<T> rx, @NotNull Consumer<T> fx, boolean immediate) {
        return watchEffect(rx.value(), fx, immediate);
    }

    /**
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    public <T> @NotNull Handle watchEffect(@NotNull Resource<T> rx, @NotNull BiConsumer<T, T> fx) {
        return watchEffect(rx.value(), fx);
    }

    /**
     * @see #watchEffect(Reactive, BiConsumer, boolean)
     */
    public <T> @NotNull Handle watchEffect(@NotNull Resource<T> rx, @NotNull BiConsumer<T, T> fx, boolean immediate) {
        return watchEffect(rx.value(), fx, immediate);
    }

    /**
     * Creates a new job.
     * Jobs are used to run tasks asynchronously.
     * Jobs are scheduled to run once immediately after creation,
     * and re-run whenever their dependencies change.
     *
     * @see Job
     */
    public @NotNull TaskHandle job(@NotNull Runnable fx) {
        return Job.create(this, fx);
    }

    /**
     * Takes a supplier and returns a resource with an initial value of null.
     *
     * @see #resource(Supplier, Object)
     */
    public <T> @NotNull Resource<T> resource(@NotNull Supplier<T> fx) {
        return Resource.create(this, fx);
    }

    /**
     * Takes a supplier and an initial value, and returns a resource.
     *
     * @see #resource(Supplier)
     */
    public <T> @NotNull Resource<T> resource(@NotNull Supplier<T> fx, T initialValue) {
        return Resource.create(this, fx, initialValue);
    }

    /**
     * Sets up a job that runs whenever the reactive value changes.
     * It is similar to {@link #watchEffect(Reactive, BiConsumer)} but it runs asynchronously.
     * The job is not run immediately after creation.
     */
    public <T> @NotNull Handle watchJob(@NotNull Reactive<T> rx, @NotNull BiConsumer<T, T> fx) {
        var watch = new WatchJob<T>(this);
        watch.setSource(rx, fx);
        return watch;
    }

    /**
     * @see #watchJob(Reactive, BiConsumer)
     */
    public <T> @NotNull Handle watchJob(@NotNull Reactive<T> rx, @NotNull Consumer<T> fx) {
        var watch = new WatchJob<T>(this);
        watch.setSource(rx, fx);
        return watch;
    }

    /**
     * @see #watchJob(Reactive, BiConsumer)
     */
    public @NotNull Handle watchJob(@NotNull Trigger rx, @NotNull Runnable fx) {
        var watch = new WatchJob<Void>(this);
        watch.setSource(rx, fx);
        return watch;
    }

    /**
     * @see #watchJob(Reactive, BiConsumer)
     */
    public <T> @NotNull Handle watchJob(@NotNull Memo<T> rx, @NotNull BiConsumer<T, T> fx) {
        var watch = new WatchJob<T>(this);
        watch.setSource(rx, fx);
        return watch;
    }

    /**
     * @see #watchJob(Reactive, BiConsumer)
     */
    public <T> @NotNull Handle watchJob(@NotNull Memo<T> rx, @NotNull Consumer<T> fx) {
        var watch = new WatchJob<T>(this);
        watch.setSource(rx, fx);
        return watch;
    }

    /**
     * @see #watchJob(Reactive, BiConsumer)
     */
    public <T> @NotNull Handle watchJob(@NotNull Resource<T> rx, @NotNull BiConsumer<T, T> fx) {
        var watch = new WatchJob<T>(this);
        watch.setSource(rx, fx);
        return watch;
    }

    /**
     * @see #watchJob(Reactive, BiConsumer)
     */
    public <T> @NotNull Handle watchJob(@NotNull Resource<T> rx, @NotNull Consumer<T> fx) {
        var watch = new WatchJob<T>(this);
        watch.setSource(rx, fx);
        return watch;
    }

    /**
     * Submits an asynchronous task that runs on its own thread.
     * Returns a future that can be used to retrieve the result of the computation.
     * Technically, these are virtual threads, designed for these sort of tasks.
     * This variant returns a result, for a variant that doesn't return a result, {@link TaskContext#submitTask(Runnable)}
     *
     * @see Resource
     * @see Job
     */
    public <R> @NotNull Future<R> task(@NotNull Callable<R> fx) {
        return TaskContext.getContext().submitTask(fx);
    }

    /**
     * Submits an asynchronous task that runs on its own thread.
     * Returns a future that can be used to check task completion.
     *
     * @see TaskContext#submitTask(Callable)
     */
    public @NotNull Future<Void> task(@NotNull Runnable fx) {
        return TaskContext.getContext().submitTask(fx);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed,
     * but no new tasks will be accepted. Invocation has no additional effect if already shut down.
     * For an immediate shutdown, see {@link #shutdownNow()}.
     */
    void shutdown() {
        checkShutdown();
        service.shutdown();
        try {
            var didShutdown = service.awaitTermination(100, TimeUnit.MILLISECONDS);
            if (didShutdown) {
                logDebug("Runtime service was shutdown successfully.");
            } else {
                logDebug(
                        "Runtime service failed to shut down in an orderly manner.\n" +
                                "Shutting down abruptly. Scheduled tasks did not execute in time."
                );
                service.shutdownNow();
            }
        } catch (Exception e) {
            service.shutdownNow();
            logWarn("Runtime service shutdown unexpectedly.");
        }
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks.
     * Does not wait until all actively executing tasks are terminated.
     */
    void shutdownNow() {
        checkShutdown();
        service.shutdownNow();
        logWarn("Runtime service was shutdown abruptly. Scheduled tasks were not executed.");
    }

    /**
     * Takes a runtime and a task, and returns a future that can be used to check task completion.
     */
    @NotNull Future<Void> submit(@NotNull Runtime runtime, @NotNull Consumer<Runtime> f) {
        checkStatus();
        return service.submit(() -> f.accept(runtime), null);
    }

    /**
     * Takes a runtime and a task, and returns a future that can be used to retrieve the result of the computation.
     */
    <T> @NotNull Future<T> submit(@NotNull Runtime runtime, @NotNull Function<Runtime, T> f) {
        checkStatus();
        return service.submit(() -> f.apply(runtime));
    }

    /**
     * Executes the given runtime task with the runtime associated with the current context.
     * Note that this function is synchronous and will block the current thread until the computation is finished.
     * For the asynchronous variant, use {@link #submitWith(Consumer)}.
     *
     * @param task a function that performs a computation using the runtime
     */
    public void with(@NotNull Consumer<Runtime> task) {
        if (isReactiveThread() || isSynced()) {
            task.accept(runtime);
            return;
        }

        logDebug("Blocking the current thread until the computation is finished.");
        Future<Void> future = submit(runtime, task);
        try {
            future.get();
        } catch (Exception e) {
            ReactiveUtil.panic(e);
        }
    }

    /**
     * Executes the given runtime task with the runtime associated with the current context and returns the result.
     * Note that this function is synchronous and will block the current thread until the result is ready.
     * For the asynchronous variant, use {@link #submitWith(Function)}.
     *
     * @param task a function that performs a computation using the runtime
     */
    public <R> @Nullable R with(@NotNull Function<Runtime, R> task) {
        if (isReactiveThread() || isSynced()) {
            return task.apply(runtime);
        }

        logDebug("Blocking the current thread until the result is ready.");
        Future<R> future = submit(runtime, task);
        try {
            return future.get();
        } catch (Exception e) {
            ReactiveUtil.panic(e);
            return null;
        }
    }

    /**
     * Attempts to perform the given runtime task. If called on the reactive thread, the task is executed immediately.
     * Otherwise, the task is submitted to the runtime queue.
     *
     * @param task a function that performs a computation using the runtime
     */
    public void doWith(@NotNull Consumer<Runtime> task) {
        if (isReactiveThread() || isSynced()) {
            task.accept(runtime);
            return;
        }

        logDebug("Submitting a task to the runtime service.");
        submit(runtime, task);
    }

    /**
     * Submits the given runtime task to the runtime associated with the current context.
     * Returns a future that can be used to check task completion.
     *
     * @param task a function that performs a computation using the runtime
     */
    public Future<Void> submitWith(@NotNull Consumer<Runtime> task) {
        logDebug("Submitting a task to the runtime service.");
        return submit(runtime, task);
    }

    /**
     * Submits the given runtime task to the runtime associated with the current context.
     * Returns a future that can be used to retrieve the result of the computation.
     *
     * @param task a function that performs a computation using the runtime
     */
    public <R> Future<R> submitWith(@NotNull Function<Runtime, R> task) {
        logDebug("Submitting a task to the runtime service.");
        return submit(runtime, task);
    }

    /**
     * Disposes the current context and runtime, and shuts down the runtime service.
     */
    @Synchronized
    public void dispose() {
        if (!isSynced())
            shutdown();
        runtime.disposeRuntime();
        active = false;
    }

    /**
     * Disposes the current context and runtime, and shuts down the runtime service immediately.
     */
    @Synchronized
    public void disposeNow() {
        if (!isSynced())
            shutdownNow();
        runtime.disposeRuntime();
        active = false;
    }

    void checkStatus() {
        if (isSynced() && !active)
            ReactiveUtil.panic("Disposed context");

        if (isShutdown())
            ReactiveUtil.panic("Terminated runtime service");
    }

    void checkShutdown() {
        if (isSynced()) {
            logWarn("Shutting down a synced runtime service.");
        }

        if (isShutdown()) {
            logWarn("Shutting down a terminated runtime service.");
        }
    }

    /**
     * Warns about a blocking function.
     *
     * @param current     blocking operation being used
     * @param alternative non-blocking alternative
     */
    void warnBlocking(String current, String alternative) {
        if (isSynced()) return;
        if (isReactiveThread()) return;

        logWarn(
                "Use of `" + current + "` outside of a reactive scope.\n" +
                        "This operation will block the current thread until the computation is finished. " +
                        "Consider using " + alternative + "` instead."
        );
    }

    void logWarn(String message) {
        log.warn(String.format("%s: %s", getDebugName(), message));
    }

    void logDebug(String message) {
        log.debug(String.format("%s: %s", getDebugName(), message));
    }

    String getDebugName() {
        return String.format("Context(%s)", name);
    }

    @Override
    public String toString() {
        return String.format("Context(%s, %s)", name, active ? "active" : "disposed");
    }

    enum ServiceType {
        Synced,
        Dedicated,
        Virtual,
        Provided
    }

}
