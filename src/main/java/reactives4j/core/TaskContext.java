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

    @NotNull Future<Void> submitTask(@NotNull Runnable fx) {
        return service.submit(fx, null);
    }

    <R> @NotNull Future<R> submitTask(@NotNull Callable<R> fx) {
        return service.submit(fx);
    }

    void shutdown() {
        service.shutdown();
    }

    void shutdownNow() {
        service.shutdownNow();
    }

    private static class TaskContextHolder {

        private static final TaskContext instance = new TaskContext();

    }

}
