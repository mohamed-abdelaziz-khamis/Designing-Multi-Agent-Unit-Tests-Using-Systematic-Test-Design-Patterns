/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */

package MediationPatterns.MatchmakerPattern.MatchmakerClientAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

@SuppressWarnings("serial")
public class MatchmakerClientAgent extends Agent {

	// The title of the requested service
	private String serviceTitle;

	// Matchmaker Agent ID
	private AID matchmaker;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Client-Agent "+getAID().getName()+" is ready.");	    		  

		matchmaker = new AID("matchmaker", AID.ISLOCALNAME);

		// Get the title of the requested service as a start-up argument
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			serviceTitle = (String) args[0];
			System.out.println("The title of the requested service: "+serviceTitle);	  

			// Perform the service request from the matchmaker agent
			addBehaviour(new RequestServicePerformer());					
		}					  
		else {
			// Make the agent terminate
			System.out.println("No service title specified");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Client-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestServicePerformer. 
	 *  This is the behaviour used by Client agent to request  
	 *  from Provider agent the required service.
	 */		
	private class RequestServicePerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;
		private AID[] serviceProviderAgents; // The list of known service provider agents
		private int repliesCnt = 0; 	// The counter of replies from service provider agents

		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the service request to the matchmaker agent
				ACLMessage matchmakerRequest = new ACLMessage(ACLMessage.REQUEST);			      

				matchmakerRequest.addReceiver(matchmaker);	      
				matchmakerRequest.setContent(serviceTitle);
				matchmakerRequest.setConversationId("service-matchmaking");
				matchmakerRequest.setReplyWith("matchmakerRequest"+System.currentTimeMillis()); // Unique value		  				  
				matchmakerRequest.addUserDefinedParameter("Service_Description_Type","service-providing");

				send(matchmakerRequest);			      

				// Prepare the template to get the service provider agent identifier
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
								MessageTemplate.and(
										MessageTemplate.MatchConversationId("service-matchmaking"),
										MessageTemplate.MatchInReplyTo(matchmakerRequest.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the reply (inform/refusal) from the matchmaker agent
				reply = receive(mt);			    				      
				if (reply != null) {				      
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {	//INFORM [provider-ID]
						try {
							serviceProviderAgents = (AID[]) reply.getContentObject();															  								  
							System.out.println("Found the following service provider agents:");
							for (int i = 0; i < serviceProviderAgents.length; ++i) {
								System.out.println(serviceProviderAgents[i].getName());
							}								  
							step = 2;
						} catch (UnreadableException e) {
							e.printStackTrace();
						}						    				     
					}				      
					else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [request-refused]
						System.out.println(reply.getContent()+ ": Matchmaker agent "
								+ reply.getSender().getName());
						myAgent.doDelete();
					}				      
				}			      
				else {
					block();
				}		   			    
				break;
			case 2:			    	
				// Send the service request to the service provider agents
				ACLMessage providerRequest = new ACLMessage(ACLMessage.REQUEST);			      

				for (int i = 0; i < serviceProviderAgents.length; ++i) {
					providerRequest.addReceiver(serviceProviderAgents[i]);
				}		      
				providerRequest.setContent(serviceTitle);
				providerRequest.setConversationId("service-matchmaking");
				providerRequest.setReplyWith("providerRequest"+System.currentTimeMillis()); // Unique value		  				  

				send(providerRequest);			      

				// Prepare the template to get the service
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
								MessageTemplate.and(
										MessageTemplate.MatchConversationId("service-matchmaking"),
										MessageTemplate.MatchInReplyTo(providerRequest.getReplyWith())));			      			    	
				step = 3;			      
				break;
			case 3:      	    
				// Receive the reply (inform/refusal) from the provider agent
				reply = receive(mt);	    			      
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) { //INFORM [service-price]
						// Service informed successfully. We can terminate
						System.out.println(serviceTitle+" successfully informed by provider agent " + reply.getSender().getName());
						System.out.println("Price = "+ reply.getContent());

						step = 4;
					}
					else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [request-refused]
						System.out.println(reply.getContent()+ ": Provider agent " + reply.getSender().getName());

						repliesCnt++;
						if (repliesCnt >= serviceProviderAgents.length) {
							// We received all replies
							step = 4; 
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
			return (step == 4);
		}
	}  // End of inner class RequestServicePerformer			
}