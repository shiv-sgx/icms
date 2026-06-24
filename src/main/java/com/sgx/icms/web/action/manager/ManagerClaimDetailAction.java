package com.sgx.icms.web.action.manager;

import com.sgx.icms.web.support.ClaimBundle;

/** Manager claim detail: full bundle with approval-decision and settlement-override panels. */
public class ManagerClaimDetailAction extends ManagerBaseAction {

    private static final long serialVersionUID = 1L;

    private long id;
    private transient ClaimBundle bundle;
    private String flashMessage;
    private String flashType;

    @Override
    public String execute() {
        bundle = claims.bundle(id);
        if (bundle == null) {
            setFlash("error", "Claim not found.");
            return "missing";
        }
        flashMessage = consumeFlash();
        flashType = consumeFlashType();
        return SUCCESS;
    }

    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    public ClaimBundle getBundle() { return bundle; }
    public String getFlashMessage() { return flashMessage; }
    public String getFlashType() { return flashType; }
}
