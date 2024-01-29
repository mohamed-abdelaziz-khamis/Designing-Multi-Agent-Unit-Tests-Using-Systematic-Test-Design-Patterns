/**
 * Design Pattern Category:		MediationPatterns
 * Design Pattern:				BrokerPattern
 * Agent Under Test:			BrokerAgent
 * Mock Agent:					MockBrokerClientAgent
 */
package MediationPatterns.BrokerPattern.BrokerAgent.MockBrokerClientAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestResult;
import MASUnitTesting.JADEMockAgent;
import MASUnitTesting.ReplyReceptionFailed;


@SuppressWarnings("serial")
public class MockBrokerClientAgent extends JADEMockAgent {

  private static ResourceBundle resMockClientAgent = 
		 ResourceBundle.getBundle
		 ("MediationPatterns.BrokerPattern.BrokerAgent.MockBrokerClientAgent.MockBrokerClientAgent");
					
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

  // The title of the requested service
  private String serviceTitle;

  // Broker Agent ID
  private AID broker;

  // Put agent initializations here
  protected void setup() {

	  // Printout a welcome message
	  System.out.println("Hallo! Mock-Client-Agent "+getAID().getName()+" is ready.");	    		  

	  broker = new AID("broker", AID.ISLOCALNAME);

	  // Get the title of the requested service as a start-up argument
	  Object[] args = getArguments();		  

	  if (args != null && args.length > 0) {

		  serviceTitle = (String) args[0];
		  System.out.println("The title of the requested service: "+serviceTitle);	  

		  // Add a TickerBehaviour that schedules a request to the broker agent every given period
		  addBehaviour(new TickerBehaviour(this, Long.parseLong(getResourceString("Ticker_Behaviour_Period"))) {

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
	  System.out.println("Mock-Client-Agent "+getAID().getName()+" terminating.");
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
	  	try {  
		  switch (step) {	
			  case 0:			    	
				  // Send the service request to the broker agent
				  ACLMessage request = new ACLMessage(ACLMessage.getInteger(getResourceString("REQUEST_Performative")));			      
	
				  request.addReceiver(broker);	      
				  request.setContent(serviceTitle);				  			  
				  request.setConversationId(getResourceString("REQUEST_ConversationID"));
				  request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				  request.addUserDefinedParameter(getResourceString("Service_Description_Type_Key"),
						  						getResourceString("Service_Description_Type_Value"));
	
				  sendMessage(request);			      
	
				  // Prepare the template to get request acceptance
				  mt = MessageTemplate.and(
						  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("ACCEPT_PROPOSAL_Performative"))),
						  MessageTemplate.and(
								  MessageTemplate.MatchConversationId(getResourceString("ACCEPT_PROPOSAL_ConversationID")),
								  MessageTemplate.MatchInReplyTo(request.getReplyWith())));			      			    	
				  step = 1;			      
				  break;			    		   
			  case 1:
				  // Receive the reply (acceptance/refusal) from the broker agent
				  reply = receiveMessage(myAgent, mt);			    				      
				  if (reply != null) {				      
					  // Reply received
					  if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {	
						  //ACCEPT_PROPOSAL [service-description-type-unavailable]				  	    	 				    	  
						  System.out.println(reply.getContent() + ": Broker agent "
								  + reply.getSender().getName());				    	 
						  step = 2;				     
					  }				      
					  else if (reply.getPerformative() == ACLMessage.REFUSE){ 
						  //REFUSE [service-description-type-available]
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
				  ACLMessage confirm = new ACLMessage(ACLMessage.getInteger(getResourceString("CONFIRM_Performative")));			      
	
				  confirm.addReceiver(broker);	      
				  confirm.setContent(serviceTitle);
				  confirm.setConversationId(getResourceString("CONFIRM_ConversationID"));
				  confirm.setReplyWith("confirm"+System.currentTimeMillis()); // Unique value		  				  
	
				  sendMessage(confirm);			      
	
				  // Prepare the template to get forwarded service
				  mt = MessageTemplate.and(
						  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative"))),
						  MessageTemplate.and(
								  MessageTemplate.MatchConversationId(getResourceString("INFORM_ConversationID")),
								  MessageTemplate.MatchInReplyTo(confirm.getReplyWith())));			      			    	
				  step = 3;			      
				  break;
			  case 3:      	    
				  // Receive the reply (inform/failure) from the broker agent
				  reply = receiveMessage(myAgent, mt);	    			      
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
	  	 catch (ReplyReceptionFailed  e) {
	  			setTestResult(prepareMessageResult(e));
	  			e.printStackTrace();
	  			myAgent.doDelete();			
		 } 																	
		 setTestResult(new TestResult());
	  }

	  public boolean done() {
		  return (step == 4);
	  }
  }  // End of inner class RequestServicePerformer			
}