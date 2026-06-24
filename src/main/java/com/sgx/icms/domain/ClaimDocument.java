package com.sgx.icms.domain;

import java.time.LocalDateTime;

/** Mirrors the {@code claim_documents} table. */
public class ClaimDocument {

    private long id;
    private long claimId;
    private String docType;
    private String fileName;
    private String filePath;
    private String uploadStatus;        // REQUIRED/CONDITIONAL/PENDING/UPLOADED/MISSING/NA
    private String verificationStatus;  // PENDING/UNDER_REVIEW/VERIFIED/FLAGGED
    private LocalDateTime uploadedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClaimId() { return claimId; }
    public void setClaimId(long claimId) { this.claimId = claimId; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public boolean isUploaded() { return "UPLOADED".equalsIgnoreCase(uploadStatus); }
}
