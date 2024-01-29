/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */
package PairPatterns.CallForProposalPattern.BuyerAgent;

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

@SuppressWarnings("serial")
public class BuyerAgent extends Agent {

   // The title of the service to buy
   private String targetServiceTitle;

   // The list of known seller agents
   private AID[] sellerAgents;
	  
   // Put agent initializations here
   protected void setup() {
		
		  // Printout a welcome message
		  System.out.println("Hallo! Buyer-Agent "+getAID().getName()+" is ready.");	    
		  
		  // Get the title of the service to buy as a start-up argument
		  Object[] args = getArguments();		  
		  
		  if (args != null && args.length > 0) {
			
			    targetServiceTitle = (String) args[0];
		        System.out.println("Target service is "+targetServiceTitle);
		    	    	    
		        // Add a TickerBehaviour that schedules a request to the seller agent every given period
		        addBehaviour(new TickerBehaviour(this, 60000) {
		      
				protected void onTick() {
			    	  
					  System.out.println("Trying to buy "+targetServiceTitle);	          
			    		
			    	  // Update the list of seller agents
			          DFAgentDescription template = new DFAgentDescription();
			          ServiceDescription sd = new ServiceDescription();
			          
			          sd.setType("service-selling");
			          
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
			          // Perform the request
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
	    System.out.println("Buyer-agent "+getAID().getName()+" terminating.");
	}
	
	 /** Inner class RequestPerformer
	    This is the behaviour used by Buyer agents to request seller agents the target service. */	
	private class RequestPerformer extends Behaviour {		
	  
	  private AID bestSeller; 		// The agent who provides the best offer 
	  private float bestPrice;  	// The best offered price
	  private int repliesCnt = 0; 	// The counter of replies from seller agents
	  private MessageTemplate mt; 	// The template to receive replies
	  private int step = 0;	  	  
	  
	  public void action() {		  
			 switch (step) {
			    case 0:			    	
			      // Send the call for proposal to all sellers
			      ACLMessage cfp = new ACLMessage(ACLMessage.CFP);			      
			      for (int i = 0; i < sellerAgents.length; ++i) {
				        cfp.addReceiver(sellerAgents[i]);
				      }			      
			      cfp.setContent(targetServiceTitle);
			      cfp.setConversationId("service-trade");
			      cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				  				  
				  send(cfp);
				  
				  // Prepare the template to get proposals
			      mt=MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			    		  MessageTemplate.and(
			    		  MessageTemplate.MatchConversationId("service-trade"),
			              MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));
			      
			      step = 1;			      
			      break;			    
			    case 1:
			    	// Receive the reply (proposals/refusals) from the seller agents
			    	ACLMessage reply = receive(mt);			    	
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
			      ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);			      
			      order.addReceiver(bestSeller);
			      order.setContent(targetServiceTitle);
			      order.setConversationId("service-trade");
			      order.setReplyWith("order"+System.currentTimeMillis());	// Unique value		      
			      send(order);			      
			      // Prepare the template to get the purchase order reply
			      mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
 		  				 				   MessageTemplate.and(
			    		  				   MessageTemplate.MatchConversationId("service-trade"),
			                               MessageTemplate.MatchInReplyTo(order.getReplyWith())));
			      step = 3;	      
			      break;     
			    case 3:      	    
			      // Receive the purchase order reply from the seller agent
			      reply = receive(mt);	    			      
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
	
	  public boolean done() {
		//(step == 2 && bestSeller == null) target service not available for sale	  	
	    return ((step == 2 && bestSeller == null) || step == 4);
	  }
	}  // End of inner class RequestPerformer
}
		



