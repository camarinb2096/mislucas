package mislucas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factory for the Normal deck data: exactly one card of each {@link CardKind}.
 *
 * <p>The returned list is in the documented table order and is not shuffled;
 * shuffling and fixed test orders belong to the game logic.</p>
 */
public final class NormalDeck {
    /** Number of cards in the Normal deck (one per week). */
    public static final int SIZE = CardKind.values().length;

    private NormalDeck() {
    }

    /**
     * Builds the Normal deck for a scenario.
     *
     * @param scenario the selected scenario, must not be {@code null}
     * @return an unmodifiable list of {@link #SIZE} cards, one per kind, in
     *         {@link CardKind} declaration order
     * @throws NullPointerException if {@code scenario} is {@code null}
     */
    public static List<Card> cardsFor(Scenario scenario) {
        Objects.requireNonNull(scenario, "scenario must not be null");
        List<Card> cards = new ArrayList<>(SIZE);
        for (CardKind kind : CardKind.values()) {
            cards.add(new Card(kind, scenario.getCardAmount(kind)));
        }
        return List.copyOf(cards);
    }
}
