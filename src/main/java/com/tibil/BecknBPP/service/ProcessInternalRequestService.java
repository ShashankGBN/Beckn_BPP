package com.tibil.BecknBPP.service;

import org.springframework.web.client.RestTemplate;

public interface ProcessInternalRequestService {
	
	public static final RestTemplate restTemplate = new RestTemplate();
	
	public void processInternalRequest(Object requestBody);
	public void processCallbackRequest();

}
