/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				BrokerPattern
 * Agent Under Test:			BrokerClientAgent
 * Mock Agent:					MockBrokerAgent
 */
package MediationPatterns.BrokerPattern.BrokerClientAgent.MockBrokerAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockBrokerAgent extends JADEMockAgent {
	private static ResourceBundle resMockBrokerAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.BrokerPattern.BrokerClientAgent.MockBrokerAgent.MockBrokerAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockBrokerAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	// The title of the service to buy
	private String serviceTitle;
	
	private String providerReplyContent;
	private int providerReplyPerformative;
	  
  
	// Put agent initializations here
	protected void setup() {
		
		// Printout a welcome message
		System.out.println("Hallo! Mock-Broker-Agent "+getAID().getName()+" is ready.");
	  
	  	/* The broker's role is that of arbiter and intermediary, 
	  	 * accessing services of one agent to satisfy the requests of another.
	  	 */
	        
      	// Add the behaviour serving service queries from client agents
      	addBehaviour(new RequestServiceServer());
      
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Broker-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class RequestServiceServer. 
	  * This is the behaviour used by Broker agent to serve incoming requests 
	  * for service from service provider agents.
	  * If there exist some service provider agent(s) had regesitered for this service description type, 
	  * the broker agent replies with an ACCEPT_PROPOSAL message. Otherwise a REFUSE message is sent back.
	  */
	   
	private class RequestServiceServer extends CyclicBehaviour {
	 	  
	 	public void action() {	 		  		 
	 		try { 
		 		MessageTemplate mt = MessageTemplate.and(
				   		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("REQUEST_Performative"))),
				   		  MessageTemplate.MatchConversationId(getResourceString("REQUEST_ConversationID"))); 
				  
		 		ACLMessage msg = receiveMessage(myAgent, mt);
	
		 		if (msg != null) {
		 			
		 			// REQUEST Message received. Process it
		 			 			
		 			serviceTitle = msg.getContent(); //REQUEST [service-title]	 			
		 			System.out.println("Target service is "+serviceTitle);
		 			    	    	    
	 				    		
					String serviceDescriptionType = msg.getUserDefinedParameter(
							 getResourceString("Service_Description_Type_Key"));
		 			System.out.println("Service description type is "+serviceDescriptionType);
	 					  
		 		  	ACLMessage reply = msg.createReply();
		 		  		

			 		// There exist some service provider agent(s) had registered for this service description type.
			 		// ACCEPT_PROPOSAL [service-description-type-available]
			 		reply.setPerformative(ACLMessage.getInteger(getResourceString("ACCEPT_PROPOSAL_Performative")));
			 		reply.setContent(getResourceString("ACCEPT_PROPOSAL_Content"));
			 		 	 
			 		sendMessage(reply);
			 		 	 
				    // Perform the request
					myAgent.addBehaviour(new ConfirmServiceServer());
	 				          
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
	}  // End of inner class RequestServiceServer
	
	/** Inner class ConfirmServiceServer. 
	  * This is the behaviour used by Broker agent to serve incoming confirmations 
	  * for service request acceptance from client agents.
	  * If the purchase has been successfully completed, the broker agent replies with 
	  * an INFORM message. Otherwise a FAILURE message is sent back.
	  */
	   
	private class ConfirmServiceServer extends CyclicBehaviour {
	 	  
	 	  public void action() {	 		  		 
	 		 try {
		 		 MessageTemplate mt = MessageTemplate.and(
				    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("CONFIRM_Performative"))),
				    		  MessageTemplate.MatchConversationId(getResourceString("CONFIRM_ConversationID"))); 
				  
		 		 ACLMessage msg = receiveMessage(myAgent, mt);
	
		 		 if (msg != null) {
		 			// CONFIRM Message received. Process it
		 			serviceTitle = msg.getContent(); //CONFIRM [service-title]
		 			
		 	      	ACLMessage reply = msg.createReply();	
		 	      
		 	      	// Perform the service request from the service provider agents
		 	      	providerReplyPerformative = ACLMessage.getInteger(getResourceString("INFORM_Performative"));
		 	      	providerReplyContent = getResourceString("INFORM_Content");
		 	       	      
			      	// Forward the reply of the service provider agent (service inform / failure)	        
			      	reply.setPerformative(providerReplyPerformative);
			      	reply.setContent(providerReplyContent);
		 	     
			      	sendMessage(reply);
		 	      	
			      	System.out.println(serviceTitle + " Reply: " + providerReplyContent + 
			      					" is sent to client agent "+msg.getSender().getName());
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
	}  // End of inner class ConfirmServiceServer		
}