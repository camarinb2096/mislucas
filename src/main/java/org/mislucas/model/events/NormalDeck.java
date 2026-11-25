package org.mislucas.model.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.mislucas.model.config.Scenario;

/**
 * Builds the Normal deck with exactly one card of each {@link CardKind}.
 */
public final class NormalDeck {
    /** Number of cards in the Normal deck (one per week). */
    public static final int SIZE = CardKind.values().length;

    /** Prevents instantiation of this deck factory. */
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
        return cardsFor(scenario, Arrays.asList(CardKind.values()));
    }

    /**
     * Builds the Normal deck in a supplied order using the scenario's amounts.
     *
     * @param scenario the selected scenario
     * @param cardOrder exactly one of each Normal card kind in reveal order
     * @return an unmodifiable deck in the supplied order
     * @throws NullPointerException if the scenario, order, or a kind is null
     * @throws IllegalArgumentException if the order is not a permutation of the Normal cards
     */
    public static List<Card> cardsFor(Scenario scenario, List<CardKind> cardOrder) {
        Objects.requireNonNull(scenario, "scenario must not be null");
        List<CardKind> order = List.copyOf(cardOrder);
        if (order.size() != SIZE || EnumSet.copyOf(order).size() != SIZE) {
            throw new IllegalArgumentException("The Normal deck requires one of each card kind.");
        }

        List<Card> cards = new ArrayList<>(SIZE);
        for (CardKind kind : order) {
            cards.add(new Card(kind, scenario.getCardAmount(kind)));
        }
        return List.copyOf(cards);
    }

    /**
     * Builds the Normal deck and shuffles it once for a new game.
     *
     * @param scenario the selected scenario
     * @return an unmodifiable shuffled deck
     * @throws NullPointerException if the scenario is null
     */
    public static List<Card> shuffledCardsFor(Scenario scenario) {
        List<Card> cards = new ArrayList<>(cardsFor(scenario));
        Collections.shuffle(cards);
        return List.copyOf(cards);
    }
}
