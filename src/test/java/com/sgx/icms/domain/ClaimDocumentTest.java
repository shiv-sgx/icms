package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ClaimDocumentTest {

    @Test
    void gettersAndSettersRoundTrip() {
        ClaimDocument d = new ClaimDocument();
        LocalDateTime uploadedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        d.setId(1L);
        d.setClaimId(2L);
        d.setDocType("RC_BOOK");
        d.setFileName("rc.pdf");
        d.setFilePath("/uploads/rc.pdf");
        d.setUploadStatus("UPLOADED");
        d.setVerificationStatus("VERIFIED");
        d.setUploadedAt(uploadedAt);

        assertEquals(1L, d.getId());
        assertEquals(2L, d.getClaimId());
        assertEquals("RC_BOOK", d.getDocType());
        assertEquals("rc.pdf", d.getFileName());
        assertEquals("/uploads/rc.pdf", d.getFilePath());
        assertEquals("UPLOADED", d.getUploadStatus());
        assertEquals("VERIFIED", d.getVerificationStatus());
        assertEquals(uploadedAt, d.getUploadedAt());
    }

    @Test
    void isUploadedTrueCaseInsensitive() {
        ClaimDocument d = new ClaimDocument();
        d.setUploadStatus("uploaded");
        assertTrue(d.isUploaded());
    }

    @Test
    void isUploadedFalseForOtherStatuses() {
        ClaimDocument d = new ClaimDocument();
        d.setUploadStatus("PENDING");
        assertFalse(d.isUploaded());
    }

    @Test
    void isUploadedFalseWhenNull() {
        ClaimDocument d = new ClaimDocument();
        assertFalse(d.isUploaded());
    }
}
