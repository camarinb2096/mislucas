package org.mislucas.model.game;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test
    void rejectsNegativeAmountsBeforeTheyCanBeDepositedOrWithdrawn() {
        assertThrows(IllegalArgumentException.class, () -> new Money(-1));
    }
}
