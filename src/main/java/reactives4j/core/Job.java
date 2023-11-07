package reactives4j.core;

public class Job extends BaseTask implements TaskHandle {

    private Job(Context cx) {
        super(cx);
    }

    static TaskHandle create(Context cx, Runnable fx) {
        var node = new Job(cx);
        node.source = cx.effect(() -> {
            node.loading.set(true);
            var taskContext = TaskContext.getContext();
            taskContext.submitTask(() -> {
                fx.run();
                node.loading.set(false);
            });
        });
        return node;
    }

}
