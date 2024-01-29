/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				BrokerPattern
 * Agent Under Test:			BrokerAgent
 * Mock Agent:					MockBrokerServiceProviderAgent
 */
package MediationPatterns.BrokerPattern.BrokerAgent.MockBrokerServiceProviderAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockBrokerServiceProviderAgent extends JADEMockAgent {
	private static ResourceBundle resMockServiceProviderAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.BrokerPattern.BrokerAgent.MockBrokerServiceProviderAgent.MockBrokerServiceProviderAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockServiceProviderAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}

	// The catalogue of service for sale (maps the title of a service to its price)
	private Hashtable<String, Float> catalogue;
	private String serviceTitle;
	private float servicePrice;
	  
    //Broker Agent ID	
    private AID broker;
  
    // Put agent initializations here
    protected void setup() {

	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Service Provider-Agent "+getAID().getName()+" is ready.");	    		  
	  
	  broker = new AID("broker", AID.ISLOCALNAME);

	  // Get the title of the service to provide as a start-up argument.
	  Object[] args = getArguments();		  
		  
	  if (args != null && args.length > 0) {
			
		  serviceTitle = (String) args[0];
	      servicePrice = Float.parseFloat((String)args[1]);
	  	  
	      // Create the catalogue
		  catalogue = new Hashtable<String, Float>();
		
		  catalogue.put(serviceTitle, servicePrice);
			
	      System.out.println(serviceTitle + " inserted into catalogue. " 
	        			+ ", Price = " + servicePrice);
	        
	      // Perform the subscription request to the broker agent
	      addBehaviour(new RequestSubscriptionPerformer());

	      // Add the behaviour serving queries from broker agents
	      addBehaviour(new OfferRequestsServer());
	
	      // Add the behaviour serving purchase orders from broker agents
	      addBehaviour(new PurchaseOrdersServer());
	    
		  addBehaviour(new WakerBehaviour(this, Long.parseLong(getResourceString("Waker_Behaviour_Period"))) {
		
			  protected void handleElapsedTimeout() {
				  // Perform the unsubscription request to the broker agent
				  addBehaviour(new RequestUnsubscriptionPerformer());
			  }
		  } );
	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No service title specified to be added in catalogue");
		  doDelete();
	  }
    }  

	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Mock-Service Provider-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class RequestSubscriptionPerformer. 
	 *  This is the behaviour used by Service Provider agent to request subscription 
	 *  from Broker agent for the required service.
	 */		
	private class RequestSubscriptionPerformer extends Behaviour {				   
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {
		  try {
			  switch (step) {	
				case 0:			    	
				  // Send the request to the broker agent
				  ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("Subscription_REQUEST_Performative")));			      
				      
				  request.addReceiver(broker);	      
				  request.setContent("subscription");
				  request.setConversationId(getResourceString("Subscription_REQUEST_ConversationID"));
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
				
				  request.addUserDefinedParameter(getResourceString("Service_Description_Type_Key"),
						  						  getResourceString("Service_Description_Type_Value"));
				  
				  request.addUserDefinedParameter(getResourceString("Service_Description_Name_Key"),
												  getResourceString("Service_Description_Name_Value"));
	
				  sendMessage(request);			      
					  
				  // Prepare the template to get subscription accept proposal
				  mt = MessageTemplate.and(
				  		 MessageTemplate.MatchPerformative(ACLMessage.getInteger(
				  				 						getResourceString("Subscription_ACCEPT_PROPOSAL_Performative"))),
				   		 MessageTemplate.and(
				   		 MessageTemplate.MatchConversationId(
				   				 						getResourceString("Subscription_ACCEPT_PROPOSAL_ConversationID")),
				   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
			      step = 1;			      
			      break;			    		   
				case 1:
				  // Receive the reply (subscription accept proposal/refusal) from the broker agent
				  reply = receiveMessage(myAgent, mt);			    				      
				  if (reply != null) {				      
				      // Reply received
					  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [subscription-accepted]				  	    	 				    	  
					      System.out.println(reply.getContent() + ": Broker agent "
					    		  			+ reply.getSender().getName());				    	 
					      step = 2;				     
					  }				      
					  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [subscription-refused]
					   	  System.out.println(reply.getContent() + ": Broker agent "
		        		  					+ reply.getSender().getName());	
					   	  myAgent.doDelete();
					  }				      
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
		  return (step == 2);
	  }
	}  // End of inner class RequestSubscriptionPerformer
	
	/** Inner class OfferRequestsServer. 
	 * This is the behaviour used by Service Provider agents to serve incoming requests for offer from broker agents.
	 * If the requested service is in the local catalogue the service provider agent replies with a PROPOSE message 
	 * specifying the price. Otherwise a REFUSE message is sent back.
	 */   
	private class OfferRequestsServer extends CyclicBehaviour {
		   
	  public void action() {
		try {
			MessageTemplate mt = MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("CFP_Performative"))),
		    		  MessageTemplate.MatchConversationId(getResourceString("CFP_ConversationID")));
				
			ACLMessage msg = receiveMessage(myAgent, mt);
				
		    if (msg != null) {
		 	      
		      // CFP Message received. Process it
		      String serviceTitle = msg.getContent(); //CFP [service-title]
		 	      
		      ACLMessage reply = msg.createReply();
		      Float price = (Float) catalogue.get(serviceTitle);
		
		      if (price != null) {	 	    	  	 	    	  	
		    	// The requested service is available for sale. Reply with the price
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("PROPOSE_Performative")));
		        reply.setContent(price.toString());	 	        
		      }
		      else {
		        // The requested service is NOT available for sale.
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
		        reply.setContent(getResourceString("REFUSE_Content")); //REFUSE [service-unavailable]
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
	}  // End of inner class OfferRequestsServer
	 	
	/** Inner class PurchaseOrdersServer.
	* This is the behaviour used by Service Provider agents to serve incoming offer acceptances (i.e. purchase orders) 
	* from broker agents. The service provider agent removes the purchased service from its catalogue and replies with
	* an INFORM message to notify the broker agent that the purchase has been successfully completed.
	*/
	   
	private class PurchaseOrdersServer extends CyclicBehaviour {
	 	  
	  public void action() {
		  try {
			  MessageTemplate mt = MessageTemplate.and(
				  		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("ACCEPT_PROPOSAL_Performative"))),
				  		  MessageTemplate.MatchConversationId(getResourceString("ACCEPT_PROPOSAL_ConversationID"))); 
				  
			  ACLMessage msg = receiveMessage(myAgent, mt);
	
			  if (msg != null) {
		 	     // ACCEPT_PROPOSAL Message received. Process it
		 	     String serviceTitle = msg.getContent(); //ACCEPT_PROPOSAL [service-title]
		
		 	      ACLMessage reply = msg.createReply();	      
		 	      Float price = (Float) catalogue.remove(serviceTitle);
		 	      
		 	      if (price != null) {	    	  
		 	    	// The requested service is still exiting and not sold to another broker.	        
		 	    	reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
		 	        System.out.println(serviceTitle+" sold to agent "+msg.getSender().getName());
		 	      }
		 	      else {
		 	        // The requested service has been sold to another broker in the meanwhile .
		 	        reply.setPerformative(ACLMessage.getInteger(getResourceString("FAILURE_Performative")));
		 	        reply.setContent(getResourceString("FAILURE_Content"));
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
	 }  // End of inner class PurchaseOrdersServer
	
	/** Inner class RequestUnsubscriptionPerformer. 
	 *  This is the behaviour used by Service Provider agent to request unsubscription 
	 *  from Broker agent.
	 */		
	private class RequestUnsubscriptionPerformer extends Behaviour {				   
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {
		try {	
			switch (step) {	
				case 0:			    	
				  // Send the request to the broker agent
				  ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("Unsubscription_REQUEST_Performative")));			      
				      
				  request.addReceiver(broker);	      
				  request.setContent("unsubscription");
				  request.setConversationId(getResourceString("Unsubscription_REQUEST_ConversationID"));
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
					  
				  sendMessage(request);			      
					  
				  // Prepare the template to get unsubscription accept proposal
				  mt = MessageTemplate.and(
				  		 MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Unsubscription_ACCEPT_PROPOSAL_Performative"))),
				   		 MessageTemplate.and(
				   		 MessageTemplate.MatchConversationId(getResourceString("Unsubscription_ACCEPT_PROPOSAL_ConversationID")),
				   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
			      step = 1;			      
			      break;			    		   
				case 1:
				  // Receive the reply (unsubscription accept proposal/refusal) from the broker agent
				  reply = receiveMessage(myAgent, mt);			    				      
				  if (reply != null) {				      
				      // Reply received
					  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [unsubscription-accepted]				  	    	 				    	  
					      System.out.println(reply.getContent() + ": Broker agent "
			        		  				+ reply.getSender().getName());				    	 
					      step = 2;				     
					  }				      
					  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [unsubscription-refused]
					   	  System.out.println(reply.getContent() + ": Broker agent "
					   			  			+ reply.getSender().getName());		
					   	  myAgent.doDelete();
					  }				      
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
	    return (step == 2);
	  }
	}  // End of inner class RequestUnsubscriptionPerformer
}
		



