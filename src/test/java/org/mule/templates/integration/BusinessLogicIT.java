/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.context.notification.NotificationException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.test.utils.ListenerProbe;

import com.mulesoft.module.batch.BatchTestHelper;
import com.netsuite.webservices.platform.core.RecordRef;
import com.netsuite.webservices.platform.core.types.RecordType;

/**
 * The objective of this class is to validate the correct behavior of the
 * Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private static final String MULE_TEST_PROPERTIES_PATH = "./src/test/resources/mule.test.properties";
	
	private BatchTestHelper helper;
	private String createdCustomerIdInNetsuite;
	private String netsCustomerSubsidiaryInternalId;
	private String createdAccountIdInSalesforce;
	private Map<String,Object> customerInNetsuite = new HashMap<>(1);
	
	private SubflowInterceptingChainLifecycleWrapper createAccountInSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper createCustomerInNetsuiteFlow;
	private SubflowInterceptingChainLifecycleWrapper queryAccountFromSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper deleteAccountFromSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper queryCustomerFromNetsuiteFlow;
	private SubflowInterceptingChainLifecycleWrapper deleteCustomerFromNetsuiteFlow;
	
	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		registerListeners();
		
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(MULE_TEST_PROPERTIES_PATH));
			netsCustomerSubsidiaryInternalId = props.getProperty("nets.customer.subsidiary.internalId");
		} catch (Exception e) {
			throw new IllegalStateException("Could not find mule.test.properties file on classpath. Please add any of those file.");
		}	
        
		helper = new BatchTestHelper(muleContext);

		createAccountInSalesforceFlow = getSubFlow("createAccountInSalesforceFlow");
		createAccountInSalesforceFlow.initialise();		
		
		createCustomerInNetsuiteFlow = getSubFlow("createCustomerInNetsuiteFlow");
		createCustomerInNetsuiteFlow.initialise();
		
		queryAccountFromSalesforceFlow = getSubFlow("queryAccountFromSalesforceFlow");
		queryAccountFromSalesforceFlow.initialise();
		
		deleteAccountFromSalesforceFlow = getSubFlow("deleteAccountFromSalesforceFlow");
		deleteAccountFromSalesforceFlow.initialise();
		
		queryCustomerFromNetsuiteFlow = getSubFlow("queryCustomerFromNetsuiteFlow");
		queryCustomerFromNetsuiteFlow.initialise();
		
		deleteCustomerFromNetsuiteFlow = getSubFlow("deleteCustomerFromNetsuiteFlow");
		deleteCustomerFromNetsuiteFlow.initialise();
		
		createTestAccount();
	}

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		deleteTestsData();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMainFlow() throws Exception {
				
		// Run poll and wait for it to run
		runSchedulersOnce(POLL_FLOW_NAME);
		waitForPollToRun();

		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();
		
		// Assert object was sync to target system 
		MuleEvent event = queryAccountFromSalesforceFlow.process(getTestEvent(customerInNetsuite.get("companyName"), MessageExchangePattern.REQUEST_RESPONSE));
		Map<String,Object> resultPayload = (Map<String,Object>)event.getMessage().getPayload();
		createdAccountIdInSalesforce = resultPayload.get("Id").toString();
		
		assertEquals("The account name should have been sync", resultPayload.get("Name"), customerInNetsuite.get("companyName"));
		assertEquals("The account phone should have been sync", resultPayload.get("Phone"), customerInNetsuite.get("phone"));
		assertEquals("The account fax should have been sync", resultPayload.get("Fax"), customerInNetsuite.get("fax"));		
	}

	private void registerListeners() throws NotificationException {
		muleContext.registerListener(pipelineListener);
	}

	private void waitForPollToRun() {
		pollProber.check(new ListenerProbe(pipelineListener));
	}

	private void createTestAccount() throws MuleException, Exception {

		customerInNetsuite.put("fax", "12345678");
		customerInNetsuite.put("phone", "555-4448");
		customerInNetsuite.put("companyName", buildUniqueName(TEMPLATE_NAME));
		
		RecordRef subsidiary = new RecordRef();
		subsidiary.setInternalId(netsCustomerSubsidiaryInternalId);
		subsidiary.setType(RecordType.SUBSIDIARY);
		
		customerInNetsuite.put("subsidiary", subsidiary);
		
		final MuleEvent event = createCustomerInNetsuiteFlow.process(getTestEvent(customerInNetsuite, MessageExchangePattern.REQUEST_RESPONSE));
		createdCustomerIdInNetsuite = ((RecordRef) event.getMessage().getPayload()).getInternalId();
	}

	private void deleteTestsData() throws MuleException, Exception {
		// Delete the created Account in Salesforce		
		final List<Object> idList = new ArrayList<Object>();
		idList.add(createdAccountIdInSalesforce);		
		deleteAccountFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created Customer in Netsuite
		deleteCustomerFromNetsuiteFlow.process(getTestEvent(createdCustomerIdInNetsuite, MessageExchangePattern.REQUEST_RESPONSE));
	}

}
