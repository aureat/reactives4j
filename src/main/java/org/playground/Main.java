package org.playground;

import static org.playground.reactives.Reactives.createContext;

public class Main {
	public static void main(String[] args) {
		createContext();
		var app = new CounterApp();
	}
}
