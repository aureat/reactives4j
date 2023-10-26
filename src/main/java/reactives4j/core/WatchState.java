package reactives4j.core;

import org.jetbrains.annotations.NotNull;
import java.util.maybe.Maybe;

import java.util.function.BiConsumer;

class WatchState<T> extends BaseState<T> {

    private final Maybe<T> cachedValue = Maybe.nothing();

    private final BaseNode<T> source;

    private final BiConsumer<T, T> function;

    private boolean isRunning = false;

    WatchState(BaseNode<T> rx, BiConsumer<T, T> fx) {
        source = rx;
        function = fx;
        cachedValue.set(rx.getValue());
    }

    @Override
    boolean run(@NotNull Runtime runtime, @NotNull BaseNode<T> node) {
        // prevent unnecessary re-runs
        if (isRunning) {
            return false;
        }

        // update the source node if necessary
        runtime.updateIfNecessary(source);
        T value = source.getValue();

        // watch is cleared to run
        isRunning = true;
        function.accept(value, cachedValue.getUnchecked());
        isRunning = false;

        // update the cached value
        cachedValue.set(value);
        return true;
    }

}
