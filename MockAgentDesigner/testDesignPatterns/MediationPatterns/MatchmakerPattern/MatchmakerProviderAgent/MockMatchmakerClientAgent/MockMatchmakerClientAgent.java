/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MatchmakerPattern
 * Agent Under Test:			MatchmakerProviderAgent
 * Mock Agent:					MockMatchmakerClientAgent
 */
package MediationPatterns.MatchmakerPattern.MatchmakerProviderAgent.MockMatchmakerClientAgent;

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
public class MockMatchmakerClientAgent extends JADEMockAgent {
	private static ResourceBundle resMockClientAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MatchmakerPattern.MatchmakerProviderAgent.MockMatchmakerClientAgent.MockMatchmakerClientAgent");
					
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

	// Provider Agent ID
	private AID provider;


	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Mock-Client-Agent "+getAID().getName()+" is ready.");	    		  

		provider = new AID("provider", AID.ISLOCALNAME);

		// Get the title of the requested service as a start-up argument
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			serviceTitle = (String) args[0];
			System.out.println("The title of the requested service: "+serviceTitle);	  

			// Perform the service request from the provider agent
			addBehaviour(new RequestServicePerformer());					
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

	/** Inner class RequestServicePerformer. 
	 *  This is the behaviour used by Client agent to request  
	 *  from Provider agent the required service.
	 */		
	private class RequestServicePerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;
		private AID[] serviceProviderAgents; // The list of known service provider agents
		private int repliesCnt = 0; 	// The counter of replies from service provider agents

		public void action() {
		  try{	
			 switch (step) {	
				case 0:			    	
					// Send the service request to the service provider agents
					ACLMessage providerRequest = new ACLMessage(ACLMessage.getInteger(
							getResourceString("Provider_REQUEST_Performative")));			      
	
					providerRequest.addReceiver(provider);		      
					providerRequest.setContent(serviceTitle);
					providerRequest.setConversationId(getResourceString("Provider_REQUEST_ConversationID"));
					providerRequest.setReplyWith("providerRequest"+System.currentTimeMillis()); // Unique value		  				  
	
					sendMessage(providerRequest);			      
	
					// Prepare the template to get the service
					mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.getInteger(
									getResourceString("Provider_INFORM_Performative"))),
									MessageTemplate.and(
											MessageTemplate.MatchConversationId(getResourceString("Provider_INFORM_ConversationID")),
											MessageTemplate.MatchInReplyTo(providerRequest.getReplyWith())));			      			    	
					step = 1;			      
					break;
				case 1:      	    
					// Receive the reply (inform/refusal) from the provider agent
					reply = receiveMessage(myAgent, mt);	    			      
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.INFORM) { //INFORM [service-price]
							// Service informed successfully. We can terminate
							System.out.println(serviceTitle+" successfully informed by provider agent " + reply.getSender().getName());
							System.out.println("Price = "+ reply.getContent());
	
							step = 2;
						}
						else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [request-refused]
							System.out.println(reply.getContent()+ ": Provider agent " + reply.getSender().getName());
	
							repliesCnt++;
							if (repliesCnt >= serviceProviderAgents.length) {
								// We received all replies
								step = 2; 
							}
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
	}  // End of inner class RequestServicePerformer			
}
		



