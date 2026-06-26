package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.DocumentDao;
import com.sgx.icms.domain.ClaimDocument;

class DocumentServiceTest {

    @TempDir
    File uploadDir;

    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        System.setProperty("icms.upload.dir", uploadDir.getAbsolutePath());
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("icms.upload.dir");
    }

    private File tempUploadSource(String content) throws Exception {
        File f = new File(uploadDir, "src-" + System.nanoTime() + ".tmp");
        Files.write(f.toPath(), content.getBytes("UTF-8"));
        return f;
    }

    @Test
    void forClaimDelegatesToDocumentDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<DocumentDao> daoMC = mockConstruction(DocumentDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(daoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(Collections.emptyList());

            DocumentService svc = new DocumentService();
            assertEquals(Collections.emptyList(), svc.forClaim(5L));
        }
    }

    @Test
    void uploadThrowsWhenFileIsNull() {
        try (MockedConstruction<DocumentDao> daoMC = mockConstruction(DocumentDao.class)) {
            DocumentService svc = new DocumentService();
            assertThrows(IllegalArgumentException.class, () -> svc.upload(5L, "FIR", null, "report.pdf"));
        }
    }

    @Test
    void uploadThrowsWhenDocTypeIsBlank() throws Exception {
        try (MockedConstruction<DocumentDao> daoMC = mockConstruction(DocumentDao.class)) {
            DocumentService svc = new DocumentService();
            File src = tempUploadSource("hello");
            assertThrows(IllegalArgumentException.class, () -> svc.upload(5L, "  ", src, "report.pdf"));
        }
    }

    @Test
    void uploadThrowsWhenExtensionNotAllowed() throws Exception {
        try (MockedConstruction<DocumentDao> daoMC = mockConstruction(DocumentDao.class)) {
            DocumentService svc = new DocumentService();
            File src = tempUploadSource("hello");
            assertThrows(IllegalArgumentException.class, () -> svc.upload(5L, "FIR", src, "report.exe"));
        }
    }

    @Test
    void uploadFillsExistingPendingSlotWhenDocTypeMatches() throws Exception {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<DocumentDao> daoMC = mockConstruction(DocumentDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            ClaimDocument slot = new ClaimDocument();
            slot.setId(42L);
            slot.setDocType("FIR");
            slot.setUploadStatus("PENDING");
            when(daoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(Arrays.asList(slot));

            DocumentService svc = new DocumentService();
            File src = tempUploadSource("hello world");
            svc.upload(5L, "FIR", src, "report.pdf");

            verify(daoMC.constructed().get(0)).markUploaded(eq(conn), eq(42L), eq("report.pdf"), any());
            verify(daoMC.constructed().get(0), never()).insert(any(), any());
        }
    }

    @Test
    void uploadInsertsNewRowWhenNoMatchingPendingSlot() throws Exception {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<DocumentDao> daoMC = mockConstruction(DocumentDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(daoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(Collections.emptyList());

            DocumentService svc = new DocumentService();
            File src = tempUploadSource("hello world");
            svc.upload(5L, "FIR", src, "report.pdf");

            verify(daoMC.constructed().get(0)).insert(eq(conn), any(ClaimDocument.class));
        }
    }

    @Test
    void uploadSanitizesClientFileNameAndPersistsActualBytes() throws Exception {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<DocumentDao> daoMC = mockConstruction(DocumentDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(daoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(Collections.emptyList());

            DocumentService svc = new DocumentService();
            File src = tempUploadSource("payload");
            svc.upload(5L, "FIR", src, "../../etc/passwd.pdf");

            org.mockito.ArgumentCaptor<ClaimDocument> captor =
                    org.mockito.ArgumentCaptor.forClass(ClaimDocument.class);
            verify(daoMC.constructed().get(0)).insert(eq(conn), captor.capture());
            assertEquals("passwd.pdf", captor.getValue().getFileName());
            assertEquals(5L, captor.getValue().getClaimId());
        }
    }
}
