package com.sgx.icms.domain;

/** Mirrors the {@code document_requirements} config table. */
public class DocumentRequirement {

    private int id;
    private String claimType;
    private String claimSubtype;
    private String docType;
    private boolean required;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }

    public String getClaimSubtype() { return claimSubtype; }
    public void setClaimSubtype(String claimSubtype) { this.claimSubtype = claimSubtype; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
