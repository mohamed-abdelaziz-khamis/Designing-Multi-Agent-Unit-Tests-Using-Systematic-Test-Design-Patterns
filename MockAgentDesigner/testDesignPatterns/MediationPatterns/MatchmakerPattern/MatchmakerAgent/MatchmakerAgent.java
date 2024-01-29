/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */

package MediationPatterns.MatchmakerPattern.MatchmakerAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.io.IOException;


@SuppressWarnings("serial")
public class MatchmakerAgent extends Agent {

	// The title of the service to buy
	private String serviceTitle;
	
	// The list of known service provider agents
	private AID[] serviceProviderAgents;
	
	// Put agent initializations here
	protected void setup() {
		
		// Printout a welcome message
		System.out.println("Hallo! Matchmaker-Agent "+getAID().getName()+" is ready.");
	  
	  	/* The Matchmaker finds the Provider of the requested service 
	  	 * and gives its identification to the Client.
	  	 */
	  
      	// Add the behaviour serving subscription/unsubscription queries from service provider agents
	  	addBehaviour(new YellowPageServer());
	      
      	// Add the behaviour serving service provider identification queries from client agents
      	addBehaviour(new RequestServiceProviderServer());
      
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Matchmaker-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class YellowPageServer. 
	  * This is the behaviour used by Matchmaker agent to serve incoming requests 
	  * for subscription/unsubscription from service provider agents.
	  * If the subscription/unsubscription is done successfully, the matchmaker agent replies with 
	  * an ACCEPT_PROPOSAL message. Otherwise a REFUSE message is sent back.
	*/   	
	private class YellowPageServer extends CyclicBehaviour {		   	  
	  public void action() {
		
		MessageTemplate mt = MessageTemplate.and(
			    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			    MessageTemplate.MatchConversationId("service-matchmaking"));			 	
		
		ACLMessage msg = receive(mt);			 	
		
		if (msg != null && (msg.getContent() == "subscription" || msg.getContent() == "unsubscription")) {
			
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
					String serviceDescriptionTypeValue = msg.getUserDefinedParameter("Service_Description_Type_Key");
					String serviceDescriptionNameValue = msg.getUserDefinedParameter("Service_Description_Name_Key");			
					sd.setType(serviceDescriptionTypeValue);
					sd.setName(serviceDescriptionNameValue);	
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
	
	
	/** Inner class RequestServiceProviderServer. 
	  * This is the behaviour used by Matchmaker agent to serve incoming requests 
	  * for service providers identification from client agents.
	  * If there exist some service provider agent(s) had registered for this service description type, 
	  * the matchmaker agent replies with an INFORM message with the service providers identification. 
	  * Otherwise a REFUSE message is sent back.
	  */
	   
	private class RequestServiceProviderServer extends CyclicBehaviour {
	 	  
	 	public void action() {	 		  		 
			 
	 		MessageTemplate mt = MessageTemplate.and(
			   		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			   		  MessageTemplate.MatchConversationId("service-matchmaking")); 
			  
	 		ACLMessage msg = receive(mt);

	 		if (msg != null) {
	 			
	 			// REQUEST Message received. Process it	 			 			
	 			serviceTitle = msg.getContent(); //REQUEST [service-title]	 			
	 			System.out.println("Target service is "+serviceTitle);
	 			    	    	     				    		
				String serviceDescriptionType = msg.getUserDefinedParameter("Service_Description_Type");
	 			System.out.println("Service description type is "+serviceDescriptionType);

	 			// Update the list of service provider agents
	 			DFAgentDescription template = new DFAgentDescription();
	 			ServiceDescription sd = new ServiceDescription();
	 			sd.setType(serviceDescriptionType);	 				          
	 			template.addServices(sd);	
	 				          
	 			try {		          	
	 				DFAgentDescription[] result = DFService.search(myAgent, template); 
	 				          	
	 		  		System.out.println("Found the following service provider agents:");
	 		  		serviceProviderAgents = new AID[result.length];
	 					          
	 		  		for (int i = 0; i < result.length; ++i) {
	 		  		   serviceProviderAgents[i] = result[i].getName();
	 				   System.out.println(serviceProviderAgents[i].getName());
	 				}
	 					  
	 		  		ACLMessage reply = msg.createReply();
	 		  		
		 			if (serviceProviderAgents.length == 0){
			 		    // No service provider agent had registered for this service description type.
			 			// REFUSE [service-description-type-unavailable]
			 		 	reply.setPerformative(ACLMessage.REFUSE);
			 		 	reply.setContent("service-description-type-unavailable");
			 		 	send(reply);
		 			}
		 			else{
		 				/* There exist some service provider agent(s) had registered for this 
		 				* service description type.*/
		 				// INFORM [serviceProviderAgents]
		 				reply.setPerformative(ACLMessage.INFORM);
		 				try {
		 					reply.setContentObject(serviceProviderAgents);
		 		 		 	send(reply);
		 				} catch (IOException e) {
		 					e.printStackTrace();
		 				}
		 			}
	 			}
	 			catch (FIPAException fe) {
	 				fe.printStackTrace();
	 			}	 				          
	 		}
	 		else {
	 		    block();
	 		}
	 	}
	}  // End of inner class RequestServiceProviderServer
}