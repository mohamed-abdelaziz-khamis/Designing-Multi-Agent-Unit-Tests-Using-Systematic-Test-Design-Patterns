/**
 * Design Pattern Category:		PairPatterns
 * Design Pattern:				BookingPattern
 * Agent Under Test:			BookingServiceProviderAgent
 * Mock Agent:					MockBookingClientAgent
 */
package PairPatterns.BookingPattern.BookingServiceProviderAgent.MockBookingClientAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;

@SuppressWarnings("serial")
public class MockBookingClientAgent extends JADEMockAgent {
	
	private static ResourceBundle resMockClientAgent = 
	 ResourceBundle.getBundle
	  ("PairPatterns.BookingPattern.BookingServiceProviderAgent.MockBookingClientAgent.MockBookingClientAgent");
	
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

	// The title of the resource to reserve
	private String targetResourceTitle;

	private AID serviceProvider; 	
	
	// Put agent initializations here
	protected void setup() {	
		  
	  // Printout a welcome message
	  System.out.println("Hallo! Mock Client-Agent " + getAID().getName() + " is ready.");	    
		  
	  // Get the title of the resource to reserve as a start-up argument
	  Object[] args = getArguments();		  
		  
	  if (args != null && args.length > 0) {			
			
		    targetResourceTitle = (String) args[0];
		    System.out.println("Target resource is "+targetResourceTitle);
			
		    serviceProvider = new AID("serviceProvider", AID.ISLOCALNAME);

		    // Perform the reservation request to the serviceProvider agent
			addBehaviour(new RequestReservationPerformer());
				
		    addBehaviour(new WakerBehaviour(this, Long.parseLong(getResourceString("Waker_Behaviour_Period"))) {
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
	    System.out.println("Mock Client-Agent "+getAID().getName()+" terminating.");
	}
	
	/** Inner class RequestReservationPerformer. This is the behaviour used by Client agents to request reservation 
	 * from service provider agents of the target resource. 
	*/		
	private class RequestReservationPerformer extends Behaviour {				   
	  private float price; 			 // The reservation offered price
	  private MessageTemplate mt; 	 // The template to receive replies 	  
	  private int step = 0;
	  private ACLMessage reply;	  
	  public void action() {
		  try {
			switch (step) {
				case 0:			    	
				  // Send the request to the service provider agent
				  ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("REQUEST_Performative")));			      					      
				  request.addReceiver(serviceProvider);	      
				  request.setContent(targetResourceTitle);
				  request.setConversationId(getResourceString("REQUEST_ConversationID"));
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value					  				  
				  sendMessage(request);			      
				  // Prepare the template to get resource proposals
				  mt = MessageTemplate.and(
				  		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Rsrc_PROPOSE_Performative"))),
				   		  MessageTemplate.and(
				   		  MessageTemplate.MatchConversationId(getResourceString("Rsrc_PROPOSE_ConversationID")),
				   		  MessageTemplate.MatchInReplyTo(request.getReplyWith())));
				      			    	
			      step = 1;			      
			      break;
			    case 1:
			    	// Receive the reply (resource proposal/waiting list proposal/refusal) from the service provider agent
			    	reply = receiveMessage(myAgent, mt);			    	
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
					ACLMessage WLAcceptProposal = new ACLMessage(ACLMessage.getInteger(getResourceString("WL_ACCEPT_PROPOSAL_Performative"))); 

					WLAcceptProposal.addReceiver(serviceProvider);
					WLAcceptProposal.setContent(targetResourceTitle);
					WLAcceptProposal.setConversationId(getResourceString("WL_ACCEPT_PROPOSAL_ConversationID"));
					WLAcceptProposal.setReplyWith("waiting-list"+System.currentTimeMillis());	// Unique value	
					WLAcceptProposal.addUserDefinedParameter(getResourceString("Waiting_List_Deadline_Key"), 
					  								         getResourceString("Waiting_List_Deadline_Value"));
						      
					sendMessage(WLAcceptProposal);			      
						      
					// Prepare the template to get the waiting-list accept proposal reply
					mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(Integer.parseInt(getResourceString("Rsrc_PROPOSE_Performative"))),
			 				MessageTemplate.and(
					    	MessageTemplate.MatchConversationId(getResourceString("Rsrc_PROPOSE_ConversationID")),
					        MessageTemplate.MatchInReplyTo(WLAcceptProposal.getReplyWith())));
					  
					step = 1;
					break;					  	    
				case 3:				  
				   // Send the reservation order to the service provider agent
				   ACLMessage order = new ACLMessage(ACLMessage.getInteger(getResourceString("Rsrc_ACCEPT_PROPOSAL_Performative"))); 
							  		      
				   order.addReceiver(serviceProvider);
				   order.setContent(targetResourceTitle);
				   order.setConversationId(getResourceString("Rsrc_ACCEPT_PROPOSAL_ConversationID"));
				   order.setReplyWith("order"+System.currentTimeMillis());	// Unique value		      
						      
				   sendMessage(order);			      
						      
				   // Prepare the template to get the reservation order reply
				   mt = MessageTemplate.and(
						  MessageTemplate.MatchPerformative(Integer.parseInt(getResourceString("INFORM_Performative"))),
			 			  MessageTemplate.and(
					   	  MessageTemplate.MatchConversationId(getResourceString("INFORM_ConversationID")),
					      MessageTemplate.MatchInReplyTo(order.getReplyWith())));
					   
				   step = 4;	    
				   break;					   
				case 4:
				   // Receive the reservation order reply from the service provider agent
				  reply = receiveMessage(myAgent, mt);	    			      

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
		catch (ReplyReceptionFailed  e) {
		  setTestResult(prepareMessageResult(e));
		  e.printStackTrace();
		  myAgent.doDelete();			
		} 																	
	    setTestResult(new TestResult());
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
		 try {
			switch (step) {
				case 0:				  
				   // Send the reservation cancelation to the service provider agent
				   ACLMessage reservationCancelation = new ACLMessage(ACLMessage.getInteger(getResourceString("CANCEL_Performative"))); 
							  		      
				   reservationCancelation.addReceiver(serviceProvider);
				   reservationCancelation.setContent(targetResourceTitle);
				   reservationCancelation.setConversationId(getResourceString("CANCEL_ConversationID"));
				   reservationCancelation.setReplyWith("reservationCancelation"+System.currentTimeMillis()); // Unique value		      
						      
				   sendMessage(reservationCancelation);			      
						      
				   // Prepare the template to get the reservation cancelation reply
				   mt = MessageTemplate.and(
						  MessageTemplate.MatchPerformative(Integer.parseInt(getResourceString("CONFIRM_Performative"))),
			 			  MessageTemplate.and(
				    	  MessageTemplate.MatchConversationId(getResourceString("CONFIRM_ConversationID")),
				          MessageTemplate.MatchInReplyTo(reservationCancelation.getReplyWith())));
					   
				   step = 1;	    
				   break;
					   
				case 1:
				      // Receive the reservation cancelation reply from the service provider agent
				      reply = receiveMessage(myAgent, mt);	    			      

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
   }  // End of inner class ReservationCancellationPerformer
	
}
		



