package com.sgx.icms.web.action.agent;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.User;
import com.sgx.icms.service.AssignmentService;
import com.sgx.icms.web.support.ClaimBundle;

/** Agent claim detail: full bundle + surveyor picker + flash. */
public class AgentClaimDetailAction extends AgentBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient AssignmentService assignmentService = new AssignmentService();

    private long id;
    private transient ClaimBundle bundle;
    private transient List<User> surveyors = Collections.emptyList();
    private String flashMessage;
    private String flashType;

    @Override
    public String execute() {
        bundle = agentClaims.bundle(id);
        if (bundle == null) {
            setFlash("error", "Claim not found.");
            return "missing";
        }
        surveyors = assignmentService.availableSurveyors();
        flashMessage = consumeFlash();
        flashType = consumeFlashType();
        return SUCCESS;
    }

    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    public ClaimBundle getBundle() { return bundle; }
    public List<User> getSurveyors() { return surveyors; }
    public String getFlashMessage() { return flashMessage; }
    public String getFlashType() { return flashType; }
}
