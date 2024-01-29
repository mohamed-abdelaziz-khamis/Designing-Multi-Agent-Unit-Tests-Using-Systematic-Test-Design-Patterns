/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 */
package PairPatterns.BookingPattern.BookingClientAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class BookingClientAgent extends Agent {
	
   // The title of the resource to reserve
   private String targetResourceTitle;

   private AID serviceProvider; 	 
 
   // Put agent initializations here
   protected void setup() {		

		  // Printout a welcome message
		  System.out.println("Hallo! Client-Agent "+getAID().getName()+" is ready.");	    		  
		  
		  // Get the title of the resource to reserve as a start-up argument
		  Object[] args = getArguments();		  
		  
		  if (args != null && args.length > 0) {			
				targetResourceTitle = (String) args[0];
			    System.out.println("Target resource is "+targetResourceTitle);
			
			    serviceProvider = new AID("serviceProvider", AID.ISLOCALNAME);

			    // Perform the reservation request to the serviceProvider agent
				addBehaviour(new RequestReservationPerformer());
				
			    addBehaviour(new WakerBehaviour(this, 60000) {
					protected void handleElapsedTimeout() {
						// Perform the reservation cancelation request to the serviceProvider agent
						addBehaviour(new ReservationCancellationPerformer());
			    	}
			    } );
		  }			  
		  else {
			    // Make the agent terminate
			    System.out.println("No target resource title specified");
			    doDelete();
		  }
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Client-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class RequestReservationPerformer. This is the behaviour used by Client agents to request reservation 
	 * from service provider agents of the target resource.*/		
	private class RequestReservationPerformer extends Behaviour {				   
	  private float price; 			 // The reservation offered price
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {		  
		switch (step) {	
			case 0:			    	
			  // Send the request to the service provider agent
			  ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      
			      
			  request.addReceiver(serviceProvider);	      
			  request.setContent(targetResourceTitle);
			  request.setConversationId("resource-booking");
			  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value		  				  
				  
			  send(request);			      
				  
			  // Prepare the template to get resource proposals
			  mt = MessageTemplate.and(
			  		 MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			   		 MessageTemplate.and(
			   		 MessageTemplate.MatchConversationId("resource-booking"),
			   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
		      step = 1;			      
		      break;			    		   
			case 1:
			  // Receive the reply (resource proposal/waiting list proposal/refusal) from the service provider agent
			  reply = receive(mt);			    				      
			  if (reply != null) {				      
			      // Reply received
				  if (reply.getPerformative() == ACLMessage.PROPOSE) {					  
				     if (reply.getContent() == "waiting-list"){ //PROPOSE [waiting-list]				    	  
				    	  //Resource is reserved				          
				    	  System.out.println(targetResourceTitle+" is currently in waiting-list");				    	  
				          step = 2;						      						    						      
				     }
				     else { // This is a reservation offer: PROPOSE [reservation-price] 						  
  				    	  //Resource is unreserved				    	 
				    	  price = Float.parseFloat(reply.getContent());				    	 				    	  
				          System.out.println(targetResourceTitle+" is available for reservation from agent "
		        		  			+ reply.getSender().getName() + " with Price = "+ price);				    	 
				          step = 3;						      						    	
					  }				     
				  }				      
				  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [resource-unavailable]
				   	  System.out.println(reply.getContent());
				   	  myAgent.doDelete();
				  }				      
			  }			      
			  else {
			      block();
			  }		   			    
		      break;
		   
			case 2:
			  // Send waiting-list accept proposal to the service provider agent
			  ACLMessage WLAcceptProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL); 

			  WLAcceptProposal.addReceiver(serviceProvider);
			  WLAcceptProposal.setContent(targetResourceTitle);
			  WLAcceptProposal.setConversationId("resource-booking");
			  WLAcceptProposal.setReplyWith("waiting-list"+System.currentTimeMillis());	// Unique value	
			  WLAcceptProposal.addUserDefinedParameter("WaitingListDeadLine","60000");
				      
			  send(WLAcceptProposal);			      
				      
			  // Prepare the template to get the waiting-list accept proposal reply
			  mt = MessageTemplate.and(
					  MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
	 				  MessageTemplate.and(
			    	  MessageTemplate.MatchConversationId("resource-booking"),
			          MessageTemplate.MatchInReplyTo(WLAcceptProposal.getReplyWith())));
			  
			  step = 1;
			  break;
			  	    
			case 3:				  
			   // Send the reservation order to the service provider agent
			   ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL); 
					  		      
			   order.addReceiver(serviceProvider);
			   order.setContent(targetResourceTitle);
			   order.setConversationId("resource-booking");
			   order.setReplyWith("order"+System.currentTimeMillis());	// Unique value		      
				      
			   send(order);			      
				      
			   // Prepare the template to get the reservation order reply
			   mt = MessageTemplate.and(
					  MessageTemplate.MatchPerformative(ACLMessage.INFORM),
	 				  MessageTemplate.and(
			    	  MessageTemplate.MatchConversationId("resource-booking"),
			          MessageTemplate.MatchInReplyTo(order.getReplyWith())));
			   
			   step = 4;	    
			   break;
			   
			case 4:
		      // Receive the reservation order reply from the service provider agent
		      reply = receive(mt);	    			      

		      if (reply != null) {					    	  
			      // Reservation order reply received						  
		    	  
		    	  if (reply.getPerformative() == ACLMessage.INFORM){ //INFORM [reservation-completed]					    		  
			          // Reservation successful.
			          System.out.println(targetResourceTitle+" successfully reserved from agent "
			        		  			+ reply.getSender().getName() + " with Price = "+ price);
			          step = 5;
		    	  }
			      else {
			          System.out.println(reply.getContent()); //FAILURE [reservation-failed]
			          myAgent.doDelete();
			      }			      		    	  		    	  
		      }						  
		      else {
			      block();
			  }
		      break;		      
			}
		}
		  
		public boolean done() {
		    return (step == 5);
		}
	}  // End of inner class RequestReservationPerformer
	
	/** Inner class ReservationCancellationPerformer
	  *  This is the behaviour used by Client agents to cancel reservation from service provider agents 
	  *  of the target resource. 
	*/	
	
	private class ReservationCancellationPerformer extends Behaviour {		
		  
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;
	  
	  public void action() {
		switch (step) {
			case 0:				  
			   // Send the reservation cancelation to the service provider agent
			   ACLMessage reservationCancelation = new ACLMessage(ACLMessage.CANCEL); 
						  		      
			   reservationCancelation.addReceiver(serviceProvider);
			   reservationCancelation.setContent(targetResourceTitle);
			   reservationCancelation.setConversationId("resource-booking");
			   reservationCancelation.setReplyWith("reservationCancelation"+System.currentTimeMillis()); // Unique value		      
					      
			   send(reservationCancelation);			      
					      
			   // Prepare the template to get the reservation cancelation reply
			   mt = MessageTemplate.and(
					  MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
		 			  MessageTemplate.and(
			    	  MessageTemplate.MatchConversationId("resource-booking"),
			          MessageTemplate.MatchInReplyTo(reservationCancelation.getReplyWith())));
				   
			   step = 1;	    
			   break;
				   
			case 1:
			      // Receive the reservation cancelation reply from the service provider agent
			      reply = receive(mt);	    			      

			      if (reply != null) {					    	  
				      // Reservation cancelation reply received						  
			    	  
			    	  if (reply.getPerformative() == ACLMessage.CONFIRM){ //CONFIRM [reservation-cancelation-completed]					    		  
				          // Reservation cancelation completed. We can terminate
				          System.out.println(targetResourceTitle+" reservation successfully cancelled from agent "
				        		  			+ reply.getSender().getName());
				          step = 2;
			    	  }
				      else {
				          System.out.println(reply.getContent()); //DISCONFIRM [reservation-cancelation-failed]
				          myAgent.doDelete();
				      }			      		    	  			    	  
			      }						  
			      else {
				      block();
				  }
			      break;
			}
		}
		  
		public boolean done() {
		    return (step == 2);
		}
	}  // End of inner class ReservationCancellationPerformer
}