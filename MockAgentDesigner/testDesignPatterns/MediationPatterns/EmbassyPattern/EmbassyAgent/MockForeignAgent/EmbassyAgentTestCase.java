/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package MediationPatterns.EmbassyPattern.EmbassyAgent.MockForeignAgent;

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
public class EmbassyAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockForeignAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.EmbassyPattern.EmbassyAgent.MockForeignAgent.MockForeignAgent");	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockForeignAgent.getString(key);
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
		return new TestSuite(EmbassyAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testEmbassy(){	
		Object[] args;		
		AgentContainer myContainer = createEnvironment();
		
		//Test Case creates Mock Agent that interacts with the Agent Under Test		
		args = new Object[4];
		args[0] =  getResourceString("Requested_Agent_Domain");	
		args[1] =  getResourceString("Digital_Certificate_Level");
		args[2] =  getResourceString("Message_Content");
		args[3] =  getResourceString("Ontology");
				
		AgentController mockForeignAgent = createAgent("foreign",
			"MediationPatterns.EmbassyPattern.EmbassyAgent.MockForeignAgent.MockForeignAgent",
			args, myContainer);	
		
		//Test Case creates the Agent Under Test
		args = new Object[7];
		args[0] = getResourceString("Offered_Agent_Domain");
		args[1] = getResourceString("Local_Digital_Certificate_Level");  
		args[2] = getResourceString("Offered_Ontology");
		args[3] = getResourceString("Foreign_Content");
		args[4] = getResourceString("Translated_Foreign_Content");
		args[5] = getResourceString("Translated_Local_Response");
		args[6] = getResourceString("Local_Response");

		
		AgentController embassyAgent = createAgent("embassy",
			"MediationPatterns.EmbassyPattern.EmbassyAgent.EmbassyAgent", args, myContainer); 		
		
		AgentManager.waitUntilTestFinishes(new AID("foreign", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockForeignAgent.myContainer.acquireLocalAgent (new AID("foreign", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		}
	}
}
