<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head with-action">
    <div>
        <h1 class="page-title">Claim Assessment — <s:property value="claim.claimNo" /></h1>
        <p class="page-sub"><s:property value="claim.claimType" /><s:if test="claim.claimSubtype != null"> · <s:property value="claim.claimSubtype" /></s:if>
            &middot; <s:property value="claim.claimantName" /> &middot; <span class="pill <s:property value='claim.statusPill'/>"><s:property value="claim.statusLabel" /></span></p>
    </div>
    <s:a action="dashboard" namespace="/surveyor" cssClass="btn btn-light">&laquo; Back</s:a>
</div>

<s:if test="flashMessage != null">
    <div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div>
</s:if>

<div class="panel">
    <div class="panel-head">Claim Summary</div>
    <div class="panel-body">
        <dl class="kv">
            <dt>Incident</dt><dd><s:property value="claim.incidentDate" /> <s:property value="claim.incidentTime" /></dd>
            <dt>Location</dt><dd><s:property value="claim.incidentLocation" /> <s:property value="claim.city" /> <s:property value="claim.state" /></dd>
            <dt>Estimated Loss</dt><dd>&#8377; <s:property value="claim.estimatedLoss" /></dd>
        </dl>
        <p class="desc"><s:property value="claim.description" /></p>
    </div>
</div>

<s:if test="assessment != null && assessment.status == 'SUBMITTED'">
    <!-- Read-only submitted assessment -->
    <div class="panel">
        <div class="panel-head">Submitted Assessment <span class="muted">Ref: <s:property value="assessment.reportRefNo" /></span></div>
        <div class="panel-body">
            <div class="card-grid">
                <div class="stat-card"><div class="stat-label">Gross Assessed</div><div class="stat-value">&#8377; <s:property value="assessment.grossAssessed" /></div></div>
                <div class="stat-card"><div class="stat-label">Depreciation</div><div class="stat-value">&#8377; <s:property value="assessment.depreciationAmt" /></div></div>
                <div class="stat-card"><div class="stat-label">Salvage</div><div class="stat-value">&#8377; <s:property value="assessment.salvageValue" /></div></div>
                <div class="stat-card"><div class="stat-label">Net Payable</div><div class="stat-value">&#8377; <s:property value="assessment.netPayable" /></div></div>
            </div>
            <p><strong>Recommendation:</strong> <s:property value="assessment.recommendation" /></p>
            <p class="muted"><s:property value="assessment.siteObservations" /></p>
            <s:if test="!components.isEmpty()">
                <table class="table">
                    <thead><tr><th>Component</th><th>Severity</th><th>Repair Cost</th><th>Replace?</th></tr></thead>
                    <tbody>
                    <s:iterator value="components">
                        <tr><td><s:property value="component" /></td><td><s:property value="severity" /></td><td>&#8377; <s:property value="repairCost" /></td><td><s:if test="replaceFlag">Yes</s:if><s:else>No</s:else></td></tr>
                    </s:iterator>
                    </tbody>
                </table>
            </s:if>
        </div>
    </div>
