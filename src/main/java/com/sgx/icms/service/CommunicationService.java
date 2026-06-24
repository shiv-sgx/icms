package com.sgx.icms.service;

import java.sql.Connection;
import java.util.List;

import com.sgx.icms.dao.CommunicationDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.Communication;
import com.sgx.icms.web.support.SessionUser;

/** Claim message thread: read history, post a message, post system messages. */
public class CommunicationService {

    private final CommunicationDao dao = new CommunicationDao();

    public List<Communication> forClaim(long claimId) {
        return Db.withConnection(conn -> dao.findByClaim(conn, claimId));
    }

    /** A user-authored message (own transaction). */
    public void postMessage(SessionUser actor, long claimId, String content) {
        Communication c = new Communication();
        c.setClaimId(claimId);
        c.setSenderId(actor.getId());
        c.setSenderName(actor.getFullName());
        c.setChannel("MESSAGE");
        c.setContent(content);
        Db.inTransaction(conn -> {
            dao.insert(conn, c);
            return null;
        });
    }

    /** A system-generated message inside the caller's transaction (e.g. acknowledgement). */
    public void system(Connection conn, long claimId, String senderName, String content) {
        Communication c = new Communication();
        c.setClaimId(claimId);
        c.setSenderName(senderName);
        c.setChannel("MESSAGE");
        c.setContent(content);
        dao.insert(conn, c);
    }
}
