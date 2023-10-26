package reactives4j.core;

import lombok.Synchronized;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Reactives {

    /**
     * Creates a new reactiveContext and sets it as the current reactiveContext.
     *
     * @return the new reactiveContext
     */
    @Synchronized
    @Contract("-> new")
    public static @NotNull ReactiveContext createContext() {
        return ReactiveContext.create();
    }

    /**
     * Gets the current reactiveContext in use.
     *
     * @return the current reactiveContext
     * @panics if no reactiveContext is available.
     */
    @Synchronized
    @Contract(pure = true)
    public static @NotNull ReactiveContext getContext() {
        return ReactiveContext.getContext();
    }

    /**
     * Sets the current reactiveContext.
     */
    @Synchronized
    public static void setContext(ReactiveContext reactiveContext) {
        ReactiveContext.setContext(reactiveContext);
    }

    /**
     * Creates a new reactive with the given value.
     *
     * @param value the value
     * @param <T>   the type of the value
     * @return the new reactive
     */
    @Contract("_ -> new")
    public static <T> @NotNull Reactive<T> reactive(T value) {
        return Reactive.create(value);
    }

    @Contract("-> new")
    public static @NotNull Trigger trigger() {
        return Trigger.create();
    }

    @Contract("_ -> new")
    public static <T> @NotNull Memo<T> memo(@NotNull Supplier<T> fx) {
        return Memo.create(fx);
    }

    @Contract("_ -> new")
    public static @NotNull Effect effect(@NotNull Runnable fx) {
        return Effect.create(fx);
    }

    @Contract("_, _ -> new")
    public static <T> @NotNull Watch<T> watch(@NotNull Reactive<T> rx, @NotNull Consumer<T> fx) {
        return Watch.create(rx, (value, _old) -> fx.accept(value));
    }

    @Contract("_, _ -> new")
    public static <T> @NotNull Watch<T> watch(@NotNull Reactive<T> rx, @NotNull BiConsumer<T, T> fx) {
        return Watch.create(rx, fx);
    }

    @Contract("_, _ -> new")
    public static <T> @NotNull Watch<T> watch(@NotNull Memo<T> rx, @NotNull Consumer<T> fx) {
        return Watch.create(rx, (value, _old) -> fx.accept(value));
    }

    @Contract("_, _ -> new")
    public static @NotNull Watch<Void> watch(@NotNull Trigger rx, @NotNull Runnable fx) {
        BiConsumer<Void, Void> f = (_1, _2) -> fx.run();
        return Watch.create(rx, f);
    }

}
