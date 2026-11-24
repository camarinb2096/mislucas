package org.mislucas.model.events;

import org.mislucas.model.config.Scenario;

/**
 * The eight cards that make up the Normal deck, one per week.
 *
 * <p>Declaration order follows the card table in {@code docs/rules.md}.
 * Each kind belongs to exactly one {@link CardType}. Amounts depend on the
 * {@link Scenario} and are stored there.</p>
 */
public enum CardKind {
    /** First optional purchase. */
    WANT_1(CardType.WANT, "Deseo 1"),
    /** Second optional purchase. */
    WANT_2(CardType.WANT, "Deseo 2"),
    /** Third optional purchase. */
    WANT_3(CardType.WANT, "Deseo 3"),
    /** Mandatory need. */
    NEED(CardType.NEED, "Necesidad"),
    /** First emergency. */
    EMERGENCY_1(CardType.EMERGENCY, "Emergencia 1"),
    /** Second emergency. */
    EMERGENCY_2(CardType.EMERGENCY, "Emergencia 2"),
    /** Extra income. */
    OPPORTUNITY(CardType.OPPORTUNITY, "Oportunidad"),
    /** Week without direct effect. */
    QUIET_WEEK(CardType.QUIET_WEEK, "Semana tranquila");

    private final CardType type;
    private final String displayName;

    /**
     * Creates a card kind.
     *
     * @param type category of the card
     * @param displayName name as written in the documentation
     */
    CardKind(CardType type, String displayName) {
        this.type = type;
        this.displayName = displayName;
    }

    /**
     * Returns the category of this card.
     *
     * @return the card type, never {@code null}
     */
    public CardType getType() {
        return type;
    }

    /**
     * Returns the documented name of this card.
     *
     * @return the display name, never {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }
}
