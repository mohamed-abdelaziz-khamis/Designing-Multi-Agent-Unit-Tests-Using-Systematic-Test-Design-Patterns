/** Test Case: defines a scenario � a set of conditions � to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package PairPatterns.BookingPattern.BookingServiceProviderAgent.MockBookingClientAgent;

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
public class BookingServiceProviderAgentTestCase extends JADETestCase {

	private static ResourceBundle resMockClientAgent = 
		ResourceBundle.getBundle
		("PairPatterns.BookingPattern.BookingServiceProviderAgent.MockBookingClientAgent.MockBookingClientAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/	
	private static String getResourceString(String key) {
		try {
			return resMockClientAgent.getString(key);
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
		return new TestSuite(BookingServiceProviderAgentTestCase.class);
	}		
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testSelling_Success(){
		Object[] args;
		AgentContainer myContainer = createEnvironment();
		//Test Case creates Mock Agent that interacts with the Agent Under Test
		args = new Object[1];
		args[0] =  getResourceString("Requested_Resource_Title");		
		AgentController mockClientAgent = createAgent("client",
			"PairPatterns.BookingPattern.BookingServiceProviderAgent.MockBookingClientAgent.MockBookingClientAgent",
			args, myContainer);	
		//Test Case creates the Agent Under Test		
		args = new Object[2];
		args[0] = getResourceString("Offered_Resource_Title"); //Resource title
		args[1] = getResourceString("Offered_Resource_Price");  //Reservation price		
		AgentController serviceProviderAgent = createAgent("serviceProvider",
			"PairPatterns.BookingPattern.BookingServiceProviderAgent.BookingServiceProviderAgent", 
			args, myContainer); 		
		AgentManager.waitUntilTestFinishes(new AID("client", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockClientAgent.myContainer.acquireLocalAgent
												(new AID("client", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		 }
	}	
	public void testSelling_Fail(){
		
	}	
}
