package mislucas.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void mapsEachKindToItsType() {
        assertAll(
                () -> assertEquals(CardType.WANT, CardKind.WANT_1.getType()),
                () -> assertEquals(CardType.WANT, CardKind.WANT_2.getType()),
                () -> assertEquals(CardType.WANT, CardKind.WANT_3.getType()),
                () -> assertEquals(CardType.NEED, CardKind.NEED.getType()),
                () -> assertEquals(CardType.EMERGENCY, CardKind.EMERGENCY_1.getType()),
                () -> assertEquals(CardType.EMERGENCY, CardKind.EMERGENCY_2.getType()),
                () -> assertEquals(CardType.OPPORTUNITY, CardKind.OPPORTUNITY.getType()),
                () -> assertEquals(CardType.QUIET_WEEK, CardKind.QUIET_WEEK.getType()));
    }

    @Test
    void createsValidCards() {
        Card want = new Card(CardKind.WANT_1, 5_000);
        assertAll(
                () -> assertEquals(CardKind.WANT_1, want.getKind()),
                () -> assertEquals(CardType.WANT, want.getType()),
                () -> assertEquals(5_000, want.getAmount()),
                () -> assertEquals(0, new Card(CardKind.QUIET_WEEK, 0).getAmount()));
    }

    @Test
    void rejectsNonZeroQuietWeek() {
        assertThrows(IllegalArgumentException.class, () -> new Card(CardKind.QUIET_WEEK, 1_000));
    }

    @Test
    void rejectsZeroOrNegativeAmountForNonQuietCards() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Card(CardKind.NEED, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Card(CardKind.OPPORTUNITY, -10_000)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Card(CardKind.EMERGENCY_1, -1)));
    }

    @Test
    void rejectsNullKind() {
        assertThrows(NullPointerException.class, () -> new Card(null, 1_000));
    }

    @Test
    void equalCardsHaveSameKindAndAmount() {
        assertAll(
                () -> assertEquals(new Card(CardKind.NEED, 8_000), new Card(CardKind.NEED, 8_000)),
                () -> assertEquals(new Card(CardKind.NEED, 8_000).hashCode(),
                        new Card(CardKind.NEED, 8_000).hashCode()));
    }
}
