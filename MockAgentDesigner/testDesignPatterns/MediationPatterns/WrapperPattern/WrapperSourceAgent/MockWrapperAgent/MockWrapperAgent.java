/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				WrapperPattern
 * Agent Under Test:			WrapperSourceAgent
 * Mock Agent:					MockWrapperAgent
 */
package MediationPatterns.WrapperPattern.WrapperSourceAgent.MockWrapperAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("all")
public class MockWrapperAgent extends JADEMockAgent {
	private static ResourceBundle resMockWrapperAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.WrapperPattern.WrapperSourceAgent.MockWrapperAgent.MockWrapperAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockWrapperAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	/* The catalogue of language to transalate between the source legacy interface and client ACL.
	 * (maps the language to its source dictionary)
	 */		
	private Hashtable<String, Dictionary<String, String>> languageCatalogue;

	// Message content is translated according the client ACL 
	private String language;
	
	/* The dictionary of source vocabulary.
	 * (maps the client content to its source content)
	 */	
	private Dictionary<String, String> sourceDictionary;

	// Content of the request message 
	private String messageContent;
	
	// Translated Request Content  
	private String translatedContent;
		
	//Source Answer
	private String sourceAnswer;
	
	//Translated Source Answer
	private String translatedSourceAnswer;
	
	// Source Agent ID
	private AID source;
	
	// Flag equals true if the known source agent replies with the source answer
	private boolean sourceAnswerSuccess;
	
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Wrapper-Agent "+getAID().getName()+" is ready.");
	  
	  /* The role of the wrapper is to interface the agents to the legacy system by agentifying the legacy.
	  */
	    
	  source = new AID("source", AID.ISLOCALNAME);
	  
	  // Get the language to translate source legacy interface and client ACL as a start-up argument.
	  Object[] args = getArguments();		 
		     	
  	  if (args != null && args.length > 0) {
			
  		  language = (String) args[0];
	  	  	  
  		  messageContent = (String) args[1];
  		  translatedContent = (String) args[2]; 

  		  translatedSourceAnswer = (String) args[3];	      
  		  sourceAnswer = (String) args[4];
			
  		  // Create the dictionary of source vocabulary.
  		  sourceDictionary.put(messageContent, translatedContent);
  		  //The sourceAnswer is translated in reverse.
  		  sourceDictionary.put(translatedSourceAnswer, sourceAnswer);
  		  
	      // Create the catalogue of language to transalate between the source legacy interface and client AC.
	      languageCatalogue = new Hashtable<String, Dictionary<String, String>>();
		
	      languageCatalogue.put(language, sourceDictionary);
	      		  
	      System.out.println(language + " inserted into language catalogue. " +
			" \n Message Content: " + messageContent + ", Translated Content: " + translatedContent + 
	      	" \n Translated Source Answer:" + translatedSourceAnswer + ", Source Answer:" + sourceAnswer +
			" \n inserted into the dictionary of source vocabulary.");
		  
      	  // Perform the translatedContent request for sourceAnswer
	      addBehaviour(new RequestSourceAnswerPerformer());
	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No language info is specified to be added in catalogue");
		  doDelete();
	  }      
	}

	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Wrapper-Agent "+getAID().getName()+" terminating.");	    
	}
  
	/** Inner class RequestSourceAnswerPerformer
	  * This is the behaviour used by Wrapper agent to request Source agents the source-answer. 
	  * */	
	private class RequestSourceAnswerPerformer extends Behaviour {		
  
	  private MessageTemplate mt; 		// The template to receive replies
	  private int step = 0;	  	  
	  
	  public void action() {
		 try{ 
			 switch (step) {
			    case 0:			    	
			      // Send the translated-content to the known source agent
			      ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("Source_Answer_REQUEST_Performative")));			      
	
		    	  request.addReceiver(source);			      
			      request.setContent(translatedContent);
			      request.setConversationId(getResourceString("Source_Answer_REQUEST_ConversationID"));
			      request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				  				  
			      sendMessage(request);
				  
				  // Prepare the template to get source-answer
			      mt=MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Source_Answer_INFORM_Performative"))),
			    		  MessageTemplate.and(
			    		  MessageTemplate.MatchConversationId(getResourceString("Source_Answer_INFORM_ConversationID")),
			              MessageTemplate.MatchInReplyTo(request.getReplyWith())));
			      
			      step = 1;			      
			      break;			    
			    case 1:
			    	// Receive the reply (source-answer inform/failure) from the source agent
			    	ACLMessage reply = receiveMessage(myAgent, mt);			    	
			    	if (reply != null) {			        
			    		// Reply received
				        if (reply.getPerformative() == ACLMessage.INFORM) {
				          // This is a source answer 
				          sourceAnswerSuccess = true;
				          sourceAnswer = reply.getContent();
					      System.out.println(translatedContent+" successfully responded by source agent " + reply.getSender().getName());
					      System.out.println(", source-answer = "+ sourceAnswer);
				        }
					    else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [translatedContent-unavailable]
						  System.out.println(reply.getContent()+ ": Source agent " + reply.getSender().getName());						   
					    }
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
	}  // End of inner class RequestSourceAnswerPerformer
}