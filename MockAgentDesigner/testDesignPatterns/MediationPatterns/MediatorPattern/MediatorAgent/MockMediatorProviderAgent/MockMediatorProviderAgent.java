/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MediatorPattern
 * Agent Under Test:			MediatorAgent
 * Mock Agent:					MockMediatorProviderAgent
 */
package MediationPatterns.MediatorPattern.MediatorAgent.MockMediatorProviderAgent;

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
public class MockMediatorProviderAgent extends JADEMockAgent {
	private static ResourceBundle resMockProviderAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MediatorPattern.MediatorAgent.MockMediatorProviderAgent.MockMediatorProviderAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockProviderAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}

	// The catalogue of sub-request for sub-result (maps the sub-request to its sub-result)
	private Hashtable<String, String> catalogue;
	
	// The title of the sub-request
	private String subRequestTitle;
	
	private String subResult;

    //Mediator Agent ID	
    private AID mediator;
    
    // Put agent initializations here
    protected void setup() {

	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Provider-Agent "+getAID().getName()+" is ready.");
	  
	  mediator = new AID("mediator", AID.ISLOCALNAME);
	  
	  // Get the title of the service to provide as a start-up argument.
	  Object[] args = getArguments();		  
		  
	  if (args != null && args.length > 0) {
			
		  subRequestTitle = (String) args[0];
		  subResult = (String) args[1];
	  	  
	      // Create the catalogue
		  catalogue = new Hashtable<String, String>();
		
		  catalogue.put(subRequestTitle, subResult);
			
	      System.out.println(subRequestTitle + " inserted into catalogue. " 
	        			+ ", sub-result = " + subResult);
	        
	      // Perform the subscription request to the mediator agent
	      addBehaviour(new RequestSubscriptionPerformer());
	      
	      // Add the behaviour serving sub-requests from mediator agents
	      addBehaviour(new SubRequestsServer());
	      
		  addBehaviour(new WakerBehaviour(this, Long.parseLong(getResourceString("Waker_Behaviour_Period"))) {
				
			  protected void handleElapsedTimeout() {
				  // Perform the unsubscription request to the mediator agent
				  addBehaviour(new RequestUnsubscriptionPerformer());
			  }
		  } );
	
	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No sub-request specified to be added in catalogue");
		  doDelete();
	  }
    }  

	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Mock-Provider-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class RequestSubscriptionPerformer. 
	 *  This is the behaviour used by Service Provider agent to request subscription 
	 *  from Mediator agent for the required service.
	 */		
	private class RequestSubscriptionPerformer extends Behaviour {				   
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {
		try{  
			switch (step) {	
				case 0:			    	
				  // Send the request to the mediator agent
				  ACLMessage request = new ACLMessage(ACLMessage.getInteger(
						  					getResourceString("Subscription_REQUEST_Performative")));			      
				      
				  request.addReceiver(mediator);	      
				  request.setContent("subscription");
				  request.setConversationId(getResourceString("Subscription_REQUEST_ConversationID"));
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
				
				  request.addUserDefinedParameter("ServiceDescriptionName", subRequestTitle);
	
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
				  // Receive the reply (subscription accept proposal/refusal) from the mediator agent
				  reply = receiveMessage(myAgent, mt);			    				      
				  if (reply != null) {				      
				      // Reply received
					  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [subscription-accepted]				  	    	 				    	  
					      System.out.println(reply.getContent() + ": Mediator agent "
					    		  			+ reply.getSender().getName());				    	 
					      step = 2;				     
					  }				      
					  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [subscription-refused]
					   	  System.out.println(reply.getContent() + ": Mediator agent "
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
	
	/** Inner class SubRequestsServer. 
	 * This is the behaviour used by Provider agents to serve incoming sub-requests from mediator agents.
	 * If the sub-request is in the local catalogue, the provider agent replies with an INFORM message 
	 * specifying the sub-result. Otherwise a FAILURE message is sent back.
	 */   
	private class SubRequestsServer extends CyclicBehaviour {
		   
	  public void action() {
		try{  
			MessageTemplate mt = MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Sub_Result_REQUEST_Performative"))),
		    		  MessageTemplate.MatchConversationId(getResourceString("Sub_Result_REQUEST_ConversationID")));
				
			ACLMessage msg = receiveMessage(myAgent, mt);
				
		    if (msg != null) {
		 	      
		      // REQUEST Message received. Process it
		      String subRequest = msg.getContent(); //REQUEST [sub-request]
		 	      
		      ACLMessage reply = msg.createReply();
		      String subResult = catalogue.get(subRequest);
		
		      if (subResult != null) {	 	    	  	 	    	  	
		    	// The sub-request is available. Reply with the sub-result
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("Sub_Result_INFORM_Performative")));
		        reply.setContent(subResult);	 	        
		      }
		      else {
		        // The sub-request is NOT available.
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("Sub_Result_FAILURE_Performative")));
		        reply.setContent(getResourceString("Sub_Result_FAILURE_Content")); //FAILURE [sub-request-unavailable]
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
	}  // End of inner class SubRequestsServer	 	
	
	/** Inner class RequestUnsubscriptionPerformer. 
	 *  This is the behaviour used by Service Provider agent to request unsubscription 
	 *  from Mediator agent.
	 */		
	private class RequestUnsubscriptionPerformer extends Behaviour {				   
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {
		try{  
			switch (step) {	
				case 0:			    	
				  // Send the request to the mediator agent
				  ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("Unsubscription_REQUEST_Performative")));			      
				      
				  request.addReceiver(mediator);	      
				  request.setContent("unsubscription");
				  request.setConversationId(getResourceString("Unsubscription_REQUEST_ConversationID"));
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
					  
				  sendMessage(request);			      
					  
				  // Prepare the template to get unsubscription accept proposal
				  mt = MessageTemplate.and(
				  		 MessageTemplate.MatchPerformative(ACLMessage.getInteger(
				  				 	getResourceString("Unsubscription_ACCEPT_PROPOSAL_Performative"))),
				   		 MessageTemplate.and(
				   		 MessageTemplate.MatchConversationId(getResourceString("Unsubscription_ACCEPT_PROPOSAL_ConversationID")),
				   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
			      step = 1;			      
			      break;			    		   
				case 1:
				  // Receive the reply (unsubscription accept proposal/refusal) from the mediator agent
				  reply = receiveMessage(myAgent, mt);			    				      
				  if (reply != null) {				      
				      // Reply received
					  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [unsubscription-accepted]				  	    	 				    	  
					      System.out.println(reply.getContent() + ": Mediator agent "
			        		  				+ reply.getSender().getName());				    	 
					      step = 2;				     
					  }				      
					  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [unsubscription-refused]
					   	  System.out.println(reply.getContent() + ": Mediator agent "
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
		



