/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MonitorPattern
 * Agent Under Test:			NotificationSubscriberAgent
 * Mock Agent:					MockMonitorAgent
 */
package MediationPatterns.MonitorPattern.NotificationSubscriberAgent.MockMonitorAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockMonitorAgent extends JADEMockAgent {
	private static ResourceBundle resMockMonitorAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MonitorPattern.NotificationSubscriberAgent.MockMonitorAgent.MockMonitorAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockMonitorAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	/* The catalogue of registered subscriber agents 
	 * (maps the subscriber agent identifier to the subject title that it registered for notification)
	*/
	private Hashtable<AID, String> catalogue;
	
	private String subjectTitle;
	private String sourceReplyContent;
	private int sourceReplyPerformative;
	  
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Monitor-Agent "+getAID().getName()+" is ready.");
	  
	  /* The role of the monitor is to accept subscriptions, request notifications from subjects of interest, 
	   * receive such notifications of events and to alert subscribers to relevant events
	   */
	    
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
	    System.out.println("Mock-Monitor-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class StateChangesNotificationServer. 
	  * This is the behaviour used by Monitor agent to serve incoming requests 
	  * for state changes notification from subscriber agents.
	  * If the subject state change notification is done successfully, the monitor agent replies with 
	  * an INFORM message. Otherwise a FAILURE message is sent back.
	  */
	   
	private class StateChangesNotificationServer extends CyclicBehaviour {	 	  
	  public void action() {	 		  		 
		try{	 
	 		 MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("SUBSCRIBE_Performative"))),
			    		  MessageTemplate.MatchConversationId(getResourceString("SUBSCRIBE_ConversationID"))); 
			  
	 		 ACLMessage msg = receiveMessage(myAgent, mt);

	 		 if (msg != null) {
	 			// SUBSCRIBE Message received. Process it
	 			subjectTitle = msg.getContent(); //SUBSCRIBE [subject-title]
	 			
 				catalogue.put(msg.getSender(), subjectTitle);
	
	 	      	ACLMessage reply = msg.createReply();	
	 	      
	 	      	// Perform the notification request from the source agent
	 	      	sourceReplyPerformative = Integer.parseInt(getResourceString("Source_Reply_Performative"));
	 	      	sourceReplyContent = getResourceString("Source_Reply_Content");
	 	       	      
		      	// Forward the reply of the source agent (notification inform / failure)	        
		      	reply.setPerformative(sourceReplyPerformative);
		      	reply.setContent(sourceReplyContent);
	 	     
	 	      	sendMessage(reply);
	 	      	
		      	System.out.println(subjectTitle + " current state: " + sourceReplyContent + 
		      					" is sent to subscriber agent "+msg.getSender().getName());
	 	    }
	 		else {
	 		    block();
	 		}
		 }
		 catch (ReplyReceptionFailed  e) {
			 setTestResult(prepareMessageResult(e));
			 e.printStackTrace();
			 myAgent.doDelete();			
		 } 																	
		 setTestResult(new TestResult());
	   }
	}  // End of inner class StateChangesNotificationServer
	
	/** Inner class UnregisterSubscriberServer.
	 * This is the behaviour used by monitor agents to serve incoming unregisteration requests 
	 * from subscriber agents. The monitor agent removes the subscriber agent from its catalogue 
	 * and replies with a CONFIRM message if the unregisteration has been successfully completed, 
	 * Otherwise a DISCONFIRM message is sent back.
	*/
	   
	private class UnregisterSubscriberServer extends CyclicBehaviour {
	 	  
		public void action() {
		  try{	
			 MessageTemplate mt = MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("CANCEL_Performative"))),
		    		  MessageTemplate.MatchConversationId(getResourceString("CANCEL_ConversationID"))); 
			  
	 		 ACLMessage msg = receiveMessage(myAgent, mt);

	 		 if (msg != null) {
	 			 // CANCEL Message received. Process it	
	 			 ACLMessage reply = msg.createReply();	      
	 			 subjectTitle = catalogue.remove(msg.getSender());
	 	      
	 			 if (subjectTitle != null) {	    	  
	 				 // The subscriber agent is still exiting and not unregistered yet.	        
	 				 reply.setPerformative(ACLMessage.getInteger(getResourceString("CONFIRM_Performative")));
	 				reply.setContent(getResourceString("CONFIRM_Content"));
	 				 System.out.println( "Agent had been unregistered: " + msg.getSender().getName() );
	 			 }
	 			 else {
	 				 // The subscriber agent is not currently registered.
	 				 reply.setPerformative(ACLMessage.getInteger(getResourceString("DISCONFIRM_Performative")));
	 				 reply.setContent(getResourceString("DISCONFIRM_Content"));
	 			 }
	 			 sendMessage(reply);
	 		 }
	 		 else {
	 			 block();
	 		 }
		 }
		 catch (ReplyReceptionFailed  e) {
			  setTestResult(prepareMessageResult(e));
			  e.printStackTrace();
			  myAgent.doDelete();			
		 } 																	
		 setTestResult(new TestResult());  
	  }
   }  // End of inner class UnregisterSubscriberServer
}
		



