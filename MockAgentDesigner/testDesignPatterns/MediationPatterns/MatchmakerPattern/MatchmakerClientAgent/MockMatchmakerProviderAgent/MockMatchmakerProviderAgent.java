/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				MatchmakerPattern
 * Agent Under Test:			MatchmakerClientAgent
 * Mock Agent:					MockMatchmakerProviderAgent
 */
package MediationPatterns.MatchmakerPattern.MatchmakerClientAgent.MockMatchmakerProviderAgent;

import jade.core.behaviours.CyclicBehaviour;
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
		("MediationPatterns.MatchmakerPattern.MatchmakerClientAgent.MockMatchmakerProviderAgent.MockMatchmakerProviderAgent");
		

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

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Mock-Provider-Agent "+getAID().getName()+" is ready.");	    		  

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

			// Add the behaviour serving queries from client agents
			addBehaviour(new ServiceRequestsServer());
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

	/** Inner class ServiceRequestsServer. 
	 * This is the behaviour used by Provider agents to serve incoming requests for service from client agents.
	 * If the requested service is in the local catalogue the provider agent replies with an INFORM message 
	 * specifying the price. Otherwise a REFUSE message is sent back.
	 */   
	private class ServiceRequestsServer extends CyclicBehaviour {

		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Service_REQUEST_Performative"))),
						MessageTemplate.MatchConversationId(getResourceString("Service_REQUEST_ConversationID")));
	
				ACLMessage msg = receiveMessage(myAgent, mt);
	
				if (msg != null) {
	
					// REQUEST Message received. Process it
					String serviceTitle = msg.getContent(); //REQUEST [service-title]
	
					ACLMessage reply = msg.createReply();
					Float price = (Float) catalogue.get(serviceTitle);
	
					if (price != null) {	 	    	  	 	    	  	
						// The requested service is available for sale. Reply with the price
						reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
						reply.setContent(price.toString());	 	        
					}
					else {
						// The requested service is NOT available for sale.
						reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
						reply.setContent(getResourceString("REFUSE_Content")); //REFUSE [service-unavailable]
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
	}  // End of inner class ServiceRequestsServer
}
		



