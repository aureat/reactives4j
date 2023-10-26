package java.util.maybe;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

abstract class BaseMaybe<T> {

    protected static final String NOTHING_ERROR_MSG = "No value present";

    protected T value;

    protected BaseMaybe(T value) {
        this.value = value;
    }

    protected static <T> void swap(@NotNull BaseMaybe<T> a, @NotNull BaseMaybe<T> b) {
        T temp = a.value;
        a.value = b.value;
        b.value = temp;
    }

    protected static <T, U> U map(@NotNull BaseMaybe<T> maybe, @NotNull Supplier<U> orElse, @NotNull Function<T, U> mapper) {
        if (maybe.isNothing()) return orElse.get();
        return mapper.apply(maybe.value);
    }

    protected static <T> T filter(@NotNull BaseMaybe<T> maybe, Predicate<T> predicate) {
        if (maybe.isNothing()) return null;
        return predicate.test(maybe.value) ? maybe.value : null;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isPresentAnd(@NotNull Predicate<T> predicate) {
        return isPresent() && predicate.test(value);
    }

    public void ifPresent(@NotNull Consumer<T> action) {
        if (isNothing()) return;
        action.accept(value);
    }

    public void ifPresentOrElse(@NotNull Consumer<T> action, @NotNull Runnable orElse) {
        if (isPresent()) {
            action.accept(value);
        } else {
            orElse.run();
        }
    }

    public boolean isNothing() {
        return value == null;
    }

    public boolean isNothingAnd(@NotNull Supplier<Boolean> predicate) {
        return isNothing() && predicate.get();
    }

    public void ifNothing(@NotNull Runnable action) {
        if (isPresent()) return;
        action.run();
    }

    public void ifNothingThrow() throws MaybeNothingException {
        ifNothingThrow(NOTHING_ERROR_MSG);
    }

    public void ifNothingThrow(@NotNull String message) throws MaybeNothingException {
        if (isPresent()) return;
        throw new MaybeNothingException(message);
    }

    public void ifNothingPanic() {
        ifNothingPanic(NOTHING_ERROR_MSG);
    }

    public void ifNothingPanic(@NotNull String message) {
        if (isPresent()) return;
        panic(new MaybeNothingException(message));
    }

    public void expectIfPresent(@NotNull Consumer<T> action, @NotNull String message) {
        ifNothingPanic(message);
        ifPresent(action);
    }

    public void expectIfPresent(@NotNull Consumer<T> action) {
        ifNothingPanic();
        ifPresent(action);
    }

    public T expectGet(@NotNull String message) {
        ifNothingPanic(message);
        return value;
    }

    public <U> U expectWith(@NotNull Function<T, U> f, String message) {
        ifNothingPanic(message);
        return f.apply(value);
    }

    public T get() throws MaybeNothingException {
        ifNothingThrow();
        return value;
    }

    public T getOr(@NotNull T other) {
        if (isNothing()) return other;
        return value;
    }

    public T getOrElse(@NotNull Supplier<T> action) {
        if (isNothing()) return action.get();
        return value;
    }

    public T getOrElseDo(@NotNull Runnable action) {
        if (isNothing()) {
            action.run();
            return null;
        }
        return value;
    }

    public T getUnchecked() {
        return value;
    }

    public void doWith(@NotNull Consumer<T> action) throws MaybeNothingException {
        ifNothingThrow();
        action.accept(value);
    }

    public void doWithUnchecked(@NotNull Consumer<T> action) {
        action.accept(value);
    }

    public <U> U with(@NotNull Function<T, U> f) throws MaybeNothingException {
        ifNothingThrow();
        return f.apply(value);
    }

    public <U> U withOr(@NotNull Function<T, U> f, U other) {
        if (isNothing()) return other;
        return f.apply(value);
    }

    public <U> U withOrElse(@NotNull Function<T, U> f, Supplier<U> orElse) {
        if (isNothing()) return orElse.get();
        return f.apply(value);
    }

    public <U> U withOrElseDo(@NotNull Function<T, U> f, Runnable orElseDo) {
        if (isNothing()) {
            orElseDo.run();
            return null;
        }
        return f.apply(value);
    }

    public <U> U withUnchecked(@NotNull Function<T, U> f) {
        return f.apply(value);
    }

    protected void panic(Exception e) {
        e.printStackTrace();
        System.exit(1);
    }

    public Stream<T> stream() {
        if (isNothing()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseMaybe<?> maybe = (BaseMaybe<?>) o;
        return Objects.equals(value, maybe.value);
    }

}
