/**
 * Design Pattern Category:	PairPatterns
 * Design Pattern:			BiddingPattern
 * Agent Under Test:		AuctioneerAgent
 * Mock Agent:				MockBidderAgent
 */
package PairPatterns.BiddingPattern.AuctioneerAgent.MockBidderAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;


@SuppressWarnings("serial")
public class MockBidderAgent extends JADEMockAgent {
	
	private static ResourceBundle resMockBidderAgent = 
		ResourceBundle.getBundle("PairPatterns.BiddingPattern.AuctioneerAgent.MockBidderAgent.MockBidderAgent");
		
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockBidderAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
				return "!" + key + "!";
		}			
	}	

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
		System.out.println("Hallo! Mock-Bidder-Agent " + getAID().getName() + " is ready.");	    

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
			sd.setType(getResourceString("Service_Description_Type"));
			sd.setName(getResourceString("Service_Description_Name"));
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
	    System.out.println("Mock-Bidder-agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class AuctionStartInformServer. 
	*  This is the behaviour used by Bidder agents to serve incoming inform for auctions starts from Auctioneer agents. 
	*  If the requested good is in the local catalogue, the bidder agent replies with a CONFIRM message 
	*  Else a DISCONFIRM message is sent back.
	*/
	private class AuctionStartInformServer extends CyclicBehaviour {
			
	  public void action() {		  
		  try {	  
			  MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.getInteger(
									getResourceString("Auction_Start_INFORM_Performative"))),
					MessageTemplate.MatchConversationId(
									getResourceString("Auction_Start_INFORM_ConversationID"))); 
				  
			  ACLMessage msg = receiveMessage(myAgent, mt);
			    
			  if (msg != null && msg.getUserDefinedParameter("auctionStatus") == "Auction-Start") {
				      
				 // INFORM Message received. Process it
				 String goodTitle = msg.getContent(); //INFORM [good-title]
				      
				 ACLMessage reply = msg.createReply();
				 BidInfo bidInfo = catalogue.get(goodTitle);
			
				 if (bidInfo != null) {		        
				    // The requested good is existing in cataglouge.			    	  
				    reply.setPerformative(ACLMessage.getInteger(getResourceString("Auction_Start_CONFIRM_Performative")));  
				    reply.setContent(getResourceString("Auction_Start_CONFIRM_Content"));			        
				 }
				 else {
				    // The requested good is NOT existing in cataglouge.
				    reply.setPerformative(ACLMessage.getInteger(getResourceString("Auction_Start_DISCONFIRM_Performative")));
				    reply.setContent(getResourceString("Auction_Start_DISCONFIRM_Content")); 
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
	}  // End of inner class AuctionStartInformServer
		
	/**Inner class CallForProposalsServer. 
	* This is the behaviour used by Bidder agents to serve incoming call for proposals from Auctioneer agents. 
	* If the requested good is in the local catalogue and the bid-price was not exceeding the max price that could 
	* be paid for this good, the bidder agent replies with a PROPOSE message 
	* Else a NOT_UNDERSTOOD message is sent back.
	*/
		
	private class CallForProposalsServer extends CyclicBehaviour {
			
	  public void action() {
		 try {	  
			MessageTemplate mt = MessageTemplate.and( 
					  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("CFP_Performative"))),
					  MessageTemplate.MatchConversationId(getResourceString("CFP_ConversationID")));
				  		  
			ACLMessage msg = receiveMessage(myAgent, mt);
				  
			if (msg != null) {
				      			  			  
				// CFP Message received. Process it
				String goodTitle = msg.getContent(); //CFP [good-title]
				      
				ACLMessage reply = msg.createReply();
				BidInfo bidInfo = catalogue.get(goodTitle);
				Float bidPrice = Float.parseFloat(msg.getUserDefinedParameter(getResourceString("Bid_Price")));
				      
				if (bidInfo != null && bidPrice <= catalogue.get(goodTitle).maximumPrice) {
				    /* The requested good is still required to be purchased and existing in the cataloug 
				     * and bid price is not exceeding the maximum price that could be paid by the bidder.
				     */  
				    reply.setPerformative(ACLMessage.getInteger(getResourceString("PROPOSE_Performative")));
				    
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
				    reply.setPerformative(ACLMessage.getInteger(getResourceString("NOT_UNDERSTOOD_Performative")));
				    reply.setContent(getResourceString("NOT_UNDERSTOOD_Content"));
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
		 try {	  
			MessageTemplate mt = MessageTemplate.and( 
				MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("REJECT_PROPOSAL_Performative"))),
				MessageTemplate.MatchConversationId(getResourceString("REJECT_PROPOSAL_ConversationID")));		  
			  		  
			ACLMessage msg = receiveMessage(myAgent, mt);
			  
			if (msg != null) {
			      			  			  
			  // REJECT_PROPOSAL Message received. Process it
			  String goodTitle = msg.getContent(); //REJECT_PROPOSAL [good-title]
			      
			  ACLMessage reply = msg.createReply();
			  BidInfo bidInfo = catalogue.get(goodTitle);
			      
			  if (bidInfo != null){
			    /* The requested good is still required to be purchased and existing in the cataloug 
			     */    	  
				  reply.setPerformative(ACLMessage.getInteger(getResourceString("Rjected_Proposal_CONFIRM_Performative")));
				  reply.setContent(bidInfo.proposedBidPrice.toString()); //CONFIRM [bid-price]
			  }
			  else {
				 /* The requested good has been purchased from another auctioneer in the meanwhile. 
				  */
				  reply.setPerformative(ACLMessage.getInteger(getResourceString("Rjected_Proposal_DISCONFIRM_Performative")));
				  reply.setContent(getResourceString("Rjected_Proposal_DISCONFIRM_Content")); //DISCONFIRM [bid-disconfirmed]
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
		 try {	  
		 	 MessageTemplate mt = MessageTemplate.and(
		 			MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("ACCEPT_PROPOSAL_Performative"))),
				    MessageTemplate.MatchConversationId(getResourceString("ACCEPT_PROPOSAL_ConversationID"))); 
				  
		 	ACLMessage msg = receiveMessage(myAgent, mt);

		 	 if (msg != null) {

		 		// ACCEPT_PROPOSAL Message received. Process it
			 	String goodTitle = msg.getContent(); //ACCEPT_PROPOSAL [good-title]
			
			 	ACLMessage reply = msg.createReply();	      
			 	BidInfo bidInfo = catalogue.get(goodTitle);
			 	      
			 	if (bidInfo != null) {	    	  
				   	/* The requested good is still required to be purchased and existing in the cataloug 
				   	*/  	        
			 	   	reply.setPerformative(ACLMessage.getInteger(getResourceString("Accepted_Proposal_CONFIRM_Performative")));
			 	   	reply.setContent(bidInfo.proposedBidPrice.toString()); //CONFIRM [bid-price]
			 	}
			 	else {
			 	   	/* The requested good has been purchased from another auctioneer in the meanwhile. 
				     */
			 	    reply.setPerformative(ACLMessage.getInteger(getResourceString("Accepted_Proposal_DISCONFIRM_Performative")));
			 	    reply.setContent(getResourceString("Accepted_Proposal_DISCONFIRM_Content")); //DISCONFIRM [bid-disconfirmed]
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
	}  	// End of inner class BidAcceptProposalServer
		   
	/** Inner class BidPriceRequestServer. 
	 *  This is the behaviour used by Bidder agents to serve incoming requests for paying bid prices to Auctioneer agents. 
	 *  If the requested good is still in the local catalogue, replies with an INFORM message to notify the auctioneer agent 
	 *  that the purchase has been successfully completed. 
	 *  Else, for any reason, if the requested good has been purchased from another auctioneer in the meanwhile,
	 *  a FAILURE message is sent back.
	 */
		 
	 private class BidPriceRequestServer extends CyclicBehaviour {
				
		public void action() {		  
			try {		  
				MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.getInteger(
														getResourceString("Winning_Bidder_REQUEST_Performative"))),
					MessageTemplate.MatchConversationId(getResourceString("Winning_Bidder_REQUEST_ConversationID"))); 
					 
				ACLMessage msg = receiveMessage(myAgent, mt);
				    
				if (msg != null) {
					      
				   // REQUEST Message received. Process it
				   String goodTitle = msg.getContent(); //REQUEST [good-title]
					      
				   ACLMessage reply = msg.createReply();
				   BidInfo bidInfo = catalogue.get(goodTitle);
				
				   if (bidInfo != null) {		        
					  // The requested good is existing in cataglouge.			    	  
					  reply.setPerformative(ACLMessage.getInteger(getResourceString("Winning_Bidder_INFORM_Performative")));  
					  reply.setContent(getResourceString("Winning_Bidder_INFORM_Content"));				  
				   }
				   else {
					  // The requested good is NOT existing in cataglouge.
					  reply.setPerformative(ACLMessage.getInteger(getResourceString("Winning_Bidder_FAILURE_Performative")));
					  reply.setContent(getResourceString("Winning_Bidder_FAILURE_Content")); 
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
	 }  // End of inner class BidPriceRequestServer
		 
	/** Inner class AuctionEndInformServer. 
	 *  This is the behaviour used by Bidder agents to serve incoming inform for auctions ends from Auctioneer agents. 
	 *  If the requested good is in the local catalogue, the bidder agent removes the purchased good from its catalogue
	 *  and replies with a CONFIRM message 
	 *  Else a DISCONFIRM message is sent back.
	 */
	private class AuctionEndInformServer extends CyclicBehaviour {
				
	  public void action() {		  
		  try {	  
			  MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.getInteger(getResourceString("Auction_End_INFORM_Performative"))),
					MessageTemplate.MatchConversationId(getResourceString("Auction_End_INFORM_ConversationID"))); 
			  
			  ACLMessage msg = receiveMessage(myAgent, mt);
			    
			  if (msg != null && msg.getUserDefinedParameter("auctionStatus") == "Auction-End") {
				      
				  // INFORM Message received. Process it
			      String goodTitle = msg.getContent(); //INFORM [good-title]
					      
			      ACLMessage reply = msg.createReply();
			      BidInfo bidInfo = catalogue.remove(goodTitle);
				
			      if (bidInfo != null) {		        
			    	  // The requested good is existing in cataglouge.			    	  
			    	  reply.setPerformative(ACLMessage.getInteger(getResourceString("Auction_End_CONFIRM_Performative")));  
			    	  reply.setContent(getResourceString("Auction_End_CONFIRM_Content"));
			      }
			      else {
			    	  // The requested good is NOT existing in cataglouge.
			    	  reply.setPerformative(ACLMessage.getInteger(getResourceString("Auction_End_DISCONFIRM_Performative")));
			    	  reply.setContent(getResourceString("Auction_End_DISCONFIRM_Content")); 
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
	}  // End of inner class AuctionEndInformServer
}

		