</s:if>
<s:else>
    <!-- Assessment form -->
    <form action="<s:url action='submitAssessment' namespace='/surveyor'/>" method="post" class="form-card" id="assessForm">
        <s:hidden name="id" value="%{id}" />

        <div class="form-section">
            <h3>Site Visit</h3>
            <div class="form-row">
                <div class="field"><label>Visit Date</label><input type="date" name="visitDate" class="input" /></div>
                <div class="field"><label>Visit Time</label><input type="time" name="visitTime" class="input" /></div>
                <div class="field"><label>Report Reference No.</label><input type="text" name="reportRefNo" class="input" placeholder="SRV-2026-..." /></div>
            </div>
            <div class="field"><label>Site Observations</label><textarea name="siteObservations" class="input" rows="2"></textarea></div>
        </div>

        <div class="form-section">
            <h3>Component Breakdown</h3>
            <table class="table comp-table" id="compTable">
                <thead><tr><th>Component</th><th>Severity</th><th>Repair Cost (&#8377;)</th><th>Replace?</th><th></th></tr></thead>
                <tbody id="compBody">
                    <tr class="comp-row">
                        <td><input type="text" name="compName" class="input" placeholder="e.g. Front Bumper" /></td>
                        <td><select name="compSeverity" class="input"><option>MINOR</option><option selected>MODERATE</option><option>SEVERE</option></select></td>
                        <td><input type="number" step="0.01" min="0" name="compCost" class="input comp-cost" value="0" /></td>
                        <td><select name="compReplace" class="input"><option value="false">No</option><option value="true">Yes</option></select></td>
                        <td><button type="button" class="btn btn-light btn-sm remove-comp">&times;</button></td>
                    </tr>
                </tbody>
            </table>
            <button type="button" class="btn btn-light btn-sm" id="addComp">+ Add Component</button>
        </div>

        <div class="form-section">
            <h3>Settlement Calculation</h3>
            <div class="form-row">
                <div class="field"><label>Policy Deductible (&#8377;)</label><input type="number" step="0.01" min="0" name="policyDeductible" id="deductible" class="input" value="0" /></div>
                <div class="field"><label>Depreciation (%)</label><input type="number" step="0.01" min="0" name="depreciationPct" id="deprPct" class="input" value="0" /></div>
                <div class="field"><label>Salvage Value (&#8377;)</label><input type="number" step="0.01" min="0" name="salvageValue" id="salvage" class="input" value="0" /></div>
            </div>
            <div class="card-grid calc-out">
                <div class="stat-card"><div class="stat-label">Gross Assessed</div><div class="stat-value">&#8377; <span id="grossOut">0</span></div></div>
                <div class="stat-card"><div class="stat-label">Depreciation Amt</div><div class="stat-value">&#8377; <span id="deprOut">0</span></div></div>
                <div class="stat-card"><div class="stat-label">Net Payable</div><div class="stat-value">&#8377; <span id="netOut">0</span></div></div>
            </div>
        </div>

        <div class="form-section">
            <h3>Recommendation</h3>
            <div class="form-row">
                <div class="field">
                    <label>Recommendation</label>
                    <select name="recommendation" class="input">
                        <option value="APPROVE_FULL">Approve Full</option>
                        <option value="PARTIAL_APPROVE" selected>Partial Approve</option>
                        <option value="REJECT">Reject</option>
                    </select>
                </div>
                <div class="field grow"><label>Remarks</label><input type="text" name="remarks" class="input" /></div>
            </div>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary" data-confirm="Submit this assessment? It cannot be edited afterwards.">Submit Assessment</button>
        </div>
    </form>
</s:else>

<!-- Documents + upload -->
<div class="panel">
    <div class="panel-head">Survey Documents</div>
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Document</th><th>Status</th></tr></thead>
            <tbody>
            <s:iterator value="documents">
                <tr><td><s:property value="docType" /><s:if test="fileName != null"> — <span class="muted small"><s:property value="fileName" /></span></s:if></td>
                    <td><span class="pill <s:if test='uploaded'>pill-ok</s:if><s:else>pill-warn</s:else>"><s:property value="uploadStatus" /></span></td></tr>
            </s:iterator>
            <s:if test="documents.isEmpty()"><tr><td colspan="2" class="empty">No documents.</td></tr></s:if>
            </tbody>
        </table>
    </div>
    <div class="panel-foot">
        <s:form action="uploadReport" namespace="/surveyor" method="post" enctype="multipart/form-data" cssClass="upload-form">
            <s:hidden name="id" value="%{id}" />
            <input type="text" name="docType" class="input" placeholder="e.g. Survey Report, Site Photo" required />
            <input type="file" name="upload" class="input" required />
            <button type="submit" class="btn btn-primary">Upload</button>
        </s:form>
    </div>
</div>
