package java.util.maybe;

public class MaybeStateException extends Exception {

    protected MaybeStateException(String message) {
        super(message);
    }

    protected MaybeStateException(String message, Throwable cause) {
        super(message, cause);
    }

    protected MaybeStateException(Throwable cause) {
        super(cause);
    }

    protected MaybeStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
