package org.mislucas.model.rules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.mislucas.model.config.GameParameters;
import org.mislucas.model.game.Money;

/**
 * Tracks a game's debts and collects installments from its jars.
 */
public final class CreditAccount {
    /**
     * The rule used to debit the game's jars in priority order.
     */
    private final ExpensePayment expensePayment;

    /**
     * All debts created during this game, in creation order.
     */
    private final List<Debt> debts = new ArrayList<>();

    /**
     * Creates a credit account for one game's jars.
     *
     * @param expensePayment the rule bound to those jars
     * @throws NullPointerException if the payment rule is null
     */
    public CreditAccount(ExpensePayment expensePayment) {
        this.expensePayment = Objects.requireNonNull(expensePayment, "expensePayment");
    }

    /**
     * Finances a mandatory shortfall and schedules its repayment.
     *
     * @param week the week when the expense occurred
     * @param shortfall the positive amount still owed
     * @param plan the chosen payment plan
     * @return the new debt with its principal, interest, and schedule
     * @throws IllegalArgumentException if the week is outside the game or the shortfall is zero
     * @throws NullPointerException if the shortfall or plan is null
     * @throws ArithmeticException if the resulting outstanding balance exceeds the integer range
     */
    public Debt finance(int week, Money shortfall, CreditPlan plan) {
        requireGameWeek(week);
        if (shortfall.pesos() == 0) {
            throw new IllegalArgumentException("The shortfall must be positive.");
        }
        Debt debt = new Debt(week, shortfall, Objects.requireNonNull(plan, "plan"));
        Math.addExact(outstandingBalance().pesos(), debt.outstandingBalance().pesos());
        debts.add(debt);
        return debt;
    }

    /**
     * Checks whether a payment is due or overdue in the given week.
     *
     * @param week the current game week
     * @return true if an unpaid installment is due
     * @throws IllegalArgumentException if the week is outside the game
     */
    public boolean hasDueInstallments(int week) {
        requireGameWeek(week);
        return debts.stream()
                .flatMap(debt -> debt.installments().stream())
                .anyMatch(installment -> isDue(installment, week));
    }

    /**
     * Collects due installments, earliest due date first. Installments with
     * the same due date are collected in debt creation order. Any unpaid
     * amount remains due for the next weekly collection without extra interest.
     *
     * @param week the current game week
     * @return the total amount drawn from Goal for these installments
     * @throws IllegalArgumentException if the week is outside the game
     */
    public Money collectDueInstallments(int week) {
        requireGameWeek(week);
        List<Installment> due = debts.stream()
                .flatMap(debt -> debt.installments().stream())
                .filter(installment -> isDue(installment, week))
                .sorted(Comparator.comparingInt(Installment::dueWeek))
                .toList();

        int paidFromGoal = 0;
        for (Installment installment : due) {
            MandatoryPayment payment = expensePayment.payInstallment(installment.remaining());
            installment.retainUnpaid(payment.shortfall());
            paidFromGoal += payment.paidFromGoal().pesos();
            if (payment.shortfall().pesos() > 0) {
                break;
            }
        }
        return new Money(paidFromGoal);
    }

    /**
     * Returns the balance of all outstanding debt, including installments due
     * after the last game week.
     *
     * @return the total outstanding balance
     */
    public Money outstandingBalance() {
        return new Money(debts.stream().mapToInt(debt -> debt.outstandingBalance().pesos()).sum());
    }

    /**
     * Identifies unfinished installments due by the current week.
     *
     * @param installment a scheduled installment
     * @param week the current game week
     * @return true when the installment is due and has an outstanding balance
     */
    private static boolean isDue(Installment installment, int week) {
        return installment.dueWeek() <= week && installment.remaining().pesos() > 0;
    }

    /**
     * Validates the week of a credit action.
     *
     * @param week the week to validate
     * @throws IllegalArgumentException if the week is outside the game
     */
    private static void requireGameWeek(int week) {
        if (week < 1 || week > GameParameters.WEEKS_PER_GAME) {
            throw new IllegalArgumentException("Week must be within the game.");
        }
    }
}
