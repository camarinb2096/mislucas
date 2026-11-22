package org.mislucas.model.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JarTest {
    @Test
    void jarsStartAtZeroWithDistinctIdentities() {
        Jar spending = new Jar(JarType.SPENDING);
        Jar goal = new Jar(JarType.GOAL);
        Jar cushion = new Jar(JarType.CUSHION);

        assertEquals(JarType.SPENDING, spending.type());
        assertEquals(JarType.GOAL, goal.type());
        assertEquals(JarType.CUSHION, cushion.type());
        assertEquals(new Money(0), spending.balance());
        assertEquals(new Money(0), goal.balance());
        assertEquals(new Money(0), cushion.balance());
    }

    @Test
    void depositsWholePesosIncludingAOneHundredPesoReturn() {
        Jar goal = new Jar(JarType.GOAL);
        Jar cushion = new Jar(JarType.CUSHION);

        goal.deposit(new Money(7_000));
        goal.deposit(new Money(100));

        assertEquals(new Money(7_100), goal.balance());
        assertEquals(new Money(0), cushion.balance());
    }

    @Test
    void withdrawsPartOfTheBalanceInWholePesos() {
        Jar spending = new Jar(JarType.SPENDING);
        spending.deposit(new Money(2_000));

        spending.withdraw(new Money(1_001));

        assertEquals(new Money(999), spending.balance());
    }

    @Test
    void withdrawsTheExactBalance() {
        Jar spending = new Jar(JarType.SPENDING);
        spending.deposit(new Money(2_000));

        spending.withdraw(new Money(2_000));

        assertEquals(new Money(0), spending.balance());
    }

    @Test
    void rejectsWithdrawalAboveTheBalanceWithoutChangingIt() {
        Jar spending = new Jar(JarType.SPENDING);
        spending.deposit(new Money(2_000));

        assertThrows(IllegalArgumentException.class, () -> spending.withdraw(new Money(2_001)));

        assertEquals(new Money(2_000), spending.balance());
    }

    @Test
    void acceptsZeroAmountOperationsOnAnEmptyJar() {
        Jar cushion = new Jar(JarType.CUSHION);

        cushion.deposit(new Money(0));
        cushion.withdraw(new Money(0));

        assertEquals(new Money(0), cushion.balance());
    }

    @Test
    void rejectsDepositOverflowWithoutChangingTheBalance() {
        Jar cushion = new Jar(JarType.CUSHION);
        Money maximum = new Money(Integer.MAX_VALUE);
        cushion.deposit(maximum);

        assertThrows(ArithmeticException.class, () -> cushion.deposit(new Money(1)));

        assertEquals(maximum, cushion.balance());
    }
}
