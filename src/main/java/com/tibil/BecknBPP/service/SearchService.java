package com.tibil.BecknBPP.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.tibil.BecknBPP.dao.ServiceRequestFlowRepository;
import com.tibil.BecknBPP.dao.ServiceRequestRepository;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.OnSearchBody;
import com.tibil.BecknBPP.dto.SearchBody;
import com.tibil.BecknBPP.model.ServiceRequest;
import com.tibil.BecknBPP.model.ServiceRequestFlow;

@Component
public class SearchService implements ProcessInternalRequestService {
	
	private ServiceRequestRepository serviceRequestRepository;
	private ServiceRequestFlowRepository serviceRequestFlowRepository;
	private final ObjectMapper objectMapper;
	
	@Autowired
	public SearchService(ServiceRequestRepository serviceRequestRepository,
			ServiceRequestFlowRepository serviceRequestFlowRepository, ObjectMapper objectMapper) {
		super();
		this.serviceRequestRepository = serviceRequestRepository;
		this.serviceRequestFlowRepository = serviceRequestFlowRepository;
		this.objectMapper = objectMapper;
		this.objectMapper.setSerializationInclusion(Include.NON_NULL);
	}



	@Override
	public void processInternalRequest(Object requestBody) {
		
		SearchBody inputBody = (SearchBody)requestBody;
		ObjectWriter ow = objectMapper.writer();
		
		ServiceRequest serviceRequest;
		try {
			serviceRequest = new ServiceRequest(
					inputBody.getContext().getTransactionId(),
					inputBody.getContext().getMessageId(),
					inputBody.getContext().getDomain(),
					inputBody.getContext().getCity(), 
					ow.writeValueAsString(inputBody),
					inputBody.getContext().getBapId()
					);
			serviceRequestRepository.save(serviceRequest);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ServiceRequestFlow flow = new ServiceRequestFlow();
		flow.setTransactionId(inputBody.getContext().getTransactionId());
		flow.setBapId(inputBody.getContext().getBapId());
		flow.setMessageId(inputBody.getContext().getMessageId());
		flow.setCreatedAt(OffsetDateTime.now());
		flow.setAction(inputBody.getContext().getAction().toString());
		try {
			flow.setData(ow.writeValueAsString(inputBody));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		serviceRequestFlowRepository.save(flow);
		
	}
	

	@Override
	@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
	public void processCallbackRequest() {
		
		OffsetDateTime end = OffsetDateTime.now();
		OffsetDateTime start = end.minusSeconds(Constants.SCHEDULE_DELAY / 1000);
		
		System.out.println(end);
		
		List<ServiceRequestFlow> flows = serviceRequestFlowRepository.getByCreatedAtBetween(start, end);
		for(ServiceRequestFlow f: flows) {
			System.out.println("Response from DB");
			System.out.println(f.toString());
			
			String onSearchJson = """
					{
	    "context": {
	        "domain": "dsep:career",
	        "country": "IND",
	        "city": "BLR",
	        "action": "on_search",
	        "core_version": "1.0.0",
	        "bap_id": "Ti-Tej-Ace",
	        "bap_uri": "http://localhost:5000",
	        "transaction_id": "790bab31-eec3-4b93-aa2d-b2a97ca934b3",
	        "message_id": "889ef92b-240a-4779-bd40-f866c25f396c",
	        "timestamp": "2022-10-11T09:55:41.161Z"
	    },
	    "message": {
	        "catalog": {
	            "descriptor": {
	                "name": "Catalog"
	            },
	            "providers": [
	                {
	                    "id": "Provider 1",
	                    "descriptor": {
	                        "name": "Provider 1"
	                    },
	                    "categories": [
	                        {
	                            "id": "Chemical-Engineering",
	                            "parent_category_id": "Engineer 101",
	                            "descriptor": {
	                                "name": "Engineer"
	                            }
	                        }
	                    ],
	                    "items": [
	                    	{
	                            "id": "id001",
	                            "parent_item_id": "id001",
	                            "descriptor": {
	                                "name": "Basics of Chemical Engineering",
	                                "long_desc": "What a chemical engineer does? how to become a chemical engineer",
	                                "images": [                   
	                                    "https://alltogether.swe.org/wp-content/uploads/2022/08/Day-in-the-Life.png"
	                                ]
	                            },
	                            "price": {
	                                "currency": "INR",
	                                "value": "10"
	                            },
	                            "category_id": "Chemical-Engineering",
	                            "recommended": false,
	                            "time": {
	                                "label": "Course Schedule",
	                                "duration": "P1W",
	                                "range": {
	                                    "start": "2023-02-26T18:29:00.000000Z",
	                                    "end": "2023-03-04T18:29:00.000000Z"
	                                }
	                            },
	                            "rating": "3",
	                            "tags": {
	                                "content_type": "Book"
	                            },
	                            "rateable": false
	                        }
	                    ]
	                }
	            ]
	        }
	    }
	}
					""";
					
			try {
				OnSearchBody body = objectMapper.readValue(onSearchJson, OnSearchBody.class);
				ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_search", body, InlineResponse2001.class);
				
				ServiceRequestFlow flow = new ServiceRequestFlow();
				flow.setTransactionId(body.getContext().getTransactionId());
				flow.setBapId(body.getContext().getBapId());
				flow.setMessageId(body.getContext().getMessageId());
				flow.setCreatedAt(OffsetDateTime.now());
				flow.setData(objectMapper.writeValueAsString(body));
				flow.setAction(body.getContext().getAction().toString());
				flow.setAck(objectMapper.writeValueAsString(response.getBody()));
				
				serviceRequestFlowRepository.save(flow);
				
				System.out.println(response);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
		}
		
	}

}
