package com.tibil.BecknBPP.service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dao.utils.DbUtils;
import com.tibil.BecknBPP.dto.Billing;
import com.tibil.BecknBPP.dto.Circle;
import com.tibil.BecknBPP.dto.Contact;
import com.tibil.BecknBPP.dto.Context.ActionEnum;
import com.tibil.BecknBPP.dto.Descriptor;
import com.tibil.BecknBPP.dto.Fulfillment;
import com.tibil.BecknBPP.dto.FulfillmentEnd;
import com.tibil.BecknBPP.dto.FulfillmentStart;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.Item;
import com.tibil.BecknBPP.dto.Location;
import com.tibil.BecknBPP.dto.OnUpdateBody;
import com.tibil.BecknBPP.dto.Order;
import com.tibil.BecknBPP.dto.Order.StateEnum;
import com.tibil.BecknBPP.dto.Payment;
import com.tibil.BecknBPP.dto.Payment.StatusEnum;
import com.tibil.BecknBPP.dto.Person;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Provider;
import com.tibil.BecknBPP.dto.Quotation;
import com.tibil.BecknBPP.dto.QuotationBreakup;
import com.tibil.BecknBPP.dto.SelectMessage;
import com.tibil.BecknBPP.dto.State;
import com.tibil.BecknBPP.dto.UpdateBody;
import com.tibil.BecknBPP.model.Candidate;
import com.tibil.BecknBPP.model.ServiceRequestFlow;

@Component
public class UpdateService implements ProcessInternalRequestService {

	private ServiceUtils utils;
	private DbUtils dbUtils;

	@Autowired
	public UpdateService(ServiceUtils utils, DbUtils dbUtils) {
		super();
		this.utils = utils;
		this.dbUtils = dbUtils;
	}

	@Override
	public void processInternalRequest(Object requestBody) {

		UpdateBody inputBody = (UpdateBody) requestBody;

		String updateRequestSerialized = utils.getSerialisedData(inputBody);
		dbUtils.insertServiceRequest(inputBody.getContext(), updateRequestSerialized);

		dbUtils.insertRequestFlow(inputBody.getContext(), updateRequestSerialized, null);

	}

	@Override
	@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
	public void processCallbackRequest() {

		List<ServiceRequestFlow> flows = dbUtils.getRequestsOnTimeAndAction(ActionEnum.UPDATE.toString());
		for (ServiceRequestFlow f : flows) {
			System.out.println("Response from DB");
			System.out.println(f.toString());

			UpdateBody updateBody = utils.deserialiseData(f.getData(), UpdateBody.class);
		
			OnUpdateBody body = getOnUpdateBody(updateBody);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_update",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));

		}

	}


	public OnUpdateBody getOnUpdateBody(UpdateBody updateBody) {

		OnUpdateBody onUpdateBody = new OnUpdateBody();
		onUpdateBody.setContext(updateBody.getContext().action(ActionEnum.ON_UPDATE));
		onUpdateBody.setMessage(new SelectMessage());
		onUpdateBody.getMessage().setOrder(new Order().id(updateBody.getMessage().getOrder().getId()).state(StateEnum.CANCELLED).provider(new Provider().id("Tibil solutions")));

	//	Item items = new Item().id("1");
		
		
		Billing billing = new Billing().phone("9620336606").name("Sanjay").email("sanjay@peppo.co.in");
		
		onUpdateBody.getMessage().getOrder().billing(billing);
		
		Fulfillment fulFillment = new Fulfillment().state(new State().descriptor(new Descriptor().name("SEARCHING-FOR-FMD-AGENT")));
		fulFillment.tracking(false).start(new FulfillmentStart().location(new Location().id("Tibil solutions").circle(new Circle().gps("12.9423184,77.6016338"))));
		fulFillment.setEnd(new FulfillmentEnd());
		fulFillment.getEnd().setLocation(new Location().gps("12.964319, 77.6810060000001"));
		fulFillment.end(new FulfillmentEnd()).contact(new Contact().phone("9620336606")).person(new Person().name("Sanjay"));

		
		onUpdateBody.getMessage().setOrder(new Order().quote(new Quotation().price(new Price().currency("INR").value("20"))));
		
		QuotationBreakup quotationBreakup = new QuotationBreakup().title("Candidate1").price(new Price().value("10"));
		quotationBreakup.title("BPP Fee").price(new Price().value("5"));
		quotationBreakup.title("BAP Fee").price(new Price().value("5"));
		
		onUpdateBody.getMessage().getOrder().setQuote(new Quotation().addBreakupItem(quotationBreakup));
		
		onUpdateBody.getMessage().setOrder(new Order().payment(new Payment().status(StatusEnum.PAID)));
		
		return onUpdateBody;
	}

}

