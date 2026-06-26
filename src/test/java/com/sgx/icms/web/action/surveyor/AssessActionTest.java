package com.sgx.icms.web.action.surveyor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.DocumentService;
import com.sgx.icms.service.SurveyorService;
import com.sgx.icms.web.support.SessionUser;

class AssessActionTest {

    private static final SessionUser SURVEYOR = new SessionUser(11, "sam", "Sam Surveyor", "s@x.com", "SURVEYOR", "HQ");

    private static Map<String, Object> sessionWith(SessionUser u) {
        Map<String, Object> session = new HashMap<>();
        session.put(SessionUser.SESSION_KEY, u);
        return session;
    }

    @Test
    void show_claimAssigned_returnsSuccess() {
        Claim claim = new Claim();
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            SurveyorService svc = surveyorMock.constructed().get(0);
            when(svc.getAssignedClaim(11L, 3L)).thenReturn(claim);
            when(docMock.constructed().get(0).forClaim(3L)).thenReturn(Collections.emptyList());

            assertEquals(Action.SUCCESS, action.show());
            assertEquals(claim, action.getClaim());
        }
    }

    @Test
    void show_claimNotAssigned_returnsMissing() {
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            SurveyorService svc = surveyorMock.constructed().get(0);
            when(svc.getAssignedClaim(11L, 3L)).thenReturn(null);

            assertEquals("missing", action.show());
        }
    }

    @Test
    void submit_happyPath_buildsComponentsAndAudits() {
        Claim claim = new Claim();
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            SurveyorService svc = surveyorMock.constructed().get(0);
            when(svc.getAssignedClaim(11L, 3L)).thenReturn(claim);

            action.setVisitDate("2026-01-10");
            action.setVisitTime("10:30");
            action.setCompName(new String[] {"Bumper", ""});
            action.setCompCost(new String[] {"1500"});
            action.setCompReplace(new String[] {"true"});

            assertEquals(Action.SUCCESS, action.submit());
            assertEquals("/surveyor/assess?id=3", action.getRedirectUrl());
            assertEquals("success", action.getFlashType());
            verify(svc).submitAssessment(any(), eq(3L), any(),
                    argThat((java.util.List<com.sgx.icms.domain.AssessmentComponent> list) -> list.size() == 1), any());
            verify(auditMock.constructed().get(0)).success(any(), eq("ASSESSMENT_FORM"), eq("claim:3"), any());
        }
    }

    @Test
    void submit_claimNotAssigned_returnsMissing() {
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            when(surveyorMock.constructed().get(0).getAssignedClaim(11L, 3L)).thenReturn(null);

            assertEquals("missing", action.submit());
            assertEquals("error", action.getFlashType());
        }
    }

    @Test
    void submit_invalidVisitDate_setsErrorFlashAndReturnsSuccess() {
        Claim claim = new Claim();
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            when(surveyorMock.constructed().get(0).getAssignedClaim(11L, 3L)).thenReturn(claim);
            action.setVisitDate("not-a-date");

            assertEquals(Action.SUCCESS, action.submit());
            assertEquals("error", action.getFlashType());
            assertEquals("Invalid visit date/time.", action.getFlashMessage());
        }
    }

    @Test
    void submit_serviceRejects_setsErrorFlash() {
        Claim claim = new Claim();
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class,
                (mock, ctx) -> doThrow(new IllegalArgumentException("Add at least one component (or a gross assessed amount)."))
                        .when(mock).submitAssessment(any(), anyLong(), any(), any(), any()));
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            when(surveyorMock.constructed().get(0).getAssignedClaim(11L, 3L)).thenReturn(claim);

            assertEquals(Action.SUCCESS, action.submit());
            assertEquals("error", action.getFlashType());
        }
    }

    @Test
    void upload_claimNotAssigned_returnsMissing() {
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            when(surveyorMock.constructed().get(0).getAssignedClaim(11L, 3L)).thenReturn(null);

            assertEquals("missing", action.upload());
        }
    }

    @Test
    void upload_success_setsSuccessFlashAndAudits() {
        Claim claim = new Claim();
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            action.setDocType("SURVEY_REPORT");
            when(surveyorMock.constructed().get(0).getAssignedClaim(11L, 3L)).thenReturn(claim);

            assertEquals(Action.SUCCESS, action.upload());
            assertEquals("success", action.getFlashType());
            verify(auditMock.constructed().get(0)).success(any(), eq("SURVEY_REPORT_UPLOAD"),
                    eq("claim:3 / SURVEY_REPORT"), any());
        }
    }

    @Test
    void upload_serviceRejects_setsErrorFlash() {
        Claim claim = new Claim();
        try (MockedConstruction<SurveyorService> surveyorMock = mockConstruction(SurveyorService.class);
             MockedConstruction<DocumentService> docMock = mockConstruction(DocumentService.class,
                     (mock, ctx) -> doThrow(new IllegalArgumentException("Please choose a file to upload."))
                             .when(mock).upload(anyLong(), any(), any(), any()));
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class)) {

            AssessAction action = new AssessAction();
            action.setSession(sessionWith(SURVEYOR));
            action.setId(3);
            when(surveyorMock.constructed().get(0).getAssignedClaim(11L, 3L)).thenReturn(claim);

            assertEquals(Action.SUCCESS, action.upload());
            assertEquals("error", action.getFlashType());
        }
    }
}
