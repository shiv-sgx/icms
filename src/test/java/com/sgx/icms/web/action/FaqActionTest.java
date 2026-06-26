package com.sgx.icms.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FaqActionTest {

    @Test
    void execute_alwaysReturnsSuccess() {
        FaqAction action = new FaqAction();
        assertEquals(FaqAction.SUCCESS, action.execute());
    }
}
