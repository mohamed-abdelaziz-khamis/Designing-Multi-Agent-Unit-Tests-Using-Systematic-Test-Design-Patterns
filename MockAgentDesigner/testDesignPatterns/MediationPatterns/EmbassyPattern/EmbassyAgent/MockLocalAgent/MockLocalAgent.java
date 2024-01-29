/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				EmbassyPattern
 * Agent Under Test:			EmbassyAgent
 * Mock Agent:					MockLocalAgent
 */
package MediationPatterns.EmbassyPattern.EmbassyAgent.MockLocalAgent;

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
public class MockLocalAgent extends JADEMockAgent {
	
	private static ResourceBundle resMockLocalAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.EmbassyPattern.EmbassyAgent.MockLocalAgent.MockLocalAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockLocalAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}

	/* The catalogue of translated performative content 
	 * (maps the translated performative content to its local response)
	 **/
	private Hashtable<String, String> catalogue;
	
	// Translated Performative Message Content 
	private String translatedContent;
	
	//Local Response
	private String localResponse;
	   
    // Put agent initializations here
    protected void setup() {

	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Local-Agent "+getAID().getName()+" is ready.");	    		  
	  
	  // Get the translated performative message content as a start-up argument.
	  Object[] args = getArguments();		  
		  
	  if (args != null && args.length > 0) {
			
		  translatedContent = (String) args[0];
		  localResponse = (String) args[1];
	  	  
	      // Create the catalogue
		  catalogue = new Hashtable<String, String>();
		
		  catalogue.put(translatedContent, localResponse);
			
	      System.out.println(translatedContent + " inserted into catalogue. " 
	        			+ ", local-response = " + localResponse);
	        
	      // Add the behaviour serving requests for local-response from embassy agents 
	      addBehaviour(new RequestLocalResponseServer());
	
	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No request for local-response specified to be added in catalogue");
		  doDelete();
	  }
    }  

	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Mock-Local-Agent "+getAID().getName()+" terminating.");
	}
	
	
	/** Inner class RequestLocalResponseServer. 
	 * This is the behaviour used by Local agents to serve incoming requests for local-response from embassy agents.
	 * If the translated content is in the local catalogue, the local agent replies with an INFORM message 
	 * specifying the local response. Otherwise a FAILURE message is sent back.
	 */   
	private class RequestLocalResponseServer extends CyclicBehaviour {
		   
	  public void action() {
		  try {
			  MessageTemplate mt = MessageTemplate.and(
					  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("REQUEST_Performative"))),
					  MessageTemplate.MatchConversationId(getResourceString("REQUEST_ConversationID")));

			  ACLMessage msg = receiveMessage(myAgent, mt);

			  if (msg != null) {

				  // REQUEST Message received. Process it
				  String translatedContent = msg.getContent(); //REQUEST [translated-content]

				  ACLMessage reply = msg.createReply();
				  String localResponse = catalogue.get(translatedContent);

				  if (localResponse != null) {	 	    	  	 	    	  	
					  // The translatedContent is available. Reply with the localResponse
					  reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
					  reply.setContent(localResponse);	 	        
				  }
				  else {
					  // The translatedContent is NOT available.
					  reply.setPerformative(ACLMessage.getInteger(getResourceString("FAILURE_Performative")));
					  reply.setContent(getResourceString("FAILURE_Content")); //FAILURE [translatedContent-unavailable]
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
	}  // End of inner class RequestLocalResponseServer	 	
}