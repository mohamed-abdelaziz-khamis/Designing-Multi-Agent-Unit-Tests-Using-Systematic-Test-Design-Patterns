/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.BrokerPattern.BrokerClientAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class BrokerClientAgent extends Agent {

	 // The title of the requested service
	 private String serviceTitle;
			   
	 // Broker Agent ID
	 private AID broker;
		  
	 // Put agent initializations here
	 protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Client-Agent "+getAID().getName()+" is ready.");	    		  
			  
		broker = new AID("broker", AID.ISLOCALNAME);
			  
		// Get the title of the requested service as a start-up argument
		Object[] args = getArguments();		  
			  
		if (args != null && args.length > 0) {
				
			serviceTitle = (String) args[0];
			System.out.println("The title of the requested service: "+serviceTitle);	  
			
		    // Add a TickerBehaviour that schedules a request to the broker agent every given period
		    addBehaviour(new TickerBehaviour(this, 60000) {
			      
				protected void onTick() {				    	  
					System.out.println("Trying to buy "+serviceTitle);	          

					// Perform the service request from the broker agent
					addBehaviour(new RequestServicePerformer());					
				}
			} );
		}
		else {
			// Make the agent terminate
			System.out.println("No service title specified");
			doDelete();
		}
	  }  

	 // Put agent clean-up operations here
	 protected void takeDown() {		 
		 // Printout a dismissal message
		 System.out.println("Client-Agent "+getAID().getName()+" terminating.");
	 }
			
	 /** Inner class RequestServicePerformer. 
	 *  This is the behaviour used by Client agent to request  
	 *  from Broker agent the required service.
	 */		
	private class RequestServicePerformer extends Behaviour {				   
	   private MessageTemplate mt; 	 // The template to receive replies 	  
	   private int step = 0;
	   private ACLMessage reply;	  
	   public void action() {		  
		   switch (step) {	
				case 0:			    	
				  // Send the service request to the broker agent
				  ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      
					      
				  request.addReceiver(broker);	      
				  request.setContent(serviceTitle);				  			  
				  request.setConversationId("service-broking");
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				  request.addUserDefinedParameter("Service_Description_Type", "service-providing");
					
				  send(request);			      
						  
				  // Prepare the template to get request acceptance
				  mt = MessageTemplate.and(
				  		 MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
				   		 MessageTemplate.and(
				   		 MessageTemplate.MatchConversationId("service-broking"),
				   		 MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
			      step = 1;			      
			      break;			    		   
				case 1:
				  // Receive the reply (acceptance/refusal) from the broker agent
				  reply = receive(mt);			    				      
				  if (reply != null) {				      
				      // Reply received
					  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	//ACCEPT_PROPOSAL [request-accepted]				  	    	 				    	  
					      System.out.println(reply.getContent() + ": Broker agent "
			        		  			+ reply.getSender().getName());				    	 
					      step = 2;				     
					  }				      
					  else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [request-refused]
						  System.out.println(reply.getContent()+ ": Broker agent "
		        		  				+ reply.getSender().getName());
						  step = 4;
					  }				      
				  }			      
				  else {
					     block();
				  }		   			    
				  break;
				case 2:			    	
				  // Send the service request acceptance confirmation to the broker agent
				  ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);			      
						      
				  confirm.addReceiver(broker);	      
				  confirm.setContent(serviceTitle);
				  confirm.setConversationId("service-broking");
				  confirm.setReplyWith("confirm"+System.currentTimeMillis()); // Unique value		  				  
						
				  send(confirm);			      
							  
				  // Prepare the template to get forwarded service
				  mt = MessageTemplate.and(
					  	  MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					   	  MessageTemplate.and(
					   	  MessageTemplate.MatchConversationId("service-broking"),
					   	  MessageTemplate.MatchInReplyTo(confirm.getReplyWith())));			      			    	
				  step = 3;			      
				  break;
			    case 3:      	    
				  // Receive the reply (inform/failure) from the broker agent
				  reply = receive(mt);	    			      
				  if (reply != null) {
				     // Reply received
				     if (reply.getPerformative() == ACLMessage.INFORM) { //INFORM [service-price]
				        // Service forwarded successfully. We can terminate
				        System.out.println(serviceTitle+" successfully forwarded by broker agent " + reply.getSender().getName());
				        System.out.println("Price = "+ reply.getContent());
				        myAgent.doDelete();
				     }
				     else { //FAILURE [service-failure]
					    System.out.println(reply.getContent()+ ": Broker agent " + reply.getSender().getName());					    
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
			 return (step == 4);
			}
		}  // End of inner class RequestServicePerformer			
}