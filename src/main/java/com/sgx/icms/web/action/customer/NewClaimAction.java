package com.sgx.icms.web.action.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Policy;

/**
 * New-claim submission. {@code show()} renders the form (GET); {@code execute()}
 * validates and creates the claim as a draft or a submission (POST). Type-specific
 * fields are captured; the claim type is derived from the selected policy.
 */
public class NewClaimAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    // form fields (strings where parsing/validation is needed)
    private long policyId;
    private String claimSubtype;
    private String incidentDate;
    private String incidentTime;
    private String incidentLocation;
    private String city;
    private String state;
    private String pinCode;
    private String description;
    private String estimatedLoss;
    private String vehicleRegNo;
    private String firNumber;
    private String policeStation;
    private String hospitalName;
    private String workshopName;
    private String thirdParty;
    private String mode = "submit"; // "submit" or "draft" (from the clicked button)

    private transient List<Policy> policies = Collections.emptyList();
    private String redirectUrl;

    private void loadPolicies() {
        if (policyholder() != null) {
            policies = claimService.policiesForCustomer(policyholder().getId());
        }
    }

    /** GET: render the empty form. */
    public String show() {
        loadPolicies();
        return INPUT;
    }

    /** POST: validate + create. */
    @Override
    public String execute() {
        loadPolicies();

        if (policyholder() == null) {
            addActionError(getText("No policyholder profile is linked to your account."));
            return INPUT;
        }
        if (policyId <= 0) {
            addFieldError("policyId", "Please select a policy.");
        }
        if (isBlank(description)) {
            addFieldError("description", "Please describe the incident.");
        }
        LocalDate incDate = null;
        if (!isBlank(incidentDate)) {
            try {
                incDate = LocalDate.parse(incidentDate.trim());
            } catch (DateTimeParseException e) {
                addFieldError("incidentDate", "Use the date picker (YYYY-MM-DD).");
            }
        }
        LocalTime incTime = null;
        if (!isBlank(incidentTime)) {
            try {
                incTime = LocalTime.parse(incidentTime.trim());
            } catch (DateTimeParseException e) {
                addFieldError("incidentTime", "Invalid time.");
            }
        }
        BigDecimal loss = BigDecimal.ZERO;
        if (!isBlank(estimatedLoss)) {
            try {
                loss = new BigDecimal(estimatedLoss.trim());
                if (loss.signum() < 0) {
                    addFieldError("estimatedLoss", "Estimated loss cannot be negative.");
                }
            } catch (NumberFormatException e) {
                addFieldError("estimatedLoss", "Enter a valid amount.");
            }
        }
        if (hasErrors()) {
            return INPUT;
        }

        Claim c = new Claim();
        c.setPolicyId(policyId);
        c.setClaimSubtype(trimToNull(claimSubtype));
        c.setIncidentDate(incDate);
        c.setIncidentTime(incTime);
        c.setIncidentLocation(trimToNull(incidentLocation));
        c.setCity(trimToNull(city));
        c.setState(trimToNull(state));
        c.setPinCode(trimToNull(pinCode));
        c.setDescription(trimToNull(description));
        c.setEstimatedLoss(loss);
        c.setVehicleRegNo(trimToNull(vehicleRegNo));
        c.setFirNumber(trimToNull(firNumber));
        c.setPoliceStation(trimToNull(policeStation));
        c.setHospitalName(trimToNull(hospitalName));
        c.setWorkshopName(trimToNull(workshopName));
        c.setThirdParty(trimToNull(thirdParty));

        boolean submit = !"draft".equalsIgnoreCase(mode);
        try {
            long claimId = claimService.createClaim(currentUser(), policyholder(), c, submit, clientIp());
            setFlash("success", submit
                    ? "Claim submitted successfully. Track its progress below."
                    : "Draft saved. You can complete and submit it any time.");
            redirectUrl = "/customer/claim?id=" + claimId;
            return SUCCESS;
        } catch (IllegalArgumentException e) {
            addActionError(e.getMessage());
            return INPUT;
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String trimToNull(String s) { return isBlank(s) ? null : s.trim(); }

    /* getters/setters */
    public void setPolicyId(long policyId) { this.policyId = policyId; }
    public long getPolicyId() { return policyId; }
    public void setClaimSubtype(String v) { this.claimSubtype = v; }
    public String getClaimSubtype() { return claimSubtype; }
    public void setIncidentDate(String v) { this.incidentDate = v; }
    public String getIncidentDate() { return incidentDate; }
    public void setIncidentTime(String v) { this.incidentTime = v; }
    public String getIncidentTime() { return incidentTime; }
    public void setIncidentLocation(String v) { this.incidentLocation = v; }
    public String getIncidentLocation() { return incidentLocation; }
    public void setCity(String v) { this.city = v; }
    public String getCity() { return city; }
    public void setState(String v) { this.state = v; }
    public String getState() { return state; }
    public void setPinCode(String v) { this.pinCode = v; }
    public String getPinCode() { return pinCode; }
    public void setDescription(String v) { this.description = v; }
    public String getDescription() { return description; }
    public void setEstimatedLoss(String v) { this.estimatedLoss = v; }
    public String getEstimatedLoss() { return estimatedLoss; }
    public void setVehicleRegNo(String v) { this.vehicleRegNo = v; }
    public String getVehicleRegNo() { return vehicleRegNo; }
    public void setFirNumber(String v) { this.firNumber = v; }
    public String getFirNumber() { return firNumber; }
    public void setPoliceStation(String v) { this.policeStation = v; }
    public String getPoliceStation() { return policeStation; }
    public void setHospitalName(String v) { this.hospitalName = v; }
    public String getHospitalName() { return hospitalName; }
    public void setWorkshopName(String v) { this.workshopName = v; }
    public String getWorkshopName() { return workshopName; }
    public void setThirdParty(String v) { this.thirdParty = v; }
    public String getThirdParty() { return thirdParty; }
    public void setMode(String v) { this.mode = v; }
    public String getMode() { return mode; }

    public List<Policy> getPolicies() { return policies; }
    public String getRedirectUrl() { return redirectUrl; }
}
