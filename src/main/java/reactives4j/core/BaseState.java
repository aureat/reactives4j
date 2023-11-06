package reactives4j.core;

import org.jetbrains.annotations.NotNull;
import reactives4j.util.ReactiveUtil;

import java.util.function.Consumer;
import java.util.function.Function;

abstract class BaseState<T> {

    protected static void panicAbsent() {
        ReactiveUtil.panic("Empty reactive value");
    }

    protected static void panicBadType() {
        ReactiveUtil.panic("This operation is not supported for a reactive state of this type");
    }

    T getValue() {
        panicBadType();
        return null;
    }

    void setValue(T newValue) {
        panicBadType();
    }

    <U> U withValue(@NotNull Function<T, U> action) {
        panicBadType();
        return null;
    }

    void doWithValue(@NotNull Consumer<T> action) {
        panicBadType();
    }

    void updateValue(@NotNull Function<T, T> updater) {
        panicBadType();
    }

    void modifyValue(@NotNull Consumer<T> action) {
        panicBadType();
    }

    boolean isSubscriber() {
        return false;
    }

    boolean isObserver() {
        return false;
    }

    boolean run(@NotNull Runtime runtime, @NotNull BaseNode<T> node) {
        return true;
    }

}
