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
	import com.tibil.BecknBPP.dto.FulfillmentStart;
	import com.tibil.BecknBPP.dto.InlineResponse2001;
	import com.tibil.BecknBPP.dto.Item;
	import com.tibil.BecknBPP.dto.Location;
import com.tibil.BecknBPP.dto.OnConfirmBody;
import com.tibil.BecknBPP.dto.Order;
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

	@Component
	public class StatusService implements ProcessInternalRequestService {

		private ServiceUtils utils;
		private DbUtils dbUtils;

		@Autowired
		public StatusService(ServiceUtils utils, DbUtils dbUtils) {
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

		}

		@Override
		@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
		public void processCallbackRequest() {

			List<ServiceRequestFlow> flows = dbUtils.getRequestsOnTimeAndAction(ActionEnum.CONFIRM.toString());
			for (ServiceRequestFlow f : flows) {
				System.out.println("Response from DB");
				System.out.println(f.toString());

				ConfirmBody confirmBody = utils.deserialiseData(f.getData(), ConfirmBody.class);
				String confirmDesignation = getConfirmDesignation(confirmBody);
				List<String> skills = getConfirmSkillSets(confirmBody);

				System.out.println("Init designation --------------" + confirmDesignation);
				skills.stream().forEach(x -> System.out.println(x));

				List<Candidate> candidates = dbUtils.getCandidatesBasedOnDesignationAndSkills(confirmDesignation, skills);
				System.out.println(candidates.size());
				candidates.stream().forEach(x -> System.out.println(x));

				OnConfirmBody body = getOnConfirmBody(confirmBody, candidates);
				ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_confirm",
						body, InlineResponse2001.class);

				dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
						utils.getSerialisedData(response.getBody()));

			}

		}

		
		public String getConfirmDesignation(ConfirmBody confirmBody) {

			return confirmBody.getMessage().getOrder().getProvider().getId();
		}

		public List<String> getConfirmSkillSets(ConfirmBody confirmBody) {

			Location confirmLocation = confirmBody.getMessage().getOrder().getFulfillment().getStart().getLocation();
			List<HashMap<String, Object>> skillSets = (List<HashMap<String, Object>>) confirmLocation.id("Tibil solutions");
			List<String> skills = skillSets.stream().map(x -> x.get("code").toString()).collect(Collectors.toList());
			return skills;
		}

		public OnConfirmBody getOnConfirmBody(ConfirmBody confirmBody, List<Candidate> candidates) {

			OnConfirmBody onConfirmBody = new OnConfirmBody();
			onConfirmBody.setContext(confirmBody.getContext().action(ActionEnum.ON_CONFIRM));
			onConfirmBody.setMessage(new SelectMessage());
			onConfirmBody.getMessage().setOrder(new Order().id("order_id-001").provider(new Provider().id("Tibil solutions")));

			Item items = new Item().id("EMP-123");
			
			Billing billing = new Billing().phone("9620336606").name("Sanjay").email("sanjay@peppo.co.in");
			
			Fulfillment fulFillment = new Fulfillment().state(new State().descriptor(new Descriptor().name("SEARCHING-FOR-FMD-AGENT")));
			fulFillment.tracking(false).start(new FulfillmentStart().location(new Location().id("Tibil solutions").circle(new Circle().gps("12.9423184,77.6016338"))));
			fulFillment.getEnd().contact(new Contact().phone("9620336606")).person(new Person().name("Sanjay")).location(new Location().gps("12.964319, 77.6810060000001"));
			
			onConfirmBody.getMessage().setOrder(new Order().quote(new Quotation().price(new Price().currency("INR").value("30")).breakup((List<QuotationBreakup>) new QuotationBreakup().title("Employee name").price(new Price().value("10.0")))));
			onConfirmBody.getMessage().setOrder(new Order().payment(new Payment().status(StatusEnum.PAID)));

			return onConfirmBody;
		}
	
}

