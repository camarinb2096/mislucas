package org.mislucas.model.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ScenarioTest {

    @Test
    void exposesAllowanceAndFixedExpenseForEachScenario() {
        assertAll(
                () -> assertEquals(10_000, Scenario.TIGHT.getWeeklyAllowance()),
                () -> assertEquals(6_000, Scenario.TIGHT.getFixedExpense()),
                () -> assertEquals(20_000, Scenario.MEDIUM.getWeeklyAllowance()),
                () -> assertEquals(12_000, Scenario.MEDIUM.getFixedExpense()),
                () -> assertEquals(40_000, Scenario.COMFORTABLE.getWeeklyAllowance()),
                () -> assertEquals(24_000, Scenario.COMFORTABLE.getFixedExpense()));
    }

    @Test
    void definesExactlyThreeScenariosWithDocumentedNames() {
        assertAll(
                () -> assertEquals(3, Scenario.values().length),
                () -> assertEquals("Ajustado", Scenario.TIGHT.getSpanishName()),
                () -> assertEquals("Medio", Scenario.MEDIUM.getSpanishName()),
                () -> assertEquals("Holgado", Scenario.COMFORTABLE.getSpanishName()));
    }

    @Test
    void exposesGoalPricesForEachScenario() {
        assertAll(
                () -> assertEquals(40_000, Goal.HEADPHONES.getPrice(Scenario.TIGHT)),
                () -> assertEquals(60_000, Goal.SNEAKERS.getPrice(Scenario.TIGHT)),
                () -> assertEquals(80_000, Goal.CLASS_TRIP.getPrice(Scenario.TIGHT)),
                () -> assertEquals(80_000, Goal.HEADPHONES.getPrice(Scenario.MEDIUM)),
                () -> assertEquals(120_000, Goal.SNEAKERS.getPrice(Scenario.MEDIUM)),
                () -> assertEquals(160_000, Goal.CLASS_TRIP.getPrice(Scenario.MEDIUM)),
                () -> assertEquals(160_000, Goal.HEADPHONES.getPrice(Scenario.COMFORTABLE)),
                () -> assertEquals(240_000, Goal.SNEAKERS.getPrice(Scenario.COMFORTABLE)),
                () -> assertEquals(320_000, Goal.CLASS_TRIP.getPrice(Scenario.COMFORTABLE)));
    }

    @Test
    void goalPriceMatchesScenarioLookup() {
        for (Scenario scenario : Scenario.values()) {
            for (Goal goal : Goal.values()) {
                assertEquals(scenario.getGoalPrice(goal), goal.getPrice(scenario));
            }
        }
    }

    @Test
    void rejectsNullArguments() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> Scenario.TIGHT.getGoalPrice(null)),
                () -> assertThrows(NullPointerException.class, () -> Scenario.TIGHT.getCardAmount(null)),
                () -> assertThrows(NullPointerException.class, () -> Goal.SNEAKERS.getPrice(null)));
    }
}
