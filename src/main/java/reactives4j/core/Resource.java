package reactives4j.core;

import lombok.extern.log4j.Log4j2;
import reactives4j.maybe.MaybeConst;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2
public class Resource<T> {

    private final Context context;

    private final Supplier<T> getter;

    private final Reactive<T> value;

    private final Effect source;

    private final Reactive<Boolean> loading;

    private Resource(Context cx, Supplier<T> fx, T initialValue) {
        context = cx;
        getter = fx;
        loading = cx.reactive(false);
        value = cx.reactive(initialValue);
        source = Effect.create(cx, this::runResource, false);
    }

    private Resource(Context cx, Supplier<T> fx) {
        this(cx, fx, null);
    }

    static <T> Resource<T> create(Context cx, Supplier<T> fx, T initialValue) {
        return new Resource<T>(cx, fx, initialValue);
    }

    static <T> Resource<T> create(Context cx, Supplier<T> fx) {
        return new Resource<>(cx, fx);
    }

    private CompletableFuture<T> runResource() {
        loading.set(true);
        var taskContext = TaskContext.getContext();
        var future = new CompletableFuture<T>();
        taskContext.submitTask(() -> {
            var v = context.getRuntime().withObserver(source, getter);
//            var v = getter.get();
            future.complete(v);
        });
        return future.thenApplyAsync(v -> {
            loading.set(false);
            value.set(v);
            return v;
        }, taskContext.getService());
    }

    /**
     * Schedules a fetch of the resource.
     * The getter function might reference other reactive values or resources,
     * in which case the fetch will be completed once all the dependencies are fetched.
     */
    public Future<T> fetch() {
        return runResource();
    }

    /**
     * Returns a reactive value that indicates whether the resource is still loading.
     */
    public Reactive<Boolean> loading() {
        return loading;
    }

    /**
     * Returns true if the resource is still loading.
     */
    public boolean isLoading() {
        return loading.get();
    }

    /**
     * Returns a reactive value that indicates the current value of the resource.
     */
    public Reactive<T> value() {
        return value;
    }

    /**
     * Returns the current value of the resource.
     * If the resource is still loading, returns nothing.
     *
     * @see MaybeConst
     */
    public MaybeConst<T> get() {
        var v = value.get();
        if (loading.getUntracked())
            return MaybeConst.nothing();
        return MaybeConst.just(v);
    }

    /**
     * Returns the current value of the resource.
     * If the resource is still loading, returns the default value.
     *
     * @see MaybeConst
     */
    public T getOrDefault(T defaultValue) {
        var v = value.get();
        if (loading.getUntracked())
            return defaultValue;
        return v;
    }

    /**
     * Executes the given action if the resource is still loading.
     * Otherwise, executes the given action with the current value of the resource.
     *
     * @param action the action to execute
     * @return the result of the action
     */
    public <U> MaybeConst<U> with(Function<T, U> action) {
        return get().map(action);
    }

    /**
     * Executes the given action if the resource is still loading.
     * Otherwise, executes the given action with the current value of the resource.
     *
     * @param defaultValue the default value to use if the resource is still loading
     * @param action       the action to execute
     * @return the result of the action
     */
    public <U> U withOrDefault(U defaultValue, Function<T, U> action) {
        return get().mapOr(defaultValue, action).getUnchecked();
    }

    /**
     * Executes the given action if the resource is still loading.
     * Otherwise, executes the given action with the current value of the resource.
     *
     * @param action the action to execute
     */
    public void doWith(Consumer<T> action) {
        get().ifPresent(action);
    }

    /**
     * Executes the given action if the resource is still loading.
     * Otherwise, executes the given action with the current value of the resource.
     *
     * @param defaultValue the default value to use if the resource is still loading
     * @param action       the action to execute
     */
    public void doWithOrElse(T defaultValue, Consumer<T> action) {
        get().ifPresentOrElse(action, () -> action.accept(defaultValue));
    }

}
