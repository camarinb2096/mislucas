package org.mislucas.model.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.mislucas.model.events.CardKind;
import org.mislucas.model.events.CardType;

/**
 * Resource scenarios of the game with the values documented in
 * {@code docs/rules.md}.
 *
 * <p>Each scenario defines the weekly allowance, the fixed expense, the
 * price of every {@link Goal} and the amount of every {@link CardKind} of the
 * Normal deck. All amounts are integer Colombian pesos (COP). The tables are
 * stored explicitly, not derived from one another.</p>
 */
public enum Scenario {
    /** Tight (<i>Ajustado</i>). */
    TIGHT("Tight", "Ajustado", 10_000, 6_000,
            new int[] {40_000, 60_000, 80_000},
            new int[] {5_000, 6_000, 10_000, 8_000, 8_000, 10_000, 10_000, 0}),
    /** Medium (<i>Medio</i>). */
    MEDIUM("Medium", "Medio", 20_000, 12_000,
            new int[] {80_000, 120_000, 160_000},
            new int[] {10_000, 12_000, 20_000, 16_000, 16_000, 20_000, 20_000, 0}),
    /** Comfortable (<i>Holgado</i>). */
    COMFORTABLE("Comfortable", "Holgado", 40_000, 24_000,
            new int[] {160_000, 240_000, 320_000},
            new int[] {20_000, 24_000, 40_000, 32_000, 32_000, 40_000, 40_000, 0});

    /** The scenario name in English. */
    private final String displayName;

    /** The scenario name in Spanish. */
    private final String spanishName;

    /** The allowance granted at the start of each week, in COP. */
    private final int weeklyAllowance;

    /** The fixed expense charged in weeks one and five, in COP. */
    private final int fixedExpense;

    /** Goal prices keyed by goal type. */
    private final Map<Goal, Integer> goalPrices;

    /** Card amounts keyed by card kind. */
    private final Map<CardKind, Integer> cardAmounts;

    /**
     * Creates a scenario.
     *
     * @param displayName English name
     * @param spanishName Spanish name
     * @param weeklyAllowance allowance granted each week, in COP
     * @param fixedExpense fixed expense charged in weeks 1 and 5, in COP
     * @param goalPrices goal prices in {@link Goal} declaration order
     * @param cardAmounts card amounts in {@link CardKind} declaration order
     */
    Scenario(String displayName, String spanishName, int weeklyAllowance,
            int fixedExpense, int[] goalPrices, int[] cardAmounts) {
        this.displayName = displayName;
        this.spanishName = spanishName;
        this.weeklyAllowance = weeklyAllowance;
        this.fixedExpense = fixedExpense;
        this.goalPrices = toMap(Goal.values(), goalPrices);
        this.cardAmounts = toMap(CardKind.values(), cardAmounts);
    }

    private static <K extends Enum<K>> Map<K, Integer> toMap(K[] keys, int[] values) {
        if (keys.length != values.length) {
            throw new IllegalStateException("Scenario data does not match " + keys.length + " entries");
        }
        Map<K, Integer> map = new EnumMap<>(keys[0].getDeclaringClass());
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the English name of this scenario.
     *
     * @return the English name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the Spanish name of this scenario.
     *
     * @return the Spanish name
     */
    public String getSpanishName() {
        return spanishName;
    }

    /**
     * Returns the weekly allowance.
     *
     * @return the allowance in COP
     */
    public int getWeeklyAllowance() {
        return weeklyAllowance;
    }

    /**
     * Returns the fixed expense (charged in weeks 1 and 5 by the weekly sequence).
     *
     * @return the fixed expense in COP
     */
    public int getFixedExpense() {
        return fixedExpense;
    }

    /**
     * Returns the price of a goal in this scenario.
     *
     * @param goal the goal, must not be {@code null}
     * @return the price in COP
     * @throws NullPointerException if {@code goal} is {@code null}
     */
    public int getGoalPrice(Goal goal) {
        if (goal == null) {
            throw new NullPointerException("goal must not be null");
        }
        return goalPrices.get(goal);
    }

    /**
     * Returns the amount of a Normal deck card in this scenario. For an
     * {@link CardType#OPPORTUNITY} card the amount is the income received;
     * for the quiet week it is 0; for other cards it is the cost.
     *
     * @param kind the card kind, must not be {@code null}
     * @return the amount in COP, never negative
     * @throws NullPointerException if {@code kind} is {@code null}
     */
    public int getCardAmount(CardKind kind) {
        if (kind == null) {
            throw new NullPointerException("kind must not be null");
        }
        return cardAmounts.get(kind);
    }
}
