package org.mislucas.model.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.events.CardKind;
import org.mislucas.model.result.GameResult;
import org.mislucas.model.rules.CreditPlan;

class GameOutcomeIntegrationTest {
    @Test
    void includesTheFinalReturnAndLimitsStarsWhenAnInstallmentUsedGoal() {
        Game game = new Game(Scenario.TIGHT, Goal.HEADPHONES, List.of(
                CardKind.NEED, CardKind.WANT_1, CardKind.WANT_2, CardKind.WANT_3,
                CardKind.OPPORTUNITY, CardKind.EMERGENCY_1, CardKind.EMERGENCY_2, CardKind.QUIET_WEEK));

        assertThrows(IllegalStateException.class, game::result);
        allocate(game, 10_000, 0, 0);
        assertEquals(new Money(0), game.chargeFixedExpense().paidFromGoal());
        game.revealCard();
        assertEquals(new Money(4_000), game.payMandatoryCard().shortfall());
        game.financeShortfall(CreditPlan.ONE_INSTALLMENT);
        game.finishWeek();

        for (int week = 2; week <= 4; week++) {
            allocate(game, 0, 10_000, 0);
            if (week == 2) {
                assertEquals(new Money(4_000), game.collectInstallments());
            }
            game.revealCard();
            game.decideWant(false);
            game.finishWeek();
        }

        allocate(game, 6_000, 4_000, 0);
        assertEquals(new Money(0), game.chargeFixedExpense().paidFromGoal());
        game.revealCard();
        assertThrows(IllegalArgumentException.class, () ->
                game.allocateOpportunity(new Money(0), new Money(0), new Money(8_000)));
        game.allocateOpportunity(new Money(0), new Money(0), new Money(10_000));
        game.finishWeek();

        allocate(game, 8_000, 2_000, 0);
        game.revealCard();
        assertEquals(new Money(0), game.payMandatoryCard().paidFromGoal());
        game.finishWeek();
        allocate(game, 0, 10_000, 0);
        game.revealCard();
        assertEquals(new Money(0), game.payMandatoryCard().paidFromGoal());
        game.finishWeek();

        allocate(game, 0, 0, 10_000);
        game.revealCard();
        game.finishWeek();

        GameResult result = game.result();
        assertTrue(result.victory());
        assertEquals(1, result.stars());
        assertEquals(new Money(43_400), result.goal());
        assertEquals(new Money(10_200), result.cushion());
        assertEquals(0, result.remainingDebtPesos());
        assertEquals(53_600L, result.netBalancePesos());
        assertEquals(new BigDecimal("58.52"), result.savingsPercentage());
        assertThrows(IllegalStateException.class, game::finishWeek);
        assertSame(result, game.result());
    }

    private static void allocate(Game game, int spending, int goal, int cushion) {
        game.allocateAllowance(new Money(spending), new Money(goal), new Money(cushion));
    }
}
