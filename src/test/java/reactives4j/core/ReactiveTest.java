package reactives4j.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactives4j.core.Reactives.context;

public class ReactiveTest {

    ReactiveContext cx;

    @BeforeAll
    static void classSetup() {
        Logger logger = LogManager.getRootLogger();
        Configurator.setAllLevels(logger.getName(), Level.INFO);
        Configurator.setAllLevels(logger.getName(), Level.WARN);
        Configurator.setAllLevels(logger.getName(), Level.DEBUG);
    }

    @AfterAll
    static void classCleanup() { }

    @BeforeEach
    void testSetup() {
        cx = context();
    }

    @AfterEach
    void testCleanup() {
        cx.dispose();
        TaskContext.getContext().getService().shutdown();
    }

    @Test
    void test1() {
        var reactive = cx.reactive(0);
        var counter = new Counter();
        cx.watchEffect(reactive, x -> counter.increment());
        reactive.set(10);
        reactive.set(20);
        assertEquals(20, reactive.get());
        assertEquals(2, counter.count());
    }

    @Test
    void test2() {
        var number = cx.reactive(10);
        var doubled = cx.memo(() -> number.get() * 2);
        assertEquals(20, doubled.get());
        number.set(30);
        assertEquals(60, doubled.get());
    }

    @Test
    void test3() {
        var number = cx.reactive(10);
        var doubled = cx.memo(() -> number.get() * 2);
        var counter = new Counter();
        cx.watchEffect(doubled, x -> counter.increment());
        number.set(20);
        number.set(30);
        assertEquals(30, number.get());
        assertEquals(60, doubled.get());
        assertEquals(2, counter.count());
    }

    @Test
    void test4() {
        var trigger = cx.trigger();
        var counter = new Counter();
        cx.watchEffect(trigger, counter::increment);
        trigger.set();
        trigger.set();
        trigger.get();
        System.out.println("count = " + counter.count());
        assertEquals(2, counter.count());
    }

    @Test
    void test5() {
        var array = cx.reactive(new ArrayList<>());
        cx.watchEffect(array, numbers -> numbers.forEach(System.out::println));
        System.out.println("Numbers:");
        array.modify(numbers -> numbers.addAll(List.of(1, 2, 3, 4, 5)));
    }

    @Test
    void test6() throws InterruptedException {
        var num1 = cx.reactive(10);
        var num2 = cx.reactive(0);
        cx.watchEffect(num1, x -> {
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
        var num1 = cx.reactive(10);
        var num2 = cx.memo(() -> num1.get() * 2);
        num1.set(30);
        num1.set(60);
        assertEquals(60, num1.get());
        assertEquals(120, num2.get());
    }

    @Test
    void test8() {
        var num = cx.reactive(0);
        cx.watchEffect(num, x -> System.out.println("x = " + x));
        num.update(x -> x + 1);
    }

    @Test
    void test9() {
        var counter = cx.reactive(new Counter());
        cx.watchEffect(counter, c -> System.out.println("count = " + c.count()));
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
        var number = cx.reactive(10);
        var doubled = cx.memo(() -> number.get() * 2);
        assertEquals(10, number.get());
        assertEquals(20, doubled.get());
    }

    @Test
    void test11() {
        var number = cx.reactive(10);
        var doubled = cx.memo(() -> number.get() * 2);
        cx.effect(() -> {
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
        var number = cx.reactive(10);
        var doubled = cx.reactive(0);
        cx.effect(() -> {
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
        var number = cx.reactive(10);
        var doubled = cx.memo(() -> number.get() * 2);
        var quadrupled = cx.memo(() -> doubled.get() * 2);
        cx.watchEffect(quadrupled, x -> {
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
        var count = cx.reactive(0);
        cx.watchEffect(count, (current, old) -> System.out.printf("changed from %s to %s\n", old, current));
        count.set(1);
        count.set(2);
        count.set(3);
        System.out.println("This should print before changes");
        assertEquals(3, count.get());
    }

//	@Test
//	void test15() {
//		var cleanupCount = new Counter();
//		var trace = new ArrayList<Integer>();
//		var number = cx.reactive(0);
//		cx.effect(() -> {
//			var value = number.get();
//			trace.add(value);
//		});
//		for (int i = 1; i <= 10; i++) {
//			number.set(i);
//		}
//		number.doWith(x -> {
//			assertEquals(10, x);
//			assertEquals(11, trace.size());
//			assertEquals(10, cleanupCount.count());
//		});
//	}
//
//	@Test
//	void test17() {
//		cx.task(() -> {
//			var n = new Random().nextInt(100);
//			try {
//				Thread.sleep(2000);
//			} catch (Exception ignored) {}
//			System.out.println(n);
//		});
//
//		var value = Reactives.reactive(10);
//		var j = job(() -> {});
//
//		Reactives.watch(value, x -> j.execute());
//
//		Reactives.effect(() -> {
//			var val = value.get();
//			cx.task(() -> System.out.println(val));
//		});
//
//		var res = resource(() -> {
//			var val = value.get();
//			System.out.println(val);
//		});
//
//		j = job(() -> {
//
//		});
//	}
//

    @Test
    void test18() throws InterruptedException {
        var a = cx.reactive(0);
        var b = cx.reactive(0);
        var sum = cx.resource(() -> {
            System.out.println("summing");
            var x = a.get();
            var y = b.get();
            return x + y;
        }, 0);
        cx.watchEffect(sum.value(), x -> System.out.println("sum = " + x));
        System.out.println("setting a to 10");
        a.set(10); // queue[0]
        System.out.println("setting b to 20");
        b.set(20); // queue[1]
        Thread.sleep(100);
        System.out.println(cx.getRuntime().pending);
    }

    @Test
    void test19() throws InterruptedException {
        var a = cx.reactive(1);
        AtomicInteger c = new AtomicInteger();
        cx.watchJob(a, value -> {
            System.out.println("a = " + value);
            c.getAndIncrement();
        });
        a.update(x -> x + 1);
        a.update(x -> x + 1);
        a.update(x -> x + 1);
        System.out.println("c = " + c.get());
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
