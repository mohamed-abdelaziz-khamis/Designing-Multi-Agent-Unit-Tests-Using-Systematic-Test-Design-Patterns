/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.BrokerPattern.BrokerAgent;

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


@SuppressWarnings("serial")
public class BrokerAgent extends Agent {

	// The title of the service to buy
	private String serviceTitle;
	
	// The list of known service provider agents
	private AID[] serviceProviderAgents;
	
	private String providerReplyContent;
	private int providerReplyPerformative;
	  
  
	// Put agent initializations here
	protected void setup() {
		
		// Printout a welcome message
		System.out.println("Hallo! Broker-Agent "+getAID().getName()+" is ready.");
	  
	  	/* The broker's role is that of arbiter and intermediary, 
	  	 * accessing services of one agent to satisfy the requests of another.
	  	 */
	  
      	// Add the behaviour serving subscription/unsubscription queries from service provider agents
	  	addBehaviour(new YellowPageServer());
	      
      	// Add the behaviour serving service queries from client agents
      	addBehaviour(new RequestServiceServer());
      
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Broker-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class YellowPageServer. 
	  * This is the behaviour used by Broker agent to serve incoming requests 
	  * for subscription/unsubscription from service provider agents.
	  * If the subscription/unsubscription is done successfully, the broker agent replies with 
	  * an ACCEPT_PROPOSAL message. Otherwise a REFUSE message is sent back.
	*/   	
	private class YellowPageServer extends CyclicBehaviour {		   	  
	  public void action() {
		MessageTemplate mt = MessageTemplate.and(
			    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			    MessageTemplate.MatchConversationId("service-broking"));			 	
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
					String serviceDescriptionTypeValue = msg.getUserDefinedParameter("Service_Description_Type");
					String serviceDescriptionNameValue = msg.getUserDefinedParameter("Service_Description_Name");			
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
	
	
	/** Inner class RequestServiceServer. 
	  * This is the behaviour used by Broker agent to serve incoming requests 
	  * for service from service provider agents.
	  * If there exist some service provider agent(s) had regesitered for this service description type, 
	  * the broker agent replies with an ACCEPT_PROPOSAL message. Otherwise a REFUSE message is sent back.
	  */
	   
	private class RequestServiceServer extends CyclicBehaviour {
	 	  
	 	public void action() {	 		  		 
			 
	 		MessageTemplate mt = MessageTemplate.and(
			   		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			   		  MessageTemplate.MatchConversationId("service-broking")); 
			  
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
		 		 	 // There exist some service provider agent(s) had registered for this service description type.
		 			 // ACCEPT_PROPOSAL [service-description-type-available]
		 		 	 reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
		 		 	 reply.setContent("service-description-type-available");
		 		 	 
		 		 	 send(reply);
		 		 	 
			         // Perform the request
				     myAgent.addBehaviour(new ConfirmServiceServer());
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
	}  // End of inner class RequestServiceServer
	
	/** Inner class ConfirmServiceServer. 
	  * This is the behaviour used by Broker agent to serve incoming confirmations 
	  * for service request acceptance from client agents.
	  * If the purchase has been successfully completed, the broker agent replies with 
	  * an INFORM message. Otherwise a FAILURE message is sent back.
	  */
	   
	private class ConfirmServiceServer extends CyclicBehaviour {
	 	  
	 	  public void action() {	 		  		 
			 
	 		 MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
			    		  MessageTemplate.MatchConversationId("service-broking")); 
			  
	 		 ACLMessage msg = receive(mt);

	 		 if (msg != null) {
	 			// CONFIRM Message received. Process it
	 			serviceTitle = msg.getContent(); //CONFIRM [service-title]
	 			
	 	      	ACLMessage reply = msg.createReply();	
	 	      
	 	      	// Perform the service request from the service provider agents
	 	      	addBehaviour(new RequestServicePerformer());
	 	       	      
		      	// Forward the reply of the service provider agent (service inform / failure)	        
		      	reply.setPerformative(providerReplyPerformative);
		      	reply.setContent(providerReplyContent);
	 	     
	 	      	send(reply);
	 	      	
		      	System.out.println(serviceTitle + " Reply: " + providerReplyContent + 
		      					" is sent to client agent "+msg.getSender().getName());
	 	    }
	 		else {
	 		    block();
	 		}
	 	  }
	}  // End of inner class ConfirmServiceServer
	
	
	 /** Inner class RequestServicePerformer
    This is the behaviour used by Broker agents to request service provider agents the target service. */	
	private class RequestServicePerformer extends Behaviour {		
  
		private AID bestServiceProvider; 		// The agent who provides the best offer 
		private float bestPrice;  				// The best offered price
		private int repliesCnt = 0; 			// The counter of replies from service provider agents
		private MessageTemplate mt; 			// The template to receive replies
		private int step = 0;	  	  
  
		public void action() {		  
			switch (step) {
			    case 0:			    	
			      // Send the call for proposal to all service providers
			      ACLMessage cfp = new ACLMessage(ACLMessage.CFP);			      
			      for (int i = 0; i < serviceProviderAgents.length; ++i) {
				        cfp.addReceiver(serviceProviderAgents[i]);
				  }			      
			      cfp.setContent(serviceTitle);
			      cfp.setConversationId("service-broking");
			      cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				  				  
				  send(cfp);
				  
				  // Prepare the template to get proposals
			      mt=MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			    		  MessageTemplate.and(
			    		  MessageTemplate.MatchConversationId("service-broking"),
			              MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));
			      
			      step = 1;			      
			      break;			    
			    case 1:
			    	// Receive the reply (proposals/refusals) from the service provider agents
			    	ACLMessage reply = receive(mt);			    	
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
					    
				        repliesCnt++;
					    if (repliesCnt >= serviceProviderAgents.length) {
					          // We received all replies
					          step = 2; 
					    }
					 }
			      else {
			        block();
			      }
			      break;			    
			    case 2:
			      // Send the purchase order to the service provider agent
			      ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);			      
			      order.addReceiver(bestServiceProvider);
			      order.setContent(serviceTitle);
			      order.setConversationId("service-broking");
			      order.setReplyWith("order"+System.currentTimeMillis());	// Unique value		      
			      send(order);			      
			      // Prepare the template to get the purchase order reply
			      mt = MessageTemplate.and(MessageTemplate.MatchPerformative(Integer.parseInt("INFORM")),
			  				 			   MessageTemplate.and(
			    		  				   MessageTemplate.MatchConversationId("service-broking"),
			                               MessageTemplate.MatchInReplyTo(order.getReplyWith())));
			      step = 3;	      
			      break;     
			    case 3:			    		   
				  // Receive the purchase order reply (service inform/failure) from the service provider agent
				  reply = receive(mt);	    			      
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
	 
	  public boolean done() {
		if (step == 2 && bestServiceProvider == null){ //target service not available for sale
			  providerReplyContent = "service-failure";
			  providerReplyPerformative = ACLMessage.FAILURE;
		}
		return ((step == 2 && bestServiceProvider == null) || step == 4);
	  }
	}  // End of inner class RequestServicePerformer		
}