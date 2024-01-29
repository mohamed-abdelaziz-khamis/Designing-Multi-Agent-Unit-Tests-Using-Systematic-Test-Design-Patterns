/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package MediationPatterns.BrokerPattern.BrokerServiceProviderAgent.MockBrokerAgent;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import MASUnitTesting.AgentManager;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.JADETestCase;
import MASUnitTesting.TestResultReporter;

@SuppressWarnings("unused")
public class BrokerServiceProviderAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockBrokerAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.BrokerPattern.BrokerServiceProviderAgent.MockBrokerAgent.MockBrokerAgent");	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockBrokerAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	
	public static void main (String[] args) {	
		junit.textui.TestRunner.run (suite());
	}	
	public static Test suite() {
		return new TestSuite(BrokerServiceProviderAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testBrokerServiceProvider(){	
		Object[] args;		
		AgentContainer myContainer = createEnvironment();
		
		//Test Case creates the Agent Under Test
		args = new Object[2];
		args[0] =  getResourceString("Offered_Service_Title");		
		args[1] =  getResourceString("Offered_Service_Price");
		
		AgentController serviceProviderAgent = createAgent("serviceProvider",
			"MediationPatterns.BrokerPattern.BrokerServiceProviderAgent.BrokerServiceProviderAgent",
			args, myContainer);	
		
		
		//Test Case creates Mock Agent that interacts with the Agent Under Test		
		AgentController mockBrokerAgent = createAgent("broker",
			"MediationPatterns.BrokerPattern.BrokerServiceProviderAgent.MockBrokerAgent.MockBrokerAgent", null, myContainer); 
		
		AgentManager.waitUntilTestFinishes(new AID("broker", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockBrokerAgent.myContainer.acquireLocalAgent (new AID("broker", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		 }
	}

}

