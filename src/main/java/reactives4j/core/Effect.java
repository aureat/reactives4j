package reactives4j.core;

public class Effect extends BaseNode<Void> {

    private Effect(ReactiveContext cx, Runnable fx) {
        super(cx, NodeType.Effect, new EffectState(fx), NodeStatus.Dirty);
    }

    static Effect create(ReactiveContext cx, Runnable fx) {
        var effect = new Effect(cx, fx);
        cx.doWith(runtime -> {
            runtime.addNode(effect);
            runtime.updateIfNecessary(effect);
        });
        return effect;
    }

}
