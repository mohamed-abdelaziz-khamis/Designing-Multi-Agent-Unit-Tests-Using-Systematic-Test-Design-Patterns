/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */

package MediationPatterns.MatchmakerPattern.MatchmakerProviderAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class MatchmakerProviderAgent extends Agent {

	// The catalogue of service for sale (maps the title of a service to its price)
	private Hashtable<String, Float> catalogue;
	private String serviceTitle;
	private float servicePrice;
	  
    //Matchmaker Agent ID	
    private AID matchmaker;
  
    // Put agent initializations here
    protected void setup() {

	  // Printout a welcome message
	  System.out.println("Hallo! Provider-Agent "+getAID().getName()+" is ready.");	    		  
	  
	  matchmaker = new AID("matchmaker", AID.ISLOCALNAME);

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
	        
	      // Perform the subscription request to the matchmaker agent
	      addBehaviour(new RequestSubscriptionPerformer());

	      // Add the behaviour serving queries from client agents
	      addBehaviour(new ServiceRequestsServer());
	    
		  addBehaviour(new WakerBehaviour(this, 60000) {
		
			  protected void handleElapsedTimeout() {
				  // Perform the unsubscription request to the matchmaker agent
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
	    System.out.println("Provider-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class RequestSubscriptionPerformer. 
	 *  This is the behaviour used by Provider agent to request subscription 
	 *  from Matchmaker agent for the required service.
	 */		
	private class RequestSubscriptionPerformer extends Behaviour {				   
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {		  
		switch (step) {	
			case 0:			    	
			  // Send the request to the matchmaker agent
			  ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      
			      
			  request.addReceiver(matchmaker);	      
			  request.setContent("subscription");
			  request.setConversationId("service-matchmaking");
			  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
			
			  request.addUserDefinedParameter("Service_Description_Type", "service-providing");			  
			  request.addUserDefinedParameter("Service_Description_Name", "JADE-service-matchmaking");

			  send(request);			      
				  
			  // Prepare the template to get subscription accept proposal
			  mt = MessageTemplate.and(
			  		 MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			   		 MessageTemplate.and(
			   		 MessageTemplate.MatchConversationId("service-matchmaking"),
			   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
		      step = 1;			      
		      break;			    		   
			case 1:
			  // Receive the reply (subscription accept proposal/refusal) from the matchmaker agent
			  reply = receive(mt);			    				      
			  if (reply != null) {				      
			      // Reply received
				  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [subscription-accepted]				  	    	 				    	  
				      System.out.println(reply.getContent() + ": Matchmaker agent "
				    		  			+ reply.getSender().getName());				    	 
				      step = 2;				     
				  }				      
				  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [subscription-refused]
				   	  System.out.println(reply.getContent() + ": Matchmaker agent "
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
	
	/** Inner class ServiceRequestsServer. 
	 * This is the behaviour used by Provider agents to serve incoming requests for service from client agents.
	 * If the requested service is in the local catalogue the provider agent replies with an INFORM message 
	 * specifying the price. Otherwise a REFUSE message is sent back.
	 */   
	private class ServiceRequestsServer extends CyclicBehaviour {
		   
	  public void action() { 		
		MessageTemplate mt = MessageTemplate.and(
	    		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	    		  MessageTemplate.MatchConversationId("service-matchmaking"));
			
		ACLMessage msg = receive(mt);
			
	    if (msg != null) {
	 	      
	      // REQUEST Message received. Process it
	      String serviceTitle = msg.getContent(); //REQUEST [service-title]
	 	      
	      ACLMessage reply = msg.createReply();
	      Float price = (Float) catalogue.get(serviceTitle);
	
	      if (price != null) {	 	    	  	 	    	  	
	    	// The requested service is available for sale. Reply with the price
	        reply.setPerformative(ACLMessage.INFORM);
	        reply.setContent(price.toString());	 	        
	      }
	      else {
	        // The requested service is NOT available for sale.
	        reply.setPerformative(ACLMessage.REFUSE);
	        reply.setContent("service-unavailable"); //REFUSE [service-unavailable]
	      }
	      send(reply);
	    }
		else {
		    block();
		}
	  }
	}  // End of inner class ServiceRequestsServer
	 	
	/** Inner class RequestUnsubscriptionPerformer. 
	 *  This is the behaviour used by Provider agent to request unsubscription 
	 *  from Matchmaker agent.
	 */		
	private class RequestUnsubscriptionPerformer extends Behaviour {				   
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {		  
		switch (step) {	
			case 0:			    	
			  // Send the request to the matchmaker agent
			  ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      
			      
			  request.addReceiver(matchmaker);	      
			  request.setContent("unsubscription");
			  request.setConversationId("service-matchmaking");
			  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
				  
			  send(request);			      
				  
			  // Prepare the template to get unsubscription accept proposal
			  mt = MessageTemplate.and(
			  		 MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			   		 MessageTemplate.and(
			   		 MessageTemplate.MatchConversationId("service-matchmaking"),
			   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
		      step = 1;			      
		      break;			    		   
			case 1:
			  // Receive the reply (unsubscription accept proposal/refusal) from the matchmaker agent
			  reply = receive(mt);			    				      
			  if (reply != null) {				      
			      // Reply received
				  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [unsubscription-accepted]				  	    	 				    	  
				      System.out.println(reply.getContent() + ": Matchmaker agent "
		        		  				+ reply.getSender().getName());				    	 
				      step = 2;				     
				  }				      
				  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [unsubscription-refused]
				   	  System.out.println(reply.getContent() + ": Matchmaker agent "
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