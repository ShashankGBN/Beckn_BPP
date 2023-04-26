package com.tibil.BecknBPP.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dao.utils.DbUtils;
import com.tibil.BecknBPP.dto.AllOfonSelectMessageOrderItemsItems;
import com.tibil.BecknBPP.dto.Category;
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
import com.tibil.BecknBPP.dto.Payment;
import com.tibil.BecknBPP.dto.Person;
import com.tibil.BecknBPP.dto.Payment.TypeEnum;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Quotation;
import com.tibil.BecknBPP.dto.QuotationBreakup;
import com.tibil.BecknBPP.dto.InitBody;
import com.tibil.BecknBPP.model.Candidate;
import com.tibil.BecknBPP.model.Designation;
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
			TypeEnum initPaymentType = getInitPaymentType(initBody);

			System.out.println("Payment Type --------------" + initPaymentType);
		
			OnInitBody body = getOnInitBody(initBody);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_init",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));
			
			dbUtils.insertServiceOrder(body.getContext(), utils.getSerialisedData(body));
			
		}

	}
	
	
	public TypeEnum getInitPaymentType(InitBody initBody) {

		return initBody.getMessage().getOrder().getPayment().getType();
	}

	public OnInitBody getOnInitBody(InitBody initBody) {

		OnInitBody onInitBody = new OnInitBody();
		onInitBody.setContext(initBody.getContext().action(ActionEnum.ON_INIT));
		onInitBody.setMessage(new OnInitMessage().order(new OnInitMessageOrder()));
		onInitBody.getMessage().getOrder().provider(new OnInitMessageOrderProvider().id("Tibil solutions"));

		OnInitMessageOrderItems onInitMessageOrderItems = new OnInitMessageOrderItems().id("1");
		onInitMessageOrderItems.quantity("1");
		
		onInitBody.getMessage().getOrder().addItemsItem(onInitMessageOrderItems);
		
		Fulfillment fulFillment = new Fulfillment().tracking(false).start(new FulfillmentStart().location(new Location().id("Tibil solutions").circle(new Circle().gps("12.9423184,77.6016338"))));
		fulFillment.setEnd(new FulfillmentEnd());
		fulFillment.getEnd().setLocation(new Location().gps("12.964319, 77.6810060000001"));
		fulFillment.end(new FulfillmentEnd()).contact(new Contact().phone("9620336606")).person(new Person().name("Sanjay"));

		onInitBody.getMessage().getOrder().setFulfillment(fulFillment);
		
		Price price = new Price().currency("INR").value("30");
		
		onInitBody.getMessage().getOrder().quote(new Quotation().price(new Price().currency("INR").value("30")));
		
		QuotationBreakup quotationBreakup = new QuotationBreakup().title("Employee name").price(new Price().value("10"));
		quotationBreakup.title("Employee name200").price(new Price().value("10"));
		quotationBreakup.title("BPP Fee").price(new Price().value("5"));
		quotationBreakup.title("BAP Fee").price(new Price().value("5"));
		
		onInitBody.getMessage().getOrder().setQuote(new Quotation().addBreakupItem(quotationBreakup));
		onInitBody.getMessage().setOrder(new OnInitMessageOrder().payment(new Payment().type(TypeEnum.POST_FULFILLMENT)));

		return onInitBody;
	}

}

