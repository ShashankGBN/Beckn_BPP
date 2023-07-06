package com.tibil.BecknBPP.service;


import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dao.utils.DbUtils;
import com.tibil.BecknBPP.dto.AllOfonSelectMessageOrderItemsItems;
import com.tibil.BecknBPP.dto.Descriptor;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.Item;
import com.tibil.BecknBPP.dto.OnSelectBody;
import com.tibil.BecknBPP.dto.OnSelectMessage;
import com.tibil.BecknBPP.dto.OnSelectMessageOrder;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Provider;
import com.tibil.BecknBPP.dto.Quotation;
import com.tibil.BecknBPP.dto.QuotationBreakup;
import com.tibil.BecknBPP.dto.SelectBody;
import com.tibil.BecknBPP.dto.Context.ActionEnum;
import com.tibil.BecknBPP.model.ServiceRequestFlow;

@Component
public class SelectService implements ProcessInternalRequestService {

	private ServiceUtils utils;
	private DbUtils dbUtils;

	@Autowired
	public SelectService(ServiceUtils utils, DbUtils dbUtils) {
		super();
		this.utils = utils;
		this.dbUtils = dbUtils;
	}

	@Override
	public void processInternalRequest(Object requestBody) {

		SelectBody inputBody = (SelectBody) requestBody;

		String selectRequestSerialized = utils.getSerialisedData(inputBody);
		dbUtils.insertServiceRequest(inputBody.getContext(), selectRequestSerialized);

		dbUtils.insertRequestFlow(inputBody.getContext(), selectRequestSerialized, null);

	}

	@Override
	@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
	public void processCallbackRequest() {

		List<ServiceRequestFlow> flows = dbUtils.getRequestsOnTimeAndAction(ActionEnum.SELECT.toString());
		for (ServiceRequestFlow f : flows) {
			System.out.println("Response from DB");
			System.out.println(f.toString());

			SelectBody selectBody = utils.deserialiseData(f.getData(), SelectBody.class);
			String selectProviderId = getSelectProviderId(selectBody);

			System.out.println("Select providerId --------------" + selectProviderId);
	
			OnSelectBody body = getOnSelectBody(selectBody);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_select",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));
		
		}
	}	

	public String getSelectProviderId(SelectBody selectBody) {
		
		return selectBody.getMessage().getOrder().getProvider().getId();
	}
	
	
	public OnSelectBody getOnSelectBody(SelectBody selectBody) {

		OnSelectBody onSelectBody = new OnSelectBody();
		onSelectBody.setContext(selectBody.getContext().action(ActionEnum.ON_SELECT));
		onSelectBody.setMessage(new OnSelectMessage().order(new OnSelectMessageOrder()));
		onSelectBody.getMessage().getOrder().provider(new Provider().id(getSelectProviderId(selectBody)));
				
		List<QuotationBreakup> quotationBreakups = new ArrayList<QuotationBreakup>();
		Double totalValue = 0.0;
	
		List<Item> itemsList = selectBody.getMessage().getOrder().getItem();
		if (itemsList != null && !itemsList.isEmpty()) {
		    for (Item item : itemsList) {
		        String itemId = item.getId();
		        String descriptorName = item.getDescriptor().getName();
		        String itemCurrency = item.getPrice().getCurrency();
		        String itemValue = item.getPrice().getValue();

		        // Create a new item for the order
		        AllOfonSelectMessageOrderItemsItems onSelectMessageOrderItems = new AllOfonSelectMessageOrderItemsItems();
		        
		        onSelectMessageOrderItems.id(itemId);
		        onSelectMessageOrderItems.descriptor(new Descriptor().name(descriptorName));
		        onSelectMessageOrderItems.price(new Price().currency(itemCurrency).value(itemValue));

		        // Add the new item to the order
		        onSelectBody.getMessage().getOrder().addItemsItem(onSelectMessageOrderItems);
		        
		        // Create a quotation
		        QuotationBreakup quotationBreakup = new QuotationBreakup();
				quotationBreakup.title(descriptorName).price(new Price().currency(itemCurrency).value(itemValue));
		        totalValue = totalValue+Double.parseDouble(itemValue);
				
				
				quotationBreakups.add(quotationBreakup);
				onSelectBody.getMessage().getOrder().setQuote(new Quotation().price(new Price().currency(itemCurrency).value(String.valueOf(totalValue))).breakup(quotationBreakups));			
		    }
		} 
		
		else {
			System.out.println("Items is null............");
		}

		return onSelectBody;		
		
	}
}
