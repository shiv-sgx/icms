<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">New Claim Submission</h1>
    <p class="page-sub">Provide the incident details. The claim type follows your selected policy.</p>
</div>

<s:if test="hasActionErrors() || hasFieldErrors()">
    <div class="alert alert-error">
        <s:actionerror />
        <s:fielderror />
    </div>
</s:if>

<s:if test="policies == null || policies.isEmpty()">
    <div class="alert alert-error">No active policies are linked to your account, so a claim cannot be filed.</div>
</s:if>
<s:else>
<form action="<s:url action='createClaim' namespace='/customer'/>" method="post" class="form-card">

    <div class="form-section">
        <h3>Policy &amp; Incident</h3>
        <div class="form-row">
            <div class="field">
                <label>Policy</label>
                <select name="policyId" class="input" required>
                    <option value="">— Select a policy —</option>
                    <s:iterator value="policies">
                        <option value="<s:property value='id'/>" <s:if test="id == policyId">selected</s:if>><s:property value="displayLabel" /></option>
                    </s:iterator>
                </select>
            </div>
            <div class="field">
                <label>Claim Subtype</label>
                <input type="text" name="claimSubtype" class="input" value="<s:property value='claimSubtype'/>" placeholder="e.g. Accident, Theft, Surgery" />
            </div>
        </div>
        <div class="form-row">
            <div class="field">
                <label>Incident Date</label>
                <input type="date" name="incidentDate" class="input" value="<s:property value='incidentDate'/>" />
            </div>
            <div class="field">
                <label>Time of Incident</label>
                <input type="time" name="incidentTime" class="input" value="<s:property value='incidentTime'/>" />
            </div>
            <div class="field">
                <label>Estimated Loss (&#8377;)</label>
                <input type="number" step="0.01" min="0" name="estimatedLoss" class="input" value="<s:property value='estimatedLoss'/>" />
            </div>
        </div>
        <div class="form-row">
            <div class="field grow">
                <label>Location</label>
                <input type="text" name="incidentLocation" class="input" value="<s:property value='incidentLocation'/>" />
            </div>
            <div class="field"><label>City</label><input type="text" name="city" class="input" value="<s:property value='city'/>" /></div>
            <div class="field"><label>State</label><input type="text" name="state" class="input" value="<s:property value='state'/>" /></div>
            <div class="field"><label>PIN</label><input type="text" name="pinCode" class="input" value="<s:property value='pinCode'/>" /></div>
        </div>
        <div class="field">
            <label>Description</label>
            <textarea name="description" class="input" rows="3" placeholder="Describe what happened..."><s:property value="description"/></textarea>
        </div>
    </div>

    <div class="form-section">
        <h3>Additional Details <span class="muted">(fill in what applies to your claim type)</span></h3>
        <div class="form-row">
            <div class="field"><label>Vehicle Registration No.</label><input type="text" name="vehicleRegNo" class="input" value="<s:property value='vehicleRegNo'/>" /></div>
            <div class="field"><label>FIR Number</label><input type="text" name="firNumber" class="input" value="<s:property value='firNumber'/>" /></div>
            <div class="field"><label>Police Station</label><input type="text" name="policeStation" class="input" value="<s:property value='policeStation'/>" /></div>
        </div>
        <div class="form-row">
            <div class="field"><label>Hospital Name</label><input type="text" name="hospitalName" class="input" value="<s:property value='hospitalName'/>" /></div>
            <div class="field"><label>Workshop / Garage Name</label><input type="text" name="workshopName" class="input" value="<s:property value='workshopName'/>" /></div>
            <div class="field"><label>Third Party</label><input type="text" name="thirdParty" class="input" value="<s:property value='thirdParty'/>" /></div>
        </div>
    </div>

    <div class="form-actions">
        <button type="submit" name="mode" value="draft" class="btn btn-light">Save as Draft</button>
        <button type="submit" name="mode" value="submit" class="btn btn-primary">Submit Claim</button>
    </div>
</form>
</s:else>
