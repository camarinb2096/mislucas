package mislucas.model;

/**
 * Category of a card in the Normal deck.
 *
 * <p>The category determines which resolution rule applies in
 * {@code docs/rules.md}. This enum only classifies cards; resolving them
 * is outside the scope of this class.</p>
 */
public enum CardType {
    /** Optional purchase; it is bought only if the player accepts. */
    WANT,
    /** Mandatory everyday expense. */
    NEED,
    /** Mandatory unexpected expense. */
    EMERGENCY,
    /** Extra income received by the player. */
    OPPORTUNITY,
    /** Week with no direct effect. */
    QUIET_WEEK
}
