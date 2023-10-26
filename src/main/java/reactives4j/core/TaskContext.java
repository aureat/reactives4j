package reactives4j.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@NoArgsConstructor
public class TaskContext {

    @Getter(AccessLevel.PACKAGE)
    private final ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();

    public static TaskContext getContext() {
        return TaskContextHolder.instance;
    }

    /**
     * Submits an asynchronous task that runs on its own thread.
     * Returns a future that can be used to check task completion.
     *
     * @see TaskContext#submitTask(Callable)
     */
    @NotNull Future<Void> submitTask(@NotNull Runnable fx) {
        return service.submit(fx, null);
    }

    /**
     * Submits an asynchronous task that runs on its own thread.
     * Returns a future that can be used to retrieve the result of the computation.
     * Technically, these are virtual threads, designed for these sort of tasks.
     * This variant returns a result, for a variant that doesn't return a result, {@link TaskContext#submitTask(Runnable)}
     *
     * @see Resource
     * @see Job
     */
    <R> @NotNull Future<R> submitTask(@NotNull Callable<R> fx) {
        return service.submit(fx);
    }

    private static class TaskContextHolder {

        private static final TaskContext instance = new TaskContext();

    }

}
