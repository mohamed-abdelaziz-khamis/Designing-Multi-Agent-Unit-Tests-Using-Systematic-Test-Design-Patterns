/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.MediatorPattern.MediatorClientAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

@SuppressWarnings("all")
public class MediatorClientAgent extends Agent {

	// The title of the request
	private String requestTitle;

	// Mediator Agent ID
	private AID mediator;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Client-Agent "+getAID().getName()+" is ready.");	    		  

		mediator = new AID("mediator", AID.ISLOCALNAME);

		// Get the title of the request as a start-up argument
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			requestTitle = (String) args[0];
			System.out.println("The title of the request: "+requestTitle);	  

			// Perform the request from the mediator agent
			addBehaviour(new RequestPerformer());					
		}					  
		else {
			// Make the agent terminate
			System.out.println("No request title specified");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Client-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestPerformer. 
	 *  This is the behaviour used by Client agent to perform the request  
	 *  from Mediator agent.
	 */		
	private class RequestPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;
		private ArrayList<String> integratedSubResults; // The list of integrated sub results
		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the request to the mediator agent
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      

				request.addReceiver(mediator);	      
				request.setContent(requestTitle);
				request.setConversationId("service-mediating");
				request.setReplyWith("request"+System.currentTimeMillis()); // Unique value

				send(request);			      

				// Prepare the template to get the request final result = integrated sub results
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
								MessageTemplate.and(
										MessageTemplate.MatchConversationId("service-mediating"),
										MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the reply (inform/failure) from the mediator agent
				reply = receive(mt);			    				      
				if (reply != null) {				      
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {	//INFORM [request-result]
						try {
							integratedSubResults = (ArrayList<String>) reply.getContentObject();															  								  
							System.out.println("Recieve the following integrated sub-results:");
							for (int i = 0; i < integratedSubResults.size(); ++i) {
								System.out.println(integratedSubResults.get(i));
							}
							System.out.println("Mediator agent: " + reply.getSender().getName());
						} catch (UnreadableException e) {
							e.printStackTrace();
						}	

					}				      
					else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [result-failed]
						System.out.println(reply.getContent()+ " : Mediator agent "
								+ reply.getSender().getName());
					}	
					step = 2;
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
	}  // End of inner class RequestPerformer			
}