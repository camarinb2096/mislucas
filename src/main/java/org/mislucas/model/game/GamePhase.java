package org.mislucas.model.game;

/**
 * The stage of a game's weekly turn.
 */
public enum GamePhase {
    /**
     * The weekly allowance is available for allocation.
     */
    AWAITING_ALLOCATION,

    /**
     * The scheduled fixed expense is due before the card can be revealed.
     */
    AWAITING_FIXED_EXPENSE,

    /**
     * The weekly card can be revealed.
     */
    AWAITING_CARD,

    /**
     * The weekly card has been revealed and the turn can be closed.
     */
    AWAITING_WEEK_END,

    /**
     * All weeks have ended and no further turn actions are accepted.
     */
    FINISHED
}
