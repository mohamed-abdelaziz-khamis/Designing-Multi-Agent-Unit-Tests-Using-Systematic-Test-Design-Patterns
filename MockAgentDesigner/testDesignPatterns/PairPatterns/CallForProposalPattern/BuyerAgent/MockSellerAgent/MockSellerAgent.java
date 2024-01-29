/**
 * Design Pattern Category:		PairPatterns
 * Design Pattern:				CallForProposalPattern
 * Agent Under Test:			BuyerAgent
 * Mock Agent:					MockSellerAgent
 */

package PairPatterns.CallForProposalPattern.BuyerAgent.MockSellerAgent;

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
public class MockSellerAgent extends JADEMockAgent {
	
  private static ResourceBundle resMockSellerAgent = 
		ResourceBundle.getBundle
		("PairPatterns.CallForProposalPattern.BuyerAgent.MockSellerAgent.MockSellerAgent");
	
  /** Returns a string from the resource bundle. We don't want to crash because of a missing String. 
	Returns the key if not found.*/
  private static String getResourceString(String key) {
		try {
			return resMockSellerAgent.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
  }	

  // The catalogue of service for sale (maps the title of a service to its price)
  private Hashtable<String, Float> catalogue;  
  private String serviceTitle;
  private float servicePrice;
 
  //Put agent initializations here
  protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Mock-Seller-Agent " + getAID().getName() + " is ready.");	    
		
		// Get the title of the service to provide as a start-up argument.
		Object[] args = getArguments();		  
		  
		if (args != null && args.length > 0) {

			serviceTitle = (String) args[0];
	        servicePrice = Float.parseFloat((String)args[1]);
	  	  
			// Create the catalogue
		    catalogue = new Hashtable<String, Float>();
		
			catalogue.put(serviceTitle, servicePrice);
			
	        System.out.println(serviceTitle + " inserted into catalogue. " 
	        			+ ", Price = " + servicePrice);
	        
		    // Register the selling service in the yellow pages
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
		    
		    // Add the behaviour serving queries from buyer agents
		    addBehaviour(new OfferRequestsServer());
		
		    // Add the behaviour serving purchase orders from buyer agents
		    addBehaviour(new PurchaseOrdersServer());
		}		
		else {
			// Make the agent terminate
			System.out.println("No resource title specified to be added in catalogue");
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
    System.out.println("Mock-Seller-Agent "+getAID().getName()+" terminating.");
  }

	/** Inner class OfferRequestsServer. 
	 * This is the behaviour used by Seller agents to serve incoming requests for offer from buyer agents.
	 * If the requested service is in the local catalogue the seller agent replies with a PROPOSE message 
	 * specifying the price. Otherwise a REFUSE message is sent back.
	*/
	  
	private class OfferRequestsServer extends CyclicBehaviour {
	  
	  public void action() {
		  try {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative (ACLMessage.getInteger(getResourceString("CFP_Performative"))),
					MessageTemplate.MatchConversationId(getResourceString("CFP_ConversationID")));
			
			ACLMessage msg = receiveMessage(myAgent, mt);
			  
		    if (msg != null) {
		      
		      // CFP Message received. Process it
		      String title = msg.getContent(); //CFP [service-title]
		      
		      ACLMessage reply = msg.createReply();
		      Float price = (Float) catalogue.get(title);
	
		      if (price != null) {		    	  
		        // The requested service is available for sale. Reply with the price
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("PROPOSE_Performative")));
		        reply.setContent(price.toString());
		        
		      }
		      else {
		        // The requested service is NOT available for sale.
		        reply.setPerformative(ACLMessage.getInteger(getResourceString("REFUSE_Performative")));
		        reply.setContent(getResourceString("REFUSE_Content")); //REFUSE [service-unavailable]
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
	}  // End of inner class OfferRequestsServer
	
	/** Inner class PurchaseOrdersServer.
	 * This is the behaviour used by Seller agents to serve incoming offer acceptances (i.e. purchase orders) 
	 * from buyer agents. The seller agent removes the purchased service from its catalogue and replies with an 
	 * INFORM message to notify the buyer that the purchase has been sucesfully completed.
	 */
  
	private class PurchaseOrdersServer extends CyclicBehaviour {
	  
	  public void action() {
		  try {			  
			  MessageTemplate mt = MessageTemplate.and( 
				  MessageTemplate.MatchPerformative(ACLMessage.getInteger(getResourceString("ACCEPT_PROPOSAL_Performative"))),
				  MessageTemplate.MatchConversationId(getResourceString("ACCEPT_PROPOSAL_ConversationID")));
			  			  
			  ACLMessage msg = receiveMessage(myAgent, mt);
		    
			  if (msg != null) {
 		    	
			      // ACCEPT_PROPOSAL Message received. Process it
			      String title = msg.getContent(); //ACCEPT_PROPOSAL [service-title]
		
			      ACLMessage reply = msg.createReply();	      
			      Float price = (Float) catalogue.get(title);
			      
			      if (price != null) {	    	  
			    	// The requested service is still exiting and not sold to another buyer.	        
			    	reply.setPerformative(ACLMessage.getInteger(getResourceString("INFORM_Performative")));
			        System.out.println(title+" sold to agent "+msg.getSender().getName());
			      }
			      else {
			        // The requested service has been sold to another buyer in the meanwhile .
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
	}  // End of inner class OfferRequestsServer
  
  }
