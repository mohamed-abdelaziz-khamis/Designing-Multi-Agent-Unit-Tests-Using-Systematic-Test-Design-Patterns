/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				BrokerPattern
 * Agent Under Test:			BrokerServiceProviderAgent
 * Mock Agent:					MockBrokerAgent
 */
package MediationPatterns.BrokerPattern.BrokerServiceProviderAgent.MockBrokerAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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


@SuppressWarnings("all")
public class MockBrokerAgent extends JADEMockAgent {
	private static ResourceBundle resMockBrokerAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.BrokerPattern.BrokerServiceProviderAgent.MockBrokerAgent.MockBrokerAgent");
	
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
	
	// Service Provider Agent ID
	private AID serviceProvider;
	
	private String providerReplyContent;
	private int providerReplyPerformative;
	  
  
	// Put agent initializations here
	protected void setup() {
		
		// Printout a welcome message
		System.out.println("Hallo! Mock-Broker-Agent "+getAID().getName()+" is ready.");
	  
	  	/* The broker's role is that of arbiter and intermediary, 
	  	 * accessing services of one agent to satisfy the requests of another.
	  	 */
	  
		serviceProvider = new AID("serviceProvider", AID.ISLOCALNAME);
		serviceTitle = getResourceString("Requested_Service_Title");
		  
      	// Add the behaviour serving subscription/unsubscription queries from service provider agents
	  	addBehaviour(new YellowPageServer());
	           	
	    // Perform the service request from the service provider agents
	    addBehaviour(new RequestServicePerformer());
      
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Broker-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class YellowPageServer. 
	  * This is the behaviour used by Broker agent to serve incoming requests 
	  * for subscription/unsubscription from service provider agents.
	  * If the subscription/unsubscription is done successfully, the broker agent replies with 
	  * an ACCEPT_PROPOSAL message. Otherwise a REFUSE message is sent back.
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
					 	// Register the provided service in the yellow pages
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
	
	/** Inner class RequestServicePerformer
    This is the behaviour used by Broker agents to request service provider agents the target service. */	
	private class RequestServicePerformer extends Behaviour {		
  
		private AID bestServiceProvider; 		// The agent who provides the best offer 
		private float bestPrice;  				// The best offered price
		
		private int repliesCnt = 0; 			// The counter of replies from service provider agents
		private MessageTemplate mt; 			// The template to receive replies
		private int step = 0;	  	  
  
		public void action() {
			try {	
				switch (step) {
				    case 0:			    	
				      // Send the call for proposal to all service providers
				      ACLMessage cfp = new ACLMessage(ACLMessage.getInteger(getResourceString("CFP_Performative")));			      
				      
				      cfp.addReceiver(serviceProvider);			      
				      cfp.setContent(serviceTitle);
				      cfp.setConversationId(getResourceString("CFP_ConversationID"));
				      cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
					  				  
				      sendMessage(cfp);
					  
					  // Prepare the template to get proposals
				      mt=MessageTemplate.and(
				    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("PROPOSE_Performative"))),
				    		  MessageTemplate.and(
				    		  MessageTemplate.MatchConversationId(getResourceString("PROPOSE_ConversationID")),
				              MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));
				      
				      step = 1;			      
				      break;			    
				    case 1:
				    	// Receive the reply (proposals/refusals) from the service provider agents
				    	ACLMessage reply = receiveMessage(myAgent, mt);			    	
				    	if (reply != null) {
					        
				    		// Reply received
					        if (reply.getPerformative() == ACLMessage.PROPOSE) {
					          // This is an offer 
					          float price = Float.parseFloat(reply.getContent());
					          if (bestServiceProvider == null || price < bestPrice) {
						            // This is the best offer at present
						            bestPrice = price;
						            bestServiceProvider = reply.getSender();
						      }
					        }						    
					        step = 2; 
						 }
				      else {
				        block();
				      }
				      break;			    
				    case 2:
				      // Send the purchase order to the service provider agent
				      ACLMessage order = new ACLMessage(ACLMessage.getInteger(getResourceString("ACCEPT_PROPOSAL_Performative")));			      
				      order.addReceiver(bestServiceProvider);
				      order.setContent(serviceTitle);
				      order.setConversationId(getResourceString("ACCEPT_PROPOSAL_ConversationID"));
				      order.setReplyWith("order"+System.currentTimeMillis());	// Unique value		      
				      sendMessage(order);			      
				      // Prepare the template to get the purchase order reply
				      mt = MessageTemplate.and(MessageTemplate.MatchPerformative(Integer.parseInt(getResourceString("INFORM_Performative"))),
				  				 			   MessageTemplate.and(
				    		  				   MessageTemplate.MatchConversationId(getResourceString("INFORM_ConversationID")),
				                               MessageTemplate.MatchInReplyTo(order.getReplyWith())));
				      step = 3;	      
				      break;     
				    case 3:			    		   
					  // Receive the purchase order reply (service inform/failure) from the service provider agent
					  reply = receiveMessage(myAgent, mt);	    			      
					  if (reply != null && 
							  (reply.getPerformative() == ACLMessage.INFORM || 
							   reply.getPerformative() == ACLMessage.FAILURE) ) {		
						  // Reply received: INFORM [service-price] or FAILURE [service-failure] 				  
						  providerReplyContent = reply.getContent();
						  providerReplyPerformative = reply.getPerformative();
						  System.out.println(reply.getContent() + ": Service Provider agent " + reply.getSender().getName());				        	
					    step = 4;
					 }
					 else {
					     block();
					 }
					 break;		   
			  	}  
		  	 } 
		  	 catch (ReplyReceptionFailed  e) {
		  			setTestResult(prepareMessageResult(e));
		  			e.printStackTrace();
		  			myAgent.doDelete();			
			 } 																	
			 setTestResult(new TestResult());
	  }
	 
	  public boolean done() {
		if (step == 2 && bestServiceProvider == null){ //target service not available for sale
			  providerReplyContent = getResourceString("FAILURE_Content");
			  providerReplyPerformative = ACLMessage.getInteger(getResourceString("FAILURE_Performative"));
		}
		return ((step == 2 && bestServiceProvider == null) || step == 4);
	  }
	}  // End of inner class RequestServicePerformer		
}