package com.tibil.BecknBPP.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ServiceRequestFlow extends BaseModel {

	@Id
	@GeneratedValue
	private long id;
	private String transactionId;
	private String messageId;
	private String bapId;
	private String bppId;
	@Column(columnDefinition = "TEXT")
	private String data;
	private String ack;
	private String action;

	@ManyToOne(fetch = FetchType.LAZY)
	private ServiceRequest serviceRequest;

	public String getAck() {
		return ack;
	}

	public String getBapId() {
		return bapId;
	}

	public String getBppId() {
		return bppId;
	}

	public String getData() {
		return data;
	}

	public long getId() {
		return id;
	}

	public String getMessageId() {
		return messageId;
	}

	public ServiceRequest getServiceRequest() {
		return serviceRequest;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setAck(String ack) {
		this.ack = ack;
	}

	public void setBapId(String bapId) {
		this.bapId = bapId;
	}

	public void setBppId(String bppId) {
		this.bppId = bppId;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setServiceRequest(ServiceRequest serviceRequest) {
		this.serviceRequest = serviceRequest;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

}
