/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.MediatorPattern.MediatorProviderAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class MediatorProviderAgent extends Agent {

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
	  System.out.println("Hallo! Provider-Agent "+getAID().getName()+" is ready.");
	  
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
	      
		  addBehaviour(new WakerBehaviour(this, 100000) {
				
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
	    System.out.println("Provider-Agent "+getAID().getName()+" terminating.");
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
		switch (step) {	
			case 0:			    	
			  // Send the request to the mediator agent
			  ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      
			      
			  request.addReceiver(mediator);	      
			  request.setContent("subscription");
			  request.setConversationId("service-mediating");
			  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
			
			  request.addUserDefinedParameter("ServiceDescriptionName", subRequestTitle);

			  send(request);			      
				  
			  // Prepare the template to get subscription accept proposal
			  mt = MessageTemplate.and(
			  		 MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			   		 MessageTemplate.and(
			   		 MessageTemplate.MatchConversationId("service-mediating"),
			   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
		      step = 1;			      
		      break;			    		   
			case 1:
			  // Receive the reply (subscription accept proposal/refusal) from the mediator agent
			  reply = receive(mt);			    				      
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
		MessageTemplate mt = MessageTemplate.and(
	    		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	    		  MessageTemplate.MatchConversationId("service-mediating"));
			
		ACLMessage msg = receive(mt);
			
	    if (msg != null) {
	 	      
	      // REQUEST Message received. Process it
	      String subRequest = msg.getContent(); //REQUEST [sub-request]
	 	      
	      ACLMessage reply = msg.createReply();
	      String subResult = catalogue.get(subRequest);
	
	      if (subResult != null) {	 	    	  	 	    	  	
	    	// The sub-request is available. Reply with the sub-result
	        reply.setPerformative(ACLMessage.INFORM);
	        reply.setContent(subResult);	 	        
	      }
	      else {
	        // The sub-request is NOT available.
	        reply.setPerformative(ACLMessage.FAILURE);
	        reply.setContent("sub-request-unavailable"); //FAILURE [sub-request-unavailable]
	      }
	      send(reply);
	    }
		else {
		    block();
		}
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
		switch (step) {	
			case 0:			    	
			  // Send the request to the mediator agent
			  ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      
			      
			  request.addReceiver(mediator);	      
			  request.setContent("unsubscription");
			  request.setConversationId("service-mediating");
			  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
				  
			  send(request);			      
				  
			  // Prepare the template to get unsubscription accept proposal
			  mt = MessageTemplate.and(
			  		 MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			   		 MessageTemplate.and(
			   		 MessageTemplate.MatchConversationId("service-mediating"),
			   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
		      step = 1;			      
		      break;			    		   
			case 1:
			  // Receive the reply (unsubscription accept proposal/refusal) from the mediator agent
			  reply = receive(mt);			    				      
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
	 
	  public boolean done() {
	    return (step == 2);
	  }
	}  // End of inner class RequestUnsubscriptionPerformer
}