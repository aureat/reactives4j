package reactives4j.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

class TriggerState extends BaseState<Void> {

    TriggerState() { }

    @Override
    Void getValue() {
        return null;
    }

    @Override
    void setValue(@NotNull Void newValue) { }

    @Override
    <U> U withValue(@NotNull Function<Void, U> action) {
        return action.apply(null);
    }

    @Override
    void updateValue(@NotNull Function<Void, Void> updater) {
        updater.apply(null);
    }

    @Override
    void modifyValue(@NotNull Consumer<Void> action) {
        action.accept(null);
    }

}
