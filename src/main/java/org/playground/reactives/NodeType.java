package org.playground.reactives;

public enum NodeType {
	Ref, Trigger, Memo, Effect, Watch;

	@Override
	public String toString() {
		return this.name();
	}
}
