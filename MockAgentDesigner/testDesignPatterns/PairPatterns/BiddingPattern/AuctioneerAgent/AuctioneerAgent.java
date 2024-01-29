/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */

package PairPatterns.BiddingPattern.AuctioneerAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Hashtable;


@SuppressWarnings("serial")
public class AuctioneerAgent extends Agent {
  
   //Auction Info of the good
   private class AuctionInfo {
	   // The auctioneer seeks to find the market price of a good by initially proposing a price below 
	   // that of the supposed market value and then gradually raising the price
	   private Float goodBidPrice;
	 
	   // As soon as one bidder indicates that it will accept the price, 
	   // the auctioneer issues a new call for bids with an incremented price.
	   private Float amountToIncrementEachRound;
	   
	   // If the last price that was accepted by a bidder exceeds the auctioneer’s (privately known) 
	   // reservation price, the good is sold to that bidder for the agreed price.
	   private Float reservedPrice;
   }
	
   // The catalogue of goods for auction (maps the title of a good to its auction info)
   private Hashtable<String, AuctionInfo> catalogue;

   // The title of the good to bid
   private String goodTitle; 
   
   private AuctionInfo auctionInfo;

   // The list of known bidder agents
   private AID[] bidderAgents;
   
   // Put agent initializations here
   protected void setup() {		

	  // Printout a welcome message
	  System.out.println("Hallo! Auctioneer-Agent "+getAID().getName()+" is ready.");	    		  
		  
	  // Get the title of the good to organize auction for as a start-up argument
	  Object[] args = getArguments();		  
		  
	  if (args != null && args.length > 0) {			
			    
		  goodTitle = (String) args[0];
			    
		  auctionInfo =  new AuctionInfo();
		  auctionInfo.goodBidPrice = Float.parseFloat((String)args[1]);
		  auctionInfo.amountToIncrementEachRound =  Float.parseFloat((String)args[2]);
		  auctionInfo.reservedPrice =  Float.parseFloat((String)args[3]);
				
		  // Create the catalogue
		  catalogue = new Hashtable<String, AuctionInfo>();
			
		  catalogue.put(goodTitle, auctionInfo);
				
		  System.out.println(" Good title inserted into catalogue: " + goodTitle + 
							 ", Initial Bid Price = " + auctionInfo.goodBidPrice.toString() + 
							 ", Amount To Increment Each Round = " + auctionInfo.amountToIncrementEachRound.toString() +
					  		 ", Reserved Price = " + auctionInfo.reservedPrice.toString());
					  
		  // Add a TickerBehaviour that schedules a request to the bidder agents every given period
		  addBehaviour(new TickerBehaviour(this, 60000) {
		      
			  protected void onTick() {
				    	  
			  	System.out.println("Organize new auction for bids on "+goodTitle);
						  
			  	// Update the list of bidder agents
			    DFAgentDescription template = new DFAgentDescription();
			    ServiceDescription sd = new ServiceDescription();
			          
			    sd.setType("good-bidding");
			          
			    template.addServices(sd);	
			          
			    try {		          			        	
			      	 DFAgentDescription[] result = DFService.search(myAgent, template); 
			          	
			       	 System.out.println("Found the following bidder agents:");
			       	 bidderAgents = new AID[result.length];
				          
				     for (int i = 0; i < result.length; ++i) {
				       	 bidderAgents[i] = result[i].getName();
				         System.out.println(bidderAgents[i].getName());
				     }			          				          
			    }
			    catch (FIPAException fe) {
			         fe.printStackTrace();
			    }
			          
			    if (bidderAgents.length > 0)
					// Organize auction to the bidder agents
					addBehaviour(new AuctionOrganizer());
			   }
		    });
		  }			  
		  else {			  
			  // Make the agent terminate
			  System.out.println("No target good title specified to be added in catalogue");
			  doDelete();			
		  }
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
	    System.out.println("Auctioneer-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class AuctionOrganizer. This is the behaviour used by Auctioneer agents to organize bids 
	 * to Bidder Agents on the target good.*/			
	private class AuctionOrganizer extends Behaviour {				   	
		private AID bestAcceptedBidder; 
		// The bidder agent whose proposal is the highest accepted proposal 		
		private AID bestRejectedBidder; 
		// The bidder agent whose proposal is the highest rejected proposal 		
		private AID winningBidder; 
		// The bidder agent whose proposal is the winner 
		private float bestAcceptedPrice;
		// The highest accepted proposal		
		private float bestRejectedPrice;  		
		// The highest rejected proposal		
		private float winningPrice;  		
		// The winner proposal
		private int repliesCnt = 0; 	// The counter of replies from bidder agents
		private MessageTemplate mt; 	// The template to receive replies		
		private int step = 0;			
		// The list of bidder agents that confirmed on entering the auction
		private ArrayList<AID> confirmedBidderAgents = new ArrayList<AID>();	    
		// The list of bidder agents that their proposals are accepted
		private ArrayList<AID> acceptedBidderAgents = new ArrayList<AID>();		
		// The list of bidder agents that their proposals are rejected
		private ArrayList<AID> rejectedBidderAgents = new ArrayList<AID>();		
		private Float bidPrice = catalogue.get(goodTitle).goodBidPrice;		
		private boolean goodSold = false;		
		public void action() {			
			switch (step) {					
				case 0:			    						
					// Send an "auction started" inform to all known bidder agents
					ACLMessage informAuctionStart = new ACLMessage(ACLMessage.INFORM);			      			      			    
					for (int i = 0; i < bidderAgents.length; ++i) {
				    	informAuctionStart.addReceiver(bidderAgents[i]);
					}		      				    
					informAuctionStart.setContent(goodTitle);
				    informAuctionStart.setConversationId("good-auction");
				    informAuctionStart.setReplyWith("informAuctionStart"+System.currentTimeMillis()); // Unique value
				    informAuctionStart.addUserDefinedParameter("auctionStatus", "Auction-Start");				  	
				    send(informAuctionStart);			      				  				  	
				    // Prepare the template to get bidding confirmation				  	
				    mt = MessageTemplate.and(
				  			MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
				  			MessageTemplate.and(
				  			MessageTemplate.MatchConversationId("good-auction"),
				  			MessageTemplate.MatchInReplyTo(informAuctionStart.getReplyWith())));			      			    	
			      	
				    repliesCnt = 0;
				    step = 1;			      
			      	break;			    		   			
			      	
				case 1:
					
					// Receive the reply (bidding confirmation/disconfirmation) from bidder agents
					ACLMessage reply = receive(mt);			    				      
				  	if (reply != null) {				      
					   // Reply received
					   if (reply.getPerformative() == ACLMessage.CONFIRM) {	//CONFIRM [bid-confirmed]				  
						   System.out.println("Bidder Agent " + reply.getSender().getName() 
						   		  			+ " : " + reply.getContent());
						   confirmedBidderAgents.add(reply.getSender());	
					   }				      
					   else { //DISCONFIRM [bid-disconfirmed]
						   System.out.println("Bidder Agent " + reply.getSender().getName() 
				    		  				+ " : " + reply.getContent());				    	 
					   }
					   repliesCnt++;
					   if (repliesCnt >= bidderAgents.length) {
						   // We received all replies
						   if (confirmedBidderAgents.size() == 0)
							   step = 10;
						   else step = 2; 
					   }
				  	}			      
				  	else {
				  		block();
				  	}		   			    
			      	break;		   
				
				case 2:					
					
					// Send the bid call for proposal to the confirmed bidder agents
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP); 
				    
					for (int i = 0; i < confirmedBidderAgents.size(); ++i) {
				    	cfp.addReceiver(confirmedBidderAgents.get(i));
					}
				  	
				    cfp.setContent(goodTitle);
				  	cfp.setConversationId("good-auction");
				  	cfp.setReplyWith("cfp"+System.currentTimeMillis());	// Unique value
				  	cfp.addUserDefinedParameter("bidPrice", "100");
				  	
				  	send(cfp);			      				      
				  	
				  	// Prepare the template to get the bidding proposals
				  	mt = MessageTemplate.and(
						    MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
		 				    MessageTemplate.and(
				    	    MessageTemplate.MatchConversationId("good-auction"),
				            MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));			  
				  	
				  	acceptedBidderAgents.clear();
				  	rejectedBidderAgents.clear();
				  	
				  	repliesCnt = 0; 				  	
				  	step = 3;
				  	break;			  	    		   
				
				case 3:
					
					// Receive the proposals from the confirmed bidder agents
					reply = receive(mt);	    			      
			      	
					if (reply != null) {					    	  
				       // Bidding proposal received						  		    	  
			    	   if (reply.getPerformative() == ACLMessage.PROPOSE){ //PROPOSE [bid-price]					    		  
				          
				          // This is an offer 
				          float proposedBidPrice = Float.parseFloat(reply.getContent());
				          		    		  
				          System.out.println(goodTitle+" is proposed from bidder agent "
				        		  			+ reply.getSender().getName() + " with Price = "+ proposedBidPrice);
				          
				          if (proposedBidPrice >=  bidPrice){
				        	  acceptedBidderAgents.add(reply.getSender());
				          }
				          else{
				        	  rejectedBidderAgents.add(reply.getSender());
				          }				          
			    	   }
				       else { //NOT_UNDERSTOOD [not-understood-call-for-proposal]
				          System.out.println(reply.getContent());
				       }
			    	  
					   repliesCnt++;
					   if (repliesCnt >= confirmedBidderAgents.size()) { 
						  // We received all replies						  
						  
						  if (rejectedBidderAgents.size() > 0){
							  step = 4;
						  }
						  else if (acceptedBidderAgents.size() > 0){
							  step = 6; 
						  }
						  else {//All replies are NOT_UNDERSTOOD
							  step = 10;
						  }
					   }			      		
			      	}			      	
			      	else {
			      		block();
				  	}
			      	break;
			    
				case 4:
					
					// Send bid reject proposal to the bidder agent
					ACLMessage bidRejectProposal = new ACLMessage(ACLMessage.REJECT_PROPOSAL); 
				    
					for (int i = 0; i < rejectedBidderAgents.size(); ++i) {
				    	bidRejectProposal.addReceiver(rejectedBidderAgents.get(i));
					}
					
					bidRejectProposal.setContent(goodTitle);
					bidRejectProposal.setConversationId("good-auction");
					bidRejectProposal.setReplyWith("bidRejectProposal"+System.currentTimeMillis());	// Unique value				      
					
					send(bidRejectProposal);			      				      
					
					// Prepare the template to get the bid reject proposal reply
					mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
			 				MessageTemplate.and(
					    	MessageTemplate.MatchConversationId("good-auction"),
					        MessageTemplate.MatchInReplyTo(bidRejectProposal.getReplyWith())));			  
					
					bestRejectedBidder = null;
					step = 5;
					break;
				
				case 5:
					
					// Receive the reply (confirmation/disconfirmation) from the rejected bidder agents
					reply = receive(mt);			    				      
				  	if (reply != null) {				      
					   // Reply received
					   if (reply.getPerformative() == ACLMessage.CONFIRM) {	//CONFIRM [bid-price]				  
						   float proposedBidPrice = Float.parseFloat(reply.getContent());
						   System.out.println("Agent " + reply.getSender().getName() 
						   		  			+ " confirm on the proposed price for the good: " + goodTitle);
					       if (bestRejectedBidder == null || proposedBidPrice > bestRejectedPrice) {					            
					          // This is the best offer at present
					          bestRejectedPrice = proposedBidPrice;
					          bestRejectedBidder = reply.getSender();
						   }	
					   }				      
					   else { //DISCONFIRM [good-unrequired]
						   System.out.println("Agent " + reply.getSender().getName() 
				    		  				+ " disconfirm on the proposed price for the good: " + goodTitle);				    	 
					   }
					   
					   repliesCnt++;
					   if (repliesCnt >= rejectedBidderAgents.size()) {
						   if (acceptedBidderAgents.size() == 0){
							   //No one from the confirmed bidder agents propose acceptable bid price
								  
							   if (bestRejectedBidder != null && bestRejectedPrice >= catalogue.get(goodTitle).reservedPrice){
								 /* The auction continues until no bidder agent is prepared to pay the proposed price,
								  * at which point the auction ends. 
								  * If the last price that was accepted by the bidder agent exceeds the auctioneer’s 
								  * (privately known) reservation price, the good is sold to that bidder for the agreed price.
								  * At the end of the auction, the auctioneer will send a request act with the winning bidder 
							      * to complete the auction transaction.
							      */
								   winningBidder = bestRejectedBidder;
								   winningPrice = bestRejectedPrice;
								   step = 8; //REQUEST[goodTitle]
							   }else{
								  /*If the last accepted price is less than the reservation price, the good is not sold.*/
								   step = 10; //INFORM[goodTitle]: Auction-End
							   } 
						   }
						   else step = 6;
					   }
				  	}
				  	else {
				  		block();
				  	}		   			    
			      	break;  						
				
				case 6:
					
					// Send bid accept proposal to the bidder agent
					ACLMessage bidAcceptProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL); 
				    for (int i = 0; i < acceptedBidderAgents.size(); ++i) {
				    	bidAcceptProposal.addReceiver(acceptedBidderAgents.get(i));
					}
				    bidAcceptProposal.setContent(goodTitle);
				    bidAcceptProposal.setConversationId("good-auction");
				    bidAcceptProposal.setReplyWith("bidAcceptProposal"+System.currentTimeMillis());	// Unique value				      
				  	send(bidAcceptProposal);			      				      
				  	// Prepare the template to get the bid accept proposal reply
				  	mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
			 				MessageTemplate.and(
					    	MessageTemplate.MatchConversationId("good-auction"),
					        MessageTemplate.MatchInReplyTo(bidAcceptProposal.getReplyWith())));			  
				  	
				  	repliesCnt = 0;
				  	bestAcceptedBidder = null;
				  	confirmedBidderAgents.clear();
				  	
				  	step = 7;
				  	break;
				
				case 7:
					
					// Receive the reply (confirmation/disconfirmation) from the accepted bidder agents
					reply = receive(mt);			    				      
				  	if (reply != null) {				      
					   // Reply received
					   if (reply.getPerformative() == ACLMessage.CONFIRM) {	//CONFIRM [bid-price]				  
						   float proposedBidPrice = Float.parseFloat(reply.getContent());
						   System.out.println("Agent " + reply.getSender().getName() 
						   		  			+ " confirm on the proposed price for the good: " + goodTitle);
					       if (bestAcceptedBidder == null || proposedBidPrice > bestAcceptedPrice) {					            
					           // This is the best offer at present
					    	   bestAcceptedPrice = proposedBidPrice;
					    	   bestAcceptedBidder = reply.getSender();
						   }
						   confirmedBidderAgents.add(reply.getSender());	
					   }				      
					   else { //DISCONFIRM [goodTitle]
						   System.out.println("Agent " + reply.getSender().getName() 
				    		  				+ " disconfirm on the proposed price for the good: " + goodTitle);				    	 
					   }
					   repliesCnt++;
					   if (repliesCnt >= acceptedBidderAgents.size()) {
						   
						   if (confirmedBidderAgents.size() == 0){
							   //No one from the accepted bidder agents confirm on its proposal
								  
							   if (bestRejectedBidder != null && bestRejectedPrice >= catalogue.get(goodTitle).reservedPrice){
								 /* 
								  * The auction continues until no bidder agent is prepared to pay the proposed price,
								  * at which point the auction ends. 
								  * If the last price that was accepted by the bidder agent exceeds the auctioneer’s 
								  * (privately known) reservation price, the good is sold to that bidder for the agreed price.
								  * At the end of the auction, the auctioneer will send a request act with the winning bidder 
						          * to complete the auction transaction.
						          */
								  winningBidder = bestRejectedBidder;
								  winningPrice = bestRejectedPrice;
								  step = 8; //REQUEST[goodTitle]								  
							   }
							   else { 
								   /*If the last accepted price is less than the reservation price, the good is not sold.*/
								   step = 10; //INFORM[goodTitle]: Auction-End
							   }
						   }
						   else{
							   /* 
							    * The Auctioneer agent issues a new call for bids to the confirmed bidder agents 
							    * with an incremented price than the best accepted proposed price.  
					           */						   
							   bidPrice = bestAcceptedPrice + catalogue.get(goodTitle).amountToIncrementEachRound;
							   step = 2;
						   }
					   }
				  	}			      
				  	else {
				  		block();
				  	}		   			    
			      	break;  	
				
				case 8:
					
					// Send bid-price request to the bidder agent
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST); 
					request.addReceiver(winningBidder);
					request.setContent(goodTitle);
					request.setConversationId("good-auction");
					request.setReplyWith("request"+System.currentTimeMillis());	// Unique value				      
				  	send(request);			      				      
				  	// Prepare the template to get the bid-price request reply
				  	mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.INFORM),
			 				MessageTemplate.and(
					    	MessageTemplate.MatchConversationId("good-auction"),
					        MessageTemplate.MatchInReplyTo(request.getReplyWith())));			  
				  	
				  	step = 9;
				  	break;
				
				case 9:
					
					// Receive the bidding order reply from the bidder agent
					reply = receive(mt);	    			      
			      	if (reply != null) {					    	  
				      // Bidding order reply received						  		    	  
			    	  if (reply.getPerformative() == ACLMessage.INFORM){ //INFORM [bid-price]				          
			    		// Bidding successful. We can terminate
				        System.out.println(goodTitle + " successfully buyed by bidder agent "+reply.getSender().getName());
				        System.out.println("Price = "+ winningPrice);
				        goodSold = true;  
			    	  }
				      else { //FAILURE [auction-failed]
				    	System.out.println("Attempt failed: Bidder agent already buyed the requested good."); 
				      }
			    	  step = 10;
			      	}						  
			      	else {
			      		block();
				  	}
			      	break;
			      	
				case 10:
					
					// Send the "auction ended" inform to all bidder agents
					ACLMessage informAuctionEnd = new ACLMessage(ACLMessage.INFORM);			      			      
				    for (int i = 0; i < bidderAgents.length; ++i) {
				    	informAuctionEnd.addReceiver(bidderAgents[i]);
					}		      
				    informAuctionEnd.setContent(goodTitle);
				    informAuctionEnd.setConversationId("good-auction");
				    informAuctionEnd.setReplyWith("informAuctionEnd"+System.currentTimeMillis()); // Unique value	
				    informAuctionEnd.addUserDefinedParameter("auctionStatus", "Auction-End");
				    
				  	send(informAuctionEnd);			      				  
				  	
				  	// Prepare the template to get auction end confirmation
				  	mt = MessageTemplate.and(
				  			MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
				  			MessageTemplate.and(
				  			MessageTemplate.MatchConversationId("good-auction"),
				  			MessageTemplate.MatchInReplyTo(informAuctionEnd.getReplyWith())));
				  	
				  	repliesCnt = 0;
				  	step = 11;			      
			      	break;			    		   			
				
				case 11:
					
					// Receive the reply (auction end confirmation/disconfirmation) from the bidder agents
					reply = receive(mt);			    				      
					if (reply != null) {				      
					   // Reply received
					   if (reply.getPerformative() == ACLMessage.CONFIRM) {	//CONFIRM [goodTitle]				  
						   System.out.println("Agent " + reply.getSender().getName() 
						   		  			+ " confirm ending auction on the good: " + goodTitle);	
					   }				      
					   else { //DISCONFIRM [goodTitle]
						   System.out.println("Agent " + reply.getSender().getName() 
				    		  				+ " disconfirm ending auction on the good: " + goodTitle);				    	 
					   }
					   repliesCnt++;
					   if (repliesCnt >= bidderAgents.length) {
						  // We received all replies
						   if (goodSold) myAgent.doDelete(); 
						   step = 12; 
					   }
				  	}			      
				  	else {
				  		block();
				  	}		   			    
					break;		   		    	
			}		
		}
		  
		public boolean done() {		    
			//Auction End
			return (step == 12);
		}
	}  // End of inner class AuctionOrganizer	
}