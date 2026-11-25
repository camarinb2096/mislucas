package org.mislucas.model.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.events.CardKind;
import org.mislucas.model.rules.CreditPlan;

class CardResolutionTest {
    @Test
    void paysAnAcceptedWantFromSpendingOnly() {
        Game game = beginWith(CardKind.WANT_1, Scenario.TIGHT);

        assertEquals(GamePhase.AWAITING_WANT_DECISION, game.phase());
        assertThrows(IllegalStateException.class, game::finishWeek);
        assertFalse(game.decideWant(true));
        assertEquals(new Money(4_000), game.balance(JarType.SPENDING));
        assertEquals(new Money(0), game.outstandingDebt());
        assertThrows(IllegalStateException.class, () -> game.decideWant(true));

        game.finishWeek();
        game.allocateAllowance(new Money(6_000), new Money(3_000), new Money(1_000));
        assertEquals(CardKind.WANT_2, game.revealCard().getKind());
        assertTrue(game.decideWant(true));
        assertEquals(new Money(4_000), game.balance(JarType.SPENDING));
        assertEquals(new Money(3_000), game.balance(JarType.GOAL));
        assertEquals(new Money(1_000), game.balance(JarType.CUSHION));
    }

    @Test
    void declinesAnAffordableWantWithoutChangingTheBalance() {
        Game game = beginWith(CardKind.WANT_1, Scenario.MEDIUM);

        assertFalse(game.decideWant(false));

        assertEquals(new Money(8_000), game.balance(JarType.SPENDING));
        assertEquals(new Money(0), game.outstandingDebt());

        game.finishWeek();
        game.allocateAllowance(new Money(20_000), new Money(0), new Money(0));
        game.revealCard();
        assertFalse(game.decideWant(false));
        assertEquals(GamePhase.AWAITING_WEEK_END, game.phase());
        assertEquals(new Money(28_000), game.balance(JarType.SPENDING));
    }

    @Test
    void financesANeedShortfallAndCollectsTheTwoInstallmentsBeforeTheNextCards() {
        Game game = beginWith(CardKind.NEED, Scenario.TIGHT);

        assertThrows(IllegalStateException.class, game::finishWeek);
        assertEquals(new Money(4_000), game.payMandatoryCard().shortfall());
        assertEquals(GamePhase.AWAITING_FINANCING, game.phase());
        assertThrows(IllegalStateException.class, game::revealCard);
        assertThrows(IllegalStateException.class, game::payMandatoryCard);
        assertThrows(IllegalStateException.class, game::finishWeek);
        assertThrows(NullPointerException.class, () -> game.financeShortfall(null));
        assertEquals(GamePhase.AWAITING_FINANCING, game.phase());

        var debt = game.financeShortfall(CreditPlan.TWO_INSTALLMENTS);

        assertEquals(new Money(400), debt.interest());
        assertEquals(new Money(4_400), game.outstandingDebt());
        assertEquals(GamePhase.AWAITING_WEEK_END, game.phase());
        for (int remaining : new int[] {2_200, 0}) {
            game.finishWeek();
            game.allocateAllowance(new Money(10_000), new Money(0), new Money(0));
            assertEquals(GamePhase.AWAITING_INSTALLMENTS, game.phase());
            assertThrows(IllegalStateException.class, game::revealCard);
            game.collectInstallments();
            assertEquals(new Money(remaining), game.outstandingDebt());
            game.revealCard();
            game.decideWant(false);
        }
    }

    @Test
    void paysAnEmergencyFromCushionBeforeSpending() {
        Game game = new Game(Scenario.TIGHT, Goal.HEADPHONES, orderStartingWith(
                CardKind.QUIET_WEEK, CardKind.EMERGENCY_1));
        game.allocateAllowance(new Money(10_000), new Money(0), new Money(0));
        game.chargeFixedExpense();
        game.revealCard();
        game.finishWeek();
        game.allocateAllowance(new Money(4_000), new Money(3_000), new Money(3_000));
        game.revealCard();

        assertEquals(new Money(0), game.payMandatoryCard().shortfall());
        assertEquals(new Money(3_000), game.balance(JarType.SPENDING));
        assertEquals(new Money(3_000), game.balance(JarType.GOAL));
        assertEquals(new Money(0), game.balance(JarType.CUSHION));
        assertEquals(GamePhase.AWAITING_WEEK_END, game.phase());
    }

