/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				WrapperPattern
 * Agent Under Test:			WrapperClientAgent
 * Mock Agent:					MockWrapperAgent
 */
package MediationPatterns.WrapperPattern.WrapperClientAgent.MockWrapperAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockWrapperAgent extends JADEMockAgent {
	private static ResourceBundle resMockWrapperAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.WrapperPattern.WrapperClientAgent.MockWrapperAgent.MockWrapperAgent");
	
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
	
	// Flag equals true if the known source agent replies with the source answer
	private boolean sourceAnswerSuccess;
	
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Wrapper-Agent "+getAID().getName()+" is ready.");
	  
	  /* The role of the wrapper is to interface the agents to the legacy system by agentifying the legacy.
	  */
	  
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
		  
	  	  // Add the behaviour serving translated source answer queries from client agents
	      addBehaviour(new RequestTranslatedSourceAnswerServer());

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
  
	/** Inner class RequestTranslatedSourceAnswerServer. 
	 * This is the behaviour used by Wrapper agents to serve incoming requests for translated source-answer 
	 * from client agents. If the language that the message content is transalated in accordance with 
	 * is in the languageCatalogue, and the message content has translation in the sourceDictionary,
	 * the wrapper agent replies with an INFORM message specifying the translated source answer. 
	 * Otherwise a FAILURE message is sent back.
	 */   
	private class RequestTranslatedSourceAnswerServer extends CyclicBehaviour {		   
	  public void action() {
		try{  
			MessageTemplate mt = MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(
		    				  getResourceString("Translation_REQUEST_Performative"))),
		    		  MessageTemplate.MatchConversationId(
		    				  getResourceString("Translation_REQUEST_ConversationID")));
				
			ACLMessage msg = receiveMessage(myAgent, mt);
				
		    if (msg != null) {
		 	      
		      // REQUEST Message received. Process it
		      messageContent = msg.getContent(); //REQUEST [message-content]
		      language = msg.getLanguage();
		 	      
		      ACLMessage reply = msg.createReply();
		      
		      sourceDictionary = languageCatalogue.get(language);	
		      if (sourceDictionary != null) {
		    	  // The language is available in the languageCatalogue 
		    	  translatedContent = sourceDictionary.get(messageContent);
		    	  if (translatedContent != null){
		    		  // The message content has translation in the sourceDictionary
			      	  
			      	  // Perform the translatedContent request for sourceAnswer
		    		  sourceAnswerSuccess = Boolean.parseBoolean(getResourceString("Source_Answer_Success"));
		    		  sourceAnswer = getResourceString("Offered_Source_Answer");
					      
				      if (!sourceAnswerSuccess){
				    	  // The translatedContent is NOT available for sourceAnswer.
				    	  reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_FAILURE_Performative")));
				    	  reply.setContent(getResourceString("Translation_FAILURE_Content")); //FAILURE [translation-failed]
				      }
				      else{
				    	   
				    	  // The translatedContent is available. Reply with the sourceAnswer translated in reverse.
				    	  boolean sourceAnswerFound = false;
				    	  
						  for (Enumeration<String> it = sourceDictionary.keys(); it.hasMoreElements(); ) {
							  translatedSourceAnswer = (String) it.nextElement();
							  if (sourceAnswer == sourceDictionary.get(translatedSourceAnswer)){
							  	 sourceAnswerFound = true;
							  	 break;						  			
							  }
						  }
						  
						  if (sourceAnswerFound){
							  //The sourceAnswer has reversed translation in the sourceDictionary
							  reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_INFORM_Performative")));
							  reply.setContent(translatedSourceAnswer);
						  }
						  else{
							  //The sourceAnswer has no reversed translation in the sourceDictionary
							  reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_FAILURE_Performative")));
							  reply.setContent(getResourceString("Translation_FAILURE_Content")); //FAILURE [translation-failed]
						  }
				      }
		    	  }
		    	  else{
		  	    	// The message content doesn't have translation in the sourceDictionary 
		  	        reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_FAILURE_Performative")));
		  	        reply.setContent(getResourceString("Translation_FAILURE_Content")); //FAILURE [translation-failed]
		    	  }
		      }
		      else {
		    	// The language is NOT available in the languageCatalogue 
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_FAILURE_Performative")));
		        reply.setContent(getResourceString("Translation_FAILURE_Content")); //FAILURE [translation-failed]
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
	}  // End of inner class RequestTranslatedSourceAnswerServer	 
}