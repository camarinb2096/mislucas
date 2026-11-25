package org.mislucas.model.game;

import java.util.List;
import java.util.Objects;
import org.mislucas.model.config.GameParameters;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.events.Card;
import org.mislucas.model.events.NormalDeck;
import org.mislucas.model.rules.CreditAccount;
import org.mislucas.model.rules.CreditPlan;
import org.mislucas.model.rules.Debt;
import org.mislucas.model.rules.ExpensePayment;
import org.mislucas.model.rules.IncomeAllocation;
import org.mislucas.model.rules.MandatoryPayment;

/**
 * Owns the state of an eight-week game and controls the order of turn actions.
 */
public final class Game {
    /**
     * The selected scenario and savings goal.
     */
    private final Scenario scenario;
    private final Goal goal;

    /**
     * The three balances owned by this game.
     */
    private final Jar spending = new Jar(JarType.SPENDING);
    private final Jar goalJar = new Jar(JarType.GOAL);
    private final Jar cushion = new Jar(JarType.CUSHION);

    /**
     * The rule that credits the weekly allowance to these jars.
     */
    private final IncomeAllocation incomeAllocation = new IncomeAllocation(spending, goalJar, cushion);

    /**
     * The rule that pays expenses from these jars.
     */
    private final ExpensePayment expensePayment = new ExpensePayment(spending, goalJar, cushion);

    /**
     * The debts and installment schedule associated with this game's jars.
     */
    private final CreditAccount creditAccount = new CreditAccount(expensePayment);

    /**
     * The unpaid portion of the current mandatory expense awaiting financing.
     */
    private Money pendingShortfall = new Money(0);

    /**
     * The weekly cards, in reveal order.
     */
    private final List<Card> deck;

    /**
     * The current week, starting at one and remaining at eight after closing.
     */
    private int week = 1;

    /**
     * The current stage of the turn.
     */
    private GamePhase phase = GamePhase.AWAITING_ALLOCATION;

    /**
     * Starts a new game with empty jars and the Normal cards in table order.
     *
     * @param scenario the selected resource scenario
     * @param goal the selected savings goal
     * @throws NullPointerException if the scenario or goal is null
     */
    public Game(Scenario scenario, Goal goal) {
        this.scenario = Objects.requireNonNull(scenario, "scenario");
        this.goal = Objects.requireNonNull(goal, "goal");
        deck = NormalDeck.cardsFor(scenario);
    }

    /**
     * Returns the scenario chosen at setup.
     *
     * @return the selected resource scenario
     */
    public Scenario scenario() {
        return scenario;
    }

    /**
     * Returns the goal chosen at setup.
     *
     * @return the selected savings goal
     */
    public Goal goal() {
        return goal;
    }

    /**
     * Returns the current week, or the last week after the game is finished.
     *
     * @return the week number from one to eight
     */
    public int week() {
        return week;
    }

    /**
     * Returns the current turn phase.
     *
     * @return the state that determines which action is allowed next
     */
    public GamePhase phase() {
        return phase;
    }

    /**
     * Returns the current balance of a jar as an immutable amount.
     *
     * @param type the jar to query
     * @return the balance in whole Colombian pesos
     * @throws NullPointerException if the jar type is null
     */
    public Money balance(JarType type) {
        return switch (type) {
            case SPENDING -> spending.balance();
            case GOAL -> goalJar.balance();
            case CUSHION -> cushion.balance();
        };
    }

    /**
     * Returns financed debt still outstanding, even after the final week.
     *
     * @return the total remaining debt in whole pesos
     */
    public Money outstandingDebt() {
        return creditAccount.outstandingBalance();
    }

