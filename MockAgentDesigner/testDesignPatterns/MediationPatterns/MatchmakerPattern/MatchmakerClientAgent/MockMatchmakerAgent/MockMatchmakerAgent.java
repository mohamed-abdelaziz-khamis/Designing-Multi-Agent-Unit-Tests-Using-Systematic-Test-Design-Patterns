/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MatchmakerPattern
 * Agent Under Test:			MatchmakerClientAgent
 * Mock Agent:					MockMatchmakerAgent
 */
package MediationPatterns.MatchmakerPattern.MatchmakerClientAgent.MockMatchmakerAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockMatchmakerAgent extends JADEMockAgent {

	private static ResourceBundle resMockMatchmakerAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MatchmakerPattern.MatchmakerClientAgent.MockMatchmakerAgent.MockMatchmakerAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockMatchmakerAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	// The title of the service to buy
	private String serviceTitle;
	
	// The list of known service provider agents
	private AID[] serviceProviderAgents;
	
	// Put agent initializations here
	protected void setup() {
		
		// Printout a welcome message
		System.out.println("Hallo! Mock-Matchmaker-Agent "+getAID().getName()+" is ready.");
	  
	  	/* The Matchmaker finds the Provider of the requested service 
	  	 * and gives its identification to the Client.
	  	 */
	       
      	// Add the behaviour serving service provider identification queries from client agents
      	addBehaviour(new RequestServiceProviderServer());
      
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Matchmaker-Agent "+getAID().getName()+" terminating.");	    
	}		
	
	/** Inner class RequestServiceProviderServer. 
	  * This is the behaviour used by Matchmaker agent to serve incoming requests 
	  * for service providers identification from client agents.
	  * If there exist some service provider agent(s) had registered for this service description type, 
	  * the matchmaker agent replies with an INFORM message with the service providers identification. 
	  * Otherwise a REFUSE message is sent back.
	  */
	   
	private class RequestServiceProviderServer extends CyclicBehaviour {
	 	  
		public void action() {	 		  		 
	 		try { 
		 		MessageTemplate mt = MessageTemplate.and(
				   		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("ServiceProvider_REQUEST_Performative"))),
				   		  MessageTemplate.MatchConversationId(getResourceString("ServiceProvider_REQUEST_ConversationID"))); 
				  
		 		ACLMessage msg = receiveMessage(myAgent, mt);
	
		 		if (msg != null) {
		 			
		 			// REQUEST Message received. Process it	 			 			
		 			serviceTitle = msg.getContent(); //REQUEST [service-title]	 			
		 			System.out.println("Target service is " + serviceTitle);
		 			    	    	     				    		
					String serviceDescriptionType = msg.getUserDefinedParameter(getResourceString("Service_Description_Type_Key"));
		 			System.out.println("Service description type is "+serviceDescriptionType);
	
		 			ACLMessage reply = msg.createReply();
		 			
			 		try {
			 			serviceProviderAgents = new AID[1];
			 			serviceProviderAgents[0] = new AID(getResourceString("Provider_Agent_Local_Name"), AID.ISLOCALNAME);

			 			// There exist some service provider agent(s) had registered for this service description type.
				 		// INFORM [serviceProviderAgents]
				 		reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
			 			reply.setContentObject(serviceProviderAgents);			 			
			 		} catch (IOException e) {			 			
			 		    // No service provider agent had registered for this service description type.
			 			// REFUSE [service-description-type-unavailable]
			 		 	reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
			 		 	reply.setContent(getResourceString("REFUSE_Content"));
			 		}
			 		sendMessage(reply);
		 		}
		 		else {
		 		    block();
		  		}  
		  	 } 
		  	 catch (ReplyReceptionFailed  e) {
		  		setTestResult(prepareMessageResult(e));
		  		e.printStackTrace();
		  		myAgent.doDelete();			
			 } 																	
			 setTestResult(new TestResult());
		  }
	}  // End of inner class RequestServiceProviderServer
}
		



