package org.mislucas.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.game.Money;
import org.mislucas.model.result.GameResult;

class GameOutcomeTest {
    @Test
    void keepsDebtSeparateAndLosesWhenNetBalanceIsNegative() {
        GameResult result = GameOutcome.calculate(Scenario.TIGHT, Goal.HEADPHONES, new Money(0),
                new Money(0), new Money(0), new Money(2_000), 90_000, true);

        assertEquals(-2_000, result.remainingDebtPesos());
        assertEquals(-2_000L, result.netBalancePesos());
        assertEquals(new Money(0), result.goal());
        assertFalse(result.victory());
        assertEquals(0, result.stars());
    }

    @Test
    void losesWhenGoalIsBelowTheTargetDespitePositiveNetBalance() {
        GameResult result = GameOutcome.calculate(Scenario.TIGHT, Goal.HEADPHONES, new Money(8_000),
                new Money(39_999), new Money(10_000), new Money(0), 90_000, false);

        assertTrue(result.netBalancePesos() > 0);
        assertFalse(result.goalMet());
        assertFalse(result.victory());
        assertEquals(0, result.stars());
    }

    @ParameterizedTest
    @CsvSource({"9999, false, 2", "10000, false, 3", "10000, true, 1"})
    void awardsStarsForAnExactGoalUsingTheCushionThresholdAndPaymentHistory(
            int cushion, boolean usedGoal, int expectedStars) {
        GameResult result = GameOutcome.calculate(Scenario.TIGHT, Goal.HEADPHONES, new Money(0),
                new Money(40_000), new Money(cushion), new Money(0), 90_000, usedGoal);

        assertTrue(result.goalMet());
        assertTrue(result.victory());
        assertEquals(expectedStars, result.stars());
    }

    @Test
    void calculatesSavedPercentageUsingAllIncomeAndKeepsSpendingOutOfTheNumerator() {
        GameResult result = GameOutcome.calculate(Scenario.TIGHT, Goal.HEADPHONES, new Money(20_000),
                new Money(40_000), new Money(10_000), new Money(0), 90_400, false);

        assertEquals(new BigDecimal("55.31"), result.savingsPercentage());
    }
}
