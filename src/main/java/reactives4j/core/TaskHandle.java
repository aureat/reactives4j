package reactives4j.core;

/**
 * Common handle interface for asynchronous reactive nodes.
 */
public interface TaskHandle extends Handle {

    /**
     * Returns the loading state of the task as a reactive.
     */
    Reactive<Boolean> loading();

    /**
     * Returns whether the task is loading.
     */
    boolean isLoading();

}
