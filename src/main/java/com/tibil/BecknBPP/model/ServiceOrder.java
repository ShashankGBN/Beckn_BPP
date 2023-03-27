package com.tibil.BecknBPP.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ServiceOrder extends BaseModel {

	@Id
	@GeneratedValue
	private long id;
	private String bapId;
	private String bppId;
	private String bapOrderId;
	private String orderState;
	private String orderStateLog;
	private double price;
	private String currency;
	private String paymentId;
	private boolean isSelected;
	private boolean ischeckedOut;
	private boolean isConfirmed;
	private boolean isTracked;
	private boolean isCanceled;
	private boolean isUpdated;
	private String canceledBy;
	private String cancellationReason;

	@ManyToOne(fetch = FetchType.LAZY)
	private ServiceRequest serviceRequest;

	public String getBapId() {
		return bapId;
	}

	public String getBapOrderId() {
		return bapOrderId;
	}

	public String getBppId() {
		return bppId;
	}

	public String getCanceledBy() {
		return canceledBy;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public String getCurrency() {
		return currency;
	}

	public long getId() {
		return id;
	}

	public String getOrderState() {
		return orderState;
	}

	public String getOrderStateLog() {
		return orderStateLog;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public double getPrice() {
		return price;
	}

	public ServiceRequest getServiceRequest() {
		return serviceRequest;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public boolean isConfirmed() {
		return isConfirmed;
	}

	public boolean isIscheckedOut() {
		return ischeckedOut;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public boolean isTracked() {
		return isTracked;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	public void setBapId(String bapId) {
		this.bapId = bapId;
	}

	public void setBapOrderId(String bapOrderId) {
		this.bapOrderId = bapOrderId;
	}

	public void setBppId(String bppId) {
		this.bppId = bppId;
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public void setCanceledBy(String canceledBy) {
		this.canceledBy = canceledBy;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

	public void setConfirmed(boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setIscheckedOut(boolean ischeckedOut) {
		this.ischeckedOut = ischeckedOut;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}

	public void setOrderStateLog(String orderStateLog) {
		this.orderStateLog = orderStateLog;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public void setServiceRequest(ServiceRequest serviceRequest) {
		this.serviceRequest = serviceRequest;
	}

	public void setTracked(boolean isTracked) {
		this.isTracked = isTracked;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

}
