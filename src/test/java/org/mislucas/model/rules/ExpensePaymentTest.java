package org.mislucas.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mislucas.model.game.Jar;
import org.mislucas.model.game.JarType;
import org.mislucas.model.game.Money;

class ExpensePaymentTest {
    private final Jar spending = new Jar(JarType.SPENDING);
    private final Jar goal = new Jar(JarType.GOAL);
    private final Jar cushion = new Jar(JarType.CUSHION);
    private final ExpensePayment payment = new ExpensePayment(spending, goal, cushion);

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void chargesFixedExpenseInWeeksOneAndFiveUsingSpendingFirst(int week) {
        fundJars(2_000, 2_000, 3_000);

        MandatoryPayment result = payment.chargeFixedExpense(week, new Money(4_000));

        assertEquals(new MandatoryPayment(new Money(0), new Money(0)), result);
        assertBalances(0, 2_000, 1_000);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 6, 7, 8})
    void skipsFixedExpenseInTheOtherWeeks(int week) {
        fundJars(2_000, 2_000, 3_000);

        MandatoryPayment result = payment.chargeFixedExpense(week, new Money(4_000));

        assertEquals(new MandatoryPayment(new Money(0), new Money(0)), result);
        assertBalances(2_000, 2_000, 3_000);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 9})
    void rejectsAWeekOutsideTheGameBeforeCharging(int week) {
        fundJars(2_000, 2_000, 3_000);

        assertThrows(IllegalArgumentException.class, () -> payment.chargeFixedExpense(week, new Money(6_000)));
        assertBalances(2_000, 2_000, 3_000);
    }

    @Test
    void paysNeedsFromSpendingBeforeUsingCushion() {
        fundJars(2_000, 5_000, 3_000);

        MandatoryPayment result = payment.payNeed(new Money(4_000));

        assertEquals(new MandatoryPayment(new Money(0), new Money(0)), result);
        assertBalances(0, 5_000, 1_000);
    }

    @Test
    void paysEmergenciesFromCushionBeforeUsingSpending() {
        fundJars(2_000, 5_000, 3_000);

        MandatoryPayment result = payment.payEmergency(new Money(4_000));

        assertEquals(new MandatoryPayment(new Money(0), new Money(0)), result);
        assertBalances(1_000, 5_000, 0);
    }

    @Test
    void reportsTheGoalContributionAfterExhaustingBothPriorityJars() {
        fundJars(2_000, 5_000, 3_000);

        MandatoryPayment result = payment.payEmergency(new Money(6_000));

        assertEquals(new MandatoryPayment(new Money(0), new Money(1_000)), result);
        assertBalances(0, 4_000, 0);
    }

    @Test
    void returnsTheMandatoryShortfallAfterUsingAllAvailableFunds() {
        fundJars(2_000, 1_000, 3_000);

        MandatoryPayment result = payment.payNeed(new Money(7_500));

        assertEquals(new MandatoryPayment(new Money(1_500), new Money(1_000)), result);
        assertBalances(0, 0, 0);
    }

    @Test
    void paysAnAcceptedWantWithTheExactSpendingBalance() {
        fundJars(2_000, 5_000, 3_000);

        assertTrue(payment.tryPayWant(new Money(2_000)));

        assertBalances(0, 5_000, 3_000);
    }

    @Test
    void rejectsAWantThatSpendingCannotCoverAndPreservesEveryBalance() {
        fundJars(2_000, 5_000, 3_000);

        assertFalse(payment.tryPayWant(new Money(2_001)));

        assertBalances(2_000, 5_000, 3_000);
    }

    private void fundJars(int spendingPesos, int goalPesos, int cushionPesos) {
        spending.deposit(new Money(spendingPesos));
        goal.deposit(new Money(goalPesos));
        cushion.deposit(new Money(cushionPesos));
    }

    private void assertBalances(int spendingPesos, int goalPesos, int cushionPesos) {
        assertEquals(new Money(spendingPesos), spending.balance());
        assertEquals(new Money(goalPesos), goal.balance());
        assertEquals(new Money(cushionPesos), cushion.balance());
    }
}
