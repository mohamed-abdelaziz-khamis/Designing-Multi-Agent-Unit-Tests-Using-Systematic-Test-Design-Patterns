/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.MonitorPattern.MonitorAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class MonitorAgent extends Agent {

	/* The catalogue of registered subscriber agents 
	 * (maps the subscriber agent identifier to the subject title that it registered for notification)
	 */
	private Hashtable<AID, String> catalogue;

	private String subjectTitle;
	private String sourceReplyContent;
	private int sourceReplyPerformative;

	//Source Agent ID
	private AID source;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Monitor-Agent "+getAID().getName()+" is ready.");

		/* The role of the monitor is to accept subscriptions, request notifications from subjects of interest, 
		 * receive such notifications of events and to alert subscribers to relevant events
		 */

		source = new AID("source", AID.ISLOCALNAME);

		// Create the catalogue
		catalogue = new Hashtable<AID, String>();

		// Add the behaviour serving state changes notification queries from subscriber agents
		addBehaviour(new StateChangesNotificationServer());

		// Add the behaviour serving unregisteration queries from subscriber agents
		addBehaviour(new UnregisterSubscriberServer());

	}

	// Put agent clean-up operations here
	protected void takeDown() {		 		
		// Printout a dismissal message
		System.out.println("Monitor-Agent "+getAID().getName()+" terminating.");	    
	}

	/** Inner class StateChangesNotificationServer. 
	 * This is the behaviour used by Monitor agent to serve incoming requests 
	 * for state changes notification from subscriber agents.
	 * If the subject state change notification is done successfully, the monitor agent replies with 
	 * an INFORM message. Otherwise a FAILURE message is sent back.
	 */

	private class StateChangesNotificationServer extends CyclicBehaviour {

		public void action() {	 		  		 

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
					MessageTemplate.MatchConversationId("notification-monitoring")); 

			ACLMessage msg = receive(mt);

			if (msg != null) {
				// SUBSCRIBE Message received. Process it
				subjectTitle = msg.getContent(); //SUBSCRIBE [subject-title]

				catalogue.put(msg.getSender(), subjectTitle);

				ACLMessage reply = msg.createReply();	

				// Perform the notification request from the source agent
				addBehaviour(new RequestNotificationPerformer());

				// Forward the reply of the source agent (notification inform / failure)	        
				reply.setPerformative(sourceReplyPerformative);
				reply.setContent(sourceReplyContent);

				send(reply);

				System.out.println(subjectTitle + " current state: " + sourceReplyContent + 
						" is sent to subscriber agent "+msg.getSender().getName());
			}
			else {
				block();
			}
		}
	}  // End of inner class StateChangesNotificationServer


	/** Inner class RequestNotificationPerformer. 
	 *  This is the behaviour used by Monitor agent to request notification 
	 *  for subject state changes from Source agent.
	 */		
	private class RequestNotificationPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the notification request to the source agent
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      

				request.addReceiver(source);	      
				request.setContent(subjectTitle);
				request.setConversationId("notification-monitoring");
				request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  

				send(request);			      

				// Prepare the template to get notification
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("notification-monitoring"),
								MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the reply (notification inform/failure) from the source agent
				reply = receive(mt);			    				      
				if (reply != null && 
						(reply.getPerformative() == ACLMessage.INFORM || 
						 reply.getPerformative() == ACLMessage.FAILURE) ) {				      

					// Reply received: INFORM [current-state] or FAILURE [notification-failure] 				  
					sourceReplyContent = reply.getContent();
					sourceReplyPerformative = reply.getPerformative();
					System.out.println(reply.getContent() + ": Source agent " + reply.getSender().getName());				  
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
	}  // End of inner class RequestNotificationPerformer

	/** Inner class UnregisterSubscriberServer.
	 * This is the behaviour used by monitor agents to serve incoming unregisteration requests 
	 * from subscriber agents. The monitor agent removes the subscriber agent from its catalogue 
	 * and replies with a CONFIRM message if the unregisteration has been successfully completed, 
	 * Otherwise a DISCONFIRM message is sent back.
	 */

	private class UnregisterSubscriberServer extends CyclicBehaviour {

		public void action() {	 		  		 
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CANCEL),
					MessageTemplate.MatchConversationId("notification-monitoring")); 

			ACLMessage msg = receive(mt);

			if (msg != null) {
				// CANCEL Message received. Process it	
				ACLMessage reply = msg.createReply();	      
				subjectTitle = catalogue.remove(msg.getSender());

				if (subjectTitle != null) {	    	  
					// The subscriber agent is still exiting and not unregistered yet.	        
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setContent("unregisteration-confirmed");
					System.out.println( "Agent had been unregistered: " + msg.getSender().getName() );
				}
				else {
					// The subscriber agent is not currently registered.
					reply.setPerformative(ACLMessage.DISCONFIRM);
					reply.setContent("unregisteration-disconfirmed");
				}
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class UnregisterSubscriberServer
}