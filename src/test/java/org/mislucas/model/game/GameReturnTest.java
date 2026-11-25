package org.mislucas.model.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.events.CardKind;

class GameReturnTest {
    @Test
    void creditsReturnsAfterTheOpportunityAndBeforeTheFinalWeekCloses() {
        Game game = new Game(Scenario.TIGHT, Goal.HEADPHONES, List.of(
                CardKind.WANT_1, CardKind.WANT_2, CardKind.WANT_3, CardKind.OPPORTUNITY,
                CardKind.NEED, CardKind.EMERGENCY_1, CardKind.EMERGENCY_2, CardKind.QUIET_WEEK));

        List<Integer> expectedReturns = List.of(0, 0, 0, 500, 500, 500, 500, 800);

        for (int week = 1; week <= expectedReturns.size(); week++) {
            game.allocateAllowance(new Money(7_000), new Money(2_000), new Money(1_000));
            if (week == 1 || week == 5) {
                game.chargeFixedExpense();
            }

            switch (game.revealCard().getType()) {
                case WANT -> game.decideWant(false);
                case NEED, EMERGENCY -> game.payMandatoryCard();
                case OPPORTUNITY -> game.allocateOpportunity(new Money(0), new Money(0), new Money(10_000));
                case QUIET_WEEK -> {
                    assertEquals(new Money(16_200), game.balance(JarType.GOAL));
                    assertEquals(new Money(500), game.totalReturns());
                }
            }

            game.finishWeek();

            if (week == 4) {
                assertEquals(new Money(8_200), game.balance(JarType.GOAL));
                assertEquals(new Money(14_300), game.balance(JarType.CUSHION));
            }
            assertEquals(new Money(expectedReturns.get(week - 1)), game.totalReturns());
        }

        assertEquals(GamePhase.FINISHED, game.phase());
        assertEquals(new Money(35_300), game.balance(JarType.SPENDING));
        assertEquals(new Money(16_500), game.balance(JarType.GOAL));
        assertEquals(new Money(1_000), game.balance(JarType.CUSHION));
    }
}
