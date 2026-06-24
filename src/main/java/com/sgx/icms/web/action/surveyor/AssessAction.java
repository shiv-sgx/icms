package com.sgx.icms.web.action.surveyor;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AssessmentComponent;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimDocument;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.DocumentService;

/**
 * Surveyor assessment screen: shows a claim's assessment form (and any existing
 * assessment), accepts the submitted component breakdown + figures, and handles
 * survey report / site-photo uploads. One method per action (DMI disabled).
 */
public class AssessAction extends SurveyorBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient DocumentService documentService = new DocumentService();
    private final transient AuditService audit = new AuditService();

    private long id; // claim id

    // view models
    private transient Claim claim;
    private transient Assessment assessment;
    private transient List<AssessmentComponent> components = Collections.emptyList();
    private transient List<ClaimDocument> documents = Collections.emptyList();
    private String flashMessage;
    private String flashType;

    // assessment form fields
    private String visitDate;
    private String visitTime;
    private String siteObservations;
    private String reportRefNo;
    private String policyDeductible;
    private String depreciationPct;
    private String salvageValue;
    private String recommendation;
    private String remarks;
    private String[] compName;
    private String[] compSeverity;
    private String[] compCost;
    private String[] compReplace;

    // upload fields
    private String docType;
    private File upload;
    private String uploadFileName;
    private String uploadContentType;

    private String redirectUrl;

    private boolean load() {
        claim = surveyorService.getAssignedClaim(currentUser().getId(), id);
        if (claim == null) {
            setFlash("error", "Claim not found or not assigned to you.");
            return false;
        }
        assessment = surveyorService.latestAssessment(id);
        if (assessment != null) {
            components = surveyorService.components(assessment.getId());
        }
        documents = documentService.forClaim(id);
        return true;
    }

    public String show() {
        if (!load()) {
            return "missing";
        }
        flashMessage = consumeFlash();
        flashType = consumeFlashType();
        return SUCCESS;
    }

    public String submit() {
        redirectUrl = assessUrl(id);
        if (surveyorService.getAssignedClaim(currentUser().getId(), id) == null) {
            setFlash("error", "Claim not found or not assigned to you.");
            return "missing";
        }
        Assessment input = new Assessment();
        try {
            input.setVisitDate(parseDate(visitDate));
            input.setVisitTime(parseTime(visitTime));
        } catch (DateTimeParseException e) {
            setFlash("error", "Invalid visit date/time.");
            return SUCCESS;
        }
        input.setSiteObservations(trimToNull(siteObservations));
        input.setReportRefNo(trimToNull(reportRefNo));
        input.setPolicyDeductible(parseDecimal(policyDeductible));
        input.setDepreciationPct(parseDecimal(depreciationPct));
        input.setSalvageValue(parseDecimal(salvageValue));
        input.setRecommendation(trimToNull(recommendation));
        input.setRemarks(trimToNull(remarks));

        try {
            List<AssessmentComponent> comps = buildComponents();
            surveyorService.submitAssessment(currentUser(), id, input, comps, clientIp());
            audit.success(currentUser(), "ASSESSMENT_FORM", "claim:" + id, clientIp());
            setFlash("success", "Assessment submitted. The agent has been notified.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String upload() {
        redirectUrl = assessUrl(id);
        if (surveyorService.getAssignedClaim(currentUser().getId(), id) == null) {
            setFlash("error", "Claim not found or not assigned to you.");
            return "missing";
        }
        try {
            documentService.upload(id, docType, upload, uploadFileName);
            audit.success(currentUser(), "SURVEY_REPORT_UPLOAD", "claim:" + id + " / " + docType, clientIp());
            setFlash("success", "Document uploaded.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    private List<AssessmentComponent> buildComponents() {
        List<AssessmentComponent> list = new ArrayList<>();
        if (compName == null) {
            return list;
        }
        for (int i = 0; i < compName.length; i++) {
            String name = compName[i];
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            AssessmentComponent c = new AssessmentComponent();
            c.setComponent(name.trim());
            c.setSeverity(valueAt(compSeverity, i, "MODERATE"));
            c.setRepairCost(parseDecimal(valueAt(compCost, i, "0")));
            c.setReplaceFlag("true".equalsIgnoreCase(valueAt(compReplace, i, "false")));
            list.add(c);
        }
        return list;
    }

    private static String valueAt(String[] arr, int i, String fallback) {
        return (arr != null && i < arr.length && arr[i] != null) ? arr[i] : fallback;
    }

    private static LocalDate parseDate(String s) {
        return (s == null || s.trim().isEmpty()) ? null : LocalDate.parse(s.trim());
    }

    private static LocalTime parseTime(String s) {
        return (s == null || s.trim().isEmpty()) ? null : LocalTime.parse(s.trim());
    }

    private static BigDecimal parseDecimal(String s) {
        if (s == null || s.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static String trimToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /* getters/setters */
    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    public Claim getClaim() { return claim; }
    public Assessment getAssessment() { return assessment; }
    public List<AssessmentComponent> getComponents() { return components; }
    public List<ClaimDocument> getDocuments() { return documents; }
    public String getFlashMessage() { return flashMessage; }
    public String getFlashType() { return flashType; }
    public String getRedirectUrl() { return redirectUrl; }

    public void setVisitDate(String v) { this.visitDate = v; }
    public void setVisitTime(String v) { this.visitTime = v; }
    public void setSiteObservations(String v) { this.siteObservations = v; }
    public void setReportRefNo(String v) { this.reportRefNo = v; }
    public void setPolicyDeductible(String v) { this.policyDeductible = v; }
    public void setDepreciationPct(String v) { this.depreciationPct = v; }
    public void setSalvageValue(String v) { this.salvageValue = v; }
    public void setRecommendation(String v) { this.recommendation = v; }
    public void setRemarks(String v) { this.remarks = v; }
    public void setCompName(String[] v) { this.compName = v; }
    public void setCompSeverity(String[] v) { this.compSeverity = v; }
    public void setCompCost(String[] v) { this.compCost = v; }
    public void setCompReplace(String[] v) { this.compReplace = v; }

    public void setDocType(String v) { this.docType = v; }
    public void setUpload(File f) { this.upload = f; }
    public void setUploadFileName(String n) { this.uploadFileName = n; }
    public void setUploadContentType(String t) { this.uploadContentType = t; }
}
