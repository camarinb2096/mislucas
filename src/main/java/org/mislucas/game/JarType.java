package org.mislucas.game;

/**
 * Identifies each of the three jars in a game.
 */
public enum JarType {
    /**
     * Holds funds for spending.
     */
    SPENDING,

    /**
     * Holds savings toward the player's goal.
     */
    GOAL,

    /**
     * Holds the player's emergency reserve.
     */
    CUSHION
}
