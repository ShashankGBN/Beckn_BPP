package com.tibil.BecknBPP.service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tibil.BecknBPP.dao.utils.DbUtils;
import com.tibil.BecknBPP.dto.Circle;
import com.tibil.BecknBPP.dto.Contact;
import com.tibil.BecknBPP.dto.Context.ActionEnum;
import com.tibil.BecknBPP.dto.Fulfillment;
import com.tibil.BecknBPP.dto.FulfillmentStart;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.Item;
import com.tibil.BecknBPP.dto.Location;
import com.tibil.BecknBPP.dto.OnInitBody;
import com.tibil.BecknBPP.dto.OnInitMessage;
import com.tibil.BecknBPP.dto.OnInitMessageOrder;
import com.tibil.BecknBPP.dto.OnInitMessageOrderProvider;
import com.tibil.BecknBPP.dto.Payment;
import com.tibil.BecknBPP.dto.Payment.TypeEnum;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Quotation;
import com.tibil.BecknBPP.dto.QuotationBreakup;
import com.tibil.BecknBPP.dto.InitBody;
import com.tibil.BecknBPP.model.Candidate;
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

	}

	@Override
	@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
	public void processCallbackRequest() {

		List<ServiceRequestFlow> flows = dbUtils.getRequestsOnTimeAndAction(ActionEnum.INIT.toString());
		for (ServiceRequestFlow f : flows) {
			System.out.println("Response from DB");
			System.out.println(f.toString());

			InitBody initBody = utils.deserialiseData(f.getData(), InitBody.class);
			String initDesignation = getInitDesignation(initBody);
			List<String> skills = getInitSkillSets(initBody);

			System.out.println("Init designation --------------" + initDesignation);
			skills.stream().forEach(x -> System.out.println(x));

			List<Candidate> candidates = dbUtils.getCandidatesBasedOnDesignationAndSkills(initDesignation, skills);
			System.out.println(candidates.size());
			candidates.stream().forEach(x -> System.out.println(x));

			OnInitBody body = getOnInitBody(initBody, candidates);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_init",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));

		}

	}

	
	public String getInitDesignation(InitBody initBody) {

		return initBody.getMessage().getOrder().getProvider().getId();
	}

	public List<String> getInitSkillSets(InitBody initBody) {

		Location initLocation = initBody.getMessage().getOrder().getFulfillment().getStart().getLocation();
		List<HashMap<String, Object>> skillSets = (List<HashMap<String, Object>>) initLocation.id("Tibil solutions");
		List<String> skills = skillSets.stream().map(x -> x.get("code").toString()).collect(Collectors.toList());
		return skills;
	}

	public OnInitBody getOnInitBody(InitBody initBody, List<Candidate> candidates) {

		OnInitBody onInitBody = new OnInitBody();
		onInitBody.setContext(initBody.getContext().action(ActionEnum.ON_INIT));
		onInitBody.setMessage(new OnInitMessage());
		onInitBody.getMessage().setOrder(new OnInitMessageOrder().provider(new OnInitMessageOrderProvider().id("Tibil solutions")));

		Item items = new Item().id("EMP-123");
		
		Fulfillment fulFillment = new Fulfillment().tracking(false).start(new FulfillmentStart().location(new Location().id("Tibil solutions").circle(new Circle().gps("12.9423184,77.6016338"))));
		fulFillment.getEnd().location(new Location().gps("12.964319, 77.6810060000001")).contact(new Contact().phone("+919945099450"));
		
		onInitBody.getMessage().setOrder(new OnInitMessageOrder().quote(new Quotation().price(new Price().currency("INR").value("30")).breakup((List<QuotationBreakup>) new QuotationBreakup().title("Employee name").price(new Price().value("10.0")))));
		onInitBody.getMessage().setOrder(new OnInitMessageOrder().payment(new Payment().type(TypeEnum.POST_FULFILLMENT)));

		return onInitBody;
	}

}

