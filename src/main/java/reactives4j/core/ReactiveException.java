package reactives4j.core;

public class ReactiveException extends RuntimeException {

    protected ReactiveException(String message) {
        super(message);
    }

    protected ReactiveException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ReactiveException(Throwable cause) {
        super(cause);
    }

    protected ReactiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString() {
        return "ReactiveException: " + getMessage();
    }

}
