package org.playground;

import org.playground.reactives.Reactive;

import javax.swing.*;
import java.awt.*;

import static org.playground.reactives.Reactives.effect;
import static org.playground.reactives.Reactives.reactive;

public class CounterApp {

	private final Reactive<Integer> count = reactive(0);

	private final JFrame frame = new JFrame();
	private final JLabel label = new JLabel("", SwingConstants.CENTER);
	private final JButton increment = new JButton("+");
	private final JButton decrement = new JButton("-");

	public CounterApp() {
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.setSize(200, 100);
		label.setPreferredSize(new Dimension(80, 20));
		frame.add(decrement);
		frame.add(label);
		frame.add(increment);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// handle reactives
		increment.addActionListener(e -> count.update(x -> x + 1));
		decrement.addActionListener(e -> count.update(x -> x - 1));
		effect(() -> label.setText(count.get().toString()));
	}

}
