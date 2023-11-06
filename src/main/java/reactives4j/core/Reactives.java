package reactives4j.core;

import lombok.Synchronized;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public final class Reactives {

    /**
     * Creates a new reactive context and returns it
     */
    @Synchronized
    @Contract("-> new")
    public static @NotNull ReactiveContext context() {
        return ReactiveContext.create();
    }

    /**
     * Creates a new named reactive context and returns it
     */
    @Synchronized
    @Contract("_ -> new")
    public static @NotNull ReactiveContext context(@NotNull String name) {
        return ReactiveContext.create(name);
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
    public static <R> @NotNull Future<R> task(@NotNull Callable<R> fx) {
        return TaskContext.getContext().submitTask(fx);
    }

    /**
     * Submits an asynchronous task that runs on its own thread.
     * Returns a future that can be used to check task completion.
     *
     * @see TaskContext#submitTask(Callable)
     */
    public static @NotNull Future<Void> task(@NotNull Runnable fx) {
        return TaskContext.getContext().submitTask(fx);
    }

}
