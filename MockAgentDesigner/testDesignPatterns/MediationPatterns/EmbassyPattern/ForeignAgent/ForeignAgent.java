/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.EmbassyPattern.ForeignAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class ForeignAgent extends Agent {

	//The foreign agent requests access to an agent domain from its embassy agent
	private String agentDomain;

	/* Depending on the level of the digital certificate provided,
	 * access may be granted or denied by the Embassy agent*/
	private int  digitalCertificateLevel;		 

	// Content of the sent performative message 
	private String messageContent;

	// Message content is translated in accordance with a standard ontology 
	private String ontology;

	// Embassy Agent ID
	private AID embassy;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Foreign-Agent "+getAID().getName()+" is ready.");	    		  

		embassy = new AID("embassy", AID.ISLOCALNAME);

		// Get the agentDomain, digitalCertificateLevel, messageContent and ontology as start-up arguments
		Object[] args = getArguments();		  

		if (args != null && args.length > 0) {

			agentDomain = (String) args[0];
			System.out.println("The foreign agent requests access to agent domain: " + agentDomain);	  

			digitalCertificateLevel = Integer.parseInt((String)args[1]);
			System.out.println("The level of the digital certificate provided: " + digitalCertificateLevel);	

			messageContent = (String) args[2];
			System.out.println("Content of the sent performative message: " + messageContent);	  

			ontology = (String) args[3];
			System.out.println("Message content is translated in accordance with a standard ontology : " + ontology);

			/* Perform the agent domain access request and getting translated local response back 
			 * from the embassy agent
			 */
			addBehaviour(new RequestAccessPerformer());					
		}

		else {
			// Make the agent terminate
			System.out.println("No agent domain access data specified");
			doDelete();
		}
	}  

	// Put agent clean-up operations here
	protected void takeDown() {		 
		// Printout a dismissal message
		System.out.println("Foreign-Agent "+getAID().getName()+" terminating.");
	}

	/** Inner class RequestAccessPerformer. 
	 *  This is the behaviour used by Foreign agent to request  
	 *  agent domain access from Embassy agent.
	 */		
	private class RequestAccessPerformer extends Behaviour {				   
		private MessageTemplate mt; 	 // The template to receive replies 	  
		private int step = 0;
		private ACLMessage reply;	  
		public void action() {		  
			switch (step) {	
			case 0:			    	
				// Send the agent domain access request to the embassy agent
				ACLMessage accessRequest = new ACLMessage(ACLMessage.REQUEST);			      

				accessRequest.addReceiver(embassy);	      
				accessRequest.setContent(agentDomain);
				accessRequest.setConversationId("request-embassy");
				accessRequest.setReplyWith("accessRequest"+System.currentTimeMillis()); // Unique value
				accessRequest.addUserDefinedParameter("DigitalCertificateLevel", Integer.toString(digitalCertificateLevel));

				send(accessRequest);			      

				// Prepare the template to get access granted 
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.AGREE),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("request-embassy"),
								MessageTemplate.MatchInReplyTo(accessRequest.getReplyWith())));			      			    	
				step = 1;			      
				break;			    		   
			case 1:
				// Receive the agent domain access request reply (agree/refusal) from the embassy agent
				reply = receive(mt);			    				      
				if (reply != null) {				      
					// Reply received
					if (reply.getPerformative() == ACLMessage.AGREE) {	//AGREE [access-granted]				  	    	 				    	  
						System.out.println(reply.getContent() + ": Embassy agent "
								+ reply.getSender().getName());				    	 
						step = 2;				     
					}				      
					else if (reply.getPerformative() == ACLMessage.REFUSE){ //REFUSE [access-denied]
						System.out.println(reply.getContent()+ ": Embassy agent "
								+ reply.getSender().getName());
						myAgent.doDelete();
					}				      
				}			      
				else {
					block();
				}		   			    
				break;
			case 2:			    	
				/*
				 * Send the content of the performative message to the embassy agent to be translated  
				 * in accordance with the standard ontology
				 */
				ACLMessage translationRequest = new ACLMessage(ACLMessage.REQUEST);			      

				translationRequest.addReceiver(embassy);	      
				translationRequest.setContent(messageContent);
				translationRequest.setOntology(ontology);
				translationRequest.setConversationId("request-embassy");
				translationRequest.setReplyWith("translationRequest"+System.currentTimeMillis()); // Unique value		  				  

				send(translationRequest);			      

				// Prepare the template to get translated local response
				mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("request-embassy"),
								MessageTemplate.MatchInReplyTo(translationRequest.getReplyWith())));			      			    	
				step = 3;			      
				break;
			case 3:      	    
				/* Receive the translation request reply (translated local response inform/failure) 
				 * from the embassy agent*/
				reply = receive(mt);	    			      
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) { //INFORM [translated-local-response]
						// Local response translated successfully. We can terminate
						System.out.println(messageContent+" successfully translated by embassy agent " + reply.getSender().getName());
						System.out.println("Translated Local Response = "+ reply.getContent());
					}
					else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [translation-failed]
						System.out.println(reply.getContent()+ ": Embassy agent " + reply.getSender().getName());
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
	}  // End of inner class RequestAccessPerformer			
}