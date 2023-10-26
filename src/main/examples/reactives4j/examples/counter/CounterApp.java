package reactives4j.examples.counter;

import reactives4j.core.Reactive;
import reactives4j.core.ReactiveContext;

import javax.swing.*;
import java.awt.*;

public class CounterApp {

    private final JFrame frame = new JFrame();

    private final JLabel label = new JLabel("", SwingConstants.CENTER);

    private final JButton increment = new JButton("+");

    private final JButton decrement = new JButton("-");

    private final Reactive<Integer> count;

    public CounterApp(ReactiveContext cx) {
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
        frame.setSize(200, 100);
        label.setPreferredSize(new Dimension(80, 20));
        frame.add(decrement);
        frame.add(label);
        frame.add(increment);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // handle reactive
        count = cx.reactive(0);
        increment.addActionListener(e -> count.update(x -> x + 1));
        decrement.addActionListener(e -> count.update(x -> x - 1));
        cx.effect(() -> label.setText(count.get().toString()));
    }

    public static void main(String[] args) {
        var cx = Reactives.createContext();
        var app = new CounterApp(cx);
    }

}
