/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MatchmakerPattern
 * Agent Under Test:			MatchmakerAgent
 * Mock Agent:					MockMatchmakerClientAgent
 */
package MediationPatterns.MatchmakerPattern.MatchmakerAgent.MockMatchmakerClientAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockMatchmakerClientAgent extends JADEMockAgent {
	private static ResourceBundle resMockClientAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MatchmakerPattern.MatchmakerAgent.MockMatchmakerClientAgent.MockMatchmakerClientAgent");
					
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

	// The title of the requested service
	private String serviceTitle;

	// Matchmaker Agent ID
	private AID matchmaker;


	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Mock-Client-Agent "+getAID().getName()+" is ready.");	    		  

		matchmaker = new AID("matchmaker", AID.ISLOCALNAME);

		// Get the title of the requested service as a start-up argument
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			serviceTitle = (String) args[0];
			System.out.println("The title of the requested service: "+serviceTitle);	  

			// Perform the service-provider request from the matchmaker agent
			addBehaviour(new RequestServiceProviderPerformer());					
		}					  
		else {
			// Make the agent terminate
			System.out.println("No service title specified");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Mock-Client-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestServiceProviderPerformer. 
	 *  This is the behaviour used by Client agent to request  
	 *  from Matchmaker agent the required service-provider.
	 */		
	private class RequestServiceProviderPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;
		private AID[] serviceProviderAgents; // The list of known service provider agents

		public void action() {
		  try{
			  switch (step) {	
				case 0:			    	
					// Send the service request to the matchmaker agent
					ACLMessage matchmakerRequest = new ACLMessage(ACLMessage.getInteger(
							getResourceString("Matchmaker_REQUEST_Performative")));			      

					matchmakerRequest.addReceiver(matchmaker);	      
					matchmakerRequest.setContent(serviceTitle);
					matchmakerRequest.setConversationId(getResourceString("Matchmaker_REQUEST_ConversationID"));
					matchmakerRequest.setReplyWith("matchmakerRequest"+System.currentTimeMillis()); // Unique value		  				  
					matchmakerRequest.addUserDefinedParameter(getResourceString("Service_Description_Type_Key"),
							getResourceString("Service_Description_Type_Value"));

					sendMessage(matchmakerRequest);			      

					// Prepare the template to get the service provider agent identifier
					mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.getInteger(
									getResourceString("Matchmaker_INFORM_Performative"))),
									MessageTemplate.and(
											MessageTemplate.MatchConversationId(getResourceString("Matchmaker_INFORM_ConversationID")),
											MessageTemplate.MatchInReplyTo(matchmakerRequest.getReplyWith())));			      			    	
					step = 1;			      
					break;			    		   
				case 1:
					// Receive the reply (inform/refusal) from the matchmaker agent
					reply = receiveMessage(myAgent, mt);			    				      
					if (reply != null) {				      
						// Reply received
						if (reply.getPerformative() == ACLMessage.INFORM) {	//INFORM [provider-ID]
							try {
								serviceProviderAgents = (AID[]) reply.getContentObject();															  								  
								System.out.println("Found the following service provider agents:");
								for (int i = 0; i < serviceProviderAgents.length; ++i) {
									System.out.println(serviceProviderAgents[i].getName());
								}								  
								step = 2;
							} catch (UnreadableException e) {
								e.printStackTrace();
							}						    				     
						}				      
						else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [request-refused]
							System.out.println(reply.getContent()+ ": Matchmaker agent "
									+ reply.getSender().getName());
							myAgent.doDelete();
						}				      
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
	}  // End of inner class RequestServiceProviderPerformer			
}
		



