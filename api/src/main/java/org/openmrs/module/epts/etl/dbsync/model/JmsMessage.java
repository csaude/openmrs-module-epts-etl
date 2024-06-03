/*
 * Copyright (C) Amiyul LLC - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holder.
 *
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holder. If you encounter this file and do not have
 * permission, please contact the copyright holder and delete this file.
 */
package org.openmrs.module.epts.etl.dbsync.model;

public class JmsMessage extends AbstractEntity {
	private static final long serialVersionUID = 2368748229055720147L;

	public enum MessageType {
		
		SYNC,
		
		RECONCILE
		
	}
	
	private Long id;
	
	private String siteId;
	
	private MessageType type;
	
	private byte[] body;
	
	private String messageId;
	
	private String syncVersion;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getSiteId() {
		return siteId;
	}
	
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	public MessageType getType() {
		return type;
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}
	
	public byte[] getBody() {
		return body;
	}
	
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getSyncVersion() {
		return syncVersion;
	}
	
	public void setSyncVersion(String syncVersion) {
		this.syncVersion = syncVersion;
	}
	
}
