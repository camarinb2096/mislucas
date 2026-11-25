package org.mislucas.model.rules;

import org.mislucas.model.game.Jar;
import org.mislucas.model.game.JarType;
import org.mislucas.model.game.Money;

/**
 * Distributes an allowance or opportunity income among the three jars of a game.
 */
public final class IncomeAllocation {
    /**
     * Required increment for each allocation, in Colombian pesos.
     */
    private static final int ALLOCATION_INCREMENT = 1_000;

    /** Jar that receives the spending allocation. */
    private final Jar spending;

    /** Jar that receives the savings-goal allocation. */
    private final Jar goal;

    /** Jar that receives the emergency-reserve allocation. */
    private final Jar cushion;

    /**
     * Binds the allocation rule to the jars that will receive the income.
     *
     * @param spending the spending jar
     * @param goal the goal jar
     * @param cushion the cushion jar
     * @throws NullPointerException if a jar is null
     * @throws IllegalArgumentException if a jar has a different type than required
     */
    public IncomeAllocation(Jar spending, Jar goal, Jar cushion) {
        if (spending.type() != JarType.SPENDING
                || goal.type() != JarType.GOAL
                || cushion.type() != JarType.CUSHION) {
            throw new IllegalArgumentException("Use the spending, goal, and cushion jars in that order.");
        }

        this.spending = spending;
        this.goal = goal;
        this.cushion = cushion;
    }

    /**
     * Deposits a complete allocation after validating all amounts and resulting balances.
     * Every jar retains its balance if validation fails.
     *
     * @param income the full income to allocate
     * @param spendingAmount the amount to deposit into the spending jar
     * @param goalAmount the amount to deposit into the goal jar
     * @param cushionAmount the amount to deposit into the cushion jar
     * @throws NullPointerException if an amount is null
     * @throws IllegalArgumentException if an allocation is outside the required increment
     *     or the amounts do not add up to the income
     * @throws ArithmeticException if a resulting balance exceeds the integer range
     */
    public void apply(Money income, Money spendingAmount, Money goalAmount, Money cushionAmount) {
        int spendingPesos = spendingAmount.pesos();
        int goalPesos = goalAmount.pesos();
        int cushionPesos = cushionAmount.pesos();

        if (spendingPesos % ALLOCATION_INCREMENT != 0
                || goalPesos % ALLOCATION_INCREMENT != 0
                || cushionPesos % ALLOCATION_INCREMENT != 0) {
            throw new IllegalArgumentException("Allocate income in increments of COP 1,000.");
        }

        long allocatedPesos = (long) spendingPesos + goalPesos + cushionPesos;
        if (allocatedPesos != income.pesos()) {
            throw new IllegalArgumentException("Allocate the full income among the three jars.");
        }

        // Check every resulting balance before depositing into the first jar.
        Math.addExact(spending.balance().pesos(), spendingPesos);
        Math.addExact(goal.balance().pesos(), goalPesos);
        Math.addExact(cushion.balance().pesos(), cushionPesos);

        spending.deposit(spendingAmount);
        goal.deposit(goalAmount);
        cushion.deposit(cushionAmount);
    }
}
