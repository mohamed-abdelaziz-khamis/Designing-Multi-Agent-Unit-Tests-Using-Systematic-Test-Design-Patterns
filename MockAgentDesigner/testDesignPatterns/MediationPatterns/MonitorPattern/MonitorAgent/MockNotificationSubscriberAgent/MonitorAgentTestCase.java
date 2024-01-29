/** Test Case: defines a scenario – a set of conditions – to which an Agent Under Test is exposed, 
 * and verifies whether this agent obeys its specification under such conditions. 
 */
package MediationPatterns.MonitorPattern.MonitorAgent.MockNotificationSubscriberAgent;

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
public class MonitorAgentTestCase extends JADETestCase {
	private static ResourceBundle resMockSubscriberAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MonitorPattern.MonitorAgent.MockNotificationSubscriberAgent.MockNotificationSubscriberAgent");	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockSubscriberAgent.getString(key);
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
		return new TestSuite(MonitorAgentTestCase.class);
	}	
	/* Each test method will be able to include agents in such environment by calling createAgent()*/
	public void testMonitoring(){	
		Object[] args;		
		AgentContainer myContainer = createEnvironment();
		
		//Test Case creates Mock Agent that interacts with the Agent Under Test		
		args = new Object[1];
		args[0] =  getResourceString("Requested_Subject_Title");		
		AgentController mockSubscriberAgent = createAgent("subscriber",
			"MediationPatterns.MonitorPattern.MonitorAgent.MockNotificationSubscriberAgent.MockNotificationSubscriberAgent",
			args, myContainer);	
		
		//Test Case creates the Agent Under Test		
		AgentController monitorAgent = createAgent("monitor",
			"MediationPatterns.MonitorPattern.MonitorAgent.MonitorAgent", null, myContainer); 		
		
		AgentManager.waitUntilTestFinishes(new AID("subscriber", AID.ISLOCALNAME));				
		JADEMockAgent mockAg = (JADEMockAgent)  mockSubscriberAgent.myContainer.acquireLocalAgent (new AID("subscriber", AID.ISLOCALNAME));
		TestResult res=((TestResultReporter) mockAg).getTestResult();							
		if(res.errorCount()>0 || res.failureCount()>0){
			System.out.println(res.toString());
		}
	}
}
