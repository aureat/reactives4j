package reactives4j.core;

import java.util.function.BiConsumer;

public class WatchEffect<T> extends BaseNode<T> implements Handle {

    private WatchEffect(ReactiveContext cx, BaseNode<T> rx, BiConsumer<T, T> fx) {
        super(cx, NodeType.Watch, new WatchState<>(rx, fx), NodeStatus.Dirty);
    }

    static <T> Handle create(ReactiveContext cx, BaseNode<T> rx, BiConsumer<T, T> fx) {
        return cx.with(runtime -> {
            var watch = new WatchEffect<>(cx, rx, fx);
            runtime.addNode(watch);
            runtime.subscribeObserver(rx, watch);
            return watch;
        });
    }

}
