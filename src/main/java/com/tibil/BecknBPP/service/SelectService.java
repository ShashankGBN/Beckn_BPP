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
import com.tibil.BecknBPP.dto.AllOfonSelectMessageOrderItemsItems;
import com.tibil.BecknBPP.dto.Catalog;
import com.tibil.BecknBPP.dto.Category;
import com.tibil.BecknBPP.dto.Descriptor;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.Item;
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
			String selectDesignation = getSelectDesignation(selectBody);
			List<String> skills = getSelectSkillSets(selectBody);

			System.out.println("Select designation --------------" + selectDesignation);
			skills.stream().forEach(x -> System.out.println(x));

			List<Candidate> candidates = dbUtils.getCandidatesBasedOnDesignationAndSkills(selectDesignation, skills);
			System.out.println(candidates.size());
			candidates.stream().forEach(x -> System.out.println(x));

			OnSelectBody body = getOnselectBody(selectBody, candidates);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_select",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));

		}

	}

	public List<Item> createCandidatesItemList(List<Candidate> candidates) {

		List<Item> items = new ArrayList<Item>();
		for (Candidate candiate : candidates) {

			Item item = new Item();
			item.setId(String.valueOf(candiate.getId()));
			item.descriptor(new Descriptor().name(candiate.getName()));
			item.price(new Price().currency("INR").value(String.valueOf(candiate.getCtc())));

			Tags tags = new Tags();
			ArrayList<HashMap<String, String>> skillSet = new ArrayList<HashMap<String, String>>();
			for (Skill skill : candiate.getSkills()) {

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("code", skill.getName());
				skillSet.add(map);
			}
			tags.put("code", "skills");
			tags.put("list", skillSet);
			item.setTags(tags);

			items.add(item);
		}
		return items;

	}

	public List<Category> createResponseCategories(List<Candidate> candidates) {

		Set<Category> categories = new HashSet<Category>();
		for (Candidate candiate : candidates) {
			Category category = new Category();
			for (Designation designation : candiate.getDesignations()) {
				category.setId(String.valueOf(designation.getId()));
				category.descriptor(new Descriptor().name(designation.getName()));
				categories.add(category);
			}
		}

		return categories.stream().collect(Collectors.toList());
	}

	public String getSelectDesignation(SelectBody selectBody) {

		return selectBody.getMessage().getOrder().getId();
	}

	public List<String> getSelectSkillSets(SelectBody selectBody) {

		Tags selectTags = selectBody.getMessage().getOrder().getProvider().getTags();
		List<HashMap<String, Object>> skillSets = (List<HashMap<String, Object>>) selectTags.get("list");
		List<String> skills = skillSets.stream().map(x -> x.get("code").toString()).collect(Collectors.toList());
		return skills;
	}

	public OnSelectBody getOnselectBody(SelectBody selectBody, List<Candidate> candidates) {

		OnSelectBody onSelectBody = new OnSelectBody();
		onSelectBody.setContext(selectBody.getContext().action(ActionEnum.ON_SELECT));
		onSelectBody.setMessage(new OnSelectMessage());
		onSelectBody.getMessage().setOrder(new OnSelectMessageOrder().provider(new Provider().id("Tibil solutions")));

		Item items = new Item().id("1").descriptor(new Descriptor().name("Candidate1"));
		items.setPrice(new Price().currency("INR"));
		items.setTags(new Tags());

		onSelectBody.getMessage().getOrder().setQuote(new Quotation().addBreakupItem(new QuotationBreakup().title("Employee name")));

		return onSelectBody;
	}

}
