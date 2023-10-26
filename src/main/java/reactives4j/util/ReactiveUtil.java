package reactives4j.util;

import reactives4j.core.ReactiveException;

public final class ReactiveUtil {

    public static void panicNoValue() {
        panic("Empty reactive");
    }

    public static void panic() {
        panic("Reactive runtime error");
    }

    public static void panic(Exception e) {
        throw new ReactiveException(e);
    }

    public static void panic(String message) {
        throw new ReactiveException(message);
    }

}
