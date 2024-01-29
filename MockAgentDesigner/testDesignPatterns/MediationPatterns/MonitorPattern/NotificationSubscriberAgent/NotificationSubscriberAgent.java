/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.MonitorPattern.NotificationSubscriberAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class NotificationSubscriberAgent extends Agent {

	/*The role of the subscriber is to subscribe for notifications of state changes to 
	 * distributed subjects (data or objects), receive notifications with current state information, 
	 * and update its local state information
	 * */

	// The subject title for monitoring state changes
	private String subjectTitle;

	//Monitor Agent ID
	private AID monitor;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Subscriber-Agent "+getAID().getName()+" is ready.");	    		  

		monitor = new AID("monitor", AID.ISLOCALNAME);

		// Get the title of the subject for monitoring state changes as a start-up argument
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			subjectTitle = (String) args[0];
			System.out.println("Subject for monitoring state changes: "+subjectTitle);	  

			// Perform the notification registeration from the monitor agent
			addBehaviour(new RegisterNotificationPerformer());

			addBehaviour(new WakerBehaviour(this, 60000) {
				protected void handleElapsedTimeout() {
					// Perform the unregisteration from the monitor agent
					addBehaviour(new UnregisterationPerformer());
				}
			});
		}

		else {
			// Make the agent terminate
			System.out.println("No subject title specified");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Subscriber-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RegisterNotificationPerformer. 
	 *  This is the behaviour used by Subscriber agent to register for notification 
	 *  for subject state changes from Monitor agent.
	 */		
	private class RegisterNotificationPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the notification registeration to the monitor agent
				ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);			      

				subscribe.addReceiver(monitor);	      
				subscribe.setContent(subjectTitle);
				subscribe.setConversationId("notification-monitoring");
				subscribe.setReplyWith("subscribe"+System.currentTimeMillis()); // Unique value		  				  

				send(subscribe);			      

				// Prepare the template to get forwarded notification
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("notification-monitoring"),
								MessageTemplate.MatchInReplyTo(subscribe.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the reply (notification inform/failure) from the monitor agent
				reply = receive(mt);			    				      
				if (reply != null) {				      
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {	//INFORM [current-state]				  	    	 				    	  
						System.out.println(reply.getContent() + ": Monitor agent "
								+ reply.getSender().getName());				    	 
						step = 2;				     
					}				      
					else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [notification-failure]
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
	}  // End of inner class RegisterNotificationPerformer

	/** Inner class UnregisterationPerformer. 
	 *  This is the behaviour used by Subscriber agent to unregister 
	 *  from Monitor agent.
	 */		
	private class UnregisterationPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the unregisteration (cancel subscription) to the monitor agent
				ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);			      

				cancel.addReceiver(monitor);	      
				cancel.setContent(subjectTitle);
				cancel.setConversationId("notification-monitoring");
				cancel.setReplyWith("cancel"+System.currentTimeMillis()); // Unique value		  				  

				send(cancel);			      

				// Prepare the template to get unregisteration confirmation
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("notification-monitoring"),
								MessageTemplate.MatchInReplyTo(cancel.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the reply (unregisteration confirm/disconfirm) from the monitor agent
				reply = receive(mt);			    				      
				if (reply != null) {				      
					// Reply received
					if (reply.getPerformative() == ACLMessage.CONFIRM){	//CONFIRM [unregisteration-confirmed]				  	    	 				    	  
						System.out.println(reply.getContent() + ": Monitor agent "
								+ reply.getSender().getName());				    	 
						step = 2;				     
					}				      
					else if (reply.getPerformative() == ACLMessage.DISCONFIRM){ //DISCONFIRM [unregisteration-disconfirmed]
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
	}  // End of inner class UnregisterationPerformer
}