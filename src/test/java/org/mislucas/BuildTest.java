package org.mislucas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mislucas.config.GameParameters;

class BuildTest {
    @Test
    void gameLastsEightWeeks() {
        assertEquals(8, GameParameters.WEEKS_PER_GAME);
    }
}
