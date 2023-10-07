package org.playground.reactives;

public class Effect extends BaseNode<Void> {

    private Effect(Context cx, Runnable fx) {
        super(cx, NodeType.Effect, new EffectState(fx), NodeStatus.Dirty);
    }

    static Effect create(Context cx, Runnable fx) {
        var effect = new Effect(cx, fx);
        cx.doWith(runtime -> {
            runtime.addNode(effect);
            runtime.updateIfNecessary(effect);
        });
        return effect;
    }

}
