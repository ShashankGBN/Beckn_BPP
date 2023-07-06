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
import com.tibil.BecknBPP.dto.Catalog;
import com.tibil.BecknBPP.dto.Category;
import com.tibil.BecknBPP.dto.Context.ActionEnum;
import com.tibil.BecknBPP.dto.Descriptor;
import com.tibil.BecknBPP.dto.InlineResponse2001;
import com.tibil.BecknBPP.dto.Item;
import com.tibil.BecknBPP.dto.OnSearchBody;
import com.tibil.BecknBPP.dto.OnSearchMessage;
import com.tibil.BecknBPP.dto.Price;
import com.tibil.BecknBPP.dto.Provider;
import com.tibil.BecknBPP.dto.SearchBody;
import com.tibil.BecknBPP.dto.Tags;
import com.tibil.BecknBPP.model.Candidate;
import com.tibil.BecknBPP.model.Designation;
import com.tibil.BecknBPP.model.ServiceRequestFlow;
import com.tibil.BecknBPP.model.Skill;

@Component
public class SearchService implements ProcessInternalRequestService {

	private ServiceUtils utils;
	private DbUtils dbUtils;

	@Autowired
	public SearchService(ServiceUtils utils, DbUtils dbUtils) {
		super();
		this.utils = utils;
		this.dbUtils = dbUtils;
	}

	@Override
	public void processInternalRequest(Object requestBody) {

		SearchBody inputBody = (SearchBody) requestBody;

		String searchRequestSerialized = utils.getSerialisedData(inputBody);
		dbUtils.insertServiceRequest(inputBody.getContext(), searchRequestSerialized);

		dbUtils.insertRequestFlow(inputBody.getContext(), searchRequestSerialized, null);

	}

	@Override
	@Scheduled(fixedDelay = Constants.SCHEDULE_DELAY)
	public void processCallbackRequest() {

		List<ServiceRequestFlow> flows = dbUtils.getRequestsOnTimeAndAction(ActionEnum.SEARCH.toString());
		for (ServiceRequestFlow f : flows) {
			System.out.println("Response from DB");
			System.out.println(f.toString());

			SearchBody searchBody = utils.deserialiseData(f.getData(), SearchBody.class);
			String searchDesignation = getSearchDesignation(searchBody);
			List<String> skills = getSearchSkillSets(searchBody);

			System.out.println("Search designation --------------" + searchDesignation);
			skills.stream().forEach(x -> System.out.println(x));

			boolean availability = true;
			
			List<Candidate> candidates = dbUtils.getCandidatesBasedOnDesignationAndSkillsAndAvailability(searchDesignation, skills, availability);
			System.out.println(candidates.size());
			candidates.stream().forEach(x -> System.out.println(x));

			OnSearchBody body = getOnsearchBody(searchBody, candidates);
			ResponseEntity<InlineResponse2001> response = restTemplate.postForEntity("http://localhost:8080/on_search",
					body, InlineResponse2001.class);

			dbUtils.insertRequestFlow(body.getContext(), utils.getSerialisedData(body),
					utils.getSerialisedData(response.getBody()));

		}

	}

	public List<Item> createCandidatesItemList(List<Candidate> candidates) {

		List<Item> items = new ArrayList<Item>();
		for (Candidate candidate : candidates) {

			Item item = new Item();
			item.setId(String.valueOf(candidate.getId()));
			item.descriptor(new Descriptor().name(candidate.getName()));
			item.price(new Price().currency("INR").value(String.valueOf(candidate.getCtc())));

			Tags tags = new Tags();
			ArrayList<HashMap<String, String>> skillSet = new ArrayList<HashMap<String, String>>();
			for (Skill skill : candidate.getSkills()) {

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

	public String getSearchDesignation(SearchBody searchBody) {

		return searchBody.getMessage().getIntent().getItem().getDescriptor().getName();
	}

	public List<String> getSearchSkillSets(SearchBody searchBody) {

		Tags searchTags = searchBody.getMessage().getIntent().getItem().getTags();
		List<HashMap<String, Object>> skillSets = (List<HashMap<String, Object>>) searchTags.get("list");
		List<String> skills = skillSets.stream().map(x -> x.get("code").toString()).collect(Collectors.toList());
		return skills;
	}

	public OnSearchBody getOnsearchBody(SearchBody searchBody, List<Candidate> candidates) {

		OnSearchBody onSearchBody = new OnSearchBody();
		onSearchBody.setContext(searchBody.getContext().action(ActionEnum.ON_SEARCH));
		onSearchBody.setMessage(new OnSearchMessage());
		onSearchBody.getMessage().setCatalog(new Catalog().descriptor(new Descriptor().name("Catalog")));

		Provider provider = new Provider().id("Tibil Solutions").descriptor(new Descriptor().name("Tibil Solutions"));
		provider.setCategories((List<Category>) createResponseCategories(candidates));
		provider.setItems(createCandidatesItemList(candidates));
		
		onSearchBody.getMessage().getCatalog().addProvidersItem(provider);

		return onSearchBody;
	}

}
