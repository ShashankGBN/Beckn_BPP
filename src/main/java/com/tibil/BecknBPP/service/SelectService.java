package com.tibil.BecknBPP.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dao.utils.DbUtils;
import com.tibil.BecknBPP.dto.AddOn;
import com.tibil.BecknBPP.dto.AllOfonSelectMessageOrderItemsItems;
import com.tibil.BecknBPP.dto.Catalog;
import com.tibil.BecknBPP.dto.Category;
import com.tibil.BecknBPP.dto.Descriptor;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.Item;
import com.tibil.BecknBPP.dto.ItemQuantity;
import com.tibil.BecknBPP.dto.OnSelectBody;
import com.tibil.BecknBPP.dto.OnSelectMessage;
import com.tibil.BecknBPP.dto.OnSelectMessageOrder;
import com.tibil.BecknBPP.dto.Order;
import com.tibil.BecknBPP.dto.OnSearchMessage;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Provider;
import com.tibil.BecknBPP.dto.Quotation;
import com.tibil.BecknBPP.dto.QuotationBreakup;
import com.tibil.BecknBPP.dto.SelectBody;
import com.tibil.BecknBPP.dto.Tags;
import com.tibil.BecknBPP.dto.Context.ActionEnum;
import com.tibil.BecknBPP.model.Candidate;
import com.tibil.BecknBPP.model.Designation;
import com.tibil.BecknBPP.model.ServiceRequestFlow;
import com.tibil.BecknBPP.model.Skill;

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
	
			OnSelectBody body = getOnselectBody(selectBody);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_select",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));

		}

	}	

	public String getSelectProviderId(SelectBody selectBody) {

		return selectBody.getMessage().getOrder().getProvider().getId();
	}
		

	public OnSelectBody getOnselectBody(SelectBody selectBody) {

		OnSelectBody onSelectBody = new OnSelectBody();
		onSelectBody.setContext(selectBody.getContext().action(ActionEnum.ON_SELECT));
		onSelectBody.setMessage(new OnSelectMessage().order(new OnSelectMessageOrder()));
		onSelectBody.getMessage().getOrder().provider(new Provider().id("Tibil solutions"));
			
		onSelectBody.getMessage().getOrder().quote(new Quotation().price(new Price().currency("INR").value("20")));
		QuotationBreakup quotationBreakup = new QuotationBreakup().title("Employee name").price(new Price().value("10"));
		quotationBreakup.title("Employee name200").price(new Price().value("10"));
		onSelectBody.getMessage().getOrder().setQuote(new Quotation().addBreakupItem(quotationBreakup));
		
		AllOfonSelectMessageOrderItemsItems onSelectMessageOrderItems = (AllOfonSelectMessageOrderItemsItems) new AllOfonSelectMessageOrderItemsItems().id("1");
		onSelectMessageOrderItems.descriptor(new Descriptor().name("Candidate1"));
		onSelectMessageOrderItems.price(new Price().currency("INR").value("500000.0"));
		
		onSelectBody.getMessage().getOrder().addItemsItem(onSelectMessageOrderItems);
		
		return onSelectBody;		
		
	}

}








/*		Item items = new Item().id("1").descriptor(new Descriptor().name("Candidate1"));
items.price(new Price());
items.getPrice().currency("INR");
items.getPrice().value("500000.0");
*/


/*		AddOn addOn = new AddOn().id("1");
addOn.descriptor(new Descriptor().name("Candidate1"));
addOn.price(new Price().currency("INR").value("500000.0"));
onSelectBody.getMessage().getOrder().addAddOnsItem(addOn);
*/



