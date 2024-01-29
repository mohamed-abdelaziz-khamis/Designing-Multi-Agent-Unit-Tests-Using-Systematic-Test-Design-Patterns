/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MatchmakerPattern
 * Agent Under Test:			MatchmakerAgent
 * Mock Agent:					MockMatchmakerProviderAgent
 */
package MediationPatterns.MatchmakerPattern.MatchmakerAgent.MockMatchmakerProviderAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockMatchmakerProviderAgent extends JADEMockAgent {

	private static ResourceBundle resMockProviderAgent = 
		ResourceBundle.getBundle
		("MediationPatterns.MatchmakerPattern.MatchmakerAgent.MockMatchmakerProviderAgent.MockMatchmakerProviderAgent");
		

	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
		Returns the key if not found.*/
		
	private static String getResourceString(String key) {
		try {
			return resMockProviderAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}

	// The catalogue of service for sale (maps the title of a service to its price)
	private Hashtable<String, Float> catalogue;
	private String serviceTitle;
	private float servicePrice;

	//Matchmaker Agent ID	
	private AID matchmaker;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Mock-Provider-Agent "+getAID().getName()+" is ready.");	    		  

		matchmaker = new AID("matchmaker", AID.ISLOCALNAME);

		// Get the title of the service to provide as a start-up argument.
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			serviceTitle = (String) args[0];
			servicePrice = Float.parseFloat((String)args[1]);

			// Create the catalogue
			catalogue = new Hashtable<String, Float>();

			catalogue.put(serviceTitle, servicePrice);

			System.out.println(serviceTitle + " inserted into catalogue. " 
					+ ", Price = " + servicePrice);

			// Perform the subscription request to the matchmaker agent
			addBehaviour(new RequestSubscriptionPerformer());

			addBehaviour(new WakerBehaviour(this, Long.parseLong(getResourceString("Waker_Behaviour_Period"))) {

				protected void handleElapsedTimeout() {
					// Perform the unsubscription request to the matchmaker agent
					addBehaviour(new RequestUnsubscriptionPerformer());
				}
			} );
		}		
		else {
			// Make the agent terminate
			System.out.println("No service title specified to be added in catalogue");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Mock-Provider-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestSubscriptionPerformer. 
	 *  This is the behaviour used by Provider agent to request subscription 
	 *  from Matchmaker agent for the required service.
	 */		
	private class RequestSubscriptionPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {
			try{
				switch (step) {	
					case 0:			    	
						// Send the request to the matchmaker agent
						ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("Subscription_REQUEST_Performative")));			      
		
						request.addReceiver(matchmaker);	      
						request.setContent("subscription");
						request.setConversationId(getResourceString("Subscription_REQUEST_ConversationID"));
						request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
		
						request.addUserDefinedParameter(getResourceString("Service_Description_Type_Key"),
								getResourceString("Service_Description_Type_Value"));
		
						request.addUserDefinedParameter(getResourceString("Service_Description_Name_Key"),
								getResourceString("Service_Description_Name_Value"));
		
						sendMessage(request);			      
		
						// Prepare the template to get subscription accept proposal
						mt = MessageTemplate.and(
								MessageTemplate.MatchPerformative(ACLMessage.getInteger(
										getResourceString("Subscription_ACCEPT_PROPOSAL_Performative"))),
										MessageTemplate.and(
												MessageTemplate.MatchConversationId(
														getResourceString("Subscription_ACCEPT_PROPOSAL_ConversationID")),
														MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
						step = 1;			      
						break;			    		   
					case 1:
						// Receive the reply (subscription accept proposal/refusal) from the matchmaker agent
						reply = receiveMessage(myAgent, mt);			    				      
						if (reply != null) {				      
							// Reply received
							if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [subscription-accepted]				  	    	 				    	  
								System.out.println(reply.getContent() + ": Matchmaker agent "
										+ reply.getSender().getName());				    	 
								step = 2;				     
							}				      
							else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [subscription-refused]
								System.out.println(reply.getContent() + ": Matchmaker agent "
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
	}  // End of inner class RequestSubscriptionPerformer

	/** Inner class RequestUnsubscriptionPerformer. 
	 *  This is the behaviour used by Provider agent to request unsubscription 
	 *  from Matchmaker agent.
	 */		
	private class RequestUnsubscriptionPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {
			try{
				switch (step) {	
					case 0:			    	
						// Send the request to the matchmaker agent
						ACLMessage request = new ACLMessage(ACLMessage.getInteger(
								getResourceString("Unsubscription_REQUEST_Performative")));			      
		
						request.addReceiver(matchmaker);	      
						request.setContent("unsubscription");
						request.setConversationId(getResourceString("Unsubscription_REQUEST_ConversationID"));
						request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
		
						sendMessage(request);			      
		
						// Prepare the template to get unsubscription accept proposal
						mt = MessageTemplate.and(
								MessageTemplate.MatchPerformative(ACLMessage.getInteger(
										getResourceString("Unsubscription_ACCEPT_PROPOSAL_Performative"))),
										MessageTemplate.and(
												MessageTemplate.MatchConversationId(
														getResourceString("Unsubscription_ACCEPT_PROPOSAL_ConversationID")),
														MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
						step = 1;			      
					break;			    		   
					case 1:
						// Receive the reply (unsubscription accept proposal/refusal) from the matchmaker agent
						reply = receiveMessage(myAgent, mt);			    				      
						if (reply != null) {				      
							// Reply received
							if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [unsubscription-accepted]				  	    	 				    	  
								System.out.println(reply.getContent() + ": Matchmaker agent "
										+ reply.getSender().getName());				    	 
								step = 2;				     
							}				      
							else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [unsubscription-refused]
								System.out.println(reply.getContent() + ": Matchmaker agent "
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
	}  // End of inner class RequestUnsubscriptionPerformer
}
