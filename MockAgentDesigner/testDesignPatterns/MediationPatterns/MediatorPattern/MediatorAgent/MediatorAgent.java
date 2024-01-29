/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.MediatorPattern.MediatorAgent;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings("all")
public class MediatorAgent extends Agent {

	// The catalogue of request for result (maps the title of a request to its sub-requests)
	private Hashtable<String, ArrayList<String>> catalogue;
	
	// The title of the request
	private String requestTitle;
	
	// The title of the sub-request
	private String subRequestTitle;

	// The list of sub requests
	private ArrayList<String> subRequests;
	
	// The list of integrated sub results
	private ArrayList<String> integratedSubResults;
	
	// The list of known service provider agents
	private AID[] serviceProviderAgents;
	
	// Flag equals true if any of the known service provider agents replies with the sub result
	private boolean subResultSuccess;
	
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mediator-Agent "+getAID().getName()+" is ready.");
	  
	  /* The mediator coordinates the cooperative behavior of the colleagues 
	   * and has acquaintance models of all colleague agents.
	  */
	    
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

		  addBehaviour(new WakerBehaviour(this, 60000) {
				
			  protected void handleElapsedTimeout() {
			  	  // Add the behaviour serving request result queries from client agents
			      addBehaviour(new RequestResultServer());
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
	    System.out.println("Mediator-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class YellowPageServer. 
	  * This is the behaviour used by the Mediator agent to serve incoming requests 
	  * for subscription/unsubscription from service provider agents.
	  * If the subscription/unsubscription is done successfully, the mediator agent replies with 
	  * an ACCEPT_PROPOSAL message. Otherwise a REFUSE message is sent back.
	*/   	
	private class YellowPageServer extends CyclicBehaviour {		   	  
	  public void action() {
		
		MessageTemplate mt = MessageTemplate.and(
			    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			    MessageTemplate.MatchConversationId("service-mediating"));			 	
		
		ACLMessage msg = receive(mt);			 	
		
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
			  		reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			  		reply.setContent("subscription-accepted");		
				 }
				 catch (FIPAException fe) {
					// The registeration is failed with the Directory Facilitator. 
				 	// Reply with subscription refused message
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("subscription-refused");		 	         
				 }	 	    	 	        
			 }	 	      
			 else if (subscriptionType == "unsubscription"){
			 	    // Deregister from the yellow pages
			 	  try {
			 	      DFService.deregister(senderAgent);
				 	  // The deregisteration is done successfully from the Directory Facilitator. 
				 	  // Reply with unsubscription accepted message
				      reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				      reply.setContent("unsubscription-accepted");
			 	  }
			 	  catch (FIPAException fe) {
				      // The deregisteration is failed from the Directory Facilitator. 
				      // Reply with unsubscription refused message
			 	      reply.setPerformative(ACLMessage.REFUSE);
				      reply.setContent("unsubscription-refused");
			      }
			 }
			 send(reply);
		 }
		 else {
		 	block();
		  }
	   }
	}  // End of inner class YellowPageServer
	
	/** Inner class RequestResultServer. 
	  * This is the behaviour used by Mediator agent to serve incoming requests for result from client agents.
	  * If there exist some service provider agent(s) had registered for this request, 
	  * the mediator agent replies with an INFORM message with the integrated sub results. 
	  * Otherwise a FAILURE message is sent back.
	  */
	   
	private class RequestResultServer extends CyclicBehaviour {
		   
	  public void action() { 		
		MessageTemplate mt = MessageTemplate.and(
		   		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		   		  MessageTemplate.MatchConversationId("service-mediating"));
			
		ACLMessage msg = receive(mt);
			
	 	if (msg != null) {
	 	      
	 	   // REQUEST Message received. Process it
	 	   requestTitle = msg.getContent(); //REQUEST [request-title]
	 	      
	 	   ACLMessage reply = msg.createReply();
	 	   subRequests = catalogue.get(requestTitle);
	
	 	   if (subRequests != null) {
	 		   
		      DFAgentDescription template;
		      ServiceDescription sd;
			          
		      DFAgentDescription[] result; 

		      integratedSubResults = new ArrayList<String>();
	 		  // The request is available in catalouge. 
	 	   	  for (int i = 0; i < subRequests.size(); i++){	 	    		  
	 	   		  subRequestTitle = subRequests.get(i);
	 	   		  System.out.println("Trying to get sub-result for the sub-request: " + subRequestTitle);	          
			    		
			      // Update the list of service provider agents
			      template = new DFAgentDescription();
			      sd = new ServiceDescription();			          
			      sd.setName(subRequestTitle);			          
			      template.addServices(sd);	
			          
			      try {
			    	  result = DFService.search(myAgent, template); 			          	
	  		       	  System.out.println("Found the following service provider agents:");
	  		       	  serviceProviderAgents = new AID[result.length];
				          
				      for (int j = 0; j < result.length; ++j) {
				       	 serviceProviderAgents[j] = result[j].getName();
				         System.out.println(serviceProviderAgents[j].getName());
				      }
				          
				      if (serviceProviderAgents.length > 0){
				        	  
				      	  subResultSuccess = false;
				          
				      	  // Perform the sub-request for sub-result
					      addBehaviour(new RequestSubResultPerformer());
						      
					      if (!subResultSuccess){
					    	  // The sub-request is NOT available for sub-result.
						 	  reply.setPerformative(ACLMessage.FAILURE);
						 	  reply.setContent("result-failed"); //FAILURE [result-failed]
						 	  break;
					      }
				      }
				      else{
					       // The sub-request is NOT available for sub-result.
					       reply.setPerformative(ACLMessage.FAILURE);
					       reply.setContent("result-failed"); //FAILURE [result-failed]
					       break;
				      }				          
			       }
			       catch (FIPAException fe) {
			    	   // The sub-request is NOT available for sub-result.
			 	       reply.setPerformative(ACLMessage.FAILURE);
			 	       reply.setContent("result-failed"); //FAILURE [result-failed]
			 	       break;			            
			       } 
			    }

	 	   	  	try {
		 	   	  	// The request is available for result. Reply with the integrated sub-results.
		 	   	  	reply.setPerformative(ACLMessage.INFORM);
					reply.setContentObject(integratedSubResults);
				} catch (IOException e) {
			 	    reply.setPerformative(ACLMessage.FAILURE);
			 	    reply.setContent("result-failed"); //FAILURE [result-failed]
				}
	 	   	}	 	        
	 	   	else {
	 	        // The request is NOT available in catalouge.
	 	        reply.setPerformative(ACLMessage.FAILURE);
	 	        reply.setContent("result-failed"); //FAILURE [result-failed]
	 	   	}
	 	   	send(reply);
	 	 }
	 	 else {
	 		block();
	 	 }
	   }
	}  // End of inner class RequestResultServer
	
	
	 /** Inner class RequestSubResultPerformer
    This is the behaviour used by Mediator agent to request service provider agents the sub-result. */	
	private class RequestSubResultPerformer extends Behaviour {		
  
	  private int repliesCnt = 0; 		// The counter of replies from service provider agents
	  private MessageTemplate mt; 		// The template to receive replies
	  private int step = 0;	  	  
	  
	  public void action() {		  
		 switch (step) {
		    case 0:			    	
		      // Send the sub-request to all known service provider agents
		      ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      
		      for (int i = 0; i < serviceProviderAgents.length; ++i) {
		    	  request.addReceiver(serviceProviderAgents[i]);
			  }			      
		      request.setContent(subRequestTitle);
		      request.setConversationId("service-mediating");
		      request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
			  				  
			  send(request);
			  
			  // Prepare the template to get sub-result
		      mt=MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		    		  MessageTemplate.and(
		    		  MessageTemplate.MatchConversationId("service-mediating"),
		              MessageTemplate.MatchInReplyTo(request.getReplyWith())));
		      
		      step = 1;			      
		      break;			    
		    case 1:
		    	// Receive the reply (sub result inform/failure) from the service provider agents
		    	ACLMessage reply = receive(mt);			    	
		    	if (reply != null) {
			        
		    		// Reply received
			        if (reply.getPerformative() == ACLMessage.INFORM) {
			          // This is a sub result 
			          subResultSuccess = true;
			          integratedSubResults.add(reply.getContent());
				      System.out.println(subRequestTitle+" successfully informed by provider agent " + reply.getSender().getName());
				      System.out.println(", sub-result = "+ reply.getContent());
				      step = 2;
			        }
				    else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [sub-request-unavailable]
					  System.out.println(reply.getContent()+ ": Provider agent " + reply.getSender().getName());
					  repliesCnt++;
					  if (repliesCnt >= serviceProviderAgents.length) {
						  // We received all replies
						  step = 2; 
					  }
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
	}  // End of inner class RequestSubResultPerformer
}