package com.sgx.icms.domain;

import java.time.LocalDateTime;

/** Mirrors the {@code communications} table (claim message thread). */
public class Communication {

    private long id;
    private long claimId;
    private Long senderId;
    private String senderName;
    private String channel;     // SMS/EMAIL/CALL/MESSAGE
    private String content;
    private LocalDateTime createdAt;
    private String claimNo;     // joined (recent-feed views)

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClaimId() { return claimId; }
    public void setClaimId(long claimId) { this.claimId = claimId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getClaimNo() { return claimNo; }
    public void setClaimNo(String claimNo) { this.claimNo = claimNo; }
}
