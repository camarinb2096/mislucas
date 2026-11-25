package org.mislucas.model.rules;

import org.mislucas.model.config.GameParameters;
import org.mislucas.model.game.Jar;
import org.mislucas.model.game.JarType;
import org.mislucas.model.game.Money;

/**
 * Credits the scheduled returns to a game's Goal and Cushion jars.
 */
public final class ReturnCredit {
    /**
     * The return rate as a percentage of each eligible balance.
     */
    private static final int RETURN_PERCENT = 2;

    /**
     * The increment used to round a return, in Colombian pesos.
     */
    private static final int RETURN_INCREMENT = 100;

    /**
     * The balance that earns one return increment at the configured rate.
     */
    private static final int BALANCE_PER_INCREMENT = RETURN_INCREMENT * 100 / RETURN_PERCENT;

    /**
     * The savings-goal jar.
     */
    private final Jar goal;

    /**
     * The emergency-reserve jar.
     */
    private final Jar cushion;

    /**
     * The sum of all returns credited by this rule.
     */
    private Money total = new Money(0);

    /**
     * Binds the return rule to the two eligible jars of one game.
     *
     * @param goal the savings-goal jar
     * @param cushion the emergency-reserve jar
     * @throws IllegalArgumentException if either jar has the wrong type
     * @throws NullPointerException if either jar is null
     */
    public ReturnCredit(Jar goal, Jar cushion) {
        if (goal.type() != JarType.GOAL || cushion.type() != JarType.CUSHION) {
            throw new IllegalArgumentException("Use the goal and cushion jars in that order.");
        }
        this.goal = goal;
        this.cushion = cushion;
    }

    /**
     * Credits each jar at the end of week four or eight and records the total.
     * The game calls this rule once when closing a week.
     *
     * @param week the closing game week
     * @throws IllegalArgumentException if the week is outside the game
     * @throws ArithmeticException if either resulting balance or the total exceeds the integer range
     */
    public void apply(int week) {
        if (week < 1 || week > GameParameters.WEEKS_PER_GAME) {
            throw new IllegalArgumentException("Week must be within the game.");
        }
        if (week != 4 && week != 8) {
            return;
        }

        Money goalReturn = roundedReturn(goal.balance());
        Money cushionReturn = roundedReturn(cushion.balance());
        int credited = Math.addExact(goalReturn.pesos(), cushionReturn.pesos());
        Money nextTotal = new Money(Math.addExact(total.pesos(), credited));
        Math.addExact(goal.balance().pesos(), goalReturn.pesos());
        Math.addExact(cushion.balance().pesos(), cushionReturn.pesos());

        goal.deposit(goalReturn);
        cushion.deposit(cushionReturn);
        total = nextTotal;
    }

    /**
     * Returns the accumulated amount credited across the two jars.
     *
     * @return all returns credited so far, in whole pesos
     */
    public Money total() {
        return total;
    }

    /**
     * Calculates the return in whole increments, rounding a half increment upward.
     *
     * @param balance the jar balance immediately before crediting
     * @return the rounded return for this jar
     */
    private static Money roundedReturn(Money balance) {
        long increments = (balance.pesos() + BALANCE_PER_INCREMENT / 2L) / BALANCE_PER_INCREMENT;
        return new Money(Math.toIntExact(increments * RETURN_INCREMENT));
    }
}
