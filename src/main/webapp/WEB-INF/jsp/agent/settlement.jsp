<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:set var="claim" value="bundle.claim" />
<s:set var="settlement" value="bundle.settlement" />

<div class="page-head with-action">
    <div>
        <h1 class="page-title">Settlement — <s:property value="#claim.claimNo" /></h1>
        <p class="page-sub"><s:property value="#claim.claimantName" /> &middot; <s:property value="#claim.claimType" /> &middot; Policy <s:property value="#claim.policyNo" /></p>
    </div>
    <s:a action="claim" namespace="/agent" cssClass="btn btn-light"><s:param name="id" value="#claim.id" />&laquo; Back to claim</s:a>
</div>

<s:if test="flashMessage != null">
    <div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div>
</s:if>

<s:if test="settlement != null">
    <!-- Payment status tracker -->
    <div class="panel">
        <div class="panel-head">Payment Status Tracker</div>
        <div class="panel-body">
            <s:set var="curIdx" value="@com.sgx.icms.domain.Settlement@TRACKER.indexOf(settlement.status)" />
            <ol class="timeline">
                <s:iterator value="@com.sgx.icms.domain.Settlement@TRACKER" status="ist">
                    <li class="tl-node <s:if test='#ist.index < #curIdx'>tl-done</s:if><s:elseif test='#ist.index == #curIdx'>tl-current</s:elseif><s:else>tl-pending</s:else>">
                        <span class="tl-dot"></span><span class="tl-label"><s:property value="top.replace('_',' ')" /></span>
                    </li>
                </s:iterator>
            </ol>
        </div>
    </div>

    <div class="grid-2">
        <div class="panel">
            <div class="panel-head">Settlement Summary</div>
            <div class="panel-body">
                <dl class="kv">
                    <dt>Final Amount</dt><dd class="big">&#8377; <s:property value="settlement.finalAmount" /></dd>
                    <dt>Status</dt><dd><span class="pill pill-info"><s:property value="settlement.status" /></span></dd>
                    <dt>Method</dt><dd><s:property value="settlement.paymentMethod" /></dd>
                    <dt>Justification</dt><dd><s:property value="settlement.justification" /></dd>
                </dl>
            </div>
        </div>
        <div class="panel">
            <div class="panel-head">Payment Details</div>
            <div class="panel-body">
                <dl class="kv">
                    <dt>Account Holder</dt><dd><s:property value="settlement.accountHolder" /></dd>
                    <dt>Bank</dt><dd><s:property value="settlement.bankName" /></dd>
                    <dt>Account No.</dt><dd><s:property value="settlement.accountNumber" /></dd>
                    <dt>IFSC</dt><dd><s:property value="settlement.ifscCode" /></dd>
                </dl>
                <s:if test="settlement.status != 'CLOSED'">
                    <s:form action="advanceSettlement" namespace="/agent" method="post">
                        <s:hidden name="id" value="%{#claim.id}" />
                        <button type="submit" class="btn btn-primary">Advance Payment Status &raquo;</button>
                    </s:form>
                </s:if>
            </div>
        </div>
    </div>
</s:if>
<s:elseif test="#claim.status == 'APPROVED'">
    <!-- Authorize settlement form -->
    <div class="panel">
        <div class="panel-head">Authorise Settlement</div>
        <div class="panel-body">
            <form action="<s:url action='processSettlement' namespace='/agent'/>" method="post" class="form-card">
                <s:hidden name="id" value="%{#claim.id}" />
                <div class="form-row">
                    <div class="field"><label>Settlement Amount (&#8377;)</label><input type="number" step="0.01" min="0" name="amount" class="input" value="<s:property value='suggestedAmount'/>" required /></div>
                    <div class="field">
                        <label>Payment Method</label>
                        <select name="paymentMethod" class="input">
                            <option value="NEFT">NEFT / Bank Transfer</option>
                            <option value="CHEQUE">Cheque</option>
                            <option value="DEMAND_DRAFT">Demand Draft</option>
                            <option value="DIRECT_TO_WORKSHOP">Direct to Workshop</option>
                        </select>
                    </div>
                </div>
                <div class="form-row">
                    <div class="field"><label>Account Holder</label><input type="text" name="accountHolder" class="input" value="<s:property value='#claim.claimantName'/>" /></div>
                    <div class="field"><label>Bank Name</label><input type="text" name="bankName" class="input" /></div>
                </div>
                <div class="form-row">
                    <div class="field"><label>Account Number</label><input type="text" name="accountNumber" class="input" /></div>
                    <div class="field"><label>IFSC Code</label><input type="text" name="ifscCode" class="input" /></div>
                </div>
                <div class="field"><label>Justification</label><input type="text" name="justification" class="input" placeholder="Basis for the settlement amount" /></div>
                <div class="form-actions"><button type="submit" class="btn btn-primary">Authorise Settlement</button></div>
            </form>
        </div>
    </div>
</s:elseif>
<s:else>
    <div class="alert alert-error">This claim is not yet approved for settlement (current status: <s:property value="#claim.statusLabel" />).</div>
</s:else>
