/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MediatorPattern
 * Agent Under Test:			MediatorClientAgent
 * Mock Agent:					MockMediatorAgent
 */
package MediationPatterns.MediatorPattern.MediatorClientAgent.MockMediatorAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockMediatorAgent extends JADEMockAgent {
	private static ResourceBundle resMockMediatorAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MediatorPattern.MediatorClientAgent.MockMediatorAgent.MockMediatorAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	
	private static String getResourceString(String key) {
		try {
			return resMockMediatorAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	

	// The catalogue of request for result (maps the title of a request to its sub-requests)
	private Hashtable<String, ArrayList<String>> catalogue;
	
	// The title of the request
	private String requestTitle;
	
	// The title of the sub-request
	private String subRequestTitle;

	// The list of sub requests
	private ArrayList<String> subRequests;
	
	// The list of integrated sub results
	private ArrayList<String> integratedSubResults;
	
	// Flag equals true if any of the known service provider agents replies with the sub result
	private boolean subResultSuccess;
	
	// Put agent initializations here
	protected void setup() {
		
	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Mediator-Agent "+getAID().getName()+" is ready.");
	  
	  /* The mediator coordinates the cooperative behavior of the colleagues 
	   * and has acquaintance models of all colleague agents.
	  */
	    
	  // Get the title of the request to provide result as a start-up argument.
	  Object[] args = getArguments();		 
		     	
  	  if (args != null && args.length > 0) {
			
  		  requestTitle = (String) args[0];
  		  subRequestTitle = (String) args[1];
  		  
  		  // Create the list of sub requests
  		  subRequests = new ArrayList<String>();
  		  subRequests.add(subRequestTitle);
	  	  
	      // Create the catalogue
		  catalogue = new Hashtable<String, ArrayList<String>>();		
		  catalogue.put(requestTitle, subRequests);
			
	      System.out.println(requestTitle + " inserted into catalogue. " +
	      					"It could be divided into the following sub requests: ");
	      
	      for (int i = 0; i < subRequests.size(); ++i)
	    	  System.out.println(subRequests.get(i));
	              
	  	  // Add the behaviour serving request result queries from client agents
	      addBehaviour(new RequestResultServer());
	  }		
	  else {
		  // Make the agent terminate
		  System.out.println("No request title specified to be added in catalogue");
		  doDelete();
	  }
      
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 		
	    // Printout a dismissal message
	    System.out.println("Mock-Mediator-Agent "+getAID().getName()+" terminating.");	    
	}
	
	/** Inner class RequestResultServer. 
	  * This is the behaviour used by Mediator agent to serve incoming requests for result from client agents.
	  * If there exist some service provider agent(s) had registered for this request, 
	  * the mediator agent replies with an INFORM message with the integrated sub results. 
	  * Otherwise a FAILURE message is sent back.
	  */
	   
	private class RequestResultServer extends CyclicBehaviour {
		   
	  public void action() {
		try{
			MessageTemplate mt = MessageTemplate.and(
			   		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Result_REQUEST_Performative"))),
			   		  MessageTemplate.MatchConversationId(getResourceString("Result_REQUEST_ConversationID")));
				
			ACLMessage msg = receiveMessage(myAgent, mt);
				
		 	if (msg != null) {
		 	      
		 	   // REQUEST Message received. Process it
		 	   requestTitle = msg.getContent(); //REQUEST [request-title]
		 	      
		 	   ACLMessage reply = msg.createReply();
		 	   subRequests = catalogue.get(requestTitle);
		
		 	   if (subRequests != null) {		 		   
		 		  // The request is available in catalouge.
		 		  integratedSubResults = new ArrayList<String>();
		 		  		 		   
		 	   	  for (int i = 0; i < subRequests.size(); ++i){	 	    		  
		 	   		  subRequestTitle = subRequests.get(i);
		 	   		  System.out.println("Trying to get sub-result for the sub-request: " + subRequestTitle);	          
				    						          			          
			      	  // Perform the sub-request for sub-result
			      	  subResultSuccess = Boolean.parseBoolean(getResourceString("Sub_Result_Success"));
			      	  integratedSubResults.add(getResourceString("Sub_Result"));
							      
				      if (!subResultSuccess){
				    	  // The sub-request is NOT available for sub-result.
					 	  reply.setPerformative(ACLMessage.getInteger(getResourceString("Result_FAILURE_Performative")));
					 	  reply.setContent(getResourceString("Result_FAILURE_Content")); //FAILURE [result-failed]
					 	  break;
				      }
				   }	
		 	   	   try {
			 	   	  	// The request is available for result. Reply with the integrated sub-results.
			 	   	  	reply.setPerformative(ACLMessage.getInteger(getResourceString("Result_INFORM_Performative")));
						reply.setContentObject(integratedSubResults);
		 	   	   } catch (IOException e) {
				 	    reply.setPerformative(ACLMessage.getInteger(getResourceString("Result_FAILURE_Performative")));
				 	    reply.setContent(getResourceString("Result_FAILURE_Content")); //FAILURE [result-failed]
		 	   	   }
		 	   	}	 	        
		 	   	else {
		 	        // The request is NOT available in catalouge.
		 	        reply.setPerformative(ACLMessage.getInteger(getResourceString("Result_FAILURE_Performative")));
		 	        reply.setContent(getResourceString("Result_FAILURE_Content")); //FAILURE [result-failed]
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
	}  // End of inner class RequestResultServer		
}