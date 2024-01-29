/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MonitorPattern
 * Agent Under Test:			NotificationSourceAgent
 * Mock Agent:					MockMonitorAgent
 */
package MediationPatterns.MonitorPattern.NotificationSourceAgent.MockMonitorAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;

import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockMonitorAgent extends JADEMockAgent {
	private static ResourceBundle resMockMonitorAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MonitorPattern.NotificationSourceAgent.MockMonitorAgent.MockMonitorAgent");
	
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

	private String subjectTitle;
	private String sourceReplyContent;
	private int sourceReplyPerformative;
	  
	//Source Agent ID
	private AID source;
	  
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Monitor-Agent "+getAID().getName()+" is ready.");
	  
	  /* The role of the monitor is to accept subscriptions, request notifications from subjects of interest, 
	   * receive such notifications of events and to alert subscribers to relevant events
	   */
	  
	  source = new AID("source", AID.ISLOCALNAME);
	  subjectTitle =  getResourceString("Requested_Subject_Title");
	       
      // Perform the notification request from the source agent
      addBehaviour(new RequestNotificationPerformer());
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Monitor-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class RequestNotificationPerformer. 
	 *  This is the behaviour used by Monitor agent to request notification 
	 *  for subject state changes from Source agent.
	 */		
	private class RequestNotificationPerformer extends Behaviour {				   
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {
		try{  
			switch (step) {	
				case 0:			    	
				  // Send the notification request to the source agent
				  ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("REQUEST_Performative")));			      
				      
				  request.addReceiver(source);	      
				  request.setContent(subjectTitle);
				  request.setConversationId(getResourceString("REQUEST_ConversationID"));
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
				
				  sendMessage(request);			      
					  
				  // Prepare the template to get notification
				  mt = MessageTemplate.and(
				  		 MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative"))),
				   		 MessageTemplate.and(
				   		 MessageTemplate.MatchConversationId(getResourceString("INFORM_ConversationID")),
				   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
			      step = 1;			      
			      break;			    		   
				case 1:
				  // Receive the reply (notification inform/failure) from the source agent
				  reply = receiveMessage(myAgent, mt);			    				      
				  if (reply != null && 
						  (reply.getPerformative() == ACLMessage.INFORM || 
						   reply.getPerformative() == ACLMessage.FAILURE) ) {				      
	
					  // Reply received: INFORM [current-state] or FAILURE [notification-failure] 				  
					  sourceReplyContent = reply.getContent();
					  sourceReplyPerformative = reply.getPerformative();
					  System.out.println(sourceReplyPerformative + " [" + sourceReplyContent
							  			+ "] : Source agent " + reply.getSender().getName());				  
					  step = 2;				  
				  }			      
				  else {
				      block();
				  }		   			    
			      break;
			 }   
		 }
		 catch (ReplyReceptionFailed  e) {
			 setTestResult(prepareMessageResult(e));
			 e.printStackTrace();
			 myAgent.doDelete();			
		 } 																	
		 setTestResult(new TestResult());
	  }	  
	 
	  public boolean done() {
	    return (step == 2);
	  }
	}  // End of inner class RequestNotificationPerformer
	
}
		



