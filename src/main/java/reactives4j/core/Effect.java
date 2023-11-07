package reactives4j.core;

public class Effect extends BaseNode<Void> implements Handle {

    private Effect(Context cx, Runnable fx) {
        super(cx, NodeType.Effect, new EffectState(fx), NodeStatus.Dirty);
    }

    static Handle create(Context cx, Runnable fx, boolean lazy) {
        if (lazy) {
            var effect = new Effect(cx, fx);
            cx.doWith(runtime -> {
                runtime.addNode(effect);
                runtime.updateIfNecessary(effect);
            });
            return effect;
        }

        return cx.with(runtime -> {
            var effect = new Effect(cx, fx);
            runtime.addNode(effect);
            runtime.updateIfNecessary(effect);
            return effect;
        });
    }

}
