package org.mislucas.model.rules;

/**
 * The repayment options for a mandatory shortfall.
 */
public enum CreditPlan {
    /**
     * One installment due next week, with zero interest.
     */
    ONE_INSTALLMENT,

    /**
     * Two weekly installments with 10 percent interest charged once.
     */
    TWO_INSTALLMENTS
}
