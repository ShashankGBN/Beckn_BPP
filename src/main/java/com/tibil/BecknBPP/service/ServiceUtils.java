package com.tibil.BecknBPP.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tibil.BecknBPP.dto.Context;
import com.tibil.BecknBPP.dto.Context.ActionEnum;

public class ServiceUtils {
	
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

}