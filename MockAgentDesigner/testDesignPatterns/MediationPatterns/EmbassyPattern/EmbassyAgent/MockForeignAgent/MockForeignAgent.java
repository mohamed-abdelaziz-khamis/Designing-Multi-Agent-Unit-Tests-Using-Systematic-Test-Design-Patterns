/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				EmbassyPattern
 * Agent Under Test:			EmbassyAgent
 * Mock Agent:					MockForeignAgent
 */
package MediationPatterns.EmbassyPattern.EmbassyAgent.MockForeignAgent;

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
public class MockForeignAgent extends JADEMockAgent {

	private static ResourceBundle resMockForeignAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.EmbassyPattern.EmbassyAgent.MockForeignAgent.MockForeignAgent");

	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
		 Returns the key if not found.*/

	private static String getResourceString(String key) {
		try {
			return resMockForeignAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}

	//The foreign agent requests access to an agent domain from its embassy agent
	private String agentDomain;

	/* Depending on the level of the digital certificate provided,
	 * access may be granted or denied by the Embassy agent*/
	private int  digitalCertificateLevel;		 

	// Content of the sent performative message 
	private String messageContent;

	// Message content is translated in accordance with a standard ontology 
	private String ontology;

	// Embassy Agent ID
	private AID embassy;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Mock-Foreign-Agent "+getAID().getName()+" is ready.");	    		  

		embassy = new AID("embassy", AID.ISLOCALNAME);

		// Get the agentDomain, digitalCertificateLevel, messageContent and ontology as start-up arguments
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			agentDomain = (String) args[0];
			System.out.println("The foreign agent requests access to agent domain: " + agentDomain);	  

			digitalCertificateLevel = Integer.parseInt((String)args[1]);
			System.out.println("The level of the digital certificate provided: " + digitalCertificateLevel);	

			messageContent = (String) args[2];
			System.out.println("Content of the sent performative message: " + messageContent);	  

			ontology = (String) args[3];
			System.out.println("Message content is translated in accordance with a standard ontology : " + ontology);

			/* Perform the agent domain access request and getting translated local response back 
			 * from the embassy agent
			 */
			addBehaviour(new RequestAccessPerformer());					
		}

		else {
			// Make the agent terminate
			System.out.println("No agent domain access data specified");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Mock-Foreign-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestAccessPerformer. 
	 *  This is the behaviour used by Foreign agent to request  
	 *  agent domain access from Embassy agent.
	 */		
	private class RequestAccessPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {
		    try{
				switch (step) {	
				case 0:			    	
					// Send the agent domain access request to the embassy agent
					ACLMessage accessRequest = new ACLMessage(ACLMessage.getInteger(getResourceString("Access_REQUEST_Performative")));			      
	
					accessRequest.addReceiver(embassy);	      
					accessRequest.setContent(agentDomain);
					accessRequest.setConversationId(getResourceString("Access_REQUEST_ConversationID"));
					accessRequest.setReplyWith("accessRequest"+System.currentTimeMillis()); // Unique value
					accessRequest.addUserDefinedParameter("DigitalCertificateLevel", Integer.toString(digitalCertificateLevel));
	
					sendMessage(accessRequest);			      
	
					// Prepare the template to get access granted 
					mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("AGREE_Performative"))),
							MessageTemplate.and(
									MessageTemplate.MatchConversationId(getResourceString("AGREE_ConversationID")),
									MessageTemplate.MatchInReplyTo(accessRequest.getReplyWith())));			      			    	
					step = 1;			      
					break;			    		   
				case 1:
					// Receive the agent domain access request reply (agree/refusal) from the embassy agent
					reply = receiveMessage(myAgent, mt);			    				      
					if (reply != null) {				      
						// Reply received
						if (reply.getPerformative() == ACLMessage.AGREE) {	//AGREE [access-granted]				  	    	 				    	  
							System.out.println(reply.getContent() + ": Embassy agent "
									+ reply.getSender().getName());				    	 
							step = 2;				     
						}				      
						else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [access-denied]
							System.out.println(reply.getContent()+ ": Embassy agent "
									+ reply.getSender().getName());
							myAgent.doDelete();
						}				      
					}			      
					else {
						block();
					}		   			    
					break;
				case 2:			    	
					/*
					 * Send the content of the performative message to the embassy agent to be translated  
					 * in accordance with the standard ontology
					 */
					ACLMessage translationRequest = new ACLMessage(ACLMessage.getInteger(
							getResourceString("Translation_REQUEST_Performative")));			      
	
					translationRequest.addReceiver(embassy);	      
					translationRequest.setContent(messageContent);
					translationRequest.setOntology(ontology);
					translationRequest.setConversationId(getResourceString("Translation_REQUEST_ConversationID"));
					translationRequest.setReplyWith("translationRequest"+System.currentTimeMillis()); // Unique value		  				  
	
					sendMessage(translationRequest);			      
	
					// Prepare the template to get translated local response
					mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative"))),
							MessageTemplate.and(
									MessageTemplate.MatchConversationId(getResourceString("INFORM_ConversationID")),
									MessageTemplate.MatchInReplyTo(translationRequest.getReplyWith())));			      			    	
					step = 3;			      
					break;
				case 3:      	    
					/* Receive the translation request reply (translated local response inform/failure) 
					 * from the embassy agent*/
					reply = receiveMessage(myAgent, mt);	    			      
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.INFORM) { //INFORM [translated-local-response]
							// Local response translated successfully. We can terminate
							System.out.println(messageContent+" successfully translated by embassy agent " + reply.getSender().getName());
							System.out.println("Translated Local Response = "+ reply.getContent());
						}
						else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [translation-failed]
							System.out.println(reply.getContent()+ ": Embassy agent " + reply.getSender().getName());
						}			        	
						step = 4;
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
			return (step == 4);
		}
	}  // End of inner class RequestAccessPerformer			
}