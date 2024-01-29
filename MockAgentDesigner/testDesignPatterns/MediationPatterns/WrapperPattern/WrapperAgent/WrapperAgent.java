/*
 * Agent Under Test (AUT): is the agent whose behavior is verified by a Test Case.
 * */

package MediationPatterns.WrapperPattern.WrapperAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;


@SuppressWarnings("serial")
public class WrapperAgent extends Agent {

	/* The catalogue of language to transalate between the source legacy interface and client ACL.
	 * (maps the language to its source dictionary)
	 */		
	private Hashtable<String, Dictionary<String, String>> languageCatalogue;

	// Message content is translated according the client ACL 
	private String language;

	/* The dictionary of source vocabulary.
	 * (maps the client content to its source content)
	 */	
	private Dictionary<String, String> sourceDictionary;

	// Content of the request message 
	private String messageContent;

	// Translated Request Content  
	private String translatedContent;
	
	//Source Answer
	private String sourceAnswer;
	
	//Translated Source Answer
	private String translatedSourceAnswer;

	// Source Agent ID
	private AID source;

	// Flag equals true if the known source agent replies with the source answer
	private boolean sourceAnswerSuccess;

	// Put agent initializations here
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hallo! Wrapper-Agent "+getAID().getName()+" is ready.");

		/* The role of the wrapper is to interface the agents to the legacy system by agentifying the legacy.
		 */

		source = new AID("source", AID.ISLOCALNAME);

		// Get the language to translate source legacy interface and client ACL as a start-up argument.
		Object[] args = getArguments();		 

		if (args != null && args.length > 0) {

			language = (String) args[0];

			messageContent = (String) args[1];
			translatedContent = (String) args[2]; 

			translatedSourceAnswer = (String) args[3];	      
			sourceAnswer = (String) args[4];
			
			// Create the dictionary of source vocabulary.
			sourceDictionary.put(messageContent, translatedContent);
			//The sourceAnswer is translated in reverse.
			sourceDictionary.put(translatedSourceAnswer, sourceAnswer);

			// Create the catalogue of language to transalate between the source legacy interface and client AC.
			languageCatalogue = new Hashtable<String, Dictionary<String, String>>();

			languageCatalogue.put(language, sourceDictionary);

			System.out.println(language + " inserted into language catalogue. " +
				" \n Message Content: " + messageContent + ", Translated Content: " + translatedContent + 
      			" \n Translated Source Answer:" + translatedSourceAnswer + ", Source Answer:" + sourceAnswer +
				" \n inserted into the dictionary of source vocabulary.");

			// Add the behaviour serving translated source answer queries from client agents
			addBehaviour(new RequestTranslatedSourceAnswerServer());

		}		
		else {
			// Make the agent terminate
			System.out.println("No language info is specified to be added in catalogue");
			doDelete();
		}

	}

	// Put agent clean-up operations here
	protected void takeDown() {		 		
		// Printout a dismissal message
		System.out.println("Wrapper-Agent "+getAID().getName()+" terminating.");	    
	}

	/** Inner class RequestTranslatedSourceAnswerServer. 
	 * This is the behaviour used by Wrapper agents to serve incoming requests for translated source-answer 
	 * from client agents. If the language that the message content is transalated in accordance with 
	 * is in the languageCatalogue, and the message content has translation in the sourceDictionary,
	 * the wrapper agent replies with an INFORM message specifying the translated source answer. 
	 * Otherwise a FAILURE message is sent back.
	 */   
	private class RequestTranslatedSourceAnswerServer extends CyclicBehaviour {

		public void action() { 		
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("service-wrapping"));

			ACLMessage msg = receive(mt);

			if (msg != null) {

				// REQUEST Message received. Process it
				messageContent = msg.getContent(); //REQUEST [message-content]
				language = msg.getLanguage();

				ACLMessage reply = msg.createReply();

				sourceDictionary = languageCatalogue.get(language);	
				if (sourceDictionary != null) {
					// The language is available in the languageCatalogue 
					translatedContent = sourceDictionary.get(messageContent);
					if (translatedContent != null){
						// The message content has translation in the sourceDictionary

						sourceAnswerSuccess = false;

						// Perform the translatedContent request for sourceAnswer
						addBehaviour(new RequestSourceAnswerPerformer());

						if (!sourceAnswerSuccess){
							// The translatedContent is NOT available for sourceAnswer.
							reply.setPerformative(ACLMessage.FAILURE);
							reply.setContent("translation-failed"); //FAILURE [translation-failed]
						}
						else{

							// The translatedContent is available. Reply with the sourceAnswer translated in reverse.
							boolean sourceAnswerFound = false;

							for (Enumeration<String> it = sourceDictionary.keys(); it.hasMoreElements(); ) {
								translatedSourceAnswer = (String) it.nextElement();
								if (sourceAnswer == sourceDictionary.get(translatedSourceAnswer)){
									sourceAnswerFound = true;
									break;						  			
								}
							}

							if (sourceAnswerFound){
								//The sourceAnswer has reversed translation in the sourceDictionary
								reply.setPerformative(ACLMessage.INFORM);
								reply.setContent(translatedSourceAnswer);
							}
							else{
								//The sourceAnswer has no reversed translation in the sourceDictionary
								reply.setPerformative(ACLMessage.FAILURE);
								reply.setContent("translation-failed"); //FAILURE [translation-failed]
							}
						}
					}
					else{
						// The message content doesn't have translation in the sourceDictionary 
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("translation-failed"); //FAILURE [translation-failed]
					}
				}
				else {
					// The language is NOT available in the languageCatalogue 
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("translation-failed"); //FAILURE [translation-failed]
				}
				send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class RequestTranslatedSourceAnswerServer	 

	/** Inner class RequestSourceAnswerPerformer
	 * This is the behaviour used by Wrapper agent to request Source agents the source-answer. 
	 * */	
	private class RequestSourceAnswerPerformer extends Behaviour {		

		private MessageTemplate mt; 		// The template to receive replies
		private int step = 0;	  	  

		public void action() {		  
			switch (step) {
			case 0:			    	
				// Send the translated-content to the known source agent
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			      

				request.addReceiver(source);			      
				request.setContent(translatedContent);
				request.setConversationId("service-wrapping");
				request.setReplyWith("request"+System.currentTimeMillis()); // Unique value

				send(request);

				// Prepare the template to get source-answer
				mt=MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("service-wrapping"),
								MessageTemplate.MatchInReplyTo(request.getReplyWith())));

				step = 1;			      
				break;			    
			case 1:
				// Receive the reply (source-answer inform/failure) from the source agent
				ACLMessage reply = receive(mt);			    	
				if (reply != null) {			        
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// This is a source answer 
						sourceAnswerSuccess = true;
						sourceAnswer = reply.getContent();
						System.out.println(translatedContent+" successfully responded by source agent " + reply.getSender().getName());
						System.out.println(", source-answer = "+ sourceAnswer);
					}
					else if (reply.getPerformative() == ACLMessage.FAILURE){ //FAILURE [translatedContent-unavailable]
						System.out.println(reply.getContent()+ ": Source agent " + reply.getSender().getName());						   
					}
					step = 2;
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
	}  // End of inner class RequestSourceAnswerPerformer
}
