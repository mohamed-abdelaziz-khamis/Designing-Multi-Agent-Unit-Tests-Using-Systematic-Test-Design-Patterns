package mockagentdesigner.classes;

public class Role {
	private int roleID;
	private String roleName;
	private int patternID;
	private String roleDescription;

	/**
	 * @param roleID the roleID to set
	 */
	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}
	/**
	 * @return the roleID
	 */
	public int getRoleID() {
		return roleID;
	}
	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
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
	 * @param roleDescription the roleDescription to set
	 */
	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}
	/**
	 * @return the patternDescription
	 */
	public String getRoleDescription() {
		return roleDescription;
	}
}
