/**
 * A Mock Object is a regular object that acts as a stub, 
 * but also includes assertions to instrument the interactions 
 * of the target object with its neighbors.
 */

package MASUnitTesting;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import junit.framework.AssertionFailedError;
import junit.framework.TestResult;
import junit.framework.Assert;

import MASUnitTesting.ReplyReceptionFailed;


/**
 * @author Mohamed Abd El Aziz
 *
 */

public class JADEMockAgent extends Agent implements TestResultReporter{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
    
	private TestResult _testResult = new TestResult();
	
	protected void sendMessage(ACLMessage message){		
		send(message);			
	}

	/*
	 * The receiveMessage() method performs assertions concerning the received message 
	 * (e.g. whether the message was received within a specific timeout, 
	 * or if it obeys a pre-defined format). 
	 * 
	 * It is implemented following the (Template Method design pattern) 
	 * in order to enable the developer to perform additional assertions 
	 * in the received message.
	 */
	
	protected ACLMessage receiveMessage(Agent myAgent, MessageTemplate mt) throws ReplyReceptionFailed{			     		
		ACLMessage reply = myAgent.receive(mt);
		Assert.assertTrue (reply != null);
		return reply;
	}
	
	@SuppressWarnings("unused")
	private void extraMessageValidation(ACLMessage message){
		
	}
	
	protected TestResult prepareMessageResult(AssertionFailedError  e){
		TestResult testResult = new TestResult();
		testResult.addFailure(null, e);
		return testResult;
	}

	public TestResult getTestResult() {
		// TODO Auto-generated method stub
		return _testResult;
	}

	public void setTestResult(TestResult testResult) {
		// TODO Auto-generated method stub
		_testResult = testResult;
	}
}
