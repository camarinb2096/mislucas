package org.mislucas.model.rules;

import org.mislucas.model.game.Money;

/**
 * A scheduled payment and its remaining balance in whole Colombian pesos.
 */
public final class Installment {
    /**
     * The original due week, retained to prioritize overdue installments.
     */
    private final int dueWeek;

    /**
     * The original amount of this installment.
     */
    private final Money amount;

    /**
     * The amount still due after any collections.
     */
    private Money remaining;

    /**
     * Schedules an installment with the full amount outstanding.
     *
     * @param dueWeek the original due week, which may follow the last game week
     * @param amount the amount scheduled
     */
    Installment(int dueWeek, Money amount) {
        this.dueWeek = dueWeek;
        this.amount = amount;
        remaining = amount;
    }

    /**
     * Returns the week this installment was originally due.
     *
     * @return the original due week
     */
    public int dueWeek() {
        return dueWeek;
    }

    /**
     * Returns the original amount.
     *
     * @return the scheduled amount
     */
    public Money amount() {
        return amount;
    }

    /**
     * Returns the amount still owed.
     *
     * @return the remaining balance
     */
    public Money remaining() {
        return remaining;
    }

    /**
     * Applies the unpaid amount reported by a collection.
     *
     * @param unpaid the portion still owed after collecting from the jars
     */
    void retainUnpaid(Money unpaid) {
        remaining = unpaid;
    }
}
