/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				WrapperPattern
 * Agent Under Test:			WrapperAgent
 * Mock Agent:					MockWrapperSourceAgent
 */
package MediationPatterns.WrapperPattern.WrapperAgent.MockWrapperSourceAgent;

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
public class MockWrapperSourceAgent extends JADEMockAgent {
	private static ResourceBundle resMockSourceAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.WrapperPattern.WrapperAgent.MockWrapperSourceAgent.MockWrapperSourceAgent");
	
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

	/* The catalogue of translated request content
	 * (maps the translated request to its answer)
	 **/
	private Hashtable<String, String> catalogue;
	
	// Translated Request Content 
	private String translatedContent;
	
	//Source Answer
	private String sourceAnswer;
	   
    // Put agent initializations here
    protected void setup() {

	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Source-Agent "+getAID().getName()+" is ready.");	    		  
	  
	  // Get the translated request content as a start-up argument.
	  Object[] args = getArguments();		  
		  
	  if (args != null && args.length > 0) {
			
		  translatedContent = (String) args[0];
		  sourceAnswer = (String) args[1];
	  	  
	      // Create the catalogue
		  catalogue = new Hashtable<String, String>();
		
		  catalogue.put(translatedContent, sourceAnswer);
			
	      System.out.println(translatedContent + " inserted into catalogue. " 
	        			+ ", source-answer = " + sourceAnswer);
	        
	      // Add the behaviour serving requests from wrapper agents for source-answer 
	      addBehaviour(new SourceAnswerRequestsServer());
	
	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No request for source-answer specified to be added in catalogue");
		  doDelete();
	  }
    }  

	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Mock-Source-Agent "+getAID().getName()+" terminating.");
	}
	
	
	/** Inner class SourceAnswerRequestsServer. 
	 * This is the behaviour used by Source agents to serve incoming requests for source-answer from wrapper agents.
	 * If the translated request content is in the local catalogue, the source agent replies with an INFORM message 
	 * specifying the source answer. Otherwise a FAILURE message is sent back.
	 */   
	private class SourceAnswerRequestsServer extends CyclicBehaviour {
		   
	  public void action() {
		 try{ 
			MessageTemplate mt = MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("REQUEST_Performative"))),
		    		  MessageTemplate.MatchConversationId(getResourceString("REQUEST_ConversationID")));
				
			ACLMessage msg = receiveMessage(myAgent, mt);
				
		    if (msg != null) {
		 	      
		      // REQUEST Message received. Process it
		      String translatedContent = msg.getContent(); //REQUEST [translated-content]
		 	      
		      ACLMessage reply = msg.createReply();
		      String sourceAnswer = catalogue.get(translatedContent);
		
		      if (sourceAnswer != null) {	 	    	  	 	    	  	
		    	// The translatedContent is available. Reply with the sourceAnswer
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
		        reply.setContent(sourceAnswer);	 	        
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
   }  // End of inner class SourceAnswerRequestsServer	 	
}
		



