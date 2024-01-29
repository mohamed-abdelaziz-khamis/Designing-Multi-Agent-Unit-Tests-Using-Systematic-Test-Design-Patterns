/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package MediationPatterns.WrapperPattern.WrapperSourceAgent.MockWrapperAgent;

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
public class WrapperSourceAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockWrapperAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.WrapperPattern.WrapperSourceAgent.MockWrapperAgent.MockWrapperAgent");	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockWrapperAgent.getString(key);
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
		return new TestSuite(WrapperSourceAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testSourceAnswer(){	
		Object[] args;		
		AgentContainer myContainer = createEnvironment();
		
		//Test Case creates the Agent Under Test
		args = new Object[2];		
		args[0] =  getResourceString("Translated_Content");		
		args[1] =  getResourceString("Offered_Source_Answer");
		
		AgentController localAgent = createAgent("source",
			"MediationPatterns.WrapperPattern.WrapperSourceAgent.WrapperSourceAgent",
			args, myContainer);	
		
		
		//Test Case creates Mock Agent that interacts with the Agent Under Test
		args = new Object[5];
		args[0] = getResourceString("Offered_Language");		
		args[1] = getResourceString("Client_Content");
		args[2] = getResourceString("Translated_Client_Content");
		args[3] = getResourceString("Translated_Source_Answer");
		args[4] = getResourceString("Source_Answer");
		
		AgentController mockWrapperAgent = createAgent("wrapper",
			"MediationPatterns.WrapperPattern.WrapperSourceAgent.MockWrapperAgent.MockWrapperAgent", args, myContainer); 
		
		AgentManager.waitUntilTestFinishes(new AID("wrapper", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockWrapperAgent.myContainer.acquireLocalAgent (new AID("wrapper", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		 }
	}
}

