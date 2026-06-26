package com.sgx.icms.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TimelineStageTest {

    @Test
    void constructorAndGetters() {
        TimelineStage stage = new TimelineStage("Submitted", TimelineStage.DONE);
        assertEquals("Submitted", stage.getLabel());
        assertEquals(TimelineStage.DONE, stage.getState());
    }

    @Test
    void stateConstantsHaveExpectedValues() {
        assertEquals("done", TimelineStage.DONE);
        assertEquals("current", TimelineStage.CURRENT);
        assertEquals("pending", TimelineStage.PENDING);
    }
}
