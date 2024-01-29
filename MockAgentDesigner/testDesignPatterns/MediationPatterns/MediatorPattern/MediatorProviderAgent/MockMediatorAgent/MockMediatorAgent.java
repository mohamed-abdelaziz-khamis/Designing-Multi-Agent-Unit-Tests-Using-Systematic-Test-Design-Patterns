/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MediatorPattern
 * Agent Under Test:			MediatorProviderAgent
 * Mock Agent:					MockMediatorAgent
 */
package MediationPatterns.MediatorPattern.MediatorProviderAgent.MockMediatorAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockMediatorAgent extends JADEMockAgent {
	private static ResourceBundle resMockMediatorAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MediatorPattern.MediatorProviderAgent.MockMediatorAgent.MockMediatorAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockMediatorAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	// The catalogue of request for result (maps the title of a request to its sub-requests)
	private Hashtable<String, ArrayList<String>> catalogue;
	
	// The title of the request
	private String requestTitle;
	
	// The title of the sub-request
	private String subRequestTitle;

	// The list of sub requests
	private ArrayList<String> subRequests;
	
	// Service Provider Agent ID
	private AID provider;
	
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Mediator-Agent "+getAID().getName()+" is ready.");
	  
	  /* The mediator coordinates the cooperative behavior of the colleagues 
	   * and has acquaintance models of all colleague agents.
	  */
	  
	  provider = new AID("provider", AID.ISLOCALNAME);
	    
	  // Get the title of the request to provide result as a start-up argument.
	  Object[] args = getArguments();		 
		     	
  	  if (args != null && args.length > 0) {
			
  		  requestTitle = (String) args[0];
  		  subRequestTitle = (String) args[1];
  		  
  		  // Create the list of sub requests
  		  subRequests = new ArrayList<String>();
  		  subRequests.add(subRequestTitle);
	  	  
	      // Create the catalogue
		  catalogue = new Hashtable<String, ArrayList<String>>();		
		  catalogue.put(requestTitle, subRequests);
			
	      System.out.println(requestTitle + " inserted into catalogue. " +
	      					"It could be divided into the following sub requests: ");
	      
	      for (int i = 0; i < subRequests.size(); ++i)
	    	  System.out.println(subRequests.get(i));
	              
	      // Add the behaviour serving subscription/unsubscription queries from service provider agents
	  	  addBehaviour(new YellowPageServer());

		  addBehaviour(new WakerBehaviour(this, Long.parseLong(getResourceString("Waker_Behaviour_Period"))) {
				
			  protected void handleElapsedTimeout() {
		      	  // Perform the sub-request for sub-result
			      addBehaviour(new RequestSubResultPerformer());
			  }
		  } );

	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No request title specified to be added in catalogue");
		  doDelete();
	  }
      
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Mediator-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class YellowPageServer. 
	  * This is the behaviour used by the Mediator agent to serve incoming requests 
	  * for subscription/unsubscription from service provider agents.
	  * If the subscription/unsubscription is done successfully, the mediator agent replies with 
	  * an ACCEPT_PROPOSAL message. Otherwise a REFUSE message is sent back.
	*/   	
	private class YellowPageServer extends CyclicBehaviour {		   	  
	  public void action() {
		try{
			MessageTemplate mt = MessageTemplate.and(
				    MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Subscription_REQUEST_Performative"))),
				    MessageTemplate.MatchConversationId(getResourceString("Subscription_REQUEST_ConversationID")));			 	
			
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
						subRequestTitle = msg.getUserDefinedParameter("ServiceDescriptionName");
						sd.setName(subRequestTitle);
	
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
	

	 /** Inner class RequestSubResultPerformer
    This is the behaviour used by Mediator agent to request service provider agents the sub-result. */	
	private class RequestSubResultPerformer extends Behaviour {		

	  private MessageTemplate mt; 		// The template to receive replies
	  private int step = 0;	  	  
	  
	  public void action() {
		 try{ 
			 switch (step) {
			    case 0:			    	
			      // Send the sub-request to all known service provider agents
			      ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("Sub_Result_REQUEST_Performative")));			      

			      request.addReceiver(provider);
			      request.setContent(subRequestTitle);
			      request.setConversationId(getResourceString("Sub_Result_REQUEST_ConversationID"));
			      request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				  				  
				  sendMessage(request);
				  
				  // Prepare the template to get sub-result
			      mt=MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Sub_Result_INFORM_Performative"))),
			    		  MessageTemplate.and(
			    		  MessageTemplate.MatchConversationId(getResourceString("Sub_Result_INFORM_ConversationID")),
			              MessageTemplate.MatchInReplyTo(request.getReplyWith())));
			      
			      step = 1;			      
			      break;			    
			    case 1:
			    	// Receive the reply (sub result inform/failure) from the service provider agents
			    	ACLMessage reply = receiveMessage(myAgent, mt);		    	
			    	if (reply != null) {				        
			    		// Reply received
				        if (reply.getPerformative() == ACLMessage.INFORM) {
				          // This is a sub result 
					      System.out.println(subRequestTitle+" successfully informed by provider agent " + reply.getSender().getName());
					      System.out.println(", sub-result = "+ reply.getContent());
				        }
					    else { //FAILURE [sub-request-unavailable]
						  System.out.println(reply.getContent()+ ": Provider agent " + reply.getSender().getName()); 
					    }
						step = 2;
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
	}  // End of inner class RequestSubResultPerformer
}
		



