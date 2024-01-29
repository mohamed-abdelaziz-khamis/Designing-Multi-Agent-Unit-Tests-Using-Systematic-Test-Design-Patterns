/**
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * 
 **/

package PairPatterns.BookingPattern.BookingServiceProviderAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class BookingServiceProviderAgent extends Agent {

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
		System.out.println("Hallo! Service Provider-Agent " + getAID().getName() + " is ready.");	    
		
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
	   System.out.println("Service Provider-Agent "+getAID().getName()+" terminating.");
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
		  
		  MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchPerformative (ACLMessage.REQUEST),
				MessageTemplate.MatchConversationId("resource-booking")); 
		  
		  ACLMessage msg = receive(mt);
	    
		  if (msg != null) {
		      
			  // REQUEST Message received. Process it
		      String resourceTitle = msg.getContent(); //REQUEST [resource-title]
		      
		      ACLMessage reply = msg.createReply();
		      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
	
		      if (reservationInfo != null) {		        
		    	  // The requested resource is existing in cataglouge.
		    	  
		    	  if (reservationInfo.reserved){ //Resource is reserved
			    	reply.setPerformative(ACLMessage.PROPOSE);  
			        reply.setContent("waiting-list");			        
		    	  }
		    	  else{ //Resource is unreserved
			    	reply.setPerformative(ACLMessage.PROPOSE);  
				    reply.setContent(reservationInfo.price.toString());				    
		    	  }
		      }
		      else {
		        // The requested resource is NOT existing in cataglouge.
		        reply.setPerformative(ACLMessage.REFUSE);
		        reply.setContent("resource-unavailable"); 
		      }
		      
		      send(reply);
		  }
		  else {
			  block();
		  }
	  }
	}  // End of inner class ResourceReservationRequestsServer
	
	/**Inner class ResourceReservationOrdersServer. 
	 * This is the behaviour used by Service Providers agents to serve incoming resource proposals acceptances 
	 * (i.e. resource reservation orders) from client agents. The service provider agent set the requested resource 
	 * as "reserved" in its catalogue and replies with an INFORM message to notify the client agent that 
	 * the reservation has been successfully completed.
	 */
	
	private class ResourceReservationOrdersServer extends CyclicBehaviour {
		
	  public void action() {
		  
		  MessageTemplate mt = MessageTemplate.and( 
				  MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
				  MessageTemplate.MatchConversationId("resource-booking"));
		  		  
		  ACLMessage msg = receive(mt);
		  
		  if (msg != null) {
		      			  			  
			  // ACCEPT_PROPOSAL Message received. Process it
		      String resourceTitle = msg.getContent(); //ACCEPT_PROPOSAL [resource-title]
		      
		      ACLMessage reply = msg.createReply();
		      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
		      
		      if (reservationInfo != null && !reservationInfo.reserved) {
		    	// The requested resource is still existing and not reserved by another client.  
		        reply.setPerformative(ACLMessage.INFORM);
		        reply.setContent("reservation-completed");
		        catalogue.get(resourceTitle).client = msg.getSender();
		        catalogue.get(resourceTitle).reserved = true;
		        System.out.println(resourceTitle + " reserved to agent " + msg.getSender().getName());
		      }
		      else {
		        // The requested resource has been reserved to another client in the meanwhile.
		        reply.setPerformative(ACLMessage.FAILURE);
		        reply.setContent("reservation-failed");
		      }    	  		      
		      send(reply);
		  }
		  else {
			  block();
		  }
	  }
	}  // End of inner class ResourceReservationOrdersServer
	
	
	/**Inner class WaitingListOrdersServer. 
	 * This is the behaviour used by Service Providers agents to serve incoming waiting list proposals acceptances 
	 * (i.e. waiting list proposal deadline) from client agents. The service provider agent puts the client agent 
	 * in waiting list till the resource becomes unreserved or the waiting list deadline is reached.
	 */
	
	private class WaitingListOrdersServer extends CyclicBehaviour {
		
	  public void action() {
		  
		  MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			    		  MessageTemplate.and(
			    		  MessageTemplate.MatchConversationId("resource-booking"),
			              MessageTemplate.MatchInReplyTo("waiting-list")));		  
		  		  
		  ACLMessage msg = receive(mt);
		  
		  if (msg != null) {
		      			  			  
			  // ACCEPT_PROPOSAL Message received. Process it
		      String resourceTitle = msg.getContent(); //ACCEPT_PROPOSAL [resource-title]
		      
		      ACLMessage reply = msg.createReply();
		      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
		      
		      if (reservationInfo != null){
		    	  // The requested resource is still existing in local catalouge
		    	  String waitingListDeadLineValue = msg.getUserDefinedParameter("WaitingListDeadLine");		    	  
		    	  
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
						    reply.setPerformative(ACLMessage.REFUSE);
						    reply.setContent("resource-unavailable"); //REFUSE [resource-unavailable]
						    deadLineTimeOut = true;
						    break;  
			    		}
			    	  }
			    	  
			    	  if (!catalogue.get(resourceTitle).reserved && !deadLineTimeOut){ //Resource is unreserved
			    		  reply.setPerformative(ACLMessage.PROPOSE);  
			    		  reply.setContent(reservationInfo.price.toString());	
			    	  }
		    	  }
		      }
		      else {
			      // The requested resource is NOT existing in cataglouge.
			      reply.setPerformative(ACLMessage.REFUSE);
			      reply.setContent("resource-unavailable"); //REFUSE [resource-unavailable]
			  }		    	  	      
		      send(reply);
		  }
		  else {
			  block();
		  }
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
			  
	 		 MessageTemplate mt = MessageTemplate.and(
			    		  MessageTemplate.MatchPerformative(ACLMessage.CANCEL),
			    		  MessageTemplate.MatchConversationId("resource-booking")); 
			  
	 		 ACLMessage msg = receive(mt);

	 		 if (msg != null) {

	 			  // CANCEL Message received. Process it
		 	      String resourceTitle = msg.getContent(); //CANCEL [resource-title]
		
		 	      ACLMessage reply = msg.createReply();	      
		 	      ReservationInfo reservationInfo = catalogue.get(resourceTitle);
		 	      
		 	      if (reservationInfo != null && reservationInfo.reserved && reservationInfo.client == msg.getSender()) {	    	  
		 	    	// The reserved resource is in the local catalogue and reserved by the client agent.	        
		 	    	reply.setPerformative(ACLMessage.CONFIRM);
		 	    	reply.setContent("reservation-cancelation-completed");
		 	    	catalogue.get(resourceTitle).client = null;
		 	    	catalogue.get(resourceTitle).reserved = false;
		 	        System.out.println(resourceTitle+" reservation canceled by agent " + msg.getSender().getName());
		 	      }
		 	      else {
		 	        // The resource is unreserved by the client client.
		 	        reply.setPerformative(ACLMessage.DISCONFIRM);
		 	        reply.setContent("reservation-cancelation-failed");
		 	      }
		 	      send(reply);
	 	    }
	 		else {
	 			 block();
	 		}
	 	  }
	 }  // End of inner class ReservationCancelationServer
}
