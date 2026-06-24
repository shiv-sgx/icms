package com.sgx.icms.web.action.agent;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.dao.CommunicationDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.Communication;

/** Communication Center: most recent messages across all claims. */
public class AgentCommunicationsAction extends AgentBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient CommunicationDao communicationDao = new CommunicationDao();

    private transient List<Communication> messages = Collections.emptyList();

    @Override
    public String execute() {
        messages = Db.withConnection(conn -> communicationDao.findRecent(conn, 30));
        return SUCCESS;
    }

    public List<Communication> getMessages() { return messages; }
}
