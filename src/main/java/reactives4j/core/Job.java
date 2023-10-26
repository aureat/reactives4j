package reactives4j.core;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Job {

    private final ReactiveContext context;

    private final Runnable action;

    private final Effect source;

    @Getter(AccessLevel.PUBLIC)
    private final Reactive<Boolean> loading;

    private Job(ReactiveContext cx, Runnable fx) {
        context = cx;
        action = fx;
        loading = cx.reactive(false);
        source = cx.effect(this::runJob);
    }

    static Job create(ReactiveContext cx, Runnable fx) {
        return new Job(cx, fx);
    }

    private CompletableFuture<TaskStatus> runJob() {
        loading.set(true);
        var taskContext = TaskContext.getContext();
        var future = new CompletableFuture<TaskStatus>();
        taskContext.submitTask(() -> {
            action.run();
            future.complete(TaskStatus.Completed);
        });
        future.thenRunAsync(() -> loading.set(false), taskContext.getService());
        return future;
    }

    /**
     * Returns a reactive value that indicates whether the job is still loading.
     */
    public Reactive<Boolean> loading() {
        return loading;
    }

    /**
     * Returns true if the job is still loading.
     */
    public boolean isLoading() {
        return loading.get();
    }

    /**
     * Schedules the job to run.
     */
    public Future<TaskStatus> run() {
        return runJob();
    }

}
