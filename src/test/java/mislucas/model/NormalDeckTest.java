package mislucas.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NormalDeckTest {

    private static void assertAmounts(Scenario scenario, int... expected) {
        List<Card> deck = NormalDeck.cardsFor(scenario);
        CardKind[] kinds = CardKind.values();
        for (int i = 0; i < kinds.length; i++) {
            assertEquals(kinds[i], deck.get(i).getKind());
            assertEquals(expected[i], deck.get(i).getAmount(), kinds[i].getDisplayName());
        }
    }

    @Test
    void tightDeckMatchesDocumentedAmounts() {
        assertAmounts(Scenario.TIGHT, 5_000, 6_000, 10_000, 8_000, 8_000, 10_000, 10_000, 0);
    }

    @Test
    void mediumDeckMatchesDocumentedAmounts() {
        assertAmounts(Scenario.MEDIUM, 10_000, 12_000, 20_000, 16_000, 16_000, 20_000, 20_000, 0);
    }

    @Test
    void comfortableDeckMatchesDocumentedAmounts() {
        assertAmounts(Scenario.COMFORTABLE, 20_000, 24_000, 40_000, 32_000, 32_000, 40_000, 40_000, 0);
    }

    @Test
    void everyDeckHasEightCardsOneOfEachKind() {
        for (Scenario scenario : Scenario.values()) {
            List<Card> deck = NormalDeck.cardsFor(scenario);
            Set<CardKind> kinds = EnumSet.noneOf(CardKind.class);
            deck.forEach(card -> kinds.add(card.getKind()));
            assertAll(
                    () -> assertEquals(8, NormalDeck.SIZE),
                    () -> assertEquals(8, deck.size()),
                    () -> assertEquals(8, kinds.size()));
        }
    }

    @Test
    void deckIsUnmodifiable() {
        List<Card> deck = NormalDeck.cardsFor(Scenario.TIGHT);
        assertThrows(UnsupportedOperationException.class, () -> deck.remove(0));
    }

    @Test
    void rejectsNullScenario() {
        assertThrows(NullPointerException.class, () -> NormalDeck.cardsFor(null));
    }
}
