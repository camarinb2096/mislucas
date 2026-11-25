package org.mislucas.model.rules;

import java.util.Objects;
import org.mislucas.model.config.GameParameters;
import org.mislucas.model.game.Jar;
import org.mislucas.model.game.JarType;
import org.mislucas.model.game.Money;

/**
 * Pays expenses from a game's jars in the order required by each expense.
 */
public final class ExpensePayment {
    /**
     * Balance used for wants and ordinary expenses.
     */
    private final Jar spending;

    /**
     * Savings used after Spending and Cushion for mandatory expenses.
     */
    private final Jar goal;

    /**
     * Emergency reserve used first for emergencies.
     */
    private final Jar cushion;

    /**
     * Binds the rule to the three jars of one game.
     *
     * @param spending the spending jar
     * @param goal the goal jar
     * @param cushion the cushion jar
     * @throws NullPointerException if a jar is null
     * @throws IllegalArgumentException if a jar has the wrong type
     */
    public ExpensePayment(Jar spending, Jar goal, Jar cushion) {
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
     * Checks whether the game schedules a fixed expense for this week.
     *
     * @param week the current game week
     * @return true for weeks 1 and 5
     * @throws IllegalArgumentException if the week is outside the game
     */
    public static boolean isFixedExpenseDue(int week) {
        if (week < 1 || week > GameParameters.WEEKS_PER_GAME) {
            throw new IllegalArgumentException("Week must be within the game.");
        }
        return week == 1 || week == 5;
    }

    /**
     * Charges the fixed expense in weeks 1 and 5 using Spending, Cushion, then Goal.
     *
     * @param week the current game week
     * @param amount the scenario's fixed expense
     * @return the shortfall and the amount paid from Goal, both zero in weeks without a charge
     * @throws IllegalArgumentException if the week is outside the game
     * @throws NullPointerException if the amount is null
     */
    public MandatoryPayment chargeFixedExpense(int week, Money amount) {
        boolean due = isFixedExpenseDue(week);
        Objects.requireNonNull(amount, "amount");
        if (due) {
            return payMandatory(amount, spending, cushion);
        }
        return new MandatoryPayment(new Money(0), new Money(0));
    }

    /**
     * Pays a need using Spending, Cushion, then Goal.
     *
     * @param amount the cost of the need
     * @return the shortfall and the amount paid from Goal
     * @throws NullPointerException if the amount is null
     */
    public MandatoryPayment payNeed(Money amount) {
        return payMandatory(amount, spending, cushion);
    }

    /**
     * Pays an emergency using Cushion, Spending, then Goal.
     *
     * @param amount the cost of the emergency
     * @return the shortfall and the amount paid from Goal
     * @throws NullPointerException if the amount is null
     */
    public MandatoryPayment payEmergency(Money amount) {
        return payMandatory(amount, cushion, spending);
    }

    /**
     * Attempts to pay for a want that the player accepted, using Spending only.
     *
     * @param amount the cost of the purchase
     * @return true if the full amount was paid, or false if Spending could not cover it
     * @throws NullPointerException if the amount is null
     */
    public boolean tryPayWant(Money amount) {
        if (spending.balance().pesos() < amount.pesos()) {
            return false;
        }
        spending.withdraw(amount);
        return true;
    }

    /**
     * Uses the expense's two priority jars before drawing from Goal.
     *
     * @param amount the total cost
     * @param first the first jar to draw from
     * @param second the next jar to draw from
     * @return the unpaid balance and the amount drawn from Goal
     */
    private MandatoryPayment payMandatory(Money amount, Jar first, Jar second) {
        int remaining = amount.pesos();
        remaining -= withdrawUpTo(first, remaining);
        remaining -= withdrawUpTo(second, remaining);
        int goalPayment = withdrawUpTo(goal, remaining);
        return new MandatoryPayment(new Money(remaining - goalPayment), new Money(goalPayment));
    }

    /**
     * Withdraws up to the requested number of pesos from a jar.
     *
     * @param jar the jar to draw from
     * @param remaining the portion of the cost still unpaid
     * @return the number of pesos withdrawn
     */
    private static int withdrawUpTo(Jar jar, int remaining) {
        int payment = Math.min(jar.balance().pesos(), remaining);
        if (payment > 0) {
            jar.withdraw(new Money(payment));
        }
        return payment;
    }
}
