package org.playground.reactives;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Reactive<T> extends BaseNode<T> {

    private Reactive(Context cx, T value) {
        super(cx, NodeType.Ref, new ReactiveState<>(value), NodeStatus.Clean);
    }

    static <T> Reactive<T> create(Context cx, T value) {
        return cx.with(runtime -> {
            var reactive = new Reactive<>(cx, value);
            runtime.addNode(reactive);
            return reactive;
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

    public void setUntracked(T newValue) {
        setNodeUntracked(newValue);
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

    /*
     * Setters: these will notify its subscribers
     */
    public void set(T newValue) {
        setNode(newValue);
    }

    public void fill(Supplier<T> action) {
        updateNode(ignored -> action.get());
    }

    public void fillUntracked(Supplier<T> action) {
        updateNodeUntracked(ignored -> action.get());
    }

    public void update(Function<T, T> updater) {
        updateNode(updater);
    }

    public void updateUntracked(Function<T, T> updater) {
        updateNodeUntracked(updater);
    }

    public void modify(Consumer<T> action) {
        modifyNode(action);
    }

    public void modifyUntracked(Consumer<T> action) {
        modifyNodeUntracked(action);
    }

}
