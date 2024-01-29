/**
 * 
 */
package MASUnitTesting;

import junit.framework.TestResult;

/**
 * @author Mohamed Abd El Aziz
 *
 */
public interface TestResultReporter {
	/**
	 * 
	 */
	public abstract void setTestResult(TestResult testResult);
	/**
	 * 
	 */
	public abstract TestResult getTestResult();
}
