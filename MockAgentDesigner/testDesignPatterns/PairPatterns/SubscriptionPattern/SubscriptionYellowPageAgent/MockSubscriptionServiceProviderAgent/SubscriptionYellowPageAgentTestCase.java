/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package PairPatterns.SubscriptionPattern.SubscriptionYellowPageAgent.MockSubscriptionServiceProviderAgent;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import MASUnitTesting.AgentManager;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.JADETestCase;
import MASUnitTesting.TestResultReporter;

@SuppressWarnings("unused")
public class SubscriptionYellowPageAgentTestCase extends JADETestCase {
		
	public static void main (String[] args) {	
		junit.textui.TestRunner.run (suite());
	}		
	
	public static Test suite() {
		return new TestSuite(SubscriptionYellowPageAgentTestCase.class);
	}		
	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testSubscription(){
		AgentContainer myContainer = createEnvironment();

		//Test Case creates the Agent Under Test		
		AgentController yellowPageAgent = createAgent("yellowPage",
			"PairPatterns.SubscriptionPattern.SubscriptionYellowPageAgent.SubscriptionYellowPageAgent", 
			null, myContainer); 		
		
		//Test Case creates Mock Agent that interacts with the Agent Under Test
		AgentController mockServiceProviderAgent = createAgent("serviceProvider",
			"PairPatterns.SubscriptionPattern.SubscriptionYellowPageAgent." +
			"MockSubscriptionServiceProviderAgent.MockSubscriptionServiceProviderAgent",
			null, myContainer);	

		AgentManager.waitUntilTestFinishes(new AID("serviceProvider", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockServiceProviderAgent.myContainer.acquireLocalAgent
												(new AID("serviceProvider", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		 }
	}		
}
