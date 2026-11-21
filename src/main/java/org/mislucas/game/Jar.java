package org.mislucas.game;

import java.util.Objects;

/**
 * Holds the balance of a spending, goal, or cushion jar.
 */
public final class Jar {
    private final JarType type;
    private Money balance = new Money(0);

    /**
     * Opens a jar with a balance of zero pesos.
     *
     * @param type the identity of the jar
     * @throws NullPointerException if the type is null
     */
    public Jar(JarType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    /**
     * Returns the identity of this jar.
     *
     * @return the jar type
     */
    public JarType type() {
        return type;
    }

    /**
     * Returns the current balance.
     *
     * @return the balance in whole Colombian pesos
     */
    public Money balance() {
        return balance;
    }

    /**
     * Adds the given amount to the balance.
     *
     * @param amount the amount to deposit
     * @throws NullPointerException if the amount is null
     * @throws ArithmeticException if the resulting balance exceeds the integer range
     */
    public void deposit(Money amount) {
        balance = new Money(Math.addExact(balance.pesos(), amount.pesos()));
    }

    /**
     * Subtracts the given amount from the balance.
     *
     * @param amount the amount to withdraw
     * @throws NullPointerException if the amount is null
     * @throws IllegalArgumentException if the amount exceeds the available balance
     */
    public void withdraw(Money amount) {
        if (amount.pesos() > balance.pesos()) {
            throw new IllegalArgumentException("Withdrawal exceeds the available balance.");
        }
        balance = new Money(balance.pesos() - amount.pesos());
    }
}
