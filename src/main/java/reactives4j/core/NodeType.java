package reactives4j.core;

public enum NodeType {
    Reactive, Trigger, Memo, Effect, Watch;

    @Override
    public String toString() {
        return this.name();
    }
}
