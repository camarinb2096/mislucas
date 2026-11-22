package org.mislucas.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mislucas.model.game.Jar;
import org.mislucas.model.game.JarType;
import org.mislucas.model.game.Money;

class IncomeAllocationTest {
    private final Jar spending = new Jar(JarType.SPENDING);
    private final Jar goal = new Jar(JarType.GOAL);
    private final Jar cushion = new Jar(JarType.CUSHION);
    private final IncomeAllocation allocation = new IncomeAllocation(spending, goal, cushion);

    @BeforeEach
    void seedBalances() {
        spending.deposit(new Money(100));
        goal.deposit(new Money(200));
        cushion.deposit(new Money(300));
    }

    @Test
    void creditsTheFullIncomeToTheThreeJars() {
        allocation.apply(new Money(10_000), new Money(4_000), new Money(5_000), new Money(1_000));

        assertBalances(4_100, 5_200, 1_300);
    }

    @Test
    void appliesAnAdditionalIncomeAndAllowsZeroAllocations() {
        allocation.apply(new Money(10_000), new Money(4_000), new Money(5_000), new Money(1_000));
        allocation.apply(new Money(20_000), new Money(0), new Money(12_000), new Money(8_000));

        assertBalances(4_100, 17_200, 9_300);
    }

    @Test
    void rejectsAllocationBelowIncomeWithoutChangingBalances() {
        assertThrows(IllegalArgumentException.class, () ->
                allocation.apply(new Money(10_000), new Money(4_000), new Money(4_000), new Money(1_000)));

        assertBalances(100, 200, 300);
    }

    @Test
    void rejectsAllocationAboveIncomeWithoutChangingBalances() {
        assertThrows(IllegalArgumentException.class, () ->
                allocation.apply(new Money(10_000), new Money(4_000), new Money(6_000), new Money(1_000)));

        assertBalances(100, 200, 300);
    }

    @Test
    void rejectsNonMultipleAmountsEvenWhenTheyAddUpToTheIncome() {
        assertThrows(IllegalArgumentException.class, () ->
                allocation.apply(new Money(10_000), new Money(4_100), new Money(4_900), new Money(1_000)));

        assertBalances(100, 200, 300);
    }

    @Test
    void rejectsNegativeAmountWithoutChangingBalances() {
        assertThrows(IllegalArgumentException.class, () ->
                allocation.apply(new Money(10_000), new Money(4_000), new Money(-1_000), new Money(7_000)));

        assertBalances(100, 200, 300);
    }

    @Test
    void rejectsAllocationTotalAboveIntegerRangeWithoutChangingBalances() {
        // The true total is COP 4,500,001,000, which wraps to the income as an int.
        assertThrows(IllegalArgumentException.class, () ->
                allocation.apply(new Money(205_033_704), new Money(1_500_000_000),
                        new Money(1_500_000_000), new Money(1_500_001_000)));

        assertBalances(100, 200, 300);
    }

    @Test
    void rejectsLastJarOverflowBeforeCreditingAnyJar() {
        cushion.deposit(new Money(Integer.MAX_VALUE - 300));

        assertThrows(ArithmeticException.class, () ->
                allocation.apply(new Money(10_000), new Money(4_000), new Money(5_000), new Money(1_000)));

        assertBalances(100, 200, Integer.MAX_VALUE);
    }

    @Test
    void rejectsJarsWithSwappedOrRepeatedRoles() {
        assertThrows(IllegalArgumentException.class, () -> new IncomeAllocation(goal, spending, cushion));
        assertThrows(IllegalArgumentException.class, () -> new IncomeAllocation(spending, goal, goal));

        assertBalances(100, 200, 300);
    }

    private void assertBalances(int spendingPesos, int goalPesos, int cushionPesos) {
        assertEquals(new Money(spendingPesos), spending.balance());
        assertEquals(new Money(goalPesos), goal.balance());
        assertEquals(new Money(cushionPesos), cushion.balance());
    }
}
