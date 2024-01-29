/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				WrapperPattern
 * Agent Under Test:			WrapperAgent
 * Mock Agent:					MockWrapperClientAgent
 */
package MediationPatterns.WrapperPattern.WrapperAgent.MockWrapperClientAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")

public class MockWrapperClientAgent extends JADEMockAgent {
	private static ResourceBundle resMockClientAgent = 
		 ResourceBundle.getBundle
		 ("MediationPatterns.WrapperPattern.WrapperAgent.MockWrapperClientAgent.MockWrapperClientAgent");
					
		 /** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
		 Returns the key if not found.*/
					
	private static String getResourceString(String key) {
	   try {
		   return resMockClientAgent.getString(key);
	   } catch (MissingResourceException e) {
		  return key;
	   } catch (NullPointerException e) {
		  return "!" + key + "!";
	   }			
	}

	// Content of the request message 
	private String messageContent;

	// Wrapper Agent translates between source legacy interface and client ACL
	private String language;

	// Wrapper Agent ID
	private AID wrapper;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Mock-Client-Agent "+getAID().getName()+" is ready.");	    		  

		wrapper = new AID("wrapper", AID.ISLOCALNAME);

		// Get the Message Content and the Client Agent Communcation Language as start-up arguments
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			messageContent = (String) args[0];
			System.out.println("Content of the request message: " + messageContent);	  

			language = (String) args[1];
			System.out.println("Client Agent Communication Langauge : " + language);

			/* Perform the required functionality request from the legacy system
			 * and getting the translated source answer back from the wrapper agent
			 */
			addBehaviour(new RequestLegacyFunctionalityPerformer());					
		}

		else {
			// Make the agent terminate
			System.out.println("No legacy functionality data specified");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Mock-Client-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestLegacyFunctionalityPerformer. 
	 *  This is the behaviour used by Client agent to request  
	 *  legacy functionality and getting the translated source 
	 *  answer back from Wrapper agent.
	 */		
	private class RequestLegacyFunctionalityPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {
			try{
				switch (step) {	
				case 0:			    	
					/*
					 * Send the content of the required legacy functionality message 
					 * to the wrapper agent to be translated according to the client ACL
					 */
					ACLMessage request = new ACLMessage(ACLMessage.getInteger(
							getResourceString("REQUEST_Performative")));			      

					request.addReceiver(wrapper);	      
					request.setContent(messageContent);
					request.setLanguage(language);
					request.setConversationId(getResourceString("REQUEST_ConversationID"));
					request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  

					sendMessage(request);			      

					// Prepare the template to get translated source answer
					mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative"))),
							MessageTemplate.and(
									MessageTemplate.MatchConversationId(getResourceString("INFORM_ConversationID")),
									MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
					step = 1;			      
					break;
				case 1:      	    
					/* Receive the legacy functionality request reply (translated source answer inform/failure) 
					 * from the wrapper agent*/
					reply = receiveMessage(myAgent, mt);	    			      
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.INFORM) { //INFORM [translated-source-answer]
							// Source answer translated successfully. We can terminate
							System.out.println(messageContent+" successfully translated by wrapper agent " + reply.getSender().getName());
							System.out.println("Translated Source Answer = "+ reply.getContent());
						}
						else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [translation-failed]
							System.out.println(reply.getContent()+ ": Wrapper agent " + reply.getSender().getName());
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
	}  // End of inner class RequestLegacyFunctionalityPerformer			
}
		



