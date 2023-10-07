package org.playground.reactives;

import java.util.concurrent.Future;

public class Trigger extends BaseNode<Void> {

    Trigger() {
        super(NodeType.Trigger, new TriggerState(), NodeStatus.Clean);
    }

    static Trigger create() {
        return withRuntime(runtime -> {
            var trigger = new Trigger();
            runtime.addNode(trigger);
            return trigger;
        });
    }

    public void get() {
        withRuntime(this::track);
    }

    public Future<Void> getAsync() {
        return submitWithRuntime(this::track);
    }

    public void doWith(Runnable action) {
        doWithRuntime(runtime -> {
            track(runtime);
            action.run();
        });
    }

    public void set() {
        submitWithRuntime(this::trigger);
    }

}
