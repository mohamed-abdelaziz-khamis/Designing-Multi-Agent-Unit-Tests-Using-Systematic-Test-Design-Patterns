/**
 * Design Pattern Category:		PairPatterns
 * Design Pattern:				SubscriptionPattern
 * Agent Under Test:			SubscriptionServiceProviderAgent
 * Mock Agent:					MockSubscriptionYellowPageAgent
 */

package PairPatterns.SubscriptionPattern.SubscriptionServiceProviderAgent.MockSubscriptionYellowPageAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.wrapper.ControllerException;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;

import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockSubscriptionYellowPageAgent extends JADEMockAgent {
	
	private static ResourceBundle resMockSubscriptionYellowPageAgent = 
		ResourceBundle.getBundle
	("PairPatterns.SubscriptionPattern.SubscriptionServiceProviderAgent.MockSubscriptionYellowPageAgent.MockSubscriptionYellowPageAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockSubscriptionYellowPageAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Yellow Page-Agent "+getAID().getName()+" is ready.");
			  
      // Add the behaviour serving subscription/unsubscription queries from service provider agents
	  addBehaviour(new YellowPageServer());
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Mock-Yellow Page-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class YellowPageServer. 
	  * This is the behaviour used by Yellow Page agent to serve incoming requests 
	  * for subscription/unsubscription from service provider agents.
	  * If the subscription/unsubscription is done successfully, the yellow page agent replies with an ACCEPT_PROPOSAL message. 
	  * Otherwise a REFUSE message is sent back.
	*/   
	
	private class YellowPageServer extends CyclicBehaviour {		   	  
	  public void action() {
		  try { 
			 MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("REQUEST_Performative"))),
			    		  MessageTemplate.MatchConversationId(getResourceString("REQUEST_ConversationID")));			 	
			 ACLMessage msg = receiveMessage(myAgent, mt);			 	
		 	 if (msg != null) {		 	      
			 	 // REQUEST Message received. Process it
			 	 String subscriptionType = msg.getContent(); //REQUEST [subscription]		
			 	 Agent senderAgent = null;
				 try {
					 senderAgent = getContainerController().getAgent(myAgent.getLocalName()).
					     					myContainer.acquireLocalAgent(msg.getSender());
				 } catch (ControllerException e) {				
					 e.printStackTrace();
				 }	 	      			 	      
			 	 ACLMessage reply = msg.createReply();			    	  
			 	 if (subscriptionType == "subscription") {	 	    	  	 	    	  		 	    	
				 	   try {
				 		  // Register the providing service in the yellow pages
					 	  DFAgentDescription dfd = new DFAgentDescription();
					 	  dfd.setName(senderAgent.getAID());					 			    
					 	  ServiceDescription sd = new ServiceDescription();					 			 
						  String serviceDescriptionTypeValue = msg.getUserDefinedParameter(
								  								   getResourceString("Service_Description_Type_Key"));
						  String serviceDescriptionNameValue = msg.getUserDefinedParameter(
								  								   getResourceString("Service_Description_Name_Key"));			
					 	  sd.setType(serviceDescriptionTypeValue);
					 	  sd.setName(serviceDescriptionNameValue);
					 	  dfd.addServices(sd);					 	  
				 		  DFService.register(senderAgent, dfd);					 	    	 
				 		  // The registeration is done successfully with the Directory Facilitator. 
				 		  // Reply with subscription accepted message
			  		 	  reply.setPerformative(ACLMessage.getInteger(getResourceString("Subscription_ACCEPT_PROPOSAL_Performative")));
			  		 	  reply.setContent(getResourceString("Subscription_ACCEPT_PROPOSAL_Content"));		
				 	   }
				 	   catch (FIPAException fe) {
					      // The registeration is failed with the Directory Facilitator. 
				 		  // Reply with subscription refused message
					      reply.setPerformative(ACLMessage.getInteger(getResourceString("Subscription_REFUSE_Performative")));
					      reply.setContent(getResourceString("Subscription_REFUSE_Content"));		 	         
				 	   }	 	    	 	        
			 	 }	 	      
			 	 else if (subscriptionType == "unsubscription"){
				 	    // Deregister from the yellow pages
				 	  try {
				 	      DFService.deregister(senderAgent);
					 	  // The deregisteration is done successfully from the Directory Facilitator. 
					 	  // Reply with unsubscription accepted message
					      reply.setPerformative(ACLMessage.getInteger(getResourceString("Unsubscription_ACCEPT_PROPOSAL_Performative")));
					      reply.setContent(getResourceString("Unsubscription_ACCEPT_PROPOSAL_Content"));
				 	  }
				 	  catch (FIPAException fe) {
					      // The deregisteration is failed from the Directory Facilitator. 
					      // Reply with unsubscription refused message
				 	      reply.setPerformative(ACLMessage.getInteger(getResourceString("Unsubscription_REFUSE_Performative")));
					      reply.setContent(getResourceString("Unsubscription_REFUSE_Content"));
				      }
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
	}  // End of inner class YellowPageServer
}