package com.tibil.BecknBPP.test.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class TestSearchController {

	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void errorRequestWithNoDomain() throws Exception
	{
		String body = """
				{
   "context":{
      "country":"IND",
      "city":"BLR",
      "action":"on_search",
      "core_version":"1.0.0",
      "bap_id":"candidate_buyer",
      "bap_uri":"http://candidate-buyer.com",
      "bpp_id":"candidate_seller",
      "bpp_uri":"http://candidate-seller.com",
      "transaction_id":"79b6a909-5236-4841-91e8-91ac7fb91516",
      "message_id":"3c470a84-7597-41c8-8f60-c02eb949e0c9",
      "timestamp":"2023-01-19T14:22:45.092741+05:30"
   },
   "message":{
      "catalog":{
		 "bpp/descriptor":{
   		 "name": "Java developer",
   		 "short_desc": "Java developer"
		 },
		 "bpp/categories":[
   		 {
      		 "id":"SSE",
      		 "descriptor":{
         		 "name":"senior software engineer",
         		 "code":"SSE"
      		 }
   		 },
   		 {
      		 "id":"DE",
      		 "descriptor":{
         		 "name":"Devops Engineer",
         		 "code":"DE"
      		 }
   		 }
		 ],
		 "bpp/providers":[
   		 {
      		 "id":"Tibil solutions",
      		 "descriptor":{
         		 "name":"Tibil solutions",
         		 "code":"Tibil solutions"
      		 },
      		 "category_id":"testwa6",
      		 "categories":[
         		 {
          		 "id":"SSE",
          		 "descriptor":{
             		 "name":"senior software engineer",
             		 "code":"SSE"
          		 }
       		 },
       		 {
          		 "id":"DE",
          		 "descriptor":{
             		 "name":"Devops Engineer",
             		 "code":"DE"
          		 }
       		 }
      		 ],
      		 "locations":[
         		 {
            		 "id":"Tibil solutions",
            		 "gps":"12.9423184,77.6016338",
            		 "address":{
               		 "door":"D2",
               		 "name":"Tibil solutions",
               		 "locality":"Swamy legato, Kadubisanahalli",
               		 "city":"Bengaluru",
               		 "state":"Karnataka",
               		 "area_code":"560045"
            		 },
            		 "circle":{
               		 "gps":"12.9423184,77.6016338",
               		 "radius":{
                  		 "value":3800.0,
                  		 "unit":"meter"
               		 }
            		 },
            		 "time":{
               		 "range":{
                  		 "start":"2023-01-19T09:00:00+05:30",
                  		 "end":"2023-01-19T19:00:00+05:30"
               		 },
               		 "days":"MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
            		 }
         		 }
      		 ],
      		 "items":[
         		 {
            		 "id":"EMP-123",
            		 "descriptor":{
               		 "name":"Employee name",
               		 "code":"EMP-123",
               		 "short_desc":"Senior software engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Senior software engineer",
               		 "experience":"3 years",
               		 "technologies": ["Java, Python"]
            		 }
         		 },
         		 {
            		 "id":"EMP-200",
            		 "descriptor":{
               		 "name":"Employee name200",
               		 "code":"EMP-200",
               		 "short_desc":"Devops Engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Devops Engineer",
               		 "experience":"5 years",
               		 "technologies": ["Git, AWS, Jenkins, Kubernates"]
            		 }
         		 }
      		 ],
                  "payment": {
                	   "time": {
                    	"schedule": {
                        	"frequency": "P1M"
                    }
                	}
            	}
   		 }
		 ]
      }
   }
}
				""";
		
		MvcResult result = mockMvc.perform(
				post("/v1/api/search")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andDo(print()).andReturn();
		
		assert(result.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
		assert(result.getResponse().getContentAsString().contains("provide a domain"));
	}
	
	@Test
	public void errorRequestWithNoCoreVersion() throws Exception
	{
		String body = """
				{
   "context":{
	  "Domain":"Human Resource",
	  "country":"IND",		
      "city":"BLR",
      "action":"on_search",
      "bap_id":"candidate_buyer",
      "bap_uri":"http://candidate-buyer.com",
      "bpp_id":"candidate_seller",
      "bpp_uri":"http://candidate-seller.com",
      "transaction_id":"79b6a909-5236-4841-91e8-91ac7fb91516",
      "message_id":"3c470a84-7597-41c8-8f60-c02eb949e0c9",
      "timestamp":"2023-01-19T14:22:45.092741+05:30"
   },
   "message":{
      "catalog":{
		 "bpp/descriptor":{
   		 "name": "Java developer",
   		 "short_desc": "Java developer"
		 },
		 "bpp/categories":[
   		 {
      		 "id":"SSE",
      		 "descriptor":{
         		 "name":"senior software engineer",
         		 "code":"SSE"
      		 }
   		 },
   		 {
      		 "id":"DE",
      		 "descriptor":{
         		 "name":"Devops Engineer",
         		 "code":"DE"
      		 }
   		 }
		 ],
		 "bpp/providers":[
   		 {
      		 "id":"Tibil solutions",
      		 "descriptor":{
         		 "name":"Tibil solutions",
         		 "code":"Tibil solutions"
      		 },
      		 "category_id":"testwa6",
      		 "categories":[
         		 {
          		 "id":"SSE",
          		 "descriptor":{
             		 "name":"senior software engineer",
             		 "code":"SSE"
          		 }
       		 },
       		 {
          		 "id":"DE",
          		 "descriptor":{
             		 "name":"Devops Engineer",
             		 "code":"DE"
          		 }
       		 }
      		 ],
      		 "locations":[
         		 {
            		 "id":"Tibil solutions",
            		 "gps":"12.9423184,77.6016338",
            		 "address":{
               		 "door":"D2",
               		 "name":"Tibil solutions",
               		 "locality":"Swamy legato, Kadubisanahalli",
               		 "city":"Bengaluru",
               		 "state":"Karnataka",
               		 "area_code":"560045"
            		 },
            		 "circle":{
               		 "gps":"12.9423184,77.6016338",
               		 "radius":{
                  		 "value":3800.0,
                  		 "unit":"meter"
               		 }
            		 },
            		 "time":{
               		 "range":{
                  		 "start":"2023-01-19T09:00:00+05:30",
                  		 "end":"2023-01-19T19:00:00+05:30"
               		 },
               		 "days":"MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
            		 }
         		 }
      		 ],
      		 "items":[
         		 {
            		 "id":"EMP-123",
            		 "descriptor":{
               		 "name":"Employee name",
               		 "code":"EMP-123",
               		 "short_desc":"Senior software engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Senior software engineer",
               		 "experience":"3 years",
               		 "technologies": ["Java, Python"]
            		 }
         		 },
         		 {
            		 "id":"EMP-200",
            		 "descriptor":{
               		 "name":"Employee name200",
               		 "code":"EMP-200",
               		 "short_desc":"Devops Engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Devops Engineer",
               		 "experience":"5 years",
               		 "technologies": ["Git, AWS, Jenkins, Kubernates"]
            		 }
         		 }
      		 ],
                  "payment": {
                	   "time": {
                    	"schedule": {
                        	"frequency": "P1M"
                    }
                	}
            	}
   		 }
		 ]
      }
   }
}
				""";
		
		MvcResult result = mockMvc.perform(
				post("/v1/api/search")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andDo(print()).andReturn();
		
		assert(result.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
		assert(result.getResponse().getContentAsString().contains("provide core version"));
	}
	
	
	@Test
	public void errorRequestWithNoBapId() throws Exception
	{
		String body = """
				{
   "context":{
	  "Domain":"Human Resource",		
      "country":"IND",
      "city":"BLR",
      "action":"on_search",
      "core_version":"1.0.0",
      "bap_uri":"http://candidate-buyer.com",
      "bpp_id":"candidate_seller",
      "bpp_uri":"http://candidate-seller.com",
      "transaction_id":"79b6a909-5236-4841-91e8-91ac7fb91516",
      "message_id":"3c470a84-7597-41c8-8f60-c02eb949e0c9",
      "timestamp":"2023-01-19T14:22:45.092741+05:30"
   },
   "message":{
      "catalog":{
		 "bpp/descriptor":{
   		 "name": "Java developer",
   		 "short_desc": "Java developer"
		 },
		 "bpp/categories":[
   		 {
      		 "id":"SSE",
      		 "descriptor":{
         		 "name":"senior software engineer",
         		 "code":"SSE"
      		 }
   		 },
   		 {
      		 "id":"DE",
      		 "descriptor":{
         		 "name":"Devops Engineer",
         		 "code":"DE"
      		 }
   		 }
		 ],
		 "bpp/providers":[
   		 {
      		 "id":"Tibil solutions",
      		 "descriptor":{
         		 "name":"Tibil solutions",
         		 "code":"Tibil solutions"
      		 },
      		 "category_id":"testwa6",
      		 "categories":[
         		 {
          		 "id":"SSE",
          		 "descriptor":{
             		 "name":"senior software engineer",
             		 "code":"SSE"
          		 }
       		 },
       		 {
          		 "id":"DE",
          		 "descriptor":{
             		 "name":"Devops Engineer",
             		 "code":"DE"
          		 }
       		 }
      		 ],
      		 "locations":[
         		 {
            		 "id":"Tibil solutions",
            		 "gps":"12.9423184,77.6016338",
            		 "address":{
               		 "door":"D2",
               		 "name":"Tibil solutions",
               		 "locality":"Swamy legato, Kadubisanahalli",
               		 "city":"Bengaluru",
               		 "state":"Karnataka",
               		 "area_code":"560045"
            		 },
            		 "circle":{
               		 "gps":"12.9423184,77.6016338",
               		 "radius":{
                  		 "value":3800.0,
                  		 "unit":"meter"
               		 }
            		 },
            		 "time":{
               		 "range":{
                  		 "start":"2023-01-19T09:00:00+05:30",
                  		 "end":"2023-01-19T19:00:00+05:30"
               		 },
               		 "days":"MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
            		 }
         		 }
      		 ],
      		 "items":[
         		 {
            		 "id":"EMP-123",
            		 "descriptor":{
               		 "name":"Employee name",
               		 "code":"EMP-123",
               		 "short_desc":"Senior software engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Senior software engineer",
               		 "experience":"3 years",
               		 "technologies": ["Java, Python"]
            		 }
         		 },
         		 {
            		 "id":"EMP-200",
            		 "descriptor":{
               		 "name":"Employee name200",
               		 "code":"EMP-200",
               		 "short_desc":"Devops Engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Devops Engineer",
               		 "experience":"5 years",
               		 "technologies": ["Git, AWS, Jenkins, Kubernates"]
            		 }
         		 }
      		 ],
                  "payment": {
                	   "time": {
                    	"schedule": {
                        	"frequency": "P1M"
                    }
                	}
            	}
   		 }
		 ]
      }
   }
}
				""";
		
		MvcResult result = mockMvc.perform(
				post("/v1/api/search")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andDo(print()).andReturn();
		
		assert(result.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
		assert(result.getResponse().getContentAsString().contains("provide BAP ID"));
	}
	
		
	@Test
	public void errorRequestWithValidRequest() throws Exception {
		
		String body = """
				{
   "context":{
	  "Domain":"Human Resource",		
      "country":"IND",
      "city":"BLR",
      "action":"on_search",
      "core_version":"1.0.0",
      "bap_id":"candidate_buyer",
      "bap_uri":"http://candidate-buyer.com",
      "bpp_id":"candidate_seller",
      "bpp_uri":"http://candidate-seller.com",
      "transaction_id":"79b6a909-5236-4841-91e8-91ac7fb91516",
      "message_id":"3c470a84-7597-41c8-8f60-c02eb949e0c9",
      "timestamp":"2023-01-19T14:22:45.092741+05:30"
   },
   "message":{
      "catalog":{
		 "bpp/descriptor":{
   		 "name": "Java developer",
   		 "short_desc": "Java developer"
		 },
		 "bpp/categories":[
   		 {
      		 "id":"SSE",
      		 "descriptor":{
         		 "name":"senior software engineer",
         		 "code":"SSE"
      		 }
   		 },
   		 {
      		 "id":"DE",
      		 "descriptor":{
         		 "name":"Devops Engineer",
         		 "code":"DE"
      		 }
   		 }
		 ],
		 "bpp/providers":[
   		 {
      		 "id":"Tibil solutions",
      		 "descriptor":{
         		 "name":"Tibil solutions",
         		 "code":"Tibil solutions"
      		 },
      		 "category_id":"testwa6",
      		 "categories":[
         		 {
          		 "id":"SSE",
          		 "descriptor":{
             		 "name":"senior software engineer",
             		 "code":"SSE"
          		 }
       		 },
       		 {
          		 "id":"DE",
          		 "descriptor":{
             		 "name":"Devops Engineer",
             		 "code":"DE"
          		 }
       		 }
      		 ],
      		 "locations":[
         		 {
            		 "id":"Tibil solutions",
            		 "gps":"12.9423184,77.6016338",
            		 "address":{
               		 "door":"D2",
               		 "name":"Tibil solutions",
               		 "locality":"Swamy legato, Kadubisanahalli",
               		 "city":"Bengaluru",
               		 "state":"Karnataka",
               		 "area_code":"560045"
            		 },
            		 "circle":{
               		 "gps":"12.9423184,77.6016338",
               		 "radius":{
                  		 "value":3800.0,
                  		 "unit":"meter"
               		 }
            		 },
            		 "time":{
               		 "range":{
                  		 "start":"2023-01-19T09:00:00+05:30",
                  		 "end":"2023-01-19T19:00:00+05:30"
               		 },
               		 "days":"MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
            		 }
         		 }
      		 ],
      		 "items":[
         		 {
            		 "id":"EMP-123",
            		 "descriptor":{
               		 "name":"Employee name",
               		 "code":"EMP-123",
               		 "short_desc":"Senior software engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Senior software engineer",
               		 "experience":"3 years",
               		 "technologies": ["Java, Python"]
            		 }
         		 },
         		 {
            		 "id":"EMP-200",
            		 "descriptor":{
               		 "name":"Employee name200",
               		 "code":"EMP-200",
               		 "short_desc":"Devops Engineer",
               		 "images":[
                 		 
               		 ]
            		 },
            		 "price":{
               		 "currency":"INR",
               		 "value":"10",
               		 "estimated_value":"10"
            		 },

            		 "tags":{
                    	"designation": "Devops Engineer",
               		 "experience":"5 years",
               		 "technologies": ["Git, AWS, Jenkins, Kubernates"]
            		 }
         		 }
      		 ],
                  "payment": {
                	   "time": {
                    	"schedule": {
                        	"frequency": "P1M"
                    }
                	}
            	}
   		 }
		 ]
      }
   }
}
				""";
		
		MvcResult result = mockMvc.perform(
				post("/v1/api/search")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andDo(print()).andReturn();
		
		assert(result.getResponse().getStatus() == HttpStatus.OK.value());
	}
	
}
