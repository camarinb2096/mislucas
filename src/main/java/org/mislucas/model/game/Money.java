package org.mislucas.model.game;

/**
 * An amount of money expressed in whole Colombian pesos.
 *
 * @param pesos the nonnegative number of Colombian pesos
 */
public record Money(int pesos) {
    /**
     * Creates a monetary amount in whole Colombian pesos.
     *
     * @param pesos the number of Colombian pesos
     * @throws IllegalArgumentException if the amount is negative
     */
    public Money {
        if (pesos < 0) {
            throw new IllegalArgumentException("Money cannot be negative.");
        }
    }
}
