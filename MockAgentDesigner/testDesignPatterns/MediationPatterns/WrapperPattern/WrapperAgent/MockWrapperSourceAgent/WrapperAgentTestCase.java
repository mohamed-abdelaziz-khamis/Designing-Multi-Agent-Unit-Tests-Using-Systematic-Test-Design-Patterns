/** Test Case: defines a scenario � a set of conditions � to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package MediationPatterns.WrapperPattern.WrapperAgent.MockWrapperSourceAgent;

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
public class WrapperAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockSourceAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.WrapperPattern.WrapperAgent.MockWrapperSourceAgent.MockWrapperSourceAgent");	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockSourceAgent.getString(key);
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
		return new TestSuite(WrapperAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testWrapping(){	
		Object[] args;		
		AgentContainer myContainer = createEnvironment();
		
		//Test Case creates Mock Agent that interacts with the Agent Under Test		
		args = new Object[2];		
		args[0] =  getResourceString("Translated_Content");		
		args[1] =  getResourceString("Offered_Source_Answer");
		
		AgentController mockSourceAgent = createAgent("source",
			"MediationPatterns.WrapperPattern.WrapperAgent.MockWrapperSourceAgent.MockWrapperSourceAgent",
			args, myContainer);	
			
		//Test Case creates the Agent Under Test
		args = new Object[5];
		args[0] = getResourceString("Offered_Language");		
		args[1] = getResourceString("Client_Content");
		args[2] = getResourceString("Translated_Client_Content");
		args[3] = getResourceString("Translated_Source_Answer");
		args[4] = getResourceString("Source_Answer");
	  	  
		AgentController wrapperAgent = createAgent("wrapper",
			"MediationPatterns.WrapperPattern.WrapperAgent.WrapperAgent", args, myContainer); 	
		
		AgentManager.waitUntilTestFinishes(new AID("source", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockSourceAgent.myContainer.acquireLocalAgent (new AID("source", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		}
	}
}
