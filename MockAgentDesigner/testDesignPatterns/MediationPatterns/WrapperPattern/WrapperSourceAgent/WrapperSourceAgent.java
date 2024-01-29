/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.WrapperPattern.WrapperSourceAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class WrapperSourceAgent extends Agent {

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
	  System.out.println("Hallo! Source-Agent "+getAID().getName()+" is ready.");	    		  
	  
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
	    System.out.println("Source-Agent "+getAID().getName()+" terminating.");
	}
	
	
	/** Inner class SourceAnswerRequestsServer. 
	 * This is the behaviour used by Source agents to serve incoming requests for source-answer from wrapper agents.
	 * If the translated request content is in the local catalogue, the source agent replies with an INFORM message 
	 * specifying the source answer. Otherwise a FAILURE message is sent back.
	 */   
	private class SourceAnswerRequestsServer extends CyclicBehaviour {
		   
	  public void action() { 		
		MessageTemplate mt = MessageTemplate.and(
	    		  MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	    		  MessageTemplate.MatchConversationId("service-wrapping"));
			
		ACLMessage msg = receive(mt);
			
	    if (msg != null) {
	 	      
	      // REQUEST Message received. Process it
	      String translatedContent = msg.getContent(); //REQUEST [translated-content]
	 	      
	      ACLMessage reply = msg.createReply();
	      String sourceAnswer = catalogue.get(translatedContent);
	
	      if (sourceAnswer != null) {	 	    	  	 	    	  	
	    	// The translatedContent is available. Reply with the sourceAnswer
	        reply.setPerformative(ACLMessage.INFORM);
	        reply.setContent(sourceAnswer);	 	        
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
	}  // End of inner class SourceAnswerRequestsServer	 	
}