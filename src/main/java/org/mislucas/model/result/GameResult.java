package org.mislucas.model.result;

import java.math.BigDecimal;
import org.mislucas.model.game.Money;

/**
 * The final balances and outcome after the last return and debt settlement.
 *
 * @param spending the final Spending balance
 * @param goal the final Goal balance
 * @param cushion the final Cushion balance
 * @param remainingDebtPesos remaining debt shown as a separate nonpositive amount
 * @param netBalancePesos the sum of the three balances less the remaining debt
 * @param goalMet whether Goal covers the selected savings target
 * @param victory whether the player met the goal with a nonnegative net balance
 * @param stars the result rating, from zero to three
 * @param savingsPercentage the percentage of all received income kept in Goal and Cushion
 */
public record GameResult(Money spending, Money goal, Money cushion, int remainingDebtPesos,
        long netBalancePesos, boolean goalMet, boolean victory, int stars, BigDecimal savingsPercentage) {
}
