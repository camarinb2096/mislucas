package org.mislucas.model.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.events.CardKind;

class GameTest {
    @Test
    void startsInWeekOneWithTheSelectedSetupAndEmptyJars() {
        Game game = new Game(Scenario.MEDIUM, Goal.CLASS_TRIP);

        assertEquals(Scenario.MEDIUM, game.scenario());
        assertEquals(Goal.CLASS_TRIP, game.goal());
        assertEquals(1, game.week());
        assertEquals(GamePhase.AWAITING_ALLOCATION, game.phase());
        assertBalances(game, 0, 0, 0);
    }

    @Test
    void rejectsRevealAndWeekEndBeforeAllocation() {
        Game game = new Game(Scenario.TIGHT, Goal.HEADPHONES);

        assertThrows(IllegalStateException.class, game::revealCard);
        assertThrows(IllegalStateException.class, game::finishWeek);

        assertEquals(1, game.week());
        assertEquals(GamePhase.AWAITING_ALLOCATION, game.phase());
        assertBalances(game, 0, 0, 0);
    }

    @Test
    void usesTheScenarioAllowanceAndAcceptsARetryInTheSameWeek() {
        Game game = new Game(Scenario.MEDIUM, Goal.SNEAKERS);

        assertThrows(IllegalArgumentException.class, () ->
                game.allocateAllowance(new Money(5_000), new Money(3_000), new Money(2_000)));
        assertEquals(1, game.week());
        assertEquals(GamePhase.AWAITING_ALLOCATION, game.phase());
        assertBalances(game, 0, 0, 0);

        game.allocateAllowance(new Money(8_000), new Money(7_000), new Money(5_000));

        assertEquals(GamePhase.AWAITING_CARD, game.phase());
        assertBalances(game, 8_000, 7_000, 5_000);
        assertEquals(10_000, game.revealCard().getAmount());
    }

    @Test
    void rejectsASecondAllocationAndRequiresACardBeforeClosingTheWeek() {
        Game game = new Game(Scenario.TIGHT, Goal.SNEAKERS);
        allocateTightAllowance(game);

        assertThrows(IllegalStateException.class, () -> allocateTightAllowance(game));
        assertThrows(IllegalStateException.class, game::finishWeek);

        assertEquals(1, game.week());
        assertEquals(GamePhase.AWAITING_CARD, game.phase());
        assertBalances(game, 5_000, 3_000, 2_000);
        assertEquals(CardKind.WANT_1, game.revealCard().getKind());
    }

    @Test
    void rejectsASecondRevealWithoutConsumingTheNextCard() {
        Game game = new Game(Scenario.TIGHT, Goal.HEADPHONES);
        allocateTightAllowance(game);
        game.revealCard();

        assertThrows(IllegalStateException.class, game::revealCard);

        assertEquals(1, game.week());
        assertEquals(GamePhase.AWAITING_WEEK_END, game.phase());
        assertBalances(game, 5_000, 3_000, 2_000);
        game.finishWeek();
        allocateTightAllowance(game);
        assertEquals(CardKind.WANT_2, game.revealCard().getKind());
    }

    @Test
    void completesExactlyEightWeeksInControlledCardOrder() {
        Game game = new Game(Scenario.TIGHT, Goal.HEADPHONES);
        List<CardKind> expectedCards = List.of(
                CardKind.WANT_1, CardKind.WANT_2, CardKind.WANT_3, CardKind.NEED,
                CardKind.EMERGENCY_1, CardKind.EMERGENCY_2, CardKind.OPPORTUNITY, CardKind.QUIET_WEEK);

        for (int week = 1; week <= expectedCards.size(); week++) {
            assertEquals(week, game.week());
            assertEquals(GamePhase.AWAITING_ALLOCATION, game.phase());
            allocateTightAllowance(game);
            assertEquals(expectedCards.get(week - 1), game.revealCard().getKind());
            assertEquals(GamePhase.AWAITING_WEEK_END, game.phase());
            game.finishWeek();
        }

        assertEquals(8, game.week());
        assertEquals(GamePhase.FINISHED, game.phase());
        assertBalances(game, 40_000, 24_000, 16_000);

        assertThrows(IllegalStateException.class, () -> allocateTightAllowance(game));
        assertThrows(IllegalStateException.class, game::revealCard);
        assertThrows(IllegalStateException.class, game::finishWeek);
        assertEquals(8, game.week());
        assertEquals(GamePhase.FINISHED, game.phase());
        assertBalances(game, 40_000, 24_000, 16_000);
    }

    private static void allocateTightAllowance(Game game) {
        game.allocateAllowance(new Money(5_000), new Money(3_000), new Money(2_000));
    }

    private static void assertBalances(Game game, int spending, int goal, int cushion) {
        assertEquals(new Money(spending), game.balance(JarType.SPENDING));
        assertEquals(new Money(goal), game.balance(JarType.GOAL));
        assertEquals(new Money(cushion), game.balance(JarType.CUSHION));
    }
}
