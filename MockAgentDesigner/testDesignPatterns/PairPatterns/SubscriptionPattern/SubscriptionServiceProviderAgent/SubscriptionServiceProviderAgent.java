/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package PairPatterns.SubscriptionPattern.SubscriptionServiceProviderAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class SubscriptionServiceProviderAgent extends Agent {

	//Yellow Page Agent ID	
	private AID yellowPage;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Service Provider-Agent "+getAID().getName()+" is ready.");	    		  

		yellowPage = new AID("yellowPage", AID.ISLOCALNAME);

		// Perform the subscription request to the yellowPage agent
		addBehaviour(new RequestSubscriptionPerformer());

		addBehaviour(new WakerBehaviour(this, 60000) {
			protected void handleElapsedTimeout() {
				// Perform the unsubscription request to the yellowPage agent
				addBehaviour(new RequestUnsubscriptionPerformer());
			}
		} );
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Service Provider-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestSubscriptionPerformer. 
	 *  This is the behaviour used by Service Provider agent to request subscription 
	 *  from Yellow Page agent for the required service.
	 */		
	private class RequestSubscriptionPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the request to the yellow page agent
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      

				request.addReceiver(yellowPage);	      
				request.setContent("subscription");
				request.setConversationId("service-subscription");
				request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  

				request.addUserDefinedParameter("Service_Description_Type", "service-providing");
				request.addUserDefinedParameter("Service_Description_Name", "JADE-service-subcription");

				send(request);			      

				// Prepare the template to get subscription accept proposal
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
								MessageTemplate.and(
										MessageTemplate.MatchConversationId("service-subscription"),
										MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the reply (subscription accept proposal/refusal) from the yellow page agent
				reply = receive(mt);			    				      
				if (reply != null) {				      
					// Reply received
					if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [subscription-accepted]				  	    	 				    	  
						System.out.println(reply.getContent() + " Yellow Page agent "
								+ reply.getSender().getName());				    	 
						step = 2;				     
					}				      
					else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [subscription-refused]
						System.out.println(reply.getContent());
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

	/** Inner class RequestUnsubscriptionPerformer. 
	 *  This is the behaviour used by Service Provider agent to request unsubscription 
	 *  from Yellow Page agent.
	 */		
	private class RequestUnsubscriptionPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the request to the yellow page agent
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      

				request.addReceiver(yellowPage);	      
				request.setContent("unsubscription");
				request.setConversationId("service-subscription");
				request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  

				send(request);			      

				// Prepare the template to get unsubscription accept proposal
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("service-subscription"),
								MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the reply (unsubscription accept proposal/refusal) from the yellow page agent
				reply = receive(mt);			    				      
				if (reply != null) {				      
					// Reply received
					if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [unsubscription-accepted]				  	    	 				    	  
						System.out.println(reply.getContent() + " Yellow Page agent "
								+ reply.getSender().getName());				    	 
						step = 2;				     
					}				      
					else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [unsubscription-refused]
						System.out.println(reply.getContent());
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