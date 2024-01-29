/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				EmbassyPattern
 * Agent Under Test:			ForeignAgent
 * Mock Agent:					MockEmbassyAgent
 */
package MediationPatterns.EmbassyPattern.ForeignAgent.MockEmbassyAgent;

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
public class MockEmbassyAgent extends JADEMockAgent {

	private static ResourceBundle resMockEmbassyAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.EmbassyPattern.ForeignAgent.MockEmbassyAgent.MockEmbassyAgent");
	
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
	           
	      // Add the behaviour serving agent domain access queries from foreign agents
	  	  addBehaviour(new RequestAccessServer());

	  	  // Add the behaviour serving translated local response queries from foreign agents
	      addBehaviour(new RequestTranslatedLocalResponseServer());

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
  
	/**
	   Inner class RequestAccessServer.
	   
	   This is the behaviour used by the Embassy agents to serve incoming requests 
	   for agent domain access from foreign agents.
	   
	   If the requested agentDomain is in the agentDomainCatalogue, the embassy agent replies 
	   with an AGREE message. Otherwise a REFUSE message is sent back.
	 */
	private class RequestAccessServer extends CyclicBehaviour {
		
	  public void action() {
		  try {		  
			  MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Access_REQUEST_Performative"))),
			    		  MessageTemplate.MatchConversationId(getResourceString("Access_REQUEST_ConversationID")));
			  
			  ACLMessage msg = receiveMessage(myAgent, mt);
			  
			  if (msg != null) {
				  // REQUEST Message received. Process it
				  agentDomain = msg.getContent(); //REQUEST [agent-domain]
				  digitalCertificateLevel = Integer.parseInt(msg.getUserDefinedParameter("DigitalCertificateLevel"));
	      
				  ACLMessage reply = msg.createReply();
				  Integer localDigitalCertificateLevel = (Integer) agentDomainCatalogue.get(agentDomain);
	
				  if (localDigitalCertificateLevel != null && digitalCertificateLevel >= localDigitalCertificateLevel) {
					  /* The agent domain is supported by the embassy agent.
					   * And the foreign agent digital certificate level is enough to access the agent domain.
					   * Reply with the AGREE [access-granted]
					   */
					  reply.setPerformative(ACLMessage.getInteger(getResourceString("AGREE_Performative")));
					  reply.setContent(getResourceString("AGREE_Content")); //AGREE [access-granted]
				  }
				  else {
					  // The requested service is NOT available for sale.
					  reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
					  reply.setContent(getResourceString("REFUSE_Content")); //REFUSE [access-denied]
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
	}  // End of inner class RequestAccessServer
	
	/** Inner class RequestTranslatedLocalResponseServer. 
	 * This is the behaviour used by Embassy agents to serve incoming requests for translated local-response 
	 * from foreign agents. If the ontology that the message content is transalated in accordance with 
	 * is in the ontologyCatalogue, and the message content has translation in the localDictionary,
	 * the embassy agent replies with an INFORM message specifying the translated local response. 
	 * Otherwise a FAILURE message is sent back.
	 */   
	private class RequestTranslatedLocalResponseServer extends CyclicBehaviour {
		   
	  public void action() {
		  try {
			MessageTemplate mt = MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(
		    				  getResourceString("Translation_REQUEST_Performative"))),
		    		  MessageTemplate.MatchConversationId(
		    				  getResourceString("Translation_REQUEST_ConversationID")));
				
			ACLMessage msg = receiveMessage(myAgent, mt);
				
		    if (msg != null) {
		 	      
		      // REQUEST Message received. Process it
		      foreignContent = msg.getContent(); //REQUEST [message-content]
		      ontology = msg.getOntology();
		 	      
		      ACLMessage reply = msg.createReply();
		      
		      localDictionary = ontologyCatalogue.get(ontology);	
		      if (localDictionary != null) {
		    	  // The ontology is available in the ontologyCatalogue 
		    	  translatedForeignContent = localDictionary.get(foreignContent);
		    	  if (translatedForeignContent != null){
		    		  // The message content has translation in the localDictionary
			      	  
			      	  // Perform the translatedContent request for localReponse
		    		  localResponseSuccess = Boolean.parseBoolean(getResourceString("Local_Response_Success"));
		    		  localResponse = getResourceString("Offered_Local_Response");
					      
				      if (!localResponseSuccess){
				    	  // The translatedContent is NOT available for localResponse.
				    	  reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_FAILURE_Performative")));
				    	  reply.setContent(getResourceString("Translation_FAILURE_Content")); //FAILURE [translation-failed]
				      }
				      else{
				    	   
				    	  // The translatedContent is available. Reply with the localResponse translated in reverse.
				    	  boolean localResponseFound = false;
				    	  
						  for (Enumeration<String> it = localDictionary.keys(); it.hasMoreElements(); ) {
							  translatedLocalResponse = (String) it.nextElement();
							  if (localResponse == localDictionary.get(translatedLocalResponse)){
							  	localResponseFound = true;
							  	break;						  			
							  }
						  }
						  
						  if (localResponseFound){
							  //The localResponse has reversed translation in the localDictionary
							  reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_INFORM_Performative")));
							  reply.setContent(translatedLocalResponse);
						  }
						  else{
							  //The localResponse has no reversed translation in the localDictionary
							  reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_FAILURE_Performative")));
							  reply.setContent(getResourceString("Translation_FAILURE_Content")); //FAILURE [translation-failed]
						  }
				      }
		    	  }
		    	  else{
		  	    	// The message content doesn't have translation in the localDictionary 
		  	        reply.setPerformative(ACLMessage.getInteger(getResourceString("Translation_FAILURE_Performative")));
		  	        reply.setContent(getResourceString("Translation_FAILURE_Content")); //FAILURE [translation-failed]
		    	  }
		      }
		      else {
		    	// The ontology is NOT available in the ontologyCatalogue 
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
	}  // End of inner class RequestTranslatedLocalResponseServer	 	
}