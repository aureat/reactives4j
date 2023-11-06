package reactives4j.core;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class BaseTask implements TaskHandle {

    @Getter(AccessLevel.PUBLIC)
    final ReactiveContext context;

    final Reactive<Boolean> loading;

    @Getter(AccessLevel.PUBLIC)
    Handle source;

    BaseTask(ReactiveContext cx) {
        context = cx;
        loading = cx.reactive(false);
    }

    /**
     * Returns the loading state of the task as a reactive.
     */
    public Reactive<Boolean> loading() {
        return loading;
    }

    /**
     * Returns whether the task is loading.
     */
    public boolean isLoading() {
        return loading.getUntracked();
    }

    /**
     * Disposes the task.
     */
    public void dispose() {
        source.dispose();
    }

}
