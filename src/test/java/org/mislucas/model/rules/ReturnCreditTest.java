package org.mislucas.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mislucas.model.game.Jar;
import org.mislucas.model.game.JarType;
import org.mislucas.model.game.Money;

class ReturnCreditTest {
    private final Jar spending = new Jar(JarType.SPENDING);
    private final Jar goal = new Jar(JarType.GOAL);
    private final Jar cushion = new Jar(JarType.CUSHION);
    private final ReturnCredit credit = new ReturnCredit(goal, cushion);

    @Test
    void creditsWeeksFourAndEightSeparatelyAndAccumulatesTheRoundedReturns() {
        spending.deposit(new Money(10_000));
        goal.deposit(new Money(7_500));
        cushion.deposit(new Money(2_499));

        credit.apply(4);

        assertCreditsAndBalances(200, 10_000, 7_700, 2_499);

        // The previous Goal credit makes the week-eight balance cross the rounding midpoint.
        goal.deposit(new Money(4_900));
        cushion.deposit(new Money(1));
        credit.apply(8);

        assertCreditsAndBalances(600, 10_000, 12_900, 2_600);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 6, 7})
    void leavesBalancesAndTotalUnchangedInOtherWeeks(int week) {
        spending.deposit(new Money(10_000));
        goal.deposit(new Money(7_500));
        cushion.deposit(new Money(5_000));

        credit.apply(week);

        assertCreditsAndBalances(0, 10_000, 7_500, 5_000);
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "2499, 0", "2500, 100", "2501, 100", "7499, 100", "7500, 200", "7501, 200"})
    void roundsEachReturnToTheNearestHundredWithTiesUpward(int balance, int expectedReturn) {
        goal.deposit(new Money(balance));

        credit.apply(4);

        assertCreditsAndBalances(expectedReturn, 0, balance + expectedReturn, 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 9})
    void rejectsWeeksOutsideTheGameWithoutCreditingEitherJar(int week) {
        goal.deposit(new Money(7_500));
        cushion.deposit(new Money(2_500));

        assertThrows(IllegalArgumentException.class, () -> credit.apply(week));

        assertCreditsAndBalances(0, 0, 7_500, 2_500);
    }

    @Test
    void rejectsIneligibleOrSwappedJars() {
        assertThrows(IllegalArgumentException.class, () -> new ReturnCredit(spending, cushion));
        assertThrows(IllegalArgumentException.class, () -> new ReturnCredit(goal, spending));
        assertThrows(IllegalArgumentException.class, () -> new ReturnCredit(cushion, goal));
    }

    @Test
    void rejectsAnOverflowInTheSecondJarBeforeCreditingEitherJar() {
        goal.deposit(new Money(7_500));
        cushion.deposit(new Money(2_500));
        credit.apply(4);
        cushion.deposit(new Money(Integer.MAX_VALUE - cushion.balance().pesos()));

        assertThrows(ArithmeticException.class, () -> credit.apply(8));

        assertCreditsAndBalances(300, 0, 7_700, Integer.MAX_VALUE);
    }

    private void assertCreditsAndBalances(int total, int spendingBalance, int goalBalance, int cushionBalance) {
        assertEquals(new Money(total), credit.total());
        assertEquals(new Money(spendingBalance), spending.balance());
        assertEquals(new Money(goalBalance), goal.balance());
        assertEquals(new Money(cushionBalance), cushion.balance());
    }
}
