package reactives4j.core;

import org.jetbrains.annotations.NotNull;
import reactives4j.maybe.Maybe;

import java.util.function.Consumer;
import java.util.function.Function;

class ReactiveState<T> extends BaseState<T> {

    private final Maybe<T> value = Maybe.nothing();

    ReactiveState(T val) {
        value.set(val);
    }

    @Override
    T getValue() {
        return value.expectPresent(BaseState::panicAbsent).getUnchecked();
    }

    @Override
    void setValue(@NotNull T newValue) {
        value.set(newValue);
    }

    @Override
    <U> U withValue(@NotNull Function<T, U> action) {
        return value.expectPresent(BaseState::panicAbsent).withUnchecked(action);
    }

    @Override
    void doWithValue(@NotNull Consumer<T> action) {
        value.expectPresent(BaseState::panicAbsent).doWithUnchecked(action);
    }

    @Override
    void updateValue(@NotNull Function<T, T> updater) {
        value.expectPresent(BaseState::panicAbsent).updateUnchecked(updater);
    }

    @Override
    void modifyValue(@NotNull Consumer<T> action) {
        value.expectPresent(BaseState::panicAbsent).doWithUnchecked(action);
    }

    @Override
    public String toString() {
        return String.format("ReactiveState(%s)", value);
    }

}
