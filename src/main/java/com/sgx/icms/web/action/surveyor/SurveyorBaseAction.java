package com.sgx.icms.web.action.surveyor;

import com.sgx.icms.service.SurveyorService;
import com.sgx.icms.web.action.BaseAction;

/** Shared base for surveyor-portal actions. */
public abstract class SurveyorBaseAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    protected final transient SurveyorService surveyorService = new SurveyorService();

    protected String assessUrl(long claimId) {
        return "/surveyor/assess?id=" + claimId;
    }
}
