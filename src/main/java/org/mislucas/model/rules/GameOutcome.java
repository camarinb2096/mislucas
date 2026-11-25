package org.mislucas.model.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.game.Money;
import org.mislucas.model.result.GameResult;

/**
 * Calculates the final result from balances after debt settlement.
 */
public final class GameOutcome {
    private GameOutcome() {
    }

    /**
     * Evaluates victory, stars, and the portion of all received income saved.
     *
     * @param scenario the selected resource scenario
     * @param target the chosen savings goal
     * @param spending the final Spending balance
     * @param goal the final Goal balance
     * @param cushion the final Cushion balance
     * @param remainingDebt the outstanding debt after settlement
     * @param receivedPesos total allowances, opportunity income, and returns in whole pesos
     * @param usedGoal whether Goal was used for mandatory expenses, installments, or settlement
     * @return the final result with the percentage rounded to two decimal places
     */
    public static GameResult calculate(Scenario scenario, Goal target, Money spending, Money goal, Money cushion,
            Money remainingDebt, long receivedPesos, boolean usedGoal) {
        long savingsPesos = (long) goal.pesos() + cushion.pesos();
        long netBalance = spending.pesos() + savingsPesos - remainingDebt.pesos();
        boolean goalMet = goal.pesos() >= target.getPrice(scenario);
        boolean victory = goalMet && netBalance >= 0;
        int stars = stars(scenario, cushion, usedGoal, victory);
        BigDecimal percentage = receivedPesos == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(savingsPesos).movePointRight(2)
                        .divide(BigDecimal.valueOf(receivedPesos), 2, RoundingMode.HALF_UP);

        return new GameResult(spending, goal, cushion, -remainingDebt.pesos(), netBalance,
                goalMet, victory, stars, percentage);
    }

    /**
     * Awards stars using the winning condition and the two savings criteria.
     *
     * @param scenario the scenario defining the weekly allowance
     * @param cushion the final Cushion balance
     * @param usedGoal whether Goal was used to pay
     * @param victory whether the player won
     * @return the number of stars earned
     */
    private static int stars(Scenario scenario, Money cushion, boolean usedGoal, boolean victory) {
        if (!victory) {
            return 0;
        }
        if (usedGoal) {
            return 1;
        }
        return cushion.pesos() >= scenario.getWeeklyAllowance() ? 3 : 2;
    }
}
