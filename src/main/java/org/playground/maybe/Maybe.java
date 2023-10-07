package org.playground.maybe;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Maybe<T> extends BaseMaybe<T> {

	private Maybe(T value) {
		super(value);
	}

	public static <T> Maybe<T> just(T value) {
		return new Maybe<>(value);
	}

	public static <T> Maybe<T> nothing() {
		return new Maybe<>(null);
	}

	public static <T> void swap(Maybe<T> a, Maybe<T> b) {
		BaseMaybe.swap(a, b);
	}

	public Maybe<T> expectPresent(String message) {
		ifNothingPanic(message);
		return this;
	}

	public Maybe<T> expectPresent(Runnable action) {
		ifNothing(action);
		return this;
	}

	public Maybe<T> expectPresent() {
		ifNothingPanic();
		return this;
	}

	public Maybe<T> expectNothing(@NotNull String message) {
		if (isNothing()) return this;
		panic(new MaybeStateException(message));
		return null;
	}

	public Maybe<T> expectNothing(@NotNull Runnable action) {
		if (isNothing()) return this;
		action.run();
		return this;
	}

	public Maybe<T> expectNothing() {
		return expectNothing("Nothing was expected, but a value is present");
	}

	public T expectUnwrap(@NotNull String message) {
		T temp = expectGet(message);
		value = null;
		return temp;
	}

	public Maybe<T> expectReplace(T newValue, @NotNull String message) {
		ifNothingPanic(message);
		return replace(newValue);
	}

	public Maybe<T> expectTake(@NotNull String message) {
		return expectReplace(null, message);
	}

	public void expectUpdate(@NotNull Function<T, T> updater, @NotNull String message) {
		ifNothingPanic(message);
		value = updater.apply(value);
	}

	public T unwrap() throws MaybeNothingException {
		T temp = get();
		value = null;
		return temp;
	}

	public T unwrapOr(T other) {
		T temp = getOr(other);
		value = null;
		return temp;
	}

	public T unwrapOrElse(Supplier<T> action) {
		T temp = getOrElse(action);
		value = null;
		return temp;
	}

	public Maybe<T> replace(T newValue) {
		T old = value;
		value = newValue;
		return just(old);
	}

	public void swap(Maybe<T> other) {
		swap(this, other);
	}

	public Maybe<T> take() {
		return replace(null);
	}

	public void set(@NotNull T newValue) {
		value = newValue;
	}

	public void setIfPresent(@NotNull T newValue) {
		if (isNothing()) return;
		value = newValue;
	}

	public void setIfNothing(@NotNull T newValue) {
		if (isPresent()) return;
		value = newValue;
	}

	public T setIfNothingAndGet(@NotNull T newValue) {
		if (isPresent()) return value;
		value = newValue;
		return newValue;
	}

	public T setAndGet(@NotNull T newValue) {
		set(newValue);
		return newValue;
	}

	public void setAndThen(@NotNull T newValue, @NotNull Function<T, T> updater) {
		set(newValue);
		updater.apply(value);
	}

	public T setAndThenGet(@NotNull T newValue, @NotNull Function<T, T> updater) {
		set(newValue);
		updater.apply(value);
		return value;
	}

	public void clear() {
		value = null;
	}

	public void fill(@NotNull Supplier<T> action) {
		value = action.get();
	}

	public void fillIfNothing(@NotNull Supplier<T> action) {
		if (isPresent()) return;
		fill(action);
	}

	public T fillIfNothingAndGet(@NotNull Supplier<T> action) {
		if (isPresent()) return value;
		value = action.get();
		return value;
	}

	public void update(@NotNull Function<T, T> updater) throws MaybeNothingException {
		ifNothingThrow();
		value = updater.apply(value);
	}

	public void updateUnchecked(@NotNull Function<T, T> updater) {
		value = updater.apply(value);
	}

	public void updateIfPresent(@NotNull Function<T, T> updater) {
		if (isNothing()) return;
		value = updater.apply(value);
	}

	public <U> Maybe<U> map(Function<T, U> mapper) {
		return just(BaseMaybe.map(this, () -> null, mapper));
	}

	public <U> Maybe<U> mapOr(U other, Function<T, U> mapper) {
		return just(BaseMaybe.map(this, () -> other, mapper));
	}

	public <U> Maybe<U> mapOrElse(Supplier<U> orElse, Function<T, U> mapper) {
		return just(BaseMaybe.map(this, orElse, mapper));
	}

	public Maybe<T> filter(Predicate<T> predicate) {
		return just(BaseMaybe.filter(this, predicate));
	}

	public Maybe<T> flatten() {
		try {
			@SuppressWarnings("unchecked") Maybe<T> inner = (Maybe<T>) value;
			return inner;
		} catch (Exception ex) {
			return nothing();
		}
	}

	public <U> Maybe<U> and(Maybe<U> other) {
		if (isNothing()) return nothing();
		return other;
	}

	public <U> Maybe<U> andThen(Function<T, Maybe<U>> f) {
		if (isNothing()) return nothing();
		return f.apply(value);
	}

	public Maybe<T> or(Maybe<T> other) {
		if (isNothing()) return other;
		return just(value);
	}

	public Maybe<T> orElse(Supplier<Maybe<T>> action) {
		if (isNothing()) return action.get();
		return just(value);
	}

	public Maybe<T> xor(Maybe<T> other) {
		if (isPresent() && other.isNothing()) return just(value);
		if (isNothing() && other.isPresent()) return just(other.value);
		return nothing();
	}

	@Override
	public String toString() {
		return isNothing() ? "Maybe.nothing" : ("Maybe.just(" + value + ")");
	}

}
