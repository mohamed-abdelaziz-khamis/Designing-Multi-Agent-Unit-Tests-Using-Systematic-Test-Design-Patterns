/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package MediationPatterns.BrokerPattern.BrokerClientAgent.MockBrokerAgent;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
public class BrokerClientAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockBrokerAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.BrokerPattern.BrokerClientAgent.MockBrokerAgent.MockBrokerAgent");	
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
		return new TestSuite(BrokerClientAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testBrokerClient(){	
		Object[] args;		
		AgentContainer myContainer = createEnvironment();
		
		//Test Case creates the Agent Under Test
		args = new Object[1];
		args[0] =  getResourceString("Requested_Service_Title");		
		AgentController clientAgent = createAgent("client",
			"MediationPatterns.BrokerPattern.BrokerClientAgent.BrokerClientAgent",
			args, myContainer);	
		
		//Test Case creates Mock Agent that interacts with the Agent Under Test		
		AgentController mockBrokerAgent = createAgent("broker",
			"MediationPatterns.BrokerPattern.BrokerClientAgent.MockBrokerAgent.MockBrokerAgent", null, myContainer); 		
		
		AgentManager.waitUntilTestFinishes(new AID("broker", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockBrokerAgent.myContainer.acquireLocalAgent (new AID("broker", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		}
	}
}

