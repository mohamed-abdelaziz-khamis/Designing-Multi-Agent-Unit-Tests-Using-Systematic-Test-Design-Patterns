/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.EmbassyPattern.EmbassyAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings("all")
public class EmbassyAgent extends Agent {

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
	  System.out.println("Hallo! Embassy-Agent "+getAID().getName()+" is ready.");
	  
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
	    System.out.println("Embassy-Agent "+getAID().getName()+" terminating.");	    
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
		  
			MessageTemplate mt = MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		    		  MessageTemplate.MatchConversationId("request-embassy"));
		  
		  ACLMessage msg = myAgent.receive(mt);
		  
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
				  reply.setPerformative(ACLMessage.AGREE);
				  reply.setContent("access-granted"); //AGREE [access-granted]
			  }
			  else {
				  // The requested service is NOT available for sale.
				  reply.setPerformative(ACLMessage.REFUSE);
				  reply.setContent("access-denied"); //REFUSE [access-denied]
			  }
			  send(reply);
		  }
		  else {
		    block();
		  }
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
		MessageTemplate mt = MessageTemplate.and(
	    		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	    		  MessageTemplate.MatchConversationId("request-embassy"));
			
		ACLMessage msg = receive(mt);
			
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
		      	  
	    		  localResponseSuccess = false;
		          
		      	  // Perform the translatedContent request for localReponse
			      addBehaviour(new RequestLocalReponsePerformer());
				      
			      if (!localResponseSuccess){
			    	  // The translatedContent is NOT available for localResponse.
			    	  reply.setPerformative(ACLMessage.FAILURE);
			    	  reply.setContent("translation-failed"); //FAILURE [translation-failed]
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
						  reply.setPerformative(ACLMessage.INFORM);
						  reply.setContent(translatedLocalResponse);
					  }
					  else{
						  //The localResponse has no reversed translation in the localDictionary
						  reply.setPerformative(ACLMessage.FAILURE);
						  reply.setContent("translation-failed"); //FAILURE [translation-failed]
					  }
			      }
	    	  }
	    	  else{
	  	    	// The message content doesn't have translation in the localDictionary 
	  	        reply.setPerformative(ACLMessage.FAILURE);
	  	        reply.setContent("translation-failed"); //FAILURE [translation-failed]
	    	  }
	      }
	      else {
	    	// The ontology is NOT available in the ontologyCatalogue 
	        reply.setPerformative(ACLMessage.FAILURE);
	        reply.setContent("translation-failed"); //FAILURE [translation-failed]
	      }
	      send(reply);
	    }
		else {
		    block();
		}
	  }
	}  // End of inner class RequestTranslatedLocalResponseServer	 
	
	 /** Inner class RequestLocalReponsePerformer
    This is the behaviour used by Embassy agent to request local agents the local-response. */	
	private class RequestLocalReponsePerformer extends Behaviour {		
  
	  private MessageTemplate mt; 		// The template to receive replies
	  private int step = 0;	  	  
	  
	  public void action() {		  
		 switch (step) {
		    case 0:			    	
		      // Send the translated-content to the known local agent
		      ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      

	    	  request.addReceiver(local);			      
		      request.setContent(translatedForeignContent);
		      request.setConversationId("request-embassy");
		      request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
			  				  
			  send(request);
			  
			  // Prepare the template to get local-response
		      mt=MessageTemplate.and(
		    		  MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		    		  MessageTemplate.and(
		    		  MessageTemplate.MatchConversationId("request-embassy"),
		              MessageTemplate.MatchInReplyTo(request.getReplyWith())));
		      
		      step = 1;			      
		      break;			    
		    case 1:
		    	// Receive the reply (local-response inform/failure) from the local agent
		    	ACLMessage reply = receive(mt);			    	
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
	
	  public boolean done() {  	
	    return (step == 2);
	  }
	}  // End of inner class RequestLocalReponsePerformer
}
