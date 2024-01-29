
/**
 * Agent Monitor: is responsible for monitoring agents’ life cycle 
 * in order to notify the Test-Case about agents’ states.
 * 
 * Since we do not want the developer of a JADE Test Case to learn about 
 * Aspect Oriented Programming (AOP) in order to call the auxiliary wait* methods, 
 * we created an auxiliary class called AgentManager. 
 * 
 * This class contains empty-body implementations of all wait* methods defined in AgentMonitor aspect. 
 * 
 * The AgentMonitor aspect intercepts all methods of the AgentManager class, 
 * and replaces each empty body method by the implementation provided by the AgentMonitor 
 * (using around advices).
 * 
 */

package MASUnitTesting;

import jade.core.AID;


/**
 * @author Mohamed Abd El Aziz
 *
 */
public class AgentManager {
	
	public static void waitUntilTestFinishes(AID agentID){		
		
	}

	public static void waitUntilAgentDie(AID agentID){
		
	}
	
	public static void waitUntilAllAgentsDie(){
		
	}
}
