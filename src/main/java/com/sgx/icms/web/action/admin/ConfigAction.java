package com.sgx.icms.web.action.admin;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.ApprovalThreshold;
import com.sgx.icms.domain.DocumentRequirement;
import com.sgx.icms.domain.NotificationTemplate;
import com.sgx.icms.domain.SlaConfig;

/**
 * Admin configuration: SLA stages, approval thresholds, notification templates, and
 * document requirements. Each config area has a show method and POST update methods
 * (DMI disabled → explicit method per mapping).
 */
public class ConfigAction extends AdminBaseAction {

    private static final long serialVersionUID = 1L;

    private transient List<SlaConfig> slaList = Collections.emptyList();
    private transient List<ApprovalThreshold> thresholdList = Collections.emptyList();
    private transient List<NotificationTemplate> templateList = Collections.emptyList();
    private transient List<DocumentRequirement> docReqList = Collections.emptyList();
    private String flashMessage;
    private String flashType;
    private String redirectUrl;

    // form fields
    private int id;
    private int hours;
    private String minAmount;
    private String maxAmount;
    private boolean active;
    private String body;
    private String claimType;
    private String claimSubtype;
    private String docType;
    private boolean required;

    private void consume() {
        flashMessage = consumeFlash();
        flashType = consumeFlashType();
    }

    /* ---- SLA ---- */
    public String sla() {
        slaList = adminService.slaConfigs();
        consume();
        return SUCCESS;
    }

    public String updateSla() {
        redirectUrl = "/admin/sla";
        try {
            adminService.updateSla(currentUser(), id, hours, clientIp());
            setFlash("success", "SLA updated.");
        } catch (RuntimeException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    /* ---- thresholds ---- */
    public String thresholds() {
        thresholdList = adminService.thresholds();
        consume();
        return SUCCESS;
    }

    public String updateThreshold() {
        redirectUrl = "/admin/thresholds";
        try {
            BigDecimal min = new BigDecimal(minAmount.trim());
            BigDecimal max = (maxAmount == null || maxAmount.trim().isEmpty()) ? null : new BigDecimal(maxAmount.trim());
            adminService.updateThreshold(currentUser(), id, min, max, clientIp());
            setFlash("success", "Threshold updated.");
        } catch (NumberFormatException | NullPointerException e) {
            setFlash("error", "Enter valid amounts.");
        } catch (RuntimeException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    /* ---- templates ---- */
    public String templates() {
        templateList = adminService.templates();
        consume();
        return SUCCESS;
    }

    public String updateTemplate() {
        redirectUrl = "/admin/templates";
        try {
            adminService.updateTemplate(currentUser(), id, active, body, clientIp());
            setFlash("success", "Template updated.");
        } catch (RuntimeException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    /* ---- document requirements ---- */
    public String documents() {
        docReqList = adminService.documentRequirements();
        consume();
        return SUCCESS;
    }

    public String addDocument() {
        redirectUrl = "/admin/documents";
        try {
            DocumentRequirement d = new DocumentRequirement();
            d.setClaimType(claimType == null ? null : claimType.trim());
            d.setClaimSubtype((claimSubtype == null || claimSubtype.trim().isEmpty()) ? null : claimSubtype.trim());
            d.setDocType(docType == null ? null : docType.trim());
            d.setRequired(required);
            adminService.addDocumentRequirement(currentUser(), d, clientIp());
            setFlash("success", "Document requirement added.");
        } catch (RuntimeException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String deleteDocument() {
        redirectUrl = "/admin/documents";
        try {
            adminService.deleteDocumentRequirement(currentUser(), id, clientIp());
            setFlash("success", "Document requirement removed.");
        } catch (RuntimeException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public List<SlaConfig> getSlaList() { return slaList; }
    public List<ApprovalThreshold> getThresholdList() { return thresholdList; }
    public List<NotificationTemplate> getTemplateList() { return templateList; }
    public List<DocumentRequirement> getDocReqList() { return docReqList; }
    public String getFlashMessage() { return flashMessage; }
    public String getFlashType() { return flashType; }
    public String getRedirectUrl() { return redirectUrl; }

    public void setId(int id) { this.id = id; }
    public void setHours(int hours) { this.hours = hours; }
    public void setMinAmount(String v) { this.minAmount = v; }
    public void setMaxAmount(String v) { this.maxAmount = v; }
    public void setActive(boolean active) { this.active = active; }
    public void setBody(String body) { this.body = body; }
    public void setClaimType(String v) { this.claimType = v; }
    public void setClaimSubtype(String v) { this.claimSubtype = v; }
    public void setDocType(String v) { this.docType = v; }
    public void setRequired(boolean required) { this.required = required; }
}
