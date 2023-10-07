package org.playground.reactives;

import java.util.concurrent.Future;

public class Trigger extends BaseNode<Void> {

    Trigger(Context cx) {
        super(cx, NodeType.Trigger, new TriggerState(), NodeStatus.Clean);
    }

    static Trigger create(Context cx) {
        return cx.with(runtime -> {
            var trigger = new Trigger(cx);
            runtime.addNode(trigger);
            return trigger;
        });
    }

    public void get() {
        context.with(this::track);
    }

    public Future<Void> getAsync() {
        return context.submitWith(this::track);
    }

    public void doWith(Runnable action) {
        context.doWith(runtime -> {
            track(runtime);
            action.run();
        });
    }

    public void set() {
        context.submitWith(this::trigger);
    }

}
