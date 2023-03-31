package com.tibil.BecknBPP.dao.utils;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dao.CandidateRepository;
import com.tibil.BecknBPP.dao.ServiceRequestFlowRepository;
import com.tibil.BecknBPP.dao.ServiceRequestRepository;
import com.tibil.BecknBPP.dto.Context;
import com.tibil.BecknBPP.model.Candidate;
import com.tibil.BecknBPP.model.ServiceRequest;
import com.tibil.BecknBPP.model.ServiceRequestFlow;
import com.tibil.BecknBPP.service.Constants;

@Component
public class DbUtils {

	private ServiceRequestRepository serviceRequestRepository;
	private ServiceRequestFlowRepository serviceRequestFlowRepository;
	private CandidateRepository candidateRepository;

	@Autowired
	public DbUtils(ServiceRequestRepository serviceRequestRepository,
			ServiceRequestFlowRepository serviceRequestFlowRepository, CandidateRepository candidateRepository) {
		super();
		this.serviceRequestRepository = serviceRequestRepository;
		this.serviceRequestFlowRepository = serviceRequestFlowRepository;
		this.candidateRepository = candidateRepository;
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
	
	public List<ServiceRequestFlow> getRequestsOnTimeAndAction(String action){
		
		OffsetDateTime end = OffsetDateTime.now();
		OffsetDateTime start = end.minusSeconds(Constants.SCHEDULE_DELAY / 1000);
		
		return serviceRequestFlowRepository.getByCreatedAtBetween(start, end);
	}
	
	public List<Candidate> getCandidatesBasedOnDesignationAndSkills(String searchDesignation, List<String>skills){
		
		return candidateRepository.findDistinctByDesignations_NameContainingIgnoreCaseAndSkills_NameIn(searchDesignation, skills);
	}

}