    /**
     * Allocates the full weekly allowance and enables the next scheduled action.
     * A rejected allocation preserves the balances and phase.
     *
     * @param spendingAmount the amount assigned to Spending
     * @param goalAmount the amount assigned to Goal
     * @param cushionAmount the amount assigned to Cushion
     * @throws IllegalStateException if the game is not awaiting allocation
     * @throws IllegalArgumentException if the amounts violate the allocation rules
     * @throws NullPointerException if an amount is null
     */
    public void allocateAllowance(Money spendingAmount, Money goalAmount, Money cushionAmount) {
        requirePhase(GamePhase.AWAITING_ALLOCATION);
        incomeAllocation.apply(new Money(scenario.getWeeklyAllowance()), spendingAmount, goalAmount, cushionAmount);
        phase = creditAccount.hasDueInstallments(week) ? GamePhase.AWAITING_INSTALLMENTS : phaseAfterInstallments();
    }

    /**
     * Collects due installments before the fixed expense or card reveal.
     *
     * @return the amount drawn from Goal during collection
     * @throws IllegalStateException if the game is not awaiting collection
     */
    public Money collectInstallments() {
        requirePhase(GamePhase.AWAITING_INSTALLMENTS);
        Money paidFromGoal = creditAccount.collectDueInstallments(week);
        phase = phaseAfterInstallments();
        return paidFromGoal;
    }

    /**
     * Charges the scenario's fixed expense once and requires financing if funds fall short.
     *
     * @return the remaining shortfall and the amount paid from Goal
     * @throws IllegalStateException if the game is not awaiting the fixed expense
     */
    public MandatoryPayment chargeFixedExpense() {
        requirePhase(GamePhase.AWAITING_FIXED_EXPENSE);
        MandatoryPayment payment = expensePayment.chargeFixedExpense(week, new Money(scenario.getFixedExpense()));
        pendingShortfall = payment.shortfall();
        phase = pendingShortfall.pesos() > 0 ? GamePhase.AWAITING_FINANCING : GamePhase.AWAITING_CARD;
        return payment;
    }

    /**
     * Finances the pending fixed-expense shortfall and enables card reveal.
     *
     * @param plan the chosen repayment option
     * @return the principal, interest, and installments for the financed expense
     * @throws IllegalStateException if the game is not awaiting financing
     * @throws NullPointerException if the plan is null
     * @throws ArithmeticException if the resulting debt exceeds the integer range
     */
    public Debt financeShortfall(CreditPlan plan) {
        requirePhase(GamePhase.AWAITING_FINANCING);
        Debt debt = creditAccount.finance(week, pendingShortfall, plan);
        pendingShortfall = new Money(0);
        phase = GamePhase.AWAITING_CARD;
        return debt;
    }

    /**
     * Reveals the card for the current week and enables the turn to close.
     *
     * @return the weekly card data
     * @throws IllegalStateException if the game is not awaiting a card
     */
    public Card revealCard() {
        requirePhase(GamePhase.AWAITING_CARD);
        Card card = deck.get(week - 1);
        phase = GamePhase.AWAITING_WEEK_END;
        return card;
    }

    /**
     * Closes the current week. The last week completes the game.
     *
     * @throws IllegalStateException if the game is not ready to close the week
     */
    public void finishWeek() {
        requirePhase(GamePhase.AWAITING_WEEK_END);
        if (week == GameParameters.WEEKS_PER_GAME) {
            phase = GamePhase.FINISHED;
        } else {
            week++;
            phase = GamePhase.AWAITING_ALLOCATION;
        }
    }

    /**
     * Determines the next action after any due installments have been collected.
     *
     * @return the fixed-expense or card phase for the current week
     */
    private GamePhase phaseAfterInstallments() {
        return ExpensePayment.isFixedExpenseDue(week) ? GamePhase.AWAITING_FIXED_EXPENSE : GamePhase.AWAITING_CARD;
    }

    /**
     * Checks that an action is allowed in the current phase.
     *
     * @param expected the phase required by the action
     * @throws IllegalStateException if the phase does not match
     */
    private void requirePhase(GamePhase expected) {
        if (phase != expected) {
            throw new IllegalStateException("Expected phase " + expected + " but was " + phase + ".");
        }
    }
}
