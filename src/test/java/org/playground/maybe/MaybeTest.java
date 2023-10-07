package org.playground.maybe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaybeTest {

    @Test
    void test1() throws MaybeNothingException {
        boolean value = true;
        Maybe<Boolean> maybe = Maybe.just(value);
        assertTrue(maybe.filter(x -> x).isPresent());
        assertFalse(maybe.map(x -> !x).unwrap());
    }

    @Test
    void test2() {
        Maybe<Boolean> value = Maybe.nothing();
        assertTrue(value.isNothing());
        value.setIfPresent(true);
        assertTrue(value.isNothing());
    }

    @Test
    void test3() {
        Maybe<Boolean> maybeBoolean = Maybe.nothing();
        assertThrows(MaybeNothingException.class, maybeBoolean::unwrap);
        Maybe<Integer> maybeInteger = maybeBoolean.map(x -> 10);
        assertTrue(maybeInteger.isNothing());
        Maybe<Integer> maybeAnotherInteger = maybeInteger.mapOr(20, x -> 10);
        assertTrue(maybeAnotherInteger.isPresentAnd(x -> x == 20));
        assertThrows(IllegalArgumentException.class, () -> MaybeConst.swap(maybeInteger, maybeAnotherInteger));
    }

    @Test
    void test4() {
        Maybe<Integer> value = Maybe.just(10);
        value.updateIfPresent(x -> x / 10);
        assertTrue(value.isPresentAnd(x -> x == 1));
    }

}
