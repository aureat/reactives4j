package reactives4j.core;

import java.util.function.BiConsumer;

public class WatchEffect<T> extends BaseNode<T> implements Handle {

    private WatchEffect(Context cx, BaseNode<T> rx, BiConsumer<T, T> fx) {
        super(cx, NodeType.Watch, new WatchState<>(rx, fx), NodeStatus.Dirty);
    }

    static <T> WatchEffect<T> create(Context cx, BaseNode<T> rx, BiConsumer<T, T> fx, boolean immediate) {
        return cx.with(runtime -> {
            var watch = new WatchEffect<>(cx, rx, fx);
            runtime.addNode(watch);
            runtime.subscribeObserver(rx, watch);
            if (immediate)
                watch.run(runtime);
            return watch;
        });
    }

    static <T> WatchEffect<T> create(Context cx, BaseNode<T> rx, BiConsumer<T, T> fx) {
        return create(cx, rx, fx, false);
    }

}
