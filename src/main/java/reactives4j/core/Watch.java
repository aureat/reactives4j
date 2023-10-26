package reactives4j.core;

import java.util.function.BiConsumer;

public class Watch<T> extends BaseNode<T> {

    private Watch(ReactiveContext cx, BaseNode<T> rx, BiConsumer<T, T> fx) {
        super(cx, NodeType.Watch, new WatchState<>(rx, fx), NodeStatus.Dirty);
    }

    static <T> Watch<T> create(ReactiveContext cx, BaseNode<T> rx, BiConsumer<T, T> fx) {
        return cx.with(runtime -> {
            var watch = new Watch<>(cx, rx, fx);
            runtime.addNode(watch);
            runtime.subscribeObserver(rx, watch);
            return watch;
        });
    }

}
