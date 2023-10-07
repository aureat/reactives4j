package org.playground.reactives;

import lombok.Synchronized;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Reactives {

	/**
	 * Creates a new context and sets it as the current context.
	 *
	 * @return the new context
	 */
	@Synchronized
	@Contract("-> new")
	public static @NotNull Context createContext() {
		return Context.create();
	}

	/**
	 * Gets the current context in use.
	 *
	 * @return the current context
	 * @panics if no context is available.
	 */
	@Synchronized
	@Contract(pure = true)
	public static @NotNull Context getContext() {
		return Context.getContext();
	}

	/**
	 * Sets the current context.
	 */
	@Synchronized
	public static void setContext(Context context) {
		Context.setContext(context);
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

	public static void onCleanup(@NotNull Runnable fx) {

	}

}
