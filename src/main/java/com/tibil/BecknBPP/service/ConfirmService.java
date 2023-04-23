package com.tibil.BecknBPP.service;

	import java.util.HashMap;
	import java.util.List;
import java.util.Map;
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
	import com.tibil.BecknBPP.dto.FulfillmentStart;
	import com.tibil.BecknBPP.dto.InlineResponse2001;
	import com.tibil.BecknBPP.dto.Item;
	import com.tibil.BecknBPP.dto.Location;
import com.tibil.BecknBPP.dto.OnConfirmBody;
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
import com.tibil.BecknBPP.dto.ConfirmBody;
	import com.tibil.BecknBPP.model.Candidate;
	import com.tibil.BecknBPP.model.ServiceRequestFlow;

import jakarta.validation.Valid;

	@Component
	public class ConfirmService implements ProcessInternalRequestService {

		private ServiceUtils utils;
		private DbUtils dbUtils;

		@Autowired
		public ConfirmService(ServiceUtils utils, DbUtils dbUtils) {
			super();
			this.utils = utils;
			this.dbUtils = dbUtils;
		}

		@Override
		public void processInternalRequest(Object requestBody) {

			ConfirmBody inputBody = (ConfirmBody) requestBody;

			String confirmRequestSerialized = utils.getSerialisedData(inputBody);
			dbUtils.insertServiceRequest(inputBody.getContext(), confirmRequestSerialized);

			dbUtils.insertRequestFlow(inputBody.getContext(), confirmRequestSerialized, null);
			
			dbUtils.insertServiceOrder(inputBody.getContext(), confirmRequestSerialized, null);


		}

		@Override
		@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
		public void processCallbackRequest() {

			List<ServiceRequestFlow> flows = dbUtils.getRequestsOnTimeAndAction(ActionEnum.CONFIRM.toString());
			for (ServiceRequestFlow f : flows) {
				System.out.println("Response from DB");
				System.out.println(f.toString());

				ConfirmBody confirmBody = utils.deserialiseData(f.getData(), ConfirmBody.class);
				@Valid Map confirmPayment = getConfirmPayment(confirmBody);

				System.out.println("transaction id --------------" + confirmPayment);

				OnConfirmBody body = getOnConfirmBody(confirmBody);
				ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_confirm",
						body, InlineResponse2001.class);

				dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
						utils.getSerialisedData(response.getBody()));
				
				dbUtils.insertServiceOrder(body.getContext(), utils.getSerialisedData(body), null);

			}

		}

		
		public @Valid Map getConfirmPayment(ConfirmBody confirmBody) {

			return confirmBody.getMessage().getOrder().getPayment().getParams();
		}


		public OnConfirmBody getOnConfirmBody(ConfirmBody confirmBody) {

			OnConfirmBody onConfirmBody = new OnConfirmBody();
			onConfirmBody.setContext(confirmBody.getContext().action(ActionEnum.ON_CONFIRM));
			onConfirmBody.setMessage(new SelectMessage());
			onConfirmBody.getMessage().setOrder(new Order().id("order_id-001").state(StateEnum.ACTIVE).provider(new Provider().id("Tibil solutions")));

			Item items = new Item().id("1");
			
			
			
			Billing billing = new Billing().phone("9620336606").name("Sanjay").email("sanjay@peppo.co.in");
			
			onConfirmBody.getMessage().getOrder().billing(billing);
			
			Fulfillment fulFillment = new Fulfillment().state(new State().descriptor(new Descriptor().name("SEARCHING-FOR-FMD-AGENT")));
			fulFillment.tracking(false).start(new FulfillmentStart().location(new Location().id("Tibil solutions").circle(new Circle().gps("12.9423184,77.6016338"))));
			fulFillment.getEnd().contact(new Contact().phone("9620336606")).person(new Person().name("Sanjay")).location(new Location().gps("12.964319, 77.6810060000001"));
			
			onConfirmBody.getMessage().getOrder().fulfillment(fulFillment);
			onConfirmBody.getMessage().getOrder().quote(new Quotation().price(new Price().currency("INR").value("30")));

			
			QuotationBreakup quotationBreakup = new QuotationBreakup().title("Employee name").price(new Price().value("10"));
			quotationBreakup.title("Employee name200").price(new Price().value("10"));
			quotationBreakup.title("BPP Fee").price(new Price().value("5"));
			quotationBreakup.title("BAP Fee").price(new Price().value("5"));
			
			onConfirmBody.getMessage().getOrder().setQuote(new Quotation().addBreakupItem(quotationBreakup));
			
			onConfirmBody.getMessage().setOrder(new Order().payment(new Payment().params(null)));
			
			return onConfirmBody;
		}
	
}
