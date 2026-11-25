package org.mislucas.model.game;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import org.mislucas.model.config.GameParameters;
import org.mislucas.model.config.Goal;
import org.mislucas.model.config.Scenario;
import org.mislucas.model.events.Card;
import org.mislucas.model.events.CardKind;
import org.mislucas.model.events.CardType;
import org.mislucas.model.events.NormalDeck;
import org.mislucas.model.rules.CreditAccount;
import org.mislucas.model.rules.CreditPlan;
import org.mislucas.model.rules.Debt;
import org.mislucas.model.rules.ExpensePayment;
import org.mislucas.model.rules.IncomeAllocation;
import org.mislucas.model.rules.MandatoryPayment;
import org.mislucas.model.rules.ReturnCredit;

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
     * The rule that credits and records returns for this game's savings jars.
     */
    private final ReturnCredit returnCredit = new ReturnCredit(goalJar, cushion);

    /**
     * The unpaid portion of the current mandatory expense awaiting financing.
     */
    private Money pendingShortfall = new Money(0);

    /**
     * The phase to resume after the pending expense is financed.
     */
    private GamePhase phaseAfterFinancing;

    /**
     * The remaining weekly cards, in reveal order.
     */
    private final Deque<Card> deck;

    /**
     * The weekly card while it still requires a decision or payment.
     */
    private Card revealedCard;

    /**
     * The current week, starting at one and remaining at eight after closing.
     */
    private int week = 1;

    /**
     * The current stage of the turn.
     */
    private GamePhase phase = GamePhase.AWAITING_ALLOCATION;

    /**
     * Starts a new game with empty jars and a shuffled Normal deck.
     *
     * @param scenario the selected resource scenario
     * @param goal the selected savings goal
     * @throws NullPointerException if the scenario or goal is null
     */
    public Game(Scenario scenario, Goal goal) {
        this(scenario, goal, new ArrayDeque<>(NormalDeck.shuffledCardsFor(scenario)));
    }

    /**
     * Starts a new game with a reproducible Normal deck order.
     *
     * @param scenario the selected resource scenario
     * @param goal the selected savings goal
     * @param cardOrder exactly one of each Normal card kind in reveal order
     * @throws NullPointerException if the scenario, goal, order, or a kind is null
     * @throws IllegalArgumentException if the order is not a permutation of the Normal cards
     */
    public Game(Scenario scenario, Goal goal, List<CardKind> cardOrder) {
        this(scenario, goal, new ArrayDeque<>(NormalDeck.cardsFor(scenario, cardOrder)));
    }

    /**
     * Creates a game that owns the supplied queue of Normal cards.
     *
     * @param scenario the selected resource scenario
     * @param goal the selected savings goal
     * @param deck the cards in reveal order
     */
    private Game(Scenario scenario, Goal goal, Deque<Card> deck) {
        this.scenario = Objects.requireNonNull(scenario, "scenario");
        this.goal = Objects.requireNonNull(goal, "goal");
        this.deck = deck;
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
     * Returns the accumulated credits from both savings jars.
     *
     * @return the total return credited in whole pesos
     */
    public Money totalReturns() {
        return returnCredit.total();
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
        advanceAfterMandatoryPayment(payment, GamePhase.AWAITING_CARD);
        return payment;
    }

    /**
     * Accepts or declines a want and completes the card's resolution.
     *
     * @param accept whether the player wants to buy the item
     * @return true if the purchase was accepted and paid, otherwise false
     * @throws IllegalStateException if the game is not awaiting a want decision
     */
    public boolean decideWant(boolean accept) {
        requirePhase(GamePhase.AWAITING_WANT_DECISION);
        boolean purchased = accept && expensePayment.tryPayWant(new Money(revealedCard.getAmount()));
        revealedCard = null;
        phase = GamePhase.AWAITING_WEEK_END;
        return purchased;
    }

    /**
     * Pays a need or emergency and requires financing for any unpaid amount.
     *
     * @return the remaining shortfall and the amount paid from Goal
     * @throws IllegalStateException if the game is not awaiting a mandatory card payment
     */
    public MandatoryPayment payMandatoryCard() {
        requirePhase(GamePhase.AWAITING_MANDATORY_PAYMENT);
        Money amount = new Money(revealedCard.getAmount());
        MandatoryPayment payment = revealedCard.getType() == CardType.NEED
                ? expensePayment.payNeed(amount)
                : expensePayment.payEmergency(amount);
        revealedCard = null;
        advanceAfterMandatoryPayment(payment, GamePhase.AWAITING_WEEK_END);
        return payment;
    }

    /**
     * Allocates the full opportunity income and completes the card's resolution.
     * A rejected allocation preserves the balances and phase.
     *
     * @param spendingAmount the amount assigned to Spending
     * @param goalAmount the amount assigned to Goal
     * @param cushionAmount the amount assigned to Cushion
     * @throws IllegalStateException if the game is not awaiting opportunity allocation
     * @throws IllegalArgumentException if the amounts violate the allocation rules
     * @throws NullPointerException if an amount is null
     */
    public void allocateOpportunity(Money spendingAmount, Money goalAmount, Money cushionAmount) {
        requirePhase(GamePhase.AWAITING_OPPORTUNITY_ALLOCATION);
        incomeAllocation.apply(new Money(revealedCard.getAmount()), spendingAmount, goalAmount, cushionAmount);
        revealedCard = null;
        phase = GamePhase.AWAITING_WEEK_END;
    }

    /**
     * Finances a mandatory shortfall and resumes the turn after that payment.
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
        phase = phaseAfterFinancing;
        phaseAfterFinancing = null;
        return debt;
    }

    /**
     * Consumes the next weekly card and enables the action required by its type.
     *
     * @return the weekly card data
     * @throws IllegalStateException if the game is not awaiting a card
     */
    public Card revealCard() {
        requirePhase(GamePhase.AWAITING_CARD);
        Card card = deck.removeFirst();
        phase = switch (card.getType()) {
            case WANT -> GamePhase.AWAITING_WANT_DECISION;
            case NEED, EMERGENCY -> GamePhase.AWAITING_MANDATORY_PAYMENT;
            case OPPORTUNITY -> GamePhase.AWAITING_OPPORTUNITY_ALLOCATION;
            case QUIET_WEEK -> GamePhase.AWAITING_WEEK_END;
        };
        if (phase != GamePhase.AWAITING_WEEK_END) {
            revealedCard = card;
        }
        return card;
    }

    /**
     * Credits scheduled returns and closes the current week.
     * The last week completes the game.
     *
     * @throws IllegalStateException if the game is not ready to close the week
     * @throws ArithmeticException if a credited balance or the total return exceeds the integer range
     */
    public void finishWeek() {
        requirePhase(GamePhase.AWAITING_WEEK_END);
        returnCredit.apply(week);
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
     * Advances after a mandatory payment or records the unpaid amount for financing.
     *
     * @param payment the result from paying out of the jars
     * @param nextPhase the phase to resume when the full cost is covered
     */
    private void advanceAfterMandatoryPayment(MandatoryPayment payment, GamePhase nextPhase) {
        if (payment.shortfall().pesos() == 0) {
            phase = nextPhase;
        } else {
            pendingShortfall = payment.shortfall();
            phaseAfterFinancing = nextPhase;
            phase = GamePhase.AWAITING_FINANCING;
        }
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
