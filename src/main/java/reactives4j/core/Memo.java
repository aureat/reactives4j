package reactives4j.core;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Memo<T> extends BaseNode<T> {

    private Memo(ReactiveContext cx, Supplier<T> fx) {
        super(cx, NodeType.Memo, new MemoState<>(fx), NodeStatus.Dirty);
    }

    static <T> Memo<T> create(ReactiveContext cx, Supplier<T> fx) {
        return cx.with(runtime -> {
            var memo = new Memo<>(cx, fx);
            runtime.addNode(memo);
            runtime.updateIfNecessary(memo);
            return memo;
        });
    }

    /*
     * Getters: these will subscribe the active observer to itself
     */
    public T get() {
        return getNode();
    }

    public Future<T> getAsync() {
        return getNodeAsync();
    }

    public T getUntracked() {
        return getNodeUntracked();
    }

    public <U> U with(Function<T, U> action) {
        return withNode(action);
    }

    public Future<T> withAsync(Function<T, T> action) {
        return withNodeAsync(action);
    }

    public <U> U withUntracked(Function<T, U> action) {
        return withNodeUntracked(action);
    }

    public void doWith(Consumer<T> action) {
        doWithNode(action);
    }

    public void doWithUntracked(Consumer<T> action) {
        doWithNodeUntracked(action);
    }

}
