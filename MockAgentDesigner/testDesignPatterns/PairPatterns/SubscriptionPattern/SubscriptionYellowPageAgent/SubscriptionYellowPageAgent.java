/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package PairPatterns.SubscriptionPattern.SubscriptionYellowPageAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

@SuppressWarnings("serial")
public class SubscriptionYellowPageAgent extends Agent {

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Yellow Page-Agent "+getAID().getName()+" is ready.");

		// Add the behaviour serving subscription/unsubscription queries from service provider agents
		addBehaviour(new YellowPageServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Yellow Page-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class YellowPageServer. 
	 * This is the behaviour used by Yellow Page agent to serve incoming requests 
	 * for subscription/unsubscription from service provider agents.
	 * If the subscription/unsubscription is done successfully, the yellow page agent replies with an ACCEPT_PROPOSAL message. 
	 * Otherwise a REFUSE message is sent back.
	 */   	
	private class YellowPageServer extends CyclicBehaviour {		   	  
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("service-subscription"));			 	
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
}