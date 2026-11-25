package org.mislucas.model.events;

import java.util.Objects;

/**
 * Immutable data of one card: its kind and its amount in COP.
 *
 * <p>The amount is always nonnegative; its meaning depends on the type
 * (cost for wants, needs and emergencies; income for opportunities; zero for
 * the quiet week). The game applies the effect when resolving the card.</p>
 */
public final class Card {
    /** The kind that determines how the game resolves this card. */
    private final CardKind kind;

    /** The scenario-specific amount in whole Colombian pesos. */
    private final int amount;

    /**
     * Creates a card.
     *
     * @param kind the card kind, must not be {@code null}
     * @param amount the amount in COP; must be 0 for the quiet week and
     *        greater than 0 for every other kind
     * @throws NullPointerException if {@code kind} is {@code null}
     * @throws IllegalArgumentException if the amount is invalid for the kind
     */
    public Card(CardKind kind, int amount) {
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        if (kind.getType() == CardType.QUIET_WEEK) {
            if (amount != 0) {
                throw new IllegalArgumentException("A quiet week has no amount");
            }
        } else if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive for " + kind.getDisplayName());
        }
        this.amount = amount;
    }

    /**
     * Returns the kind of this card.
     *
     * @return the kind, never {@code null}
     */
    public CardKind getKind() {
        return kind;
    }

    /**
     * Returns the type of this card.
     *
     * @return the type, never {@code null}
     */
    public CardType getType() {
        return kind.getType();
    }

    /**
     * Returns the amount of this card.
     *
     * @return the amount in COP
     */
    public int getAmount() {
        return amount;
    }

    /**
     * {@inheritDoc}
     * Two cards are equal when both their kind and amount match.
     *
     * @param other the object to compare
     * @return true if the other object is an equal card
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Card)) {
            return false;
        }
        Card card = (Card) other;
        return kind == card.kind && amount == card.amount;
    }

    /**
     * {@inheritDoc}
     * The hash is based on the card kind and amount.
     *
     * @return the card's hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(kind, amount);
    }

    /**
     * {@inheritDoc}
     * The returned text contains the card name and amount.
     *
     * @return a display string for this card
     */
    @Override
    public String toString() {
        return kind.getDisplayName() + " (COP " + amount + ")";
    }
}
