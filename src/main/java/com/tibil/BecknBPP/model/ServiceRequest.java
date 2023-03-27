package com.tibil.BecknBPP.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class ServiceRequest extends BaseModel {

	@Id
	private String transactionId;
	private String messageId;
	private String domain;
	private String city;

	@Column(columnDefinition = "TEXT")
	private String data;

	private String bapId;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceRequest")
	private List<ServiceRequestFlow> serviceRequestFlow;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceRequest")
	private List<ServiceOrder> serviceOrder;

	public ServiceRequest() {
		super();
	}

	public ServiceRequest(String transactionId, String messageId, String domain, String city, String data,
			String bapId) {
		super();
		this.transactionId = transactionId;
		this.messageId = messageId;
		this.domain = domain;
		this.city = city;
		this.data = data;
		this.bapId = bapId;
	}

	public String getBapId() {
		return bapId;
	}

	public String getCity() {
		return city;
	}

	public String getData() {
		return data;
	}

	public String getDomain() {
		return domain;
	}

	public String getMessageId() {
		return messageId;
	}

	public List<ServiceOrder> getServiceOrder() {
		return serviceOrder;
	}

	public List<ServiceRequestFlow> getServiceRequestFlow() {
		return serviceRequestFlow;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setBapId(String bapId) {
		this.bapId = bapId;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void setServiceOrder(List<ServiceOrder> serviceOrder) {
		this.serviceOrder = serviceOrder;
	}

	public void setServiceRequestFlow(List<ServiceRequestFlow> serviceRequest) {
		this.serviceRequestFlow = serviceRequest;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
}
