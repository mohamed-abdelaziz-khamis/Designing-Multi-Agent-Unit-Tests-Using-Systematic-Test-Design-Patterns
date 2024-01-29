package mockagentdesigner.classes;

public class InteractingRole {
	private int roleID;
	private int interactingRoleID;

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
	 * @param interactingRoleID the interactingRoleID to set
	 */
	public void setInteractingRoleID(int interactingRoleID) {
		this.interactingRoleID = interactingRoleID;
	}
	/**
	 * @return the interactingRoleID
	 */
	public int getInteractingRoleID() {
		return interactingRoleID;
	}
}
