package org.mislucas.model.rules;

import java.util.Objects;
import org.mislucas.model.game.Money;

/**
 * Reports the unpaid portion of an expense and the savings used to cover it.
 *
 * @param shortfall the unpaid amount
 * @param paidFromGoal the amount withdrawn from Goal for this expense
 */
public record MandatoryPayment(Money shortfall, Money paidFromGoal) {
    /**
     * Creates a payment result in whole Colombian pesos.
     *
     * @param shortfall the unpaid amount
     * @param paidFromGoal the amount withdrawn from Goal
     * @throws NullPointerException if either amount is null
     */
    public MandatoryPayment {
        Objects.requireNonNull(shortfall, "shortfall");
        Objects.requireNonNull(paidFromGoal, "paidFromGoal");
    }
}
