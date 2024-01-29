/**
 * Design Pattern Category:		PairPatterns
 * Design Pattern:				BookingPattern
 * Agent Under Test:			BookingClientAgent
 * Mock Agent:					MockBookingServiceProviderAgent
 */
package PairPatterns.BookingPattern.BookingClientAgent.MockBookingServiceProviderAgent;

import jade.core.AID;
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
public class MockBookingServiceProviderAgent extends JADEMockAgent {
	
	private static ResourceBundle resMockServiceProviderAgent = 
		ResourceBundle.getBundle
			("PairPatterns.BookingPattern.BookingClientAgent.MockBookingServiceProviderAgent.MockBookingServiceProviderAgent");
	
	/** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/	
	private static String getResourceString(String key) {
		try {
			return resMockServiceProviderAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}	
	
	//Reservation Info of the resource
	private class ReservationInfo {
		Float price;
		Boolean reserved;
		AID client;
	}
	
	/*The catalogue of resources for reservation (maps the title of a resource to its reservation info)
	 * ReservationInfo: 
	 * 		price: Resource reservation price
	 * 		reserved: Either the resource is reserved or not
	 * 		client: Currenlty resource reserver  	
	 */
	private Hashtable<String, ReservationInfo> catalogue;		
	private String resourceTitle;
	private ReservationInfo reservationInfo;
	
