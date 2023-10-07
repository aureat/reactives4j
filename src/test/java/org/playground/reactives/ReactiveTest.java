package org.playground.reactives;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.playground.reactives.Reactives.*;

public class ReactiveTest {

	@BeforeAll
	static void classSetup() {
		RuntimeService.initialize();
		Logger logger = LogManager.getRootLogger();
		Configurator.setAllLevels(logger.getName(), Level.INFO);
		Configurator.setAllLevels(logger.getName(), Level.WARN);
		Configurator.setAllLevels(logger.getName(), Level.DEBUG);
	}

	@AfterAll
	static void classCleanup() {
		RuntimeService.shutdown();
	}

	@BeforeEach
	void testSetup() {
		createContext();
	}

	@AfterEach
	void testCleanup() {
		getContext().dispose();
	}

	@Test
	void test1() {
		var reactive = reactive(0);
		var counter = new Counter();
		watch(reactive, x -> counter.increment());
		reactive.set(10);
		reactive.set(20);
		assertEquals(20, reactive.get());
		assertEquals(2, counter.count());
	}

	@Test
	void test2() throws InterruptedException {
		var number = reactive(10);
		var doubled = memo(() -> number.get() * 2);
		assertEquals(20, doubled.get());
		number.set(30);
		assertEquals(60, doubled.get());
	}

	@Test
	void test3() {
		var number = reactive(10);
		var doubled = memo(() -> number.get() * 2);
		var counter = new Counter();
		watch(doubled, x -> counter.increment());
		number.set(20);
		number.set(30);
		assertEquals(30, number.get());
		assertEquals(60, doubled.get());
		assertEquals(2, counter.count());
	}

	@Test
	void test4() {
		var trigger = trigger();
		var counter = new Counter();
		watch(trigger, counter::increment);
		trigger.set();
		trigger.set();
		trigger.get();
		assertEquals(2, counter.count());
	}

	@Test
	void test5() {
		var array = reactive(new ArrayList<>());
		watch(array, numbers -> numbers.forEach(System.out::println));
		System.out.println("Numbers:");
		array.modify(numbers -> numbers.addAll(List.of(1, 2, 3, 4, 5)));
	}

	@Test
	void test6() throws InterruptedException {
		var num1 = reactive(10);
		var num2 = reactive(0);
		watch(num1, x -> {
			System.out.println("x = " + x);
			num2.set(x * 2);
		});
		num1.set(30);
		num1.set(60);
		assertEquals(60, num1.get());
		assertEquals(120, num2.get());
	}

	@Test
	void test7() {
		var num1 = reactive(10);
		var num2 = memo(() -> num1.get() * 2);
		num1.set(30);
		num1.set(60);
		assertEquals(60, num1.get());
		assertEquals(120, num2.get());
	}

	@Test
	void test8() {
		var num = reactive(0);
		watch(num, x -> System.out.println("x = " + x));
		num.update(x -> x + 1);
	}

	@Test
	void test9() {
		var counter = reactive(new Counter());
		watch(counter, c -> System.out.println("count = " + c.count()));
		Runnable buttonIncrement = () -> counter.modify(Counter::increment);
		Runnable buttonDecrement = () -> counter.modify(Counter::decrement);
		buttonIncrement.run();
		buttonIncrement.run();
		buttonIncrement.run();
		buttonIncrement.run();
		buttonIncrement.run();
		buttonDecrement.run();
		buttonDecrement.run();
		counter.doWith(c -> assertEquals(3, c.count()));
	}

	@Test
	void test10() {
		var number = reactive(10);
		var doubled = memo(() -> number.get() * 2);
		assertEquals(10, number.get());
		assertEquals(20, doubled.get());
	}

	@Test
	void test11() {
		var number = reactive(10);
		var doubled = memo(() -> number.get() * 2);
		effect(() -> {
			var value = number.get();
			System.out.println("number = " + value);
		});
		assertEquals(20, doubled.get());
		number.set(30);
		number.set(60);
		assertEquals(60, number.get());
		assertEquals(120, doubled.get());
	}

	@Test
	void test12() {
		var number = reactive(10);
		var doubled = reactive(0);
		effect(() -> {
			var value = number.get();
			System.out.println("x = " + value);
			doubled.setUntracked(value * 2);
		});
		assertEquals(20, doubled.get());
		number.set(30);
		number.set(60);
		assertEquals(60, number.get());
		assertEquals(120, doubled.get());
	}

	@Test
	void test13() {
		var number = reactive(10);
		var doubled = memo(() -> number.get() * 2);
		var quadrupled = memo(() -> doubled.get() * 2);
		watch(quadrupled, x -> {
			System.out.println("quadrupled = " + x);
		});
		assertEquals(10, number.get());
		assertEquals(20, doubled.get());
		assertEquals(40, quadrupled.get());
		number.set(30);
		assertEquals(60, doubled.get());
		assertEquals(120, quadrupled.get());
		number.set(60);
		quadrupled.doWith(x -> assertEquals(240, x));
	}

	@Test
	void test14() {
		var count = reactive(0);
		watch(count, (current, old) -> System.out.printf("changed from %s to %s\n", old, current));
		count.set(1);
		count.set(2);
		count.set(3);
		System.out.println("This should print before changes");
		assertEquals(3, count.get());
	}

	@Test
	void test15() {
		var cleanupCount = new Counter();
		var trace = new ArrayList<Integer>();
		var number = reactive(0);
		effect(() -> {
			var value = number.get();
			trace.add(value);
			onCleanup(cleanupCount::increment);
		});
		for (int i = 1; i <= 10; i++) {
			number.set(i);
		}
		number.doWith(x -> {
			assertEquals(10, x);
			assertEquals(11, trace.size());
			assertEquals(10, cleanupCount.count());
		});
	}

	@Test
	void test16() {
		var number = reactive(0);
		var trace = new A
		{
			var trace = new ArrayList<Integer>();
			effect(() -> trace.add(number.get()));
		}
	}

	static class Counter {
		private int count = 0;

		public int count() {
			return count;
		}

		public void increment() {
			count++;
		}

		public void decrement() {
			count--;
		}
	}

}
