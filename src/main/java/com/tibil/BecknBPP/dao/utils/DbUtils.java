package com.tibil.BecknBPP.dao.utils;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dao.CandidateRepository;
import com.tibil.BecknBPP.dao.ServiceOrderRepository;
import com.tibil.BecknBPP.dao.ServiceRequestFlowRepository;
import com.tibil.BecknBPP.dao.ServiceRequestRepository;
import com.tibil.BecknBPP.dto.Context;
import com.tibil.BecknBPP.dto.OnInitMessageOrder;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Quotation;
import com.tibil.BecknBPP.model.Candidate;
import com.tibil.BecknBPP.model.ServiceOrder;
import com.tibil.BecknBPP.model.ServiceRequest;
import com.tibil.BecknBPP.model.ServiceRequestFlow;
import com.tibil.BecknBPP.service.Constants;
import com.tibil.BecknBPP.dto.Order;

@Component
public class DbUtils {

	private ServiceRequestRepository serviceRequestRepository;
	private ServiceRequestFlowRepository serviceRequestFlowRepository;
	private CandidateRepository candidateRepository;
	private ServiceOrderRepository serviceOrderRepository;

	@Autowired
	public DbUtils(ServiceRequestRepository serviceRequestRepository,
			ServiceRequestFlowRepository serviceRequestFlowRepository, CandidateRepository candidateRepository, ServiceOrderRepository serviceOrderRepository) {
		super();
		this.serviceRequestRepository = serviceRequestRepository;
		this.serviceRequestFlowRepository = serviceRequestFlowRepository;
		this.candidateRepository = candidateRepository;
		this.serviceOrderRepository = serviceOrderRepository;
	}

	public ServiceRequest insertServiceRequest(Context context, String data) {

		ServiceRequest serviceRequest = new ServiceRequest(context.getTransactionId(), context.getMessageId(),
				context.getDomain(), context.getCity(), data, context.getBapId());
		return serviceRequestRepository.save(serviceRequest);
	}

	public ServiceRequestFlow insertRequestFlow(Context context, String data, String ack) {

		ServiceRequestFlow flow = new ServiceRequestFlow();
		flow.setTransactionId(context.getTransactionId());
		flow.setBapId(context.getBapId());
		flow.setMessageId(context.getMessageId());
		flow.setCreatedAt(OffsetDateTime.now());
		flow.setData(data);
		flow.setAction(context.getAction().toString());
		flow.setAck(ack);

		return serviceRequestFlowRepository.save(flow);

	}
	
	public ServiceOrder insertServiceOrder(Context context, String data, Price price)
	{
		ServiceOrder serviceOrder = new ServiceOrder();
		Order order = new Order();
		serviceOrder.setCreatedAt(OffsetDateTime.now());
		serviceOrder.setBapId(context.getBapId());
		serviceOrder.setBapOrderId(order.getId());
		serviceOrder.setPaymentId(data);
		serviceOrder.setBppId(context.getBppId());
		serviceOrder.setCurrency(price.getCurrency());
		
		return serviceOrderRepository.save(serviceOrder);
	}
	
	public List<ServiceRequestFlow> getRequestsOnTimeAndAction(String action){
		
		OffsetDateTime end = OffsetDateTime.now();
		OffsetDateTime start = end.minusSeconds(Constants.SCHEDULE_DELAY / 1000);
		
		return serviceRequestFlowRepository.getByCreatedAtBetweenAndAction(start, end, action);
	}
	
	public List<Candidate> getCandidatesBasedOnDesignationAndSkills(String searchDesignation, List<String>skills){
		
		return candidateRepository.findDistinctByDesignations_NameContainingIgnoreCaseAndSkills_NameIn(searchDesignation, skills);
	}

}
