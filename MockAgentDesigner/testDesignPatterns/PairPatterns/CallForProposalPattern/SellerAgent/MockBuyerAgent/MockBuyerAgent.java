/**
 * Design Pattern Category:		PairPatterns
 * Design Pattern:				CallForProposalPattern
 * Agent Under Test:			SellerAgent
 * Mock Agent:					MockBuyerAgent
 */
package PairPatterns.CallForProposalPattern.SellerAgent.MockBuyerAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockBuyerAgent extends JADEMockAgent {
	
	
	private static ResourceBundle resMockBuyerAgent = 
		ResourceBundle.getBundle("PairPatterns.CallForProposalPattern.SellerAgent.MockBuyerAgent.MockBuyerAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
	private static String getResourceString(String key) {
		try {
			return resMockBuyerAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}
	
	// The title of the service to buy
	private String targetServiceTitle;
	
	// The list of known seller agents
	private AID[] sellerAgents;
	
	// Put agent initializations here
	protected void setup() {
		
		 // Printout a welcome message
		  System.out.println("Hallo! Mock-Buyer-Agent "+getAID().getName()+" is ready.");
		  
		  // Get the title of the service to buy as a start-up argument
		  Object[] args = getArguments();
		  
		  if (args != null && args.length > 0) {
			  
			targetServiceTitle = (String) args[0];
		    System.out.println("Target service is "+targetServiceTitle);
 
		    // Add a TickerBehaviour that schedules a request to the seller agent every given period
		    addBehaviour(new TickerBehaviour(this, Long.parseLong(getResourceString("Ticker_Behaviour_Period"))) {
		      
		    	protected void onTick() {
		    		
				  System.out.println("Trying to buy "+targetServiceTitle);
				  
		    	  // Update the list of seller agents
		          DFAgentDescription template = new DFAgentDescription();
		          ServiceDescription sd = new ServiceDescription();
		          
		          sd.setType(getResourceString("Service_Description_Type"));
		          
		          template.addServices(sd);
		          
		          try {		          	
		        	
		        	  DFAgentDescription[] result = DFService.search(myAgent, template); 
		          	
  		          	  System.out.println("Found the following seller agents:");
			          sellerAgents = new AID[result.length];
			          
			          for (int i = 0; i < result.length; ++i) {
			             sellerAgents[i] = result[i].getName();
				         System.out.println(sellerAgents[i].getName());
			          }
		          }
		          catch (FIPAException fe) {
		            fe.printStackTrace();
		          }
				  
		          if (sellerAgents.length > 0)
		          //Perform the request
				  myAgent.addBehaviour(new RequestPerformer());
		      	}
		    } );
		  }
		  else {
		    // Make the agent terminate
		    System.out.println("No target service title specified");
		    doDelete();
		  }
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Mock-Buyer-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class RequestPerformer.
	   This is the behaviour used by Buyer agents to request seller agents the target service. */
	private class RequestPerformer extends Behaviour {          	  
		private AID bestSeller; 		// The agent who provides the best offer 
	  	private float bestPrice;  		// The best offered price
	  	private int repliesCnt = 0; 	// The counter of replies from seller agents
	  	private MessageTemplate mt; 	// The template to receive replies
	  	private int step = 0;
	  
	  	public void action() {
	  		try {			  
				switch (step) {
			    case 0:			    	
			      // Send the call for proposal to all sellers
			      ACLMessage cfp = new ACLMessage(ACLMessage.CFP);			      
			      for (int i = 0; i < sellerAgents.length; ++i) {
				        cfp.addReceiver(sellerAgents[i]);
				  }			      
			      cfp.setContent(targetServiceTitle);
			      cfp.setConversationId(getResourceString("CFP_ConversationID"));
			      cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				  cfp.setSender(new AID("buyer", AID.ISLOCALNAME));				  
				  sendMessage(cfp);			      
				  // Prepare the template to get proposals
			      mt=MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("PROPOSE_Performative"))),
			    		  MessageTemplate.and(
			    		  MessageTemplate.MatchConversationId(getResourceString("PROPOSE_ConversationID")),
			              MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));
			      step = 1;			      
			      break;			      

			    case 1:

			    	// Receive the reply (proposals/refusals) from the seller agents 
			    	ACLMessage reply = receiveMessage(myAgent, mt);			    	
			    	if (reply != null) {
			    		// Reply received
				        if (reply.getPerformative() == ACLMessage.PROPOSE) {
				            
				        	// This is an offer 
				        	float price = Float.parseFloat(reply.getContent());
				            
				        	if (bestSeller == null || price < bestPrice) {
					            // This is the best offer at present
					            bestPrice = price;
					            bestSeller = reply.getSender();
					        }
					    }					    
				        repliesCnt++;
					    if (repliesCnt >= sellerAgents.length) {
					          // We received all replies
					          step = 2; 
					    }
					 }
			      else {
			        block();
			      }
			      break;			    
			    
			    case 2:
			      // Send the purchase order to the seller agent
			      ACLMessage order = new ACLMessage(ACLMessage.getInteger(getResourceString("ACCEPT_PROPOSAL_Performative")));			      
			      order.addReceiver(bestSeller);
			      order.setContent(targetServiceTitle);
			      order.setConversationId(getResourceString("ACCEPT_PROPOSAL_ConversationID"));
			      order.setReplyWith("order"+System.currentTimeMillis());	// Unique value		      
			      sendMessage(order);			      
			      // Prepare the template to get the purchase order reply
			      mt = MessageTemplate.and(MessageTemplate.MatchPerformative(Integer.parseInt(getResourceString("INFORM_Performative"))),
 		  				 				   MessageTemplate.and(
			    		  				   MessageTemplate.MatchConversationId(getResourceString("INFORM_ConversationID")),
			                               MessageTemplate.MatchInReplyTo(order.getReplyWith())));
			      step = 3;	      
			      break;     
			    case 3:      	    
			      // Receive the purchase order reply from the seller agent
			      reply = receiveMessage(myAgent, mt);	    			      
			      if (reply != null) {
			        // Purchase order reply received
			        if (reply.getPerformative() == ACLMessage.INFORM) {
			          // Purchase successful. We can terminate
			          System.out.println(targetServiceTitle+" successfully purchased from agent "+reply.getSender().getName());
			          System.out.println("Price = "+ bestPrice);
			          myAgent.doDelete();
			        }
			        else {
			          System.out.println("Attempt failed: requested service already sold.");
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
		  //(step == 2 && bestSeller == null) target service not available for sale
		  return ((step == 2 && bestSeller == null) || step == 4);
	  }
  }  // End of inner class RequestPerformer
}
		



