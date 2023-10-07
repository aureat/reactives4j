package org.playground.reactives;

import java.util.function.BiConsumer;

import static org.playground.reactives.RuntimeService.withRuntime;

public class Watch<T> extends BaseNode<T> {

    private Watch(BaseNode<T> rx, BiConsumer<T, T> fx) {
        super(NodeType.Watch, new WatchState<>(rx, fx), NodeStatus.Dirty);
    }

    static <T> Watch<T> create(BaseNode<T> rx, BiConsumer<T, T> fx) {
        return withRuntime(runtime -> {
            var watch = new Watch<>(rx, fx);
            runtime.addNode(watch);
            runtime.subscribeObserver(rx, watch);
            return watch;
        });
    }

}
