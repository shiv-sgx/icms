package com.sgx.icms.web.action.customer;

import java.io.File;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.DocumentService;

/** Handles a customer document upload for one of their claims. */
public class DocumentUploadAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient DocumentService documentService = new DocumentService();
    private final transient AuditService audit = new AuditService();

    private long claimId;
    private String docType;
    private File upload;
    private String uploadFileName;
    private String uploadContentType;

    private String redirectUrl;

    @Override
    public String execute() {
        redirectUrl = "/customer/claim?id=" + claimId;
        if (policyholder() == null) {
            return "missing";
        }
        Claim claim = claimService.getOwnedClaim(policyholder().getId(), claimId);
        if (claim == null) {
            setFlash("error", "Claim not found.");
            return "missing";
        }
        try {
            documentService.upload(claimId, docType, upload, uploadFileName);
            audit.success(currentUser(), "DOC_UPLOAD", claim.getClaimNo() + " / " + docType, clientIp());
            setFlash("success", "Document uploaded.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public void setClaimId(long claimId) { this.claimId = claimId; }
    public long getClaimId() { return claimId; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getDocType() { return docType; }
    public void setUpload(File upload) { this.upload = upload; }
    public void setUploadFileName(String n) { this.uploadFileName = n; }
    public void setUploadContentType(String t) { this.uploadContentType = t; }
    public String getRedirectUrl() { return redirectUrl; }
}
