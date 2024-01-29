/**
 * Test Suite: which consists in a set of Test Cases and a set of operations performed to 
 * prepare the test environment before a Test Case (e.g., SellerTestCase) starts.
 * 
 * 
 * This class defines a set of concrete and abstract methods which support the implementation of 
 * the test methods (equivalent to our Test Case concept: SellerTestCase). 
 *
 */
package MASUnitTesting;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import junit.framework.TestCase;


/**
 * @author Mohamed Abd El Aziz
 *
 */

public class JADETestCase extends TestCase {

	public JADETestCase() {
		// TODO Auto-generated constructor stub
	   // createEnvironment()
	}

	/**
	 * 
	 */
	private AgentController agent;


	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub		
		super.setUp();	
	}

	/* 
	 * @see junit.framework.TestCase#tearDown()
	 * 
	 * The tearDown() method removes all agents from the environment 
	 * after the execution of each test method
	 */
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	/*
	 * The createEnvironment() method is called inside the JADETestCase constructor. 
	 * It is responsible for creating the JADE environment that will be active during the execution 
	 * of all test methods.
	 * */
	
	protected AgentContainer createEnvironment(){
		//The following command will start the main-container of the platform.
		//String args[] = {"-gui "};
		//jade.Boot.main(args);	
		
		//  Get a hold on JADE runtime
		Runtime  rt = Runtime.instance();
		
		
		// Create a default profile
		Profile p = new ProfileImpl();

		return rt.createMainContainer(p);
	}
	
	protected AgentController createAgent(String agentName, String className, Object[] args, AgentContainer ac) {
		// TODO Auto-generated constructor stub
		
		//arg += agentName + ":" + className + "( " + args + " ) ";
		
		try {
			agent = ac.createNewAgent(agentName, className, args);
			//Fire up the agent
			agent.start();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return agent;				  
	}
}