	// Put agent initializations here
	protected void setup() {	

		// Printout a welcome message
		System.out.println("Hallo! Mock-Service Provider-Agent " + getAID().getName() + " is ready.");
		  
		// Get the title of the resource to provide as a start-up argument, that is initially not reserved
		Object[] args = getArguments();
		  
		if (args != null && args.length > 0) {
			
			resourceTitle = (String) args[0];			

			reservationInfo =  new ReservationInfo();
	        reservationInfo.price = Float.parseFloat((String)args[1]);
			reservationInfo.reserved = false;
	        reservationInfo.client = null;

			// Create the catalogue
	        catalogue = new Hashtable<String, ReservationInfo>();
	       
			catalogue.put(resourceTitle, reservationInfo);
			
	        System.out.println(resourceTitle + " inserted into catalogue. " 
	        			+ " Price = " + reservationInfo.price.toString());
 
	        // Add the behaviour serving resource reservation requests from client agents
	        addBehaviour(new ResourceReservationRequestsServer());

	        // Add the behaviour serving resource reservation orders from client agents
	        addBehaviour(new ResourceReservationOrdersServer());
	        
	        // Add the behaviour serving waiting list proposal deadlines from client agents
	        addBehaviour(new WaitingListOrdersServer());

	        // Add the behaviour serving reservation cancelation from client agents
	        addBehaviour(new ReservationCancelationServer());
		  }
		else {
		    // Make the agent terminate
		    System.out.println("No resource specified to be added in catalogue");
		    doDelete();
		}
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {		 
	    // Printout a dismissal message
	    System.out.println("Mock-Service Provider-Agent "+getAID().getName()+" terminating.");
	}	
	 
	/** Inner class ResourceReservationRequestsServer. 
	 * This is the behaviour used by Service Providers agents to serve incoming requests for resource reservation 
	 * from client agents. 
	 * If the requested resource is in the local catalogue, the service provider agent replies with a PROPOSE message 
	 * 	 with "waiting-list" content in case the resource is reserved 
	 *	 Or "resource-price" content in case the resource is unreserved. 
	 * Else a REFUSE message is sent back.
	 */	
	private class ResourceReservationRequestsServer extends CyclicBehaviour {		
	  public void action() {		  
		try {  
		  MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.getInteger(getResourceString("REQUEST_Performative"))),
					MessageTemplate.MatchConversationId(getResourceString("REQUEST_ConversationID"))); 		  
		  ACLMessage msg = receiveMessage(myAgent, mt);	    
		  if (msg != null) {		      
			  // REQUEST Message received. Process it
		      String resourceTitle = msg.getContent(); //REQUEST [resource-title]		      
		      ACLMessage reply = msg.createReply();
		      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
		      if (reservationInfo != null) {		        
		    	  // The requested resource is existing in cataglouge.		    	  
		    	  if (reservationInfo.reserved){ //Resource is reserved
			    	reply.setPerformative(ACLMessage.getInteger(getResourceString("WL_PROPOSE_Performative")));  
			        reply.setContent(getResourceString("WL_PROPOSE_Content"));			        
		    	  }
		    	  else{ //Resource is unreserved
			    	reply.setPerformative(ACLMessage.getInteger(getResourceString("Rsrc_PROPOSE_Performative")));  
				    reply.setContent(reservationInfo.price.toString());				    
		    	  }
		      }
		      else {
		        // The requested resource is NOT existing in cataglouge.
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
		        reply.setContent(getResourceString("REFUSE_Content")); 
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
	  }  // End of inner class ResourceReservationRequestsServer
	}
	
	/**Inner class ResourceReservationOrdersServer. 
	 * This is the behaviour used by Service Providers agents to serve incoming resource proposals acceptances 
	 * (i.e. resource reservation orders) from client agents. The service provider agent set the requested resource 
	 * as "reserved" in its catalogue and replies with an INFORM message to notify the client agent that 
	 * the reservation has been successfully completed.
	 */
	
	private class ResourceReservationOrdersServer extends CyclicBehaviour {
		
	  public void action() {
		 try {  
			  MessageTemplate mt = MessageTemplate.and( 
				  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("Rsrc_ACCEPT_PROPOSAL_Performative"))),
				  MessageTemplate.MatchConversationId(getResourceString("Rsrc_ACCEPT_PROPOSAL_ConversationID")));
		  		  
			  ACLMessage msg = receiveMessage(myAgent, mt);
			  
			  if (msg != null) {
			      			  			  
				  // ACCEPT_PROPOSAL Message received. Process it
			      String resourceTitle = msg.getContent(); //ACCEPT_PROPOSAL [resource-title]
			      
			      ACLMessage reply = msg.createReply();
			      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
			      
			      if (reservationInfo != null && !reservationInfo.reserved) {
			    	// The requested resource is still existing and not reserved by another client.  
			        reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
			        reply.setContent(getResourceString("INFORM_Content"));
			        catalogue.get(resourceTitle).client = msg.getSender();
			        catalogue.get(resourceTitle).reserved = true;
			        System.out.println(resourceTitle + " reserved to agent " + msg.getSender().getName());
			      }
			      else {
			        // The requested resource has been reserved to another client in the meanwhile.
			        reply.setPerformative(ACLMessage.getInteger(getResourceString("FAILURE_Performative")));
			        reply.setContent(getResourceString("FAILURE_Content"));
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
	}  // End of inner class ResourceReservationOrdersServer
	
	
	/**Inner class WaitingListOrdersServer. 
	 * This is the behaviour used by Service Providers agents to serve incoming waiting list proposals acceptances 
	 * (i.e. waiting list proposal deadline) from client agents. The service provider agent puts the client agent 
	 * in waiting list till the resource becomes unreserved or the waiting list deadline is reached.
	 */
	
	private class WaitingListOrdersServer extends CyclicBehaviour {
		
	  public void action() {
		  try {
			  MessageTemplate mt = MessageTemplate.and(
				    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("WL_ACCEPT_PROPOSAL_Performative"))),
				    		  MessageTemplate.and(
				    		  MessageTemplate.MatchConversationId(getResourceString("WL_ACCEPT_PROPOSAL_ConversationID")),
				              MessageTemplate.MatchInReplyTo(getResourceString("WL_PROPOSE_Content"))));		  
			  		  
			  ACLMessage msg = receiveMessage(myAgent, mt);
			  
			  if (msg != null) {
			      			  			  
				  // ACCEPT_PROPOSAL Message received. Process it
			      String resourceTitle = msg.getContent(); //ACCEPT_PROPOSAL [resource-title]
			      
			      ACLMessage reply = msg.createReply();
			      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
			      
			      if (reservationInfo != null){
			    	  // The requested resource is still existing in local catalouge
			    	  String waitingListDeadLineValue = msg.getUserDefinedParameter(getResourceString("Waiting_List_Deadline_Key"));		    	  
			    	  
			    	  if (waitingListDeadLineValue != null){
			    		  /*An Accept_Proposal message has been sent by a client replying 
			    		  *the Waiting_list propose sent by the serviceProvider that contains 
			    		  *the Waiting_list time-out (WLDeadLine)*/
	
				    	  long waitingListDeadLine = Integer.valueOf(waitingListDeadLineValue);
						  long deadLine =  System.currentTimeMillis() + waitingListDeadLine;
						  boolean deadLineTimeOut = false; 
				    	  			    	  			    	  
				    	  while (catalogue.get(resourceTitle).reserved){
				    		if (System.currentTimeMillis() == deadLine) {
							    /* When reaching the time-out and the resource is still reserved, 
							     * the service provider sends a refusal to the client informing that 
							     * the resource is unavailable 
							     */
							    reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
							    reply.setContent(getResourceString("REFUSE_Content")); //REFUSE [resource-unavailable]
							    deadLineTimeOut = true;
							    break;  
				    		}
				    	  }
				    	  
				    	  if (!catalogue.get(resourceTitle).reserved && !deadLineTimeOut){ //Resource is unreserved
				    		  reply.setPerformative(ACLMessage.getInteger(getResourceString("Rsrc_PROPOSE_Performative")));  
				    		  reply.setContent(reservationInfo.price.toString());	
				    	  }
			    	  }
			      }
			      else {
				      // The requested resource is NOT existing in cataglouge.
				      reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
				      reply.setContent(getResourceString("REFUSE_Content")); //REFUSE [resource-unavailable]
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
	}  // End of inner class WaitingListOrdersServer

	 /** Inner class ReservationCancelationServer. 
	 * This is the behaviour used by Service Providers agents to serve incoming reservation cancelation from client agents. 
	 * If the reserved resource is in the local catalogue and reserved by the client agent, the service provider agent makes 
	 * the reserved resource status as "unreserved" in its catalogue and replies with a CONFIRM message to notify the client 
	 * agent that the reservation cancelation has been successfully completed.
	 */  
	  
	private class ReservationCancelationServer extends CyclicBehaviour {
	 	  
	 public void action() {	 		  		 
	 	try {		  
	 		 MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("CANCEL_Performative"))),
			    		  MessageTemplate.MatchConversationId(getResourceString("CANCEL_ConversationID"))); 
			  
	 		 ACLMessage msg = receiveMessage(myAgent, mt);

	 		 if (msg != null) {

	 			  // CANCEL Message received. Process it
		 	      String resourceTitle = msg.getContent(); //CANCEL [resource-title]
		
		 	      ACLMessage reply = msg.createReply();	      
		 	      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
		 	      
		 	      if (reservationInfo != null && reservationInfo.reserved && reservationInfo.client == msg.getSender()) {	    	  
		 	    	// The reserved resource is in the local catalogue and reserved by the client agent.	        
		 	    	reply.setPerformative(ACLMessage.getInteger(getResourceString("CONFIRM_Performative")));
		 	    	reply.setContent(getResourceString("CONFIRM_Content"));
		 	    	catalogue.get(resourceTitle).client = null;
		 	    	catalogue.get(resourceTitle).reserved = false;
		 	        System.out.println(resourceTitle+" reservation canceled by agent " + msg.getSender().getName());
		 	      }
		 	      else {
		 	        // The resource is unreserved by the client client.
		 	        reply.setPerformative(ACLMessage.getInteger(getResourceString("DISCONFIRM_Performative")));
		 	        reply.setContent(getResourceString("DISCONFIRM_Content"));
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
	 }  // End of inner class ReservationCancelationServer
}
		



