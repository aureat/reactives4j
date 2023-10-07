package org.playground.reactives;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.playground.reactives.util.ReactiveUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2
public class Context {

    /**
     * Counter used to generate unique names for contexts.
     */
    private static int counter = 0;

    /**
     * Runtime associated with the current context.
     */
    @Getter(AccessLevel.PUBLIC)
    private final Runtime runtime = Runtime.create();

    /**
     * Flag indicating whether the context is active or disposed.
     */
    @Getter(AccessLevel.PUBLIC)
    private final boolean active = true;

    /**
     * Unique identifier for the current context.
     */
    @Getter(AccessLevel.PUBLIC)
    private String name;

    /**
     * Thread associated with the current context.
     */
    @Getter(AccessLevel.PUBLIC)
    private Thread thread;

    /**
     * Executor service associated with the current context.
     */
    @Getter(AccessLevel.PACKAGE)
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
        Context context = new Context();
        context.name = "context" + counter++;
        log.info("Creating and initializing a new runtime context " + context.name + ".");
        context.initialize();
        return context;
    }

    /**
     * Give a custom name to the runtime context.
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Initializes the executor service associated with the current context.
     */
    void initialize() {
        service = Executors.newSingleThreadExecutor(r -> {
            thread = new Thread(r);
            return thread;
        });
    }

    public boolean isUninitialized() {
        return service == null || isDestroyed();
    }

    public boolean isReactiveThread() {
        return Thread.currentThread() == thread;
    }

    public boolean isDestroyed() {
        return service.isTerminated() || service.isShutdown();
    }

    @Synchronized
    public void dispose() {
        shutdown();
        runtime.dispose();
        if (current.isPresentAnd(x -> x == this)) {
            current.clear();
        }
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
     * Takes a getter function and returns a readonly reactive value.
     * <p> The value can be retrieved using {@link Reactive#get()} or equivalent methods inside a reactive closure.
     * Just like {@link #reactive(Object)}, read (retrieve) operations on memos are also tracked. </p>
     *
     * @param fx  getter function
     * @param <T> type of the inner value
     * @return the new memo
     */
    @Contract("_ -> new")
    public <T> @NotNull Memo<T> memo(@NotNull Supplier<T> fx) {
        return Memo.create(this, fx);
    }

    /**
     * Takes a reactive closure and returns an effect handle.
     * Effects are used to trigger side effects without holding a value.
     * Effects are queued to run once immediately after creation,
     * and re-run whenever their dependencies change.
     *
     * @param fx reactive closure
     * @return the new effect
     */
    @Contract("_ -> new")
    public @NotNull Effect effect(@NotNull Runnable fx) {
        return Effect.create(this, fx);
    }

    /**
     * Watch on a reactive value that does not track the old value.
     *
     * @see #watch(Reactive, BiConsumer)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Watch<T> watch(@NotNull Reactive<T> rx, @NotNull Consumer<T> fx) {
        return Watch.create(this, rx, (value, _old) -> fx.accept(value));
    }

    /**
     * Takes a reactive value and a function, and returns a watch handle.
     * Watches explicitly declare their dependency and run only when their dependency changes.
     * This variant tracks the old value of the reactive value.
     *
     * @param <T> type of the reactive value
     * @param rx  reactive value
     * @param fx  reactive closure
     * @return the new watch
     * @see #watch(Reactive, Consumer)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Watch<T> watch(@NotNull Reactive<T> rx, @NotNull BiConsumer<T, T> fx) {
        return Watch.create(this, rx, fx);
    }

    /**
     * Watch on a memo that does not track the old value.
     *
     * @see #watch(Memo, BiConsumer)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Watch<T> watch(@NotNull Memo<T> rx, @NotNull Consumer<T> fx) {
        return Watch.create(this, rx, (value, _old) -> fx.accept(value));
    }

    /**
     * Takes a memo and a function, and returns a watch handle.
     * For a variant that takes a reactive value, see {@link #watch(Reactive, BiConsumer)}.
     * This variant tracks the old value of the memo.
     *
     * @param <T> type of the memo
     * @param rx  memo
     * @param fx  reactive closure
     * @return the new watch
     * @see #watch(Memo, Consumer)
     */
    @Contract("_, _ -> new")
    public <T> @NotNull Watch<T> watch(@NotNull Memo<T> rx, @NotNull BiConsumer<T, T> fx) {
        return Watch.create(this, rx, fx);
    }

    /**
     * Takes a trigger and a function with no arguments, and returns a watch handle.
     *
     * @param rx trigger
     * @param fx reactive closure
     * @return the new watch
     * @see #watch(Reactive, BiConsumer)
     */
    @Contract("_, _ -> new")
    public @NotNull Watch<Void> watch(@NotNull Trigger rx, @NotNull Runnable fx) {
        BiConsumer<Void, Void> f = (_1, _2) -> fx.run();
        return Watch.create(this, rx, f);
    }

    @NotNull Future<Void> submit(@NotNull Runtime runtime, @NotNull Consumer<Runtime> f) {
        if (isUninitialized()) {
            ReactiveUtil.panic("Uninitialized runtime service");
        }
        return service.submit(() -> f.accept(runtime), null);
    }

    <T> @NotNull Future<T> submit(@NotNull Runtime runtime, @NotNull Function<Runtime, T> f) {
        if (isUninitialized()) {
            ReactiveUtil.panic("Uninitialized runtime service");
        }
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
        if (isReactiveThread()) {
            task.accept(runtime);
            return;
        }

        log.debug("Blocking the current thread until the computation is finished.");
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
        if (isReactiveThread()) {
            return task.apply(runtime);
        }

        log.debug("Blocking the current thread until the result is ready.");
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
        if (isReactiveThread()) {
            task.accept(runtime);
            return;
        }

        log.debug("Submitting a task to the runtime service.");
        submit(runtime, task);
    }

    /**
     * Submits the given runtime task to the runtime associated with the current context.
     * Returns a future that can be used to check task completion.
     *
     * @param task a function that performs a computation using the runtime
     */
    public Future<Void> submitWith(@NotNull Consumer<Runtime> task) {
        log.debug("Submitting a task to the runtime service.");
        return submit(runtime, task);
    }

    /**
     * Submits the given runtime task to the runtime associated with the current context.
     * Returns a future that can be used to retrieve the result of the computation.
     *
     * @param task a function that performs a computation using the runtime
     */
    public <R> Future<R> submitWith(@NotNull Function<Runtime, R> task) {
        log.debug("Submitting a task to the runtime service.");
        return submit(runtime, task);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed,
     * but no new tasks will be accepted. Invocation has no additional effect if already shut down.
     * For an immediate shutdown, see {@link #shutdownNow()}.
     */
    public void shutdown() {
        if (isUninitialized()) {
            log.warn("Shutting down an uninitialized runtime service.");
            return;
        }

        service.shutdown();
        try {
            var didShutdown = service.awaitTermination(100, TimeUnit.MILLISECONDS);
            if (didShutdown) {
                log.info("Runtime service was shutdown successfully.");
            } else {
                log.warn("Runtime service failed to shut down in an orderly manner.\n" + "Shutting down abruptly. Scheduled tasks did not execute in time.");
                service.shutdownNow();
            }
        } catch (Exception e) {
            service.shutdownNow();
            log.warn("Runtime service shutdown unexpectedly.");
        }
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks.
     * Does not wait until all actively executing tasks are terminated.
     */
    public void shutdownNow() {
        if (isUninitialized()) {
            log.warn("Shutting down uninitialized runtime service.");
            return;
        }

        service.shutdownNow();
        log.info("Runtime service was shutdown abruptly.");
        log.warn("Scheduled tasks were not executed.");
    }

    /**
     * Warns about a blocking function.
     *
     * @param current     function being used
     * @param alternative asynchronous alternative
     */
    void warnBlocking(String current, String alternative) {
        if (isReactiveThread()) {
            return;
        }
        log.warn("Use of `" + current + "` outside of a reactive scope.\n" + "This operation will block the current thread until the computation is finished. " + "Consider using " + alternative + "` instead.");
    }

    @Override
    public String toString() {
        return String.format("Context(%s, %s)", name, active ? "active" : "disposed");
    }

}
