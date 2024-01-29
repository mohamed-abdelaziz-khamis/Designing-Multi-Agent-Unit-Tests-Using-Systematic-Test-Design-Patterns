package mockagentdesigner.classes;

public class PatternCategory {
	private int patternCategoryID;
	private String patternCategoryName;
	private String patternCategoryDescription;
	/**
	 * @param patternCategoryName the patternCategoryName to set
	 */
	public void setPatternCategoryName(String patternCategoryName) {
		this.patternCategoryName = patternCategoryName;
	}
	/**
	 * @return the patternCategoryName
	 */
	public String getPatternCategoryName() {
		return patternCategoryName;
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
	 * @param patternCategoryDescription the patternCategoryDescription to set
	 */
	public void setPatternCategoryDescription(String patternCategoryDescription) {
		this.patternCategoryDescription = patternCategoryDescription;
	}
	/**
	 * @return the patternCategoryDescription
	 */
	public String getPatternCategoryDescription() {
		return patternCategoryDescription;
	}
}
