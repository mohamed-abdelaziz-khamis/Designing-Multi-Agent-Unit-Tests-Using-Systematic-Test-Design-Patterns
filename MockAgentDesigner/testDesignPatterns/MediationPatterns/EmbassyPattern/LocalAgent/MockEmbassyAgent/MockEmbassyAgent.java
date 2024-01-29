/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				EmbassyPattern
 * Agent Under Test:			LocalAgent
 * Mock Agent:					MockEmbassyAgent
 */
package MediationPatterns.EmbassyPattern.LocalAgent.MockEmbassyAgent;

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
public class MockEmbassyAgent extends JADEMockAgent {
	private static ResourceBundle resMockEmbassyAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.EmbassyPattern.LocalAgent.MockEmbassyAgent.MockEmbassyAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockEmbassyAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	/* The catalogue of agent domain to grant/deny access 
	 * (maps the agent domain to its digital certificate level)
	 */
	private Hashtable<String, Integer> agentDomainCatalogue;
	
	// The foreign agent requests access to an agent domain from its embassy agent
	private String agentDomain;
	 
	/* Depending on the level of the digital certificate provided,
	 * access may be granted or denied by the Embassy agent*/
	private int  digitalCertificateLevel;		 

	/* The catalogue of ontology to transalate the message content in accordance with.
	 * (maps the ontology to its local dictionary)
	 */		
	private Hashtable<String, Dictionary<String, String>> ontologyCatalogue;

	// Message content is translated in accordance with a standard ontology 
	private String ontology;
	
	/* The dictionary of local vocabulary.
	 * (maps the foreign content to its local content)
	 */	
	private Dictionary<String, String> localDictionary;

	// Content of the sent performative message 
	private String foreignContent;
	
	// Translated Performative Message Content 
	private String translatedForeignContent;
		
	// Local Response
	private String localResponse;
	
	// Translated Local Response
	private String translatedLocalResponse;
	
	// Local Agent ID
	private AID local;
	
	// Flag equals true if the known Local agent replies with the local reponse
	private boolean localResponseSuccess;
	
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Embassy-Agent "+getAID().getName()+" is ready.");
	  
	  /* The embassy agent grant/deny access to agent domain depending on the level of the digital certificate provided. 
	   * Messages are submitted by the foreign agent to the embassy for translation. 
	   * The content of the message is translated in accordance with a standard ontology. 
	   * A library of such ontologies would need to be available. 
	   * Translated messages must then pass through another level of security before being forwarded to the domain agents.
	   * The result of the translated query/message are passed back out to the foreign agent, translated in reverse.
	  */
	    
	  local = new AID("local", AID.ISLOCALNAME);
	  
	  // Get the name of the agent domain to grant/deny access as a start-up argument.
	  Object[] args = getArguments();		 
		     	
  	  if (args != null && args.length > 0) {
			
  		  agentDomain = (String) args[0];
  		  digitalCertificateLevel = Integer.parseInt((String)args[1]);
	  	  
	      // Create the catalogue of agent domain to grant/deny access
  		  agentDomainCatalogue = new Hashtable<String, Integer>();
		
  		  agentDomainCatalogue.put(agentDomain, digitalCertificateLevel);
			
	      System.out.println(agentDomain + " inserted into agent domain catalogue. " +
	      					"In order to be accessed must have at least digital certificate level: " + 
	      					digitalCertificateLevel);	      

	      ontology = (String) args[2];
	      
	      foreignContent = (String) args[3];	      
	      translatedForeignContent = (String) args[4];
	      
	      translatedLocalResponse = (String) args[5];	      
	      localResponse = (String) args[6];

	  	  // Create the dictionary of local vocabulary that maps the foreign content to its local content
	      localDictionary.put(foreignContent, translatedForeignContent);
	      //The localResponse is translated in reverse.
	      localDictionary.put(translatedLocalResponse, localResponse);
	  	  
	      // Create the catalogue of ontology to transalate the message content in accordance with.
	      ontologyCatalogue = new Hashtable<String, Dictionary<String, String>>();
		
	      ontologyCatalogue.put(ontology, localDictionary);
	      		  
	      System.out.println(ontology + " inserted into ontology catalogue. " +
	      			" \n Foreign Content:" + foreignContent + ", Translated Foreign Content:" + translatedForeignContent + 
	      			" \n Translated Local Response:" + translatedLocalResponse + ", Local Response:" + localResponse +
	      			" \n inserted into local dictionary.");
	           
      	  // Perform the translatedContent request for localReponse
	      addBehaviour(new RequestLocalReponsePerformer());
	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No agent domain info is specified to be added in catalogue");
		  doDelete();
	  }      
	}

	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Embassy-Agent "+getAID().getName()+" terminating.");	    
	}
	
	 /** Inner class RequestLocalReponsePerformer
    This is the behaviour used by Embassy agent to request local agents the local-response. */	
	private class RequestLocalReponsePerformer extends Behaviour {		
  
	  private MessageTemplate mt; 		// The template to receive replies
	  private int step = 0;	  	  
	  
	  public void action() {
		  try {
			 switch (step) {
			    case 0:			    	
			      // Send the translated-content to the known local agent
			      ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("Local_Response_REQUEST_Performative")));			      
	
		    	  request.addReceiver(local);			      
			      request.setContent(translatedForeignContent);
			      request.setConversationId(getResourceString("Local_Response_REQUEST_ConversationID"));
			      request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				  				  
			      sendMessage(request);
				  
				  // Prepare the template to get local-response
			      mt=MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Local_Response_INFORM_Performative"))),
			    		  MessageTemplate.and(
			    		  MessageTemplate.MatchConversationId(getResourceString("Local_Response_INFORM_ConversationID")),
			              MessageTemplate.MatchInReplyTo(request.getReplyWith())));
			      
			      step = 1;			      
			      break;			    
			    case 1:
			    	// Receive the reply (local-response inform/failure) from the local agent
			    	ACLMessage reply = receiveMessage(myAgent, mt);		    	
			    	if (reply != null) {			        
			    		// Reply received
				        if (reply.getPerformative() == ACLMessage.INFORM) {
				          // This is a local response 
				          localResponseSuccess = true;
				          localResponse = reply.getContent();
					      System.out.println(translatedForeignContent+" successfully responded by local agent " + reply.getSender().getName());
					      System.out.println(", local-response = "+ localResponse);
				        }
					    else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [translatedContent-unavailable]
						  System.out.println(reply.getContent()+ ": Local agent " + reply.getSender().getName());						   
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
	}  // End of inner class RequestLocalReponsePerformer
}