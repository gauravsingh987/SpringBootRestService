package com.rahulshettyacademy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahulshettyacademy.controller.LibraryController;
import com.rahulshettyacademy.controller.ProductsPrices;
import com.rahulshettyacademy.controller.SpecificProduct;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;

//to identify unit test case in springboot
@SpringBootTest
//extending the properties of springboot with pact contract test class
@ExtendWith(PactConsumerTestExt.class)
// name of provider where the test has to be run, it can be anything to identify provider
@PactTestFor(providerName = "CoursesCatalogue")
public class PactConsumerDemo {
	
	//create object for librarycontroller class for whom unit test case is to written
	@Autowired
	private LibraryController libraryController;
	
	//mentioning who is the consumer
	@Pact(consumer="BooksCatalogue")
	//Pact server configuration details - mocking the real call to course microservice
	public RequestResponsePact PactallCoursesDetailsConfig(PactDslWithProvider builder)
	{
		return builder.given("courses exist")
		.uponReceiving("getting all courses details")
		//request to Pact Server
		.path("/allCourseDetails")
		//mock response expected
		.willRespondWith()
		.status(200)
		//schema creation of the json response
		.body(PactDslJsonArray.arrayMinLike(2)
				.integerType("price", 10)
				.closeObject()).toPact();
					
		
	}
	
	
	@Test
	//the test will be executed as per the behaviour defined in the configuration defined above
	@PactTestFor(pactVersion = PactSpecVersion.V3,pactMethod="PactallCoursesDetailsConfig")
	
	public void testAllProductsSum(MockServer mockServer) throws JsonMappingException, JsonProcessingException
	
	{
		//expected response
		String expectedJson ="{\"booksPrice\":250,\"coursesPrice\":20}";
		//baseUrl of pact server is provided so that instead of real provider url, the mockServer url is hit
		libraryController.setBaseUrl(mockServer.getUrl());
		//the actual response after calling getProductPrices method
		ProductsPrices productsPrices = libraryController.getProductPrices();
		ObjectMapper obj = new ObjectMapper();
		//converting the response into json string
		String jsonActual = obj.writeValueAsString(productsPrices);
		//comparing the response of productprices method with expected response
		Assertions.assertEquals(expectedJson, jsonActual);
	}
	
	
	
}
