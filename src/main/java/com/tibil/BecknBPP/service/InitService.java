package com.tibil.BecknBPP.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dto.OnSelectBody;
import com.tibil.BecknBPP.dto.OnSelectMessage;
import com.tibil.BecknBPP.dto.OnSelectMessageOrder;
import com.tibil.BecknBPP.dao.utils.DbUtils;
import com.tibil.BecknBPP.dto.AllOfonSelectMessageOrderItemsItems;
import com.tibil.BecknBPP.dto.Circle;
import com.tibil.BecknBPP.dto.Contact;
import com.tibil.BecknBPP.dto.Descriptor;
import com.tibil.BecknBPP.dto.Context.ActionEnum;
import com.tibil.BecknBPP.dto.Fulfillment;
import com.tibil.BecknBPP.dto.FulfillmentEnd;
import com.tibil.BecknBPP.dto.FulfillmentStart;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.Item;
import com.tibil.BecknBPP.dto.Location;
import com.tibil.BecknBPP.dto.OnInitBody;
import com.tibil.BecknBPP.dto.OnInitMessage;
import com.tibil.BecknBPP.dto.OnInitMessageOrder;
import com.tibil.BecknBPP.dto.OnInitMessageOrderItems;
import com.tibil.BecknBPP.dto.OnInitMessageOrderProvider;
import com.tibil.BecknBPP.dto.OnInitMessageOrderProviderLocation;
import com.tibil.BecknBPP.dto.Payment;
import com.tibil.BecknBPP.dto.Payment.TypeEnum;
import com.tibil.BecknBPP.dto.Person;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Quotation;
import com.tibil.BecknBPP.dto.QuotationBreakup;
import com.tibil.BecknBPP.dto.SelectBody;
import com.tibil.BecknBPP.dto.InitBody;
import com.tibil.BecknBPP.model.ServiceRequestFlow;

@Component
public class InitService implements ProcessInternalRequestService {

	private ServiceUtils utils;
	private DbUtils dbUtils;

	@Autowired
	public InitService(ServiceUtils utils, DbUtils dbUtils) {
		super();
		this.utils = utils;
		this.dbUtils = dbUtils;
	}

	@Override
	public void processInternalRequest(Object requestBody) {

		InitBody inputBody = (InitBody) requestBody;

		String initRequestSerialized = utils.getSerialisedData(inputBody);
		dbUtils.insertServiceRequest(inputBody.getContext(), initRequestSerialized);

		dbUtils.insertRequestFlow(inputBody.getContext(), initRequestSerialized, null);
		
		dbUtils.insertServiceOrder(inputBody.getContext(), initRequestSerialized);

	}

	@Override
	@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
	public void processCallbackRequest() {

		List<ServiceRequestFlow> flows = dbUtils.getRequestsOnTimeAndAction(ActionEnum.INIT.toString());
		for (ServiceRequestFlow f : flows) {
			System.out.println("Response from DB");
			System.out.println(f.toString());

			InitBody initBody = utils.deserialiseData(f.getData(), InitBody.class);
			String providerId = getProviderId(initBody);

			System.out.println("Provider Id --------------" + providerId);
		
			OnInitBody body = getOnInitBody(initBody);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_init",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));
			
			dbUtils.insertServiceOrder(body.getContext(), utils.getSerialisedData(body));
			
		}

	}
	
	
	public String getProviderId(InitBody initBody) {

		return initBody.getMessage().getOrder().getProvider().getId();
	}

	public OnInitBody getOnInitBody(InitBody initBody) {

		OnInitBody onInitBody = new OnInitBody();
		onInitBody.setContext(initBody.getContext().action(ActionEnum.ON_INIT));
		onInitBody.setMessage(new OnInitMessage().order(new OnInitMessageOrder()));
		
		Fulfillment fulFillment = new Fulfillment().tracking(false).start(new FulfillmentStart().location(new Location().id("Tibil solutions").circle(new Circle().gps("12.9423184,77.6016338"))));
		fulFillment.setEnd(new FulfillmentEnd());
//		fulFillment.getEnd().setLocation(new Location().gps(initBody.getMessage().getOrder().getFulfillment().getEnd().getLocation().getGps()));
//		fulFillment.end(new FulfillmentEnd()).contact(new Contact().phone(initBody.getMessage().getOrder().getFulfillment().getEnd().getContact().getPhone())).person(new Person().name(initBody.getMessage().getOrder().getFulfillment().getEnd().getLocation().getGps()));
		
		
		
		onInitBody.getMessage().setOrder(new OnInitMessageOrder().payment(new Payment().type(initBody.getMessage().getOrder().getPayment().getType())));
		onInitBody.getMessage().getOrder().provider(new OnInitMessageOrderProvider().id(getProviderId(initBody)));
//		onInitBody.getMessage().getOrder().setFulfillment(fulFillment);		
		
		List<Item> itemsList = initBody.getMessage().getOrder().getItem();
		if (itemsList != null && !itemsList.isEmpty()) {
		    for (Item item : itemsList) {
		        String itemId = item.getId();

		        // Create a new item for the order
		        OnInitMessageOrderItems onInitMessageOrderItems = new OnInitMessageOrderItems();
		        
		        onInitMessageOrderItems.id(itemId);
		        onInitMessageOrderItems.quantity("1");

		        // Add the new item to the order
		        onInitBody.getMessage().getOrder().addItemsItem(onInitMessageOrderItems);
		        		
		    }
		} 
		
		
		OnSelectBody onSelectBody = new OnSelectBody();
		onSelectBody.setMessage(new OnSelectMessage());
		onSelectBody.getMessage().setOrder(new OnSelectMessageOrder());
		onSelectBody.getMessage().getOrder().setQuote(new Quotation());
		List<QuotationBreakup> QuotationBreakups = onSelectBody.getMessage().getOrder().getQuote().getBreakup();
		onSelectBody.getMessage().getOrder().getQuote().setPrice(new Price());
		Price price = onSelectBody.getMessage().getOrder().getQuote().getPrice();
		String currency = onSelectBody.getMessage().getOrder().getQuote().getPrice().getCurrency();
		
		onInitBody.getMessage().getOrder().setQuote(new Quotation().price(new Price().currency(currency).value(String.valueOf(price))).breakup(QuotationBreakups));//onSelectBody.getMessage().getOrder().getQuote().getBreakup()));			
		onInitBody.getMessage().setOrder(new OnInitMessageOrder().payment(new Payment().type(initBody.getMessage().getOrder().getPayment().getType())));

		return onInitBody;
	}

}

