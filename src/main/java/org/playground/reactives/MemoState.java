package org.playground.reactives;

import org.jetbrains.annotations.NotNull;
import org.playground.maybe.Maybe;

import java.util.function.Function;
import java.util.function.Supplier;

class MemoState<T> extends BaseState<T> {

    private final Supplier<T> function;

    private final Maybe<T> value = Maybe.nothing();

    MemoState(Supplier<T> fx) {
        function = fx;
    }

    @Override
    T getValue() {
        return value.expectPresent(BaseState::panicAbsent).getUnchecked();
    }

    @Override
    <U> U withValue(@NotNull Function<T, U> action) {
        return value.expectPresent(BaseState::panicAbsent).withUnchecked(action);
    }

    @Override
    boolean canObserve() {
        return true;
    }

    @Override
    boolean run(@NotNull Runtime runtime, @NotNull BaseNode<T> node) {
        Maybe<T> oldValue = value.take();
        T newValue = function.get();
        value.set(newValue);
        return !value.equals(oldValue);
    }

}
