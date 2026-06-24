package com.sgx.icms.web.action.customer;

/** Customer withdraws one of their own claims (if its status permits). */
public class WithdrawAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    private long claimId;
    private String redirectUrl;

    @Override
    public String execute() {
        redirectUrl = "/customer/claim?id=" + claimId;
        if (policyholder() == null) {
            return "missing";
        }
        try {
            boolean ok = claimService.withdraw(currentUser(), policyholder().getId(), claimId, clientIp());
            setFlash(ok ? "success" : "error",
                    ok ? "Claim withdrawn." : "Claim not found.");
        } catch (IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public void setClaimId(long claimId) { this.claimId = claimId; }
    public long getClaimId() { return claimId; }
    public String getRedirectUrl() { return redirectUrl; }
}
