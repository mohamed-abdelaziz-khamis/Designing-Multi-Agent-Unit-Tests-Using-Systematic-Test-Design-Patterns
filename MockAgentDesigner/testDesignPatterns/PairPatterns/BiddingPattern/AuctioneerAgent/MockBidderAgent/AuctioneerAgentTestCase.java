/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package PairPatterns.BiddingPattern.AuctioneerAgent.MockBidderAgent;

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
public class AuctioneerAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockBidderAgent = 
		ResourceBundle.getBundle
		("PairPatterns.BiddingPattern.AuctioneerAgent.MockBidderAgent.MockBidderAgent");		
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockBidderAgent.getString(key);
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
		return new TestSuite(AuctioneerAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testAuctioning(){
		AgentContainer myContainer = createEnvironment();		
		Object[] args;				
		//Test Case creates the Agent Under Test	
		args = new Object[4];		
		args[0] =  getResourceString("Auctioneer_Good_Title");
		args[1] =  getResourceString("Auctioneer_Good_Bid_Price");
		args[2] =  getResourceString("Auctioneer_Amount_To_Increment_Each_Round");
		args[3] =  getResourceString("Auctioneer_Good_Reserved_Price");		    	
		AgentController auctioneerAgent = createAgent("auctioneer",
			"PairPatterns.BiddingPattern.AuctioneerAgent.AuctioneerAgent", args, myContainer); 		
		//Test Case creates Mock Agent that interacts with the Agent Under Test	
		args = new Object[3];		
		args[0] = getResourceString("Bidder_Good_Title");
		args[1] = getResourceString("Bidder_Amount_To_Increment_Each_Bid");	
		args[2] = getResourceString("Bidder_Maximum_Price");		
		AgentController mockBidderAgent = createAgent("bidder",
			"PairPatterns.BiddingPattern.AuctioneerAgent.MockBidderAgent.MockBidderAgent",
			args, myContainer);				
		AgentManager.waitUntilTestFinishes(new AID("bidder", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockBidderAgent.myContainer.acquireLocalAgent
												(new AID("bidder", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		 }
	}		
}
