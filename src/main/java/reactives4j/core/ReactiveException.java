package reactives4j.core;

public class ReactiveException extends RuntimeException {

    public ReactiveException(String message) {
        super(message);
    }

    public ReactiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReactiveException(Throwable cause) {
        super(cause);
    }

    public ReactiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString() {
        return "ReactiveException: " + getMessage();
    }

}
