/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */

package PairPatterns.BiddingPattern.BidderAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class BidderAgent extends Agent {

	//Bid Info of the good
	private class BidInfo {	        
		// The amount of money that the bidder propose to pay for the good in the current bid.
		private Float proposedBidPrice;

		// The amount of money to increment whenever a new bid-price is announced by the auctioneer.
		private Float amountToIncrementEachBid;

		// The maximum price that the bidder can pay for the good
		private Float maximumPrice;
	}

	// The catalogue of goods for auction (maps the title of a good to its bidding info)
	private Hashtable<String, BidInfo> catalogue;

	// The title of the good to bid
	private String goodTitle;

	private BidInfo bidInfo;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Bidder-Agent " + getAID().getName() + " is ready.");	    

		// Get the title of the good to bid on as a start-up argument.
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			goodTitle = (String) args[0];

			bidInfo =  new BidInfo();
			bidInfo.amountToIncrementEachBid = Float.parseFloat((String)args[1]);
			bidInfo.maximumPrice = Float.parseFloat((String)args[2]);

			// Create the catalogue
			catalogue = new Hashtable<String, BidInfo>();

			catalogue.put(goodTitle, bidInfo);

			System.out.println("  Target good is " + goodTitle + 
					", Amount to increment each bid is " + bidInfo.amountToIncrementEachBid +
					", Maximum Price is " + bidInfo.maximumPrice);

			// Register the good-bidding service in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());

			ServiceDescription sd = new ServiceDescription();
			sd.setType("good-bidding");
			sd.setName("JADE-good-auctioning");
			dfd.addServices(sd);

			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
				doDelete();
			}

			// Add the behaviour serving auction start inform from auctioneer agents
			addBehaviour(new AuctionStartInformServer());

			// Add the behaviour serving call for proposals from auctioneer agents
			addBehaviour(new CallForProposalsServer());

			// Add the behaviour serving bid reject proposal from auctioneer agents
			addBehaviour(new BidRejectProposalServer());

			// Add the behaviour serving bid accept proposal from auctioneer agents
			addBehaviour(new BidAcceptProposalServer());

			// Add the behaviour serving bid-price request from auctioneer agents
			addBehaviour(new BidPriceRequestServer());

			// Add the behaviour serving auction end inform from auctioneer agents
			addBehaviour(new AuctionEndInformServer());

		}
		else {
			// Make the agent terminate
			System.out.println("No target good title  is specified.");
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
		System.out.println("Bidder-agent "+getAID().getName()+" terminating.");
	}

	/** Inner class AuctionStartInformServer. 
	 *  This is the behaviour used by Bidder agents to serve incoming inform for auctions starts from Auctioneer agents. 
	 *  If the requested good is in the local catalogue, the bidder agent replies with a CONFIRM message 
	 *  Else a DISCONFIRM message is sent back.
	 */
	private class AuctionStartInformServer extends CyclicBehaviour {

		public void action() {		  

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.INFORM),
							MessageTemplate.MatchConversationId("good-auction")); 

			ACLMessage msg = receive(mt);

			if (msg != null && msg.getUserDefinedParameter("auctionStatus") == "Auction-Start") {

				// INFORM Message received. Process it
				String goodTitle = msg.getContent(); //INFORM [good-title]

				ACLMessage reply = msg.createReply();
				BidInfo bidInfo = catalogue.get(goodTitle);

				if (bidInfo != null) {		        
					// The requested good is existing in cataglouge.			    	  
					reply.setPerformative(ACLMessage.CONFIRM);  
					reply.setContent("bid-confirmed");			        
				}
				else {
					// The requested good is NOT existing in cataglouge.
					reply.setPerformative(ACLMessage.DISCONFIRM);
					reply.setContent("bid-disconfirmed"); 
				}			      
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class AuctionStartInformServer

	/**Inner class CallForProposalsServer. 
	 * This is the behaviour used by Bidder agents to serve incoming call for proposals from Auctioneer agents. 
	 * If the requested good is in the local catalogue and the bid-price was not exceeding the max price that could 
	 * be paid for this good, the bidder agent replies with a PROPOSE message 
	 * Else a NOT_UNDERSTOOD message is sent back.
	 */

	private class CallForProposalsServer extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and( 
					MessageTemplate.MatchPerformative(ACLMessage.CFP),
					MessageTemplate.MatchConversationId("good-auction"));

			ACLMessage msg = receive(mt);

			if (msg != null) {

				// CFP Message received. Process it
				String goodTitle = msg.getContent(); //CFP [good-title]

				ACLMessage reply = msg.createReply();
				BidInfo bidInfo = catalogue.get(goodTitle);
				Float bidPrice = Float.parseFloat(msg.getUserDefinedParameter("Bid_Price"));

				if (bidInfo != null && bidPrice <= catalogue.get(goodTitle).maximumPrice) {
					/* The requested good is still required to be purchased and existing in the cataloug 
					 * and bid price is not exceeding the maximum price that could be paid by the bidder.
					 */  
					reply.setPerformative(ACLMessage.PROPOSE);

					if (bidPrice + bidInfo.amountToIncrementEachBid > catalogue.get(goodTitle).maximumPrice)			    
						catalogue.get(goodTitle).proposedBidPrice = bidPrice;
					else catalogue.get(goodTitle).proposedBidPrice = bidPrice + bidInfo.amountToIncrementEachBid;

					reply.setContent(catalogue.get(goodTitle).proposedBidPrice.toString());
					System.out.println("Proposal is sent for " + goodTitle + " to auctioneer agent " 
							+ msg.getSender().getName());
				}
				else {
					/* The requested good has been purchased from another auctioneer in the meanwhile.
					 * Or the bid price is exceeding the maximum price that could be paid by the bidder. 
					 */
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					reply.setContent("message-not-understood");
				}    	  		      
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class CallForProposalsServer


	/** Inner class BidRejectProposalServer. 
	 *  This is the behaviour used by Bidder agents to serve incoming bid reject proposals from Auctioneer agents. 
	 *  If the requested good is still required to be purchased and existing in the local catalogue, 
	 *  the bidder agent replies with a CONFIRM message.
	 *  Else if the requested good has been purchased from another auctioneer in the meanwhile,
	 *  a DISCONFIRM message is sent back. 
	 */

	private class BidRejectProposalServer extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and( 
					MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
					MessageTemplate.MatchConversationId("good-auction"));		  

			ACLMessage msg = receive(mt);

			if (msg != null) {

				// REJECT_PROPOSAL Message received. Process it
				String goodTitle = msg.getContent(); //REJECT_PROPOSAL [good-title]

				ACLMessage reply = msg.createReply();
				BidInfo bidInfo = catalogue.get(goodTitle);

				if (bidInfo != null){
					/* The requested good is still required to be purchased and existing in the cataloug 
					 */    	  
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setContent(bidInfo.proposedBidPrice.toString()); //CONFIRM [bid-price]
				}
				else {
					/* The requested good has been purchased from another auctioneer in the meanwhile. 
					 */
					reply.setPerformative(ACLMessage.DISCONFIRM);
					reply.setContent("bid-disconfirmed"); //DISCONFIRM [bid-disconfirmed]
				}		    	  	      
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class BidRejectProposalServer

	/** Inner class BidAcceptProposalServer. 
	 *  This is the behaviour used by Bidder agents to serve incoming bid accept proposals from Auctioneer agents. 
	 *  If the requested good is still required to be purchased and existing in the local catalogue, 
	 *  the bidder agent replies with a CONFIRM message. 
	 *  Else if the requested good has been purchased from another auctioneer in the meanwhile,
	 *  a DISCONFIRM message is sent back.
	 */  

	private class BidAcceptProposalServer extends CyclicBehaviour {

		public void action() {	 		  		 

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
					MessageTemplate.MatchConversationId("good-auction")); 

			ACLMessage msg = receive(mt);

			if (msg != null) {

				// ACCEPT_PROPOSAL Message received. Process it
				String goodTitle = msg.getContent(); //ACCEPT_PROPOSAL [good-title]

				ACLMessage reply = msg.createReply();	      
				BidInfo bidInfo = catalogue.get(goodTitle);

				if (bidInfo != null) {	    	  
					/* The requested good is still required to be purchased and existing in the cataloug 
					 */  	        
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setContent(bidInfo.proposedBidPrice.toString()); //CONFIRM [bid-price]
				}
				else {
					/* The requested good has been purchased from another auctioneer in the meanwhile. 
					 */
					reply.setPerformative(ACLMessage.DISCONFIRM);
					reply.setContent("bid-disconfirmed"); //DISCONFIRM [bid-disconfirmed]
				}
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class BidAcceptProposalServer

	/** Inner class BidPriceRequestServer. 
	 *  This is the behaviour used by Bidder agents to serve incoming requests for paying bid prices to Auctioneer agents. 
	 *  If the requested good is still in the local catalogue, replies with an INFORM message to notify the auctioneer agent 
	 *  that the purchase has been successfully completed. 
	 *  Else, for any reason, if the requested good has been purchased from another auctioneer in the meanwhile,
	 *  a FAILURE message is sent back.
	 */

	private class BidPriceRequestServer extends CyclicBehaviour {

		public void action() {		  

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.REQUEST),
							MessageTemplate.MatchConversationId("good-auction")); 

			ACLMessage msg = receive(mt);

			if (msg != null) {

				// REQUEST Message received. Process it
				String goodTitle = msg.getContent(); //REQUEST [good-title]

				ACLMessage reply = msg.createReply();
				BidInfo bidInfo = catalogue.get(goodTitle);

				if (bidInfo != null) {		        
					// The requested good is existing in cataglouge.			    	  
					reply.setPerformative(ACLMessage.INFORM);  
					reply.setContent("500");				  
				}
				else {
					// The requested good is NOT existing in cataglouge.
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("auction-failed"); 
				}			      
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class BidPriceRequestServer

	/** Inner class AuctionEndInformServer. 
	 *  This is the behaviour used by Bidder agents to serve incoming inform for auctions ends from Auctioneer agents. 
	 *  If the requested good is in the local catalogue, the bidder agent removes the purchased good from its catalogue
	 *  and replies with a CONFIRM message 
	 *  Else a DISCONFIRM message is sent back.
	 */
	private class AuctionEndInformServer extends CyclicBehaviour {

		public void action() {		  

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("good-auction")); 

			ACLMessage msg = receive(mt);

			if (msg != null && msg.getUserDefinedParameter("auctionStatus") == "Auction-End") {

				// INFORM Message received. Process it
				String goodTitle = msg.getContent(); //INFORM [good-title]

				ACLMessage reply = msg.createReply();
				BidInfo bidInfo = catalogue.remove(goodTitle);

				if (bidInfo != null) {		        
					// The requested good is existing in cataglouge.			    	  
					reply.setPerformative(ACLMessage.CONFIRM);  
					reply.setContent("auction-end-confirmed");
				}
				else {
					// The requested good is NOT existing in cataglouge.
					reply.setPerformative(ACLMessage.DISCONFIRM);
					reply.setContent("auction-end-disconfirmed"); 
				}			      
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class AuctionEndInformServer
}
