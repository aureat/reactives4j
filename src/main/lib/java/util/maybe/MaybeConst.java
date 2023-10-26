package java.util.maybe;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MaybeConst<T> extends BaseMaybe<T> {

    protected static final MaybeConst<?> NOTHING = new MaybeConst<>(null);

    protected MaybeConst(T value) {
        super(value);
    }

    public static <T> MaybeConst<T> just(T value) {
        if (value == null) {
            return nothing();
        }
        return new MaybeConst<>(value);
    }

    public static <T> MaybeConst<T> nothing() {
        @SuppressWarnings("unchecked") MaybeConst<T> nothing = (MaybeConst<T>) NOTHING;
        return nothing;
    }

    public static <T> void swap(MaybeConst<T> a, MaybeConst<T> b) throws IllegalArgumentException {
        if (a.isNothing() || b.isNothing()) {
            throw new IllegalArgumentException("Cannot swap with MaybeConst.nothing");
        }
        BaseMaybe.swap(a, b);
    }

    public MaybeConst<T> ifPresentOr(Consumer<T> action, MaybeConst<T> other) {
        if (isNothing()) return other;
        action.accept(value);
        return this;
    }

    public MaybeConst<T> expectPresent(String message) {
        ifNothingPanic(message);
        return this;
    }

    public MaybeConst<T> expectPresent() {
        ifNothingPanic();
        return this;
    }

    public <U> MaybeConst<U> map(Function<T, U> mapper) {
        return just(map(this, () -> null, mapper));
    }

    public <U> MaybeConst<U> mapOr(U other, Function<T, U> mapper) {
        return just(map(this, () -> other, mapper));
    }

    public <U> MaybeConst<U> mapOrElse(Supplier<U> orElse, Function<T, U> mapper) {
        return just(map(this, orElse, mapper));
    }

    public MaybeConst<T> filter(Predicate<T> predicate) {
        return just(filter(this, predicate));
    }

    public MaybeConst<T> flatten() {
        try {
            @SuppressWarnings("unchecked") MaybeConst<T> inner = (MaybeConst<T>) value;
            return inner;
        } catch (Exception ex) {
            return nothing();
        }
    }

    public <U> MaybeConst<U> and(MaybeConst<U> other) {
        if (isNothing()) return nothing();
        return other;
    }

    public <U> MaybeConst<U> andThen(Function<T, MaybeConst<U>> f) {
        if (isNothing()) return nothing();
        return f.apply(value);
    }

    public MaybeConst<T> or(MaybeConst<T> other) {
        if (isNothing()) return other;
        return just(value);
    }

    public MaybeConst<T> orElse(Supplier<MaybeConst<T>> action) {
        if (isNothing()) return action.get();
        return just(value);
    }

    public MaybeConst<T> xor(MaybeConst<T> other) {
        if (isPresent() && other.isNothing()) return just(value);
        if (isNothing() && other.isPresent()) return just(other.value);
        return nothing();
    }

    @Override
    public String toString() {
        return isNothing() ? "MaybeConst.nothing" : ("MaybeConst.just(" + value + ")");
    }

}
