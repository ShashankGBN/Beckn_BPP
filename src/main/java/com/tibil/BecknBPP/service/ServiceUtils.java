package com.tibil.BecknBPP.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibil.BecknBPP.dto.Context;
import com.tibil.BecknBPP.dto.Context.ActionEnum;

@Component
public class ServiceUtils {
	
	private ObjectMapper objectMapper;
	
	
	@Autowired
	public ServiceUtils(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
		this.objectMapper.setSerializationInclusion(Include.NON_NULL);
	}

	public Context getContext() {
		
		Context context = new Context();
		context.setAction(ActionEnum.SEARCH);
		context.setBapId("");
		context.setBapUri("http://localhost:8080");
		context.setCity("BLR");
		context.setCountry("IND");
		context.setDomain("dsep:jobs");
		context.setTransactionId(UUID.randomUUID().toString());
		context.setMessageId(UUID.randomUUID().toString());
		context.setTimestamp(OffsetDateTime.now());
		
		return context;
		
	}
	
	public String getSerialisedData(Object body) {
		
		try {
			return objectMapper.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public <T> T deserialiseData(String body, Class<T> classType) {
		
		try {
			return objectMapper.readValue(body, classType);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}