package java.util.maybe;

public class MaybeNothingException extends MaybeStateException {

    protected MaybeNothingException(String message) {
        super(message);
    }

    protected MaybeNothingException(String message, Throwable cause) {
        super(message, cause);
    }

    protected MaybeNothingException(Throwable cause) {
        super(cause);
    }

    protected MaybeNothingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
