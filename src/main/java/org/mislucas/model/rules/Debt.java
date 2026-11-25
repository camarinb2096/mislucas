package org.mislucas.model.rules;

import java.util.List;
import org.mislucas.model.game.Money;

/**
 * A financed mandatory shortfall and its repayment schedule.
 */
public final class Debt {
    /**
     * The amount financed before interest.
     */
    private final Money principal;

    /**
     * The interest charged once when the debt is created.
     */
    private final Money interest;

    /**
     * The financing option chosen by the player.
     */
    private final CreditPlan plan;

    /**
     * The collection schedule, including installments already paid.
     */
    private final List<Installment> installments;

    /**
     * Calculates interest and schedules each installment.
     *
     * @param week the week in which the shortfall occurred
     * @param principal the financed amount
     * @param plan the chosen financing option
     * @throws ArithmeticException if the total owed exceeds the integer range
     */
    Debt(int week, Money principal, CreditPlan plan) {
        this.principal = principal;
        this.plan = plan;
        if (plan == CreditPlan.ONE_INSTALLMENT) {
            interest = new Money(0);
            installments = List.of(new Installment(week + 1, principal));
        } else {
            // Round 10 percent to the nearest whole peso, rounding a tie upward.
            interest = new Money((int) ((principal.pesos() + 5L) / 10));
            int total = Math.addExact(principal.pesos(), interest.pesos());
            int first = total / 2;
            installments = List.of(
                    new Installment(week + 1, new Money(first)),
                    new Installment(week + 2, new Money(total - first)));
        }
    }

    /**
     * Returns the financed amount.
     *
     * @return the principal in whole pesos
     */
    public Money principal() {
        return principal;
    }

    /**
     * Returns the original financing cost.
     *
     * @return the total interest in whole pesos
     */
    public Money interest() {
        return interest;
    }

    /**
     * Returns the selected financing option.
     *
     * @return the credit plan
     */
    public CreditPlan plan() {
        return plan;
    }

    /**
     * Returns the installments in order of their original due weeks.
     *
     * @return an unmodifiable list of the scheduled installments
     */
    public List<Installment> installments() {
        return installments;
    }

    /**
     * Returns the amount still owed across all installments.
     *
     * @return the outstanding balance in whole pesos
     */
    public Money outstandingBalance() {
        return new Money(installments.stream().mapToInt(installment -> installment.remaining().pesos()).sum());
    }
}
