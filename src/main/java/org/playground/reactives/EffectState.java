package org.playground.reactives;

import org.jetbrains.annotations.NotNull;

class EffectState extends BaseState<Void> {

    private final Runnable function;

    EffectState(Runnable fx) {
        function = fx;
    }

    @Override
    boolean canObserve() {
        return true;
    }

    @Override
    boolean run(@NotNull Runtime runtime, @NotNull BaseNode<Void> node) {
        runtime.cleanupSources(node);
        function.run();
        return true;
    }

}
