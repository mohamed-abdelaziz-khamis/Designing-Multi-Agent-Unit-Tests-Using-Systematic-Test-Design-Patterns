/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package PairPatterns.CallForProposalPattern.BuyerAgent.MockSellerAgent;

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
public class BuyerAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockSellerAgent = 
		ResourceBundle.getBundle
		("PairPatterns.CallForProposalPattern.BuyerAgent.MockSellerAgent.MockSellerAgent");	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockSellerAgent.getString(key);
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
		return new TestSuite(BuyerAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testBuying(){
		AgentContainer myContainer = createEnvironment();
		Object[] args;		
		//Test Case creates the Agent Under Test	
		args = new Object[1];
		args[0] =  getResourceString("Requested_Service_Title");		
		AgentController buyerAgent = createAgent("buyer",
			"PairPatterns.CallForProposalPattern.BuyerAgent.BuyerAgent", args, myContainer); 		
		//Test Case creates Mock Agent that interacts with the Agent Under Test	
		args = new Object[2];
		args[0] = getResourceString("Offered_Resource_Title"); //Resource title
		args[1] = getResourceString("Offered_Resource_Price");  //Resource price	
		AgentController mockSellerAgent = createAgent("seller",
			"PairPatterns.CallForProposalPattern.BuyerAgent.MockSellerAgent.MockSellerAgent",
			args, myContainer);		
		AgentManager.waitUntilTestFinishes(new AID("seller", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockSellerAgent.myContainer.acquireLocalAgent
												(new AID("seller", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		 }
	}		
}