    @Test
    void requiresAnImmediateOpportunityAllocationAndAllowsRetryAfterRejectingIt() {
        Game game = beginWith(CardKind.OPPORTUNITY, Scenario.MEDIUM);

        assertEquals(GamePhase.AWAITING_OPPORTUNITY_ALLOCATION, game.phase());
        assertThrows(IllegalStateException.class, game::finishWeek);
        assertThrows(IllegalStateException.class, game::payMandatoryCard);
        assertThrows(IllegalArgumentException.class, () ->
                game.allocateOpportunity(new Money(5_000), new Money(5_000), new Money(5_000)));
        assertEquals(new Money(8_000), game.balance(JarType.SPENDING));
        assertEquals(new Money(0), game.balance(JarType.GOAL));
        assertEquals(new Money(0), game.balance(JarType.CUSHION));
        assertEquals(GamePhase.AWAITING_OPPORTUNITY_ALLOCATION, game.phase());

        game.allocateOpportunity(new Money(5_000), new Money(10_000), new Money(5_000));

        assertEquals(new Money(13_000), game.balance(JarType.SPENDING));
        assertEquals(new Money(10_000), game.balance(JarType.GOAL));
        assertEquals(new Money(5_000), game.balance(JarType.CUSHION));
        assertEquals(GamePhase.AWAITING_WEEK_END, game.phase());
        assertThrows(IllegalStateException.class, () ->
                game.allocateOpportunity(new Money(5_000), new Money(10_000), new Money(5_000)));
    }

    @Test
    void resolvesAQuietWeekWithoutChangingBalances() {
        Game game = beginWith(CardKind.QUIET_WEEK, Scenario.TIGHT);

        assertEquals(GamePhase.AWAITING_WEEK_END, game.phase());
        assertEquals(new Money(4_000), game.balance(JarType.SPENDING));
        assertEquals(new Money(0), game.balance(JarType.GOAL));
        assertEquals(new Money(0), game.balance(JarType.CUSHION));
        assertEquals(new Money(0), game.outstandingDebt());
    }

    @Test
    void requiresACompleteUniqueDeckAndCopiesTheSuppliedOrder() {
        var order = orderStartingWith(CardKind.QUIET_WEEK);
        var invalidOrder = new ArrayList<>(order);
        invalidOrder.set(1, CardKind.QUIET_WEEK);
        assertThrows(IllegalArgumentException.class, () -> new Game(Scenario.TIGHT, Goal.HEADPHONES, invalidOrder));
        assertThrows(IllegalArgumentException.class, () -> new Game(Scenario.TIGHT, Goal.HEADPHONES, List.of()));

        Game game = new Game(Scenario.TIGHT, Goal.HEADPHONES, order);
        order.clear();
        game.allocateAllowance(new Money(10_000), new Money(0), new Money(0));
        game.chargeFixedExpense();
        assertEquals(CardKind.QUIET_WEEK, game.revealCard().getKind());
    }

    private static Game beginWith(CardKind first, Scenario scenario) {
        Game game = new Game(scenario, Goal.HEADPHONES, orderStartingWith(first));
        game.allocateAllowance(new Money(scenario.getWeeklyAllowance()), new Money(0), new Money(0));
        game.chargeFixedExpense();
        assertEquals(first, game.revealCard().getKind());
        return game;
    }

    private static List<CardKind> orderStartingWith(CardKind... first) {
        List<CardKind> prefix = List.of(first);
        List<CardKind> result = new ArrayList<>(prefix);
        for (CardKind kind : CardKind.values()) {
            if (!prefix.contains(kind)) {
                result.add(kind);
            }
        }
        return result;
    }
}
