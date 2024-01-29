package mockagentdesigner.classes;

public class Pattern {
	private int patternID;
	private String patternName;
	private int patternCategoryID;
	private String patternDescription;

	/**
	 * @param patternName the patternName to set
	 */
	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}
	/**
	 * @return the patternName
	 */
	public String getPatternName() {
		return patternName;
	}
	/**
	 * @param patternID the patternID to set
	 */
	public void setPatternID(int patternID) {
		this.patternID = patternID;
	}
	/**
	 * @return the patternID
	 */
	public int getPatternID() {
		return patternID;
	}
	/**
	 * @param patternCategoryID the patternCategoryID to set
	 */
	public void setPatternCategoryID(int patternCategoryID) {
		this.patternCategoryID = patternCategoryID;
	}
	/**
	 * @return the patternCategoryID
	 */
	public int getPatternCategoryID() {
		return patternCategoryID;
	}
	/**
	 * @param patternDescription the patternDescription to set
	 */
	public void setPatternDescription(String patternDescription) {
		this.patternDescription = patternDescription;
	}
	/**
	 * @return the patternDescription
	 */
	public String getPatternDescription() {
		return patternDescription;
	}
}
