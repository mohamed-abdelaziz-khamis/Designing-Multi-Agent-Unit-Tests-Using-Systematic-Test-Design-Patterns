/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.EmbassyPattern.LocalAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;


@SuppressWarnings("serial")
public class LocalAgent extends Agent {

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
	  System.out.println("Hallo! Local-Agent "+getAID().getName()+" is ready.");	    		  
	  
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
	    System.out.println("Local-Agent "+getAID().getName()+" terminating.");
	}
	
	
	/** Inner class RequestLocalResponseServer. 
	 * This is the behaviour used by Local agents to serve incoming requests for local-response from embassy agents.
	 * If the translated content is in the local catalogue, the local agent replies with an INFORM message 
	 * specifying the local response. Otherwise a FAILURE message is sent back.
	 */   
	private class RequestLocalResponseServer extends CyclicBehaviour {
		   
	  public void action() { 		
		MessageTemplate mt = MessageTemplate.and(
	    		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	    		  MessageTemplate.MatchConversationId("request-embassy"));
			
		ACLMessage msg = receive(mt);
			
	    if (msg != null) {
	 	      
	      // REQUEST Message received. Process it
	      String translatedContent = msg.getContent(); //REQUEST [translated-content]
	 	      
	      ACLMessage reply = msg.createReply();
	      String localResponse = catalogue.get(translatedContent);
	
	      if (localResponse != null) {	 	    	  	 	    	  	
	    	// The translatedContent is available. Reply with the localResponse
	        reply.setPerformative(ACLMessage.INFORM);
	        reply.setContent(localResponse);	 	        
	      }
	      else {
	        // The translatedContent is NOT available.
	        reply.setPerformative(ACLMessage.FAILURE);
	        reply.setContent("translatedContent-unavailable"); //FAILURE [translatedContent-unavailable]
	      }
	      send(reply);
	    }
		else {
		    block();
		}
	  }
	}  // End of inner class RequestLocalResponseServer	 	
}