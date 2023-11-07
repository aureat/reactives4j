package reactives4j.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WatchJob<T> extends BaseTask implements TaskHandle {

    WatchJob(Context cx) {
        super(cx);
    }

    void setSource(Reactive<T> rx, BiConsumer<T, T> fx) {
        source = context.watchEffect(rx, (oldValue, newValue) -> {
            apply(() -> fx.accept(oldValue, newValue));
        });
    }

    void setSource(Reactive<T> rx, Consumer<T> fx) {
        source = context.watchEffect(rx, (oldValue, newValue) -> {
            apply(() -> fx.accept(newValue));
        });
    }

    void setSource(Memo<T> rx, BiConsumer<T, T> fx) {
        source = context.watchEffect(rx, (oldValue, newValue) -> {
            apply(() -> fx.accept(oldValue, newValue));
        });
    }

    void setSource(Memo<T> rx, Consumer<T> fx) {
        source = context.watchEffect(rx, (oldValue, newValue) -> {
            apply(() -> fx.accept(newValue));
        });
    }

    void setSource(Trigger rx, Runnable fx) {
        source = context.watchEffect(rx, () -> apply(fx));
    }

    void setSource(Resource<T> rx, BiConsumer<T, T> fx) {
        setSource(rx.value(), fx);
    }

    void setSource(Resource<T> rx, Consumer<T> fx) {
        setSource(rx.value(), fx);
    }

    void apply(Runnable fx) {
        loading.set(true);
        var taskContext = TaskContext.getContext();
        taskContext.submitTask(() -> {
            fx.run();
            loading.set(false);
        });
    }

}
