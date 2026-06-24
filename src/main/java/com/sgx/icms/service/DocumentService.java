package com.sgx.icms.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.config.AppConfig;
import com.sgx.icms.dao.DocumentDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.ClaimDocument;

/**
 * Claim document handling: lists, and securely stores uploads OUTSIDE the WAR.
 * Validates extension, sanitises the client filename (never trusts it for the
 * stored path), and matches an existing required-doc slot when possible.
 */
public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    private static final List<String> ALLOWED_EXT =
            Arrays.asList("pdf", "jpg", "jpeg", "png", "gif", "doc", "docx");

    private final DocumentDao dao = new DocumentDao();

    public List<ClaimDocument> forClaim(long claimId) {
        return Db.withConnection(conn -> dao.findByClaim(conn, claimId));
    }

    /**
     * Stores an uploaded file for a claim and records it. Throws
     * {@link IllegalArgumentException} on validation failure (caller surfaces it).
     */
    public void upload(long claimId, String docType, File tmpFile, String clientFileName) {
        if (tmpFile == null) {
            throw new IllegalArgumentException("Please choose a file to upload.");
        }
        if (docType == null || docType.trim().isEmpty()) {
            throw new IllegalArgumentException("Please specify the document type.");
        }
        String safeName = sanitize(clientFileName);
        String ext = extensionOf(safeName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file type. Allowed: " + String.join(", ", ALLOWED_EXT));
        }

        Path destDir = baseDir().resolve("claims").resolve(String.valueOf(claimId));
        String storedName = System.nanoTime() + "_" + safeName;
        Path dest = destDir.resolve(storedName);
        try {
            Files.createDirectories(destDir);
            Files.copy(tmpFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("Failed to store upload for claim {} doc '{}'", claimId, docType, e);
            throw new IllegalStateException("Could not save the uploaded file. Please try again.");
        }

        final String storedPath = dest.toAbsolutePath().toString();
        final String dt = docType.trim();
        Db.inTransaction(conn -> {
            // Fill an existing pending slot of this doc type if present, else add a new row.
            ClaimDocument slot = dao.findByClaim(conn, claimId).stream()
                    .filter(d -> dt.equalsIgnoreCase(d.getDocType()) && !d.isUploaded())
                    .findFirst().orElse(null);
            if (slot != null) {
                dao.markUploaded(conn, slot.getId(), safeName, storedPath);
            } else {
                ClaimDocument d = new ClaimDocument();
                d.setClaimId(claimId);
                d.setDocType(dt);
                d.setFileName(safeName);
                d.setFilePath(storedPath);
                d.setUploadStatus("UPLOADED");
                d.setVerificationStatus("UNDER_REVIEW");
                dao.insert(conn, d);
            }
            return null;
        });
        LOG.info("Stored document '{}' for claim {} at {}", dt, claimId, storedPath);
    }

    private Path baseDir() {
        String configured = AppConfig.get().get("icms.upload.dir", System.getProperty("java.io.tmpdir"));
        // Expand ${user.home} / ${java.io.tmpdir} tokens that Properties won't resolve.
        configured = configured
                .replace("${user.home}", System.getProperty("user.home"))
                .replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"));
        return Paths.get(configured);
    }

    private static String sanitize(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "upload";
        }
        // Strip any directory components and disallow path/control characters.
        String base = new File(name).getName();
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
