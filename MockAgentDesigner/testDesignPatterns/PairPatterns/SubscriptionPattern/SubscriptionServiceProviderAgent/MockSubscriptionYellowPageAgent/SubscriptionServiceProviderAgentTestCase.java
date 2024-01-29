/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package PairPatterns.SubscriptionPattern.SubscriptionServiceProviderAgent.MockSubscriptionYellowPageAgent;

import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.AgentContainer;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import MASUnitTesting.AgentManager;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.JADETestCase;
import MASUnitTesting.TestResultReporter;

@SuppressWarnings("unused")
public class SubscriptionServiceProviderAgentTestCase extends JADETestCase {
	
	public static void main (String[] args) {	
		junit.textui.TestRunner.run (suite());
	}		
	
	public static Test suite() {
		return new TestSuite(SubscriptionServiceProviderAgentTestCase.class);
	}		
	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testSubscription(){
		AgentContainer myContainer = createEnvironment();

		//Test Case creates Mock Agent that interacts with the Agent Under Test
		AgentController mockYellowPageAgent = createAgent("yellowPage",
			"PairPatterns.SubscriptionPattern.SubscriptionServiceProviderAgent." +
			"MockSubscriptionYellowPageAgent.MockSubscriptionYellowPageAgent", 
			null, myContainer); 		
		
		//Test Case creates the Agent Under Test		
		AgentController serviceProviderAgent = createAgent("serviceProvider",
			"PairPatterns.SubscriptionPattern.SubscriptionServiceProviderAgent." +
			"SubscriptionServiceProviderAgent", null, myContainer);	

		AgentManager.waitUntilTestFinishes(new AID("yellowPage", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockYellowPageAgent.myContainer.acquireLocalAgent
												(new AID("yellowPage", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		 }
	}		
}
