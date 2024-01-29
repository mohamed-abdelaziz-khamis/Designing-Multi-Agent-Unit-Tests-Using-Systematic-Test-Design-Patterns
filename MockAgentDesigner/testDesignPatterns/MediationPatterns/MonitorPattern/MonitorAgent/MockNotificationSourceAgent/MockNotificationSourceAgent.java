/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MonitorPattern
 * Agent Under Test:			MonitorAgent
 * Mock Agent:					MockNotificationSourceAgent
 */
package MediationPatterns.MonitorPattern.MonitorAgent.MockNotificationSourceAgent;

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
public class MockNotificationSourceAgent extends JADEMockAgent {
	private static ResourceBundle resMockSourceAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MonitorPattern.MonitorAgent.MockNotificationSourceAgent.MockNotificationSourceAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockSourceAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

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
	  System.out.println("Hallo! Mock-Source-Agent "+getAID().getName()+" is ready.");
	  
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
	    System.out.println("Mock-Source-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class StateChangesNotificationServer. 
	  * This is the behaviour used by Source agent to serve incoming requests 
	  * for state changes notification from monitor agents.
	  * If the subject state change notification is done successfully, the source agent replies with 
	  * an INFORM message. Otherwise a FAILURE message is sent back.
	  */
	   
	private class StateChangesNotificationServer extends CyclicBehaviour {
	 	  
	  public void action() {
		  try{
			  MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("REQUEST_Performative"))),
			    		  MessageTemplate.MatchConversationId(getResourceString("REQUEST_ConversationID"))); 
			  
	 		 ACLMessage msg = receiveMessage(myAgent, mt);

	 		 if (msg != null) {
	 	      // REQUEST Message received. Process it
	 	      String subjectTitle = msg.getContent(); //REQUEST [subject-title]
	
	 	      ACLMessage reply = msg.createReply();	      
	 	      String currentState = catalogue.get(subjectTitle);
	 	      
	 	      if (currentState != null) {	    	  
	 	    	// The requested subject is exiting in the catalouge.	        
	 	    	reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
	 	    	reply.setContent(currentState);
	 	        System.out.println(subjectTitle + " current state: " + currentState + 
	 	        				  " is sent to monitor agent "+msg.getSender().getName());
	 	      }
	 	      else {
	 	        // The requested subject is not exiting in the catalouge.
	 	        reply.setPerformative(ACLMessage.getInteger(getResourceString("FAILURE_Performative")));
	 	        reply.setContent(getResourceString("FAILURE_Content"));
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
	}  // End of inner class StateChangesNotificationServer
}