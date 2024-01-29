/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.MonitorPattern.NotificationSourceAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class NotificationSourceAgent extends Agent {

	/* The role of the subject is to provide notifications of state changes as requested.
	 * */
	
	/* The catalogue of subjects for monitoring state changes 
	   (maps the title of a subject to its current state)
	*/
	private Hashtable<String, String> catalogue;
	private String subjectTitle;
	private String currentState;
	  
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Source-Agent "+getAID().getName()+" is ready.");
	  
	  // Get the title of the subject for state changes monitoring as a start-up argument.
	  Object[] args = getArguments();		  
		  
	  if (args != null && args.length > 0) {

		   subjectTitle = (String) args[0];
		   currentState = (String) args[1];
	  	  
		   // Create the catalogue
		   catalogue = new Hashtable<String, String>();
		
		   catalogue.put(subjectTitle, currentState);
			
	       System.out.println(subjectTitle + " inserted into catalogue. " 
	        			+ ", Current State = " + currentState);
			  
           // Add the behaviour serving state changes notification queries from monitor agents
	       addBehaviour(new StateChangesNotificationServer());
		}		
		else {
			// Make the agent terminate
			System.out.println("No subject title specified to be added in catalogue");
			doDelete();
		}
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Source-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class StateChangesNotificationServer. 
	  * This is the behaviour used by Source agent to serve incoming requests 
	  * for state changes notification from monitor agents.
	  * If the subject state change notification is done successfully, the source agent replies with 
	  * an INFORM message. Otherwise a FAILURE message is sent back.
	  */
	   
	private class StateChangesNotificationServer extends CyclicBehaviour {
	 	  
	 	public void action() {	 		  		 
			  MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			    		  MessageTemplate.MatchConversationId("notification-monitoring")); 
			  
	 		 ACLMessage msg = receive(mt);

	 		 if (msg != null) {
	 	      // REQUEST Message received. Process it
	 	      String subjectTitle = msg.getContent(); //REQUEST [subject-title]
	
	 	      ACLMessage reply = msg.createReply();	      
	 	      String currentState = catalogue.get(subjectTitle);
	 	      
	 	      if (currentState != null) {	    	  
	 	    	// The requested subject is exiting in the catalouge.	        
	 	    	reply.setPerformative(ACLMessage.INFORM);
	 	    	reply.setContent(currentState);
	 	        System.out.println(subjectTitle + " current state: " + currentState + 
	 	        				  " is sent to monitor agent "+msg.getSender().getName());
	 	      }
	 	      else {
	 	        // The requested subject is not exiting in the catalouge.
	 	        reply.setPerformative(ACLMessage.FAILURE);
	 	        reply.setContent("notification-failure");
	 	      }
	 	      send(reply);
	 	    }
	 		else {
	 		    block();
	 		}
	 	}
	}  // End of inner class StateChangesNotificationServer
}