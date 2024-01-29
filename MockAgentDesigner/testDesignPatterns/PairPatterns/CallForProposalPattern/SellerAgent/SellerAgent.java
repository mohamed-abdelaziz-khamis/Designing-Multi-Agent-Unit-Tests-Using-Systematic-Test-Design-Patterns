/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */

package PairPatterns.CallForProposalPattern.SellerAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class SellerAgent extends Agent {

	// The catalogue of service for sale (maps the title of a service to its price)
	private Hashtable<String, Float> catalogue;
	private String serviceTitle;
	private float servicePrice;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Seller-Agent " + getAID().getName() + " is ready.");	    

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

			// Register the selling service in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());

			ServiceDescription sd = new ServiceDescription();
			sd.setType("service-selling");
			sd.setName("JADE-service-trading");
			dfd.addServices(sd);

			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
				doDelete();
			}

			// Add the behaviour serving queries from buyer agents
			addBehaviour(new OfferRequestsServer());

			// Add the behaviour serving purchase orders from buyer agents
			addBehaviour(new PurchaseOrdersServer());
		}		
		else {
			// Make the agent terminate
			System.out.println("No service title specified to be added in catalogue");
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Seller-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class OfferRequestsServer. 
	 * This is the behaviour used by Seller agents to serve incoming requests for offer from buyer agents.
	 * If the requested service is in the local catalogue the seller agent replies with a PROPOSE message 
	 * specifying the price. Otherwise a REFUSE message is sent back.
	 */   
	private class OfferRequestsServer extends CyclicBehaviour {

		public void action() { 		
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CFP),
					MessageTemplate.MatchConversationId("service-trade"));

			ACLMessage msg = receive(mt);

			if (msg != null) {

				// CFP Message received. Process it
				String serviceTitle = msg.getContent(); //CFP [service-title]

				ACLMessage reply = msg.createReply();
				Float price = (Float) catalogue.get(serviceTitle);

				if (price != null) {	 	    	  	 	    	  	
					// The requested service is available for sale. Reply with the price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(price.toString());	 	        
				}
				else {
					// The requested service is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("service-unavailable"); //REFUSE [service-unavailable]
				}
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	/** Inner class PurchaseOrdersServer.
	 * This is the behaviour used by Seller agents to serve incoming offer acceptances (i.e. purchase orders) 
	 * from buyer agents. The seller agent removes the purchased service from its catalogue and replies with an 
	 * INFORM message to notify the buyer agent that the purchase has been successfully completed.
	 */

	private class PurchaseOrdersServer extends CyclicBehaviour {

		public void action() {	 		  		 
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
					MessageTemplate.MatchConversationId("service-trade")); 

			ACLMessage msg = receive(mt);

			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String serviceTitle = msg.getContent(); //ACCEPT_PROPOSAL [service-title]

				ACLMessage reply = msg.createReply();	      
				Float price = (Float) catalogue.remove(serviceTitle);

				if (price != null) {	    	  
					// The requested service is still exiting and not sold to another buyer.	        
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(serviceTitle+" sold to agent "+msg.getSender().getName());
				}
				else {
					// The requested service has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("sale-failed");
				}
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class PurchaseOrdersServer
}