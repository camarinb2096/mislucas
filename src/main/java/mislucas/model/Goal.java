package mislucas.model;

/**
 * Savings goals the player can choose at setup.
 *
 * <p>The price of a goal depends on the selected {@link Scenario}; use
 * {@link #getPrice(Scenario)} to obtain it in integer Colombian pesos.</p>
 */
public enum Goal {
    /** Headphones goal. */
    HEADPHONES("Audifonos"),
    /** Sneakers goal. */
    SNEAKERS("Tenis"),
    /** Class trip goal. */
    CLASS_TRIP("Viaje de curso");

    private final String displayName;

    /**
     * Creates a goal.
     *
     * @param displayName name as written in the documentation
     */
    Goal(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the documented name of this goal.
     *
     * @return the display name, never {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the price of this goal in the given scenario.
     *
     * @param scenario the selected scenario, must not be {@code null}
     * @return the price in COP
     * @throws NullPointerException if {@code scenario} is {@code null}
     */
    public int getPrice(Scenario scenario) {
        if (scenario == null) {
            throw new NullPointerException("scenario must not be null");
        }
        return scenario.getGoalPrice(this);
    }
}
