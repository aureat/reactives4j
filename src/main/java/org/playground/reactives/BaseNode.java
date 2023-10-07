package org.playground.reactives;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

@Log4j2
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseNode<T> {

    /**
     * Context associated with the current node.
     */
    @Getter(AccessLevel.PUBLIC)
    final Context context;

    /**
     * Node
     */
    @Getter(AccessLevel.PUBLIC)
    final NodeType type;

    @Getter(AccessLevel.PROTECTED)
    final BaseState<T> state;

    @Getter(AccessLevel.PROTECTED)
    NodeStatus status;

    /**
     * Check node type
     *
     * @see NodeType
     */
    public boolean isType(NodeType type) {
        return this.type == type;
    }

    /**
     * Dispose node
     */
    public void dispose() {
        context.with(runtime -> {
            runtime.disposeNode(this);
        });
    }

    boolean isCheck() { return status == NodeStatus.Check; }

    boolean isDirty() { return status == NodeStatus.Dirty; }

    boolean isMarked() { return status == NodeStatus.Marked; }

    boolean isDirtyOrMarked() { return status == NodeStatus.Dirty || status == NodeStatus.Marked; }

    void setClean() { status = NodeStatus.Clean; }

    void setCheck() { status = NodeStatus.Check; }

    void setDirty() { status = NodeStatus.Dirty; }

    void setMarked() { status = NodeStatus.Marked; }

    T getValue() {
        return state.getValue();
    }

    void setValue(T newValue) {
        state.setValue(newValue);
    }

    boolean canObserve() {
        return state.canObserve();
    }

    boolean run(Runtime runtime) {
        return state.run(runtime, this);
    }

    T getNode() {
        context.warnBlocking("get()", "getAsync()");
        return context.with(runtime -> {
            track(runtime);
            return state.getValue();
        });
    }

    void setNode(T newValue) {
        context.doWith(runtime -> {
            state.setValue(newValue);
            trigger(runtime);
        });
    }

    Future<T> getNodeAsync() {
        return context.submitWith(runtime -> {
            track(runtime);
            return state.getValue();
        });
    }

    T getNodeUntracked() {
        context.warnBlocking("getUntracked()", "getUntrackedAsync()");
        return context.with(runtime -> {
            runtime.updateIfNecessary(this);
            return state.getValue();
        });
    }

    void setNodeUntracked(T newValue) {
        context.doWith(runtime -> {
            state.setValue(newValue);
        });
    }

    <U> U withNode(Function<T, U> action) {
        context.warnBlocking("with()", "withAsync()");
        return context.with(runtime -> {
            track(runtime);
            return state.withValue(action);
        });
    }

    <U> Future<U> withNodeAsync(Function<T, U> action) {
        return context.submitWith(runtime -> {
            track(runtime);
            return state.withValue(action);
        });
    }

    <U> U withNodeUntracked(Function<T, U> action) {
        context.warnBlocking("withUntracked()", "withUntrackedAsync()");
        return context.with(runtime -> {
            runtime.updateIfNecessary(this);
            return state.withValue(action);
        });
    }

    void doWithNode(Consumer<T> action) {
        context.doWith(runtime -> {
            track(runtime);
            state.doWithValue(action);
        });
    }

    void doWithNodeUntracked(Consumer<T> action) {
        context.doWith(runtime -> {
            runtime.updateIfNecessary(this);
            state.doWithValue(action);
        });
    }

    void updateNode(Function<T, T> updater) {
        context.doWith(runtime -> {
            state.updateValue(updater);
            trigger(runtime);
        });
    }

    void updateNodeUntracked(Function<T, T> updater) {
        context.doWith(runtime -> {
            state.updateValue(updater);
        });
    }

    void modifyNode(Consumer<T> action) {
        context.doWith(runtime -> {
            state.modifyValue(action);
            trigger(runtime);
        });
    }

    void modifyNodeUntracked(Consumer<T> action) {
        context.doWith(runtime -> {
            state.modifyValue(action);
        });
    }

    void track(@NotNull Runtime runtime) {
        runtime.subscribeObserver(this);
        runtime.updateIfNecessary(this);
    }

    void trigger(@NotNull Runtime runtime) {
        runtime.markDirty(this);
        runtime.runEffects();
    }

}
