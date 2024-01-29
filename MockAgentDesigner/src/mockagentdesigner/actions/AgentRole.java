package mockagentdesigner.actions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import mockagentdesigner.Activator;
import mockagentdesigner.classes.Pattern;
import mockagentdesigner.classes.PatternCategory;
import mockagentdesigner.classes.Role;
import mockagentdesigner.dataaccess.DAPattern;
import mockagentdesigner.dataaccess.DARole;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.Bundle;

import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;


/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class AgentRole extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites */
	Composite composite;
	Group roleGroup, roleNameGroup, patternNameGroup, roleDescriptionGroup, optionsGroup;
	
	/*Declare the used widgets */
	Label labelRoleName, labelPatternName,labelRoleDescription;
	Combo comboRoleName, comboPatternName;
	Text textRoleName, textRoleDescription, textRoleDescriptionAddEdit;
	Button buttonAddRole, buttonEditRole, buttonSaveRole, buttonDeleteRole, buttonCancelRole;
	ArrayList<Role> roles = null;
	ArrayList<Pattern> patterns = null;
	String mode;
	String testDesignPatternsDirectory;
	int selectedIndex;
	
	private static ResourceBundle resActions = ResourceBundle.getBundle("mockagentdesigner_actions");
	
	/**
	 * Returns a string from the resource bundle.
	 * We don't want to crash because of a missing String.
	 * Returns the key if not found.
	 */
	static String getResourceString(String key) {
		try {
			return resActions.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}
	
	private void setTestDesignPatternsDirectory(){
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path("testDesignPatterns"); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
		URL fileUrl = null;
		try {
		fileUrl = FileLocator.toFileURL(url);
		}
		catch (IOException e) {
		// Will happen if the file cannot be read for some reason
			MessageDialog.openError(getShell(), getResourceString("IO_Exception_Title"), getResourceString("IO_Exception_Message"));
		}
		File file = new File(fileUrl.getPath());
		testDesignPatternsDirectory = file.getPath();
	}

	/**
	 * The constructor.
	 */
	public AgentRole() {
        super(null);
        setTestDesignPatternsDirectory();
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		setBlockOnOpen(true);
        open();        
        close();
	}

    /**
     * Overrides {@link ApplicationWindow#configureShell(
     * org.eclipse.swt.widgets.Shell)}.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        
        shell.setText("Agent Role");
        shell.setSize(420, 290);
        shell.setLocation(100, 100);
        
    }
    
    private void handleView(){
    	
    	if (mode == "view"){    		
    		
    		comboRoleName.setVisible(true);
    		textRoleName.setVisible(false);
    		
    		if (comboRoleName.getItemCount()>0){
    			selectedIndex = comboRoleName.getSelectionIndex();
    			fillComboPatternName(roles.get(selectedIndex).getPatternID());
    			comboPatternName.select(0);
    			textRoleDescription.setText(roles.get(selectedIndex).getRoleDescription());
    		}else {
    			comboPatternName.removeAll();
    			textRoleDescription.setText("");
    		}
    		
    		comboPatternName.setEnabled(false);
    		textRoleDescription.setVisible(true);
    		textRoleDescriptionAddEdit.setVisible(false);
    		
    		buttonAddRole.setEnabled(true);
    		buttonEditRole.setEnabled(true);
    		buttonSaveRole.setEnabled(false);
    		buttonDeleteRole.setEnabled(true);
    		
    		comboRoleName.setFocus();
    	}
    	
    	else if (mode == "add"){
    		
    		comboRoleName.setVisible(false);
    		textRoleName.setVisible(true);
    		textRoleName.setText("");
    		
    		comboPatternName.setEnabled(true);  		
    		fillComboPatternName(0);

    		textRoleDescription.setVisible(false);
    		textRoleDescriptionAddEdit.setVisible(true);
    		textRoleDescriptionAddEdit.setText("");

    		buttonAddRole.setEnabled(false);
    		buttonEditRole.setEnabled(false);
    		buttonSaveRole.setEnabled(true);
    		buttonDeleteRole.setEnabled(false);
    	}
    	
    	else if (mode == "edit"){    		   	   		
    		
    		comboRoleName.setVisible(false);   		
    		textRoleName.setVisible(true);    		
    		textRoleName.setText(comboRoleName.getText());
    		
    		comboPatternName.setEnabled(true);
    		
			selectedIndex = comboRoleName.getSelectionIndex();
        	fillComboPatternName(0);       	
    		comboPatternName.setText(getPatternName(roles.get(selectedIndex).getPatternID()));
     		
    		textRoleDescription.setVisible(false);
    		textRoleDescriptionAddEdit.setVisible(true);    		    		
    		textRoleDescriptionAddEdit.setText(textRoleDescription.getText());
    		
    		buttonAddRole.setEnabled(false);
    		buttonEditRole.setEnabled(false);
    		buttonSaveRole.setEnabled(true);
    		buttonDeleteRole.setEnabled(false);
    	}
    }
    
    /**
     * Overrides {@link org.eclipse.jface.window.Window#createContents(
     * org.eclipse.swt.widgets.Composite)}.
     */
    protected Control createContents(final Composite parent) {        
    	composite = new Composite(parent, SWT.NONE);
    	composite.setLayout (new GridLayout (1, false));
				             
        createRoleGroup();    	            
        createOptionsGroup();
               
        return composite;
    }
    
	/**
	 * Creates the "AgentRole" group.
	 */
	private void createRoleGroup () {		
	
		/*
		 * Create the "AgentRole" group.  
		 * This is the group from which the user chooses the required AgentRole.
		 */	

		roleGroup = new Group (composite, SWT.NONE);
		roleGroup.setLayout (new GridLayout (2, false));
		roleGroup.setLayoutData (new GridData(GridData.FILL_BOTH));		
		roleGroup.setText ("Choose Agent Role");
		
		/* Create the widgets inside this group*/
	
        createLabelRoleName();
        createRoleNameGroup();
        createComboRoleName();
        createTextRoleName();
        
        createLabelPatternName();
        createPatternNameGroup();
        createComboPatternName();
        
        createLabelRoleDescription();
        createRoleDescriptionGroup();
        createTextRoleDescription();
        createTextRoleDescriptionAddEdit();
	}

    /**
     * Creates a label that represents the Role Name.
     */
    private void createLabelRoleName() {       
		labelRoleName = new Label (roleGroup, SWT.NONE);
		labelRoleName.setText ("Agent Role Name");
    }
    
	private void createRoleNameGroup(){
		roleNameGroup = new Group (roleGroup, SWT.NONE);
		roleNameGroup.setLayout (new FormLayout ());		
	}
    
	private void fillComboRoleName(int roleID){
		comboRoleName.removeAll();
				
		DARole daRole = new DARole();
		try {
			roles = daRole.GetRoles_ByRoleID(roleID);
		} catch (XPathParseException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
			 close();

		} catch (XPathEvalException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
			 close();

		} catch (NavException e) {			
			 MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
			 close();
		}
		
	    Iterator<Role> it = roles.iterator();
	            
	    while(it.hasNext()){
	    	Role role = it.next();            
	    	comboRoleName.add(role.getRoleName());
	    	comboRoleName.setData (role.getRoleName(), role.getRoleID());
	    }
	}
	
	private void fillComboPatternName(int patternID){
		comboPatternName.removeAll();
				
		DAPattern daPattern = new DAPattern();
		try {
			patterns = daPattern.GetPatterns_ByPatternID(patternID);
		} catch (XPathParseException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
			 close();

		} catch (XPathEvalException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
			 close();

		} catch (NavException e) {			
			 MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
			 close();
		}		
		
        Iterator<Pattern> it = patterns.iterator();
        
        while(it.hasNext()){
        	Pattern pattern = it.next();            
        	comboPatternName.add(pattern.getPatternName());
        	comboPatternName.setData (pattern.getPatternName(), pattern.getPatternID());
        }
		
	}
	
	private String getPatternName(int patternID){
				
		DAPattern daPattern = new DAPattern();
		try {
			patterns = daPattern.GetPatterns_ByPatternID(patternID);
		} catch (XPathParseException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
			 close();

		} catch (XPathEvalException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
			 close();

		} catch (NavException e) {			
			 MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
			 close();
		}		
				
        return patterns.get(0).getPatternName();
	} 
	
	private String getPatternCategoryName(int patternID){
		
		DAPattern daPattern = new DAPattern();
		PatternCategory patternCategory = new PatternCategory();
		try {
			patternCategory = daPattern.GetPatternCategory_ByPatternID(patternID);
		} catch (XPathParseException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
			 close();

		} catch (XPathEvalException e) {
			 MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
			 close();

		} catch (NavException e) {			
			 MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
			 close();
		}
		
		return patternCategory.getPatternCategoryName();
	}
	
	/**
     * Creates a combo that represents the Role Name.
     */
    private void createComboRoleName() {       
		comboRoleName = new Combo (roleNameGroup, SWT.READ_ONLY);
		fillComboRoleName(0);
		if (comboRoleName.getItemCount()>0) 
			comboRoleName.select(0);	
		comboRoleName.setLayoutData (new FormData(220,10));		
		comboRoleName.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	selectedIndex = comboRoleName.getSelectionIndex();
            	fillComboPatternName(roles.get(selectedIndex).getPatternID());
            	comboPatternName.select(0);
            	textRoleDescription.setText(roles.get(selectedIndex).getRoleDescription());
            }
        });
    }
    
    /**
     * Creates a text box that represents the Role Name.
     */
    private void createTextRoleName() {	  			
    	textRoleName = new Text (roleNameGroup, SWT.BORDER);
    	textRoleName.setLayoutData (new FormData(235,15));
    }

    /**
     * Creates a label that represents the Pattern Name.
     */
    private void createLabelPatternName() {       
		labelPatternName = new Label (roleGroup, SWT.NONE);
		labelPatternName.setText ("Design Pattern Name");
    }
    
	private void createPatternNameGroup(){
		patternNameGroup = new Group (roleGroup, SWT.NONE);
		patternNameGroup.setLayout (new FormLayout ());		
	}	
	
    /**
     * Creates a combo that represents the Pattern Name.
     */
    private void createComboPatternName() {       
		comboPatternName = new Combo (patternNameGroup, SWT.READ_ONLY);
		if (comboRoleName.getItemCount()>0){
			fillComboPatternName(roles.get(0).getPatternID());
			comboPatternName.select(0);
		}
		comboPatternName.setEnabled(false);
		comboPatternName.setLayoutData(new FormData(220,10));
    }
      
    /**
     * Creates a label that represents the Role Description.
     */
    private void createLabelRoleDescription() {       
		labelRoleDescription = new Label (roleGroup, SWT.NONE);
		labelRoleDescription.setText ("Agent Role Description");
    }
    
	private void createRoleDescriptionGroup(){
		roleDescriptionGroup = new Group (roleGroup, SWT.NONE);
		roleDescriptionGroup.setLayout (new FormLayout ());		
	}
	
    /**
     * Creates a text box that represents the Role Description.
     */
    private void createTextRoleDescription() {
    	textRoleDescription = new Text (roleDescriptionGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP |SWT.READ_ONLY);
    	if (comboRoleName.getItemCount()>0)
    		textRoleDescription.setText(roles.get(0).getRoleDescription());
    	textRoleDescription.setLayoutData (new FormData(235, 60));
    }    
	
    /**
     * Creates a text box that represents the Role Description in Add Edit Mode.
     */
    private void createTextRoleDescriptionAddEdit() {		
    	textRoleDescriptionAddEdit = new Text (roleDescriptionGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
    	textRoleDescriptionAddEdit.setText ("");
    	textRoleDescriptionAddEdit.setLayoutData (new FormData(235, 60));
    }
    
    /**
	 * Creates the "Options" group.
	 */
	private void createOptionsGroup() {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can either add, edit or delete
		 * the role or close this form.
		 */	

		optionsGroup = new Group (composite, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (5, true));
		optionsGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		optionsGroup.setText ("Choose Option");
		
		/* Create the widgets inside this group*/
        createButtonAddRole();
      	createButtonEditRole();
      	createButtonSaveRole();
      	createButtonDeleteRole();
      	createButtonCancelRole();       
	}
	
    /**
     * Creates a button that adds a new agent role.
     */
    private void createButtonAddRole() {
        buttonAddRole = new Button(optionsGroup, SWT.PUSH);
        buttonAddRole.setText("Add");
        buttonAddRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonAddRole.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	mode = "add";
            	handleView();
            }
        });
    }
    
    /**
     * Creates a button that edits an existing role.
     */
    private void createButtonEditRole() {
    	buttonEditRole = new Button(optionsGroup, SWT.PUSH);
    	buttonEditRole.setText("Edit");
    	buttonEditRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonEditRole.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	if (comboRoleName.getItemCount()==0) return;
            	mode = "edit";
            	handleView();
            }
        });
    }
    
    
    /**
     * Creates a button that saves the existing role.
     */
    private void createButtonSaveRole() {
    	
    	buttonSaveRole = new Button(optionsGroup, SWT.PUSH);
    	buttonSaveRole.setText("Save");
    	buttonSaveRole.setEnabled(false);
    	buttonSaveRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonSaveRole.addSelectionListener(new SelectionAdapter() {
    		
    		public void widgetSelected(SelectionEvent arg0) {
            	
    			if (textRoleName.getText().trim().equals(new String(""))){    			
       			 MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
       			 textRoleName.setFocus();
       			 return;
       			}
    			
    			if (comboPatternName.getText().trim().equals(new String(""))){    			
          		  MessageDialog.openError(getShell(), getResourceString("Invalid_Parent_Title"), getResourceString("Invalid_Parent_Message"));
          		  comboPatternName.setFocus();
          		  return;
          		}
    			
    			boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Save_Title"), getResourceString("Confirm_Save_Message"));
                
	            if (answer){	
            		
	            	DARole daRole = new DARole();
            		
            		Role role = new Role();

            		role.setRoleName(textRoleName.getText());
            		role.setPatternID(Integer.parseInt(comboPatternName.getData(comboPatternName.getText()).toString()));
            		role.setRoleDescription(textRoleDescriptionAddEdit.getText());
            		           		
	            	if (mode == "add"){	            		
            		
	            		int newRoleID = 0;
	            		
	            		try {
	            			newRoleID = daRole.insert(role);	            			
	            			if (newRoleID == -1){
	            				MessageDialog.openError(getShell(), getResourceString("Name_Exists_Error_Title"), getResourceString("Name_Exists_Error_Message"));
	            				textRoleName.setFocus();
	            				return;
	            			}
	            		} 
	            		catch (XPathParseException e) {
	            			MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
	            			close();
	            		} catch (XPathEvalException e) {
	            			MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
	            			close();
	            		} catch (NavException e) {
	            			MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
	            			close();			
	            		} 
	            		catch (ModifyException e) {
	            			MessageDialog.openError(getShell(), getResourceString("Modify_Exception_Title"), getResourceString("Modify_Exception_Message"));
	            			close();			
	            		}catch (TranscodeException e) {
	            			MessageDialog.openError(getShell(), getResourceString("Transcode_Exception_Title"), getResourceString("Transcode_Exception_Message"));
	            			close();
	            		} catch (IOException e) {
	            			MessageDialog.openError(getShell(), getResourceString("IO_Exception_Title"), getResourceString("IO_Exception_Message"));
	            			close();
	            		}
	            		
	            		role.setRoleID(newRoleID);
	            		roles.add(role);
	            		
	                	comboRoleName.add(role.getRoleName());
	                	comboRoleName.setData(role.getRoleName(), newRoleID);	                	
	                	comboRoleName.setText(role.getRoleName()); 
	                	
	                	
		                MessageDialog.openInformation(getShell(), getResourceString("Inform_Save_Title"), getResourceString("Inform_Save_Message"));

	                	
	            		int patternID = role.getPatternID();
	            		String patternName = getPatternName(patternID);
	            		String patternCategoryName = getPatternCategoryName(patternID);
		                
		                String pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName + "/" + comboRoleName.getText();
	                	File file = new File(pathName);
	                	boolean success;
						success = file.mkdir();

	                    if (!success) {
	                        // Directory was not successfully created
	            			MessageDialog.openError(getShell(), getResourceString("Directory_Creation_Failed_Title"), getResourceString("Directory_Creation_Failed_Message"));
	                    }
	            	}
	            	
	            	else if (mode == "edit"){
	            		
	            		role.setRoleID(Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString()));

	            		try {
	            			if (!daRole.update(role)){
    	            			MessageDialog.openError(getShell(), getResourceString("Name_Exists_Error_Title"), getResourceString("Name_Exists_Error_Message"));
    	            			textRoleName.setFocus();
    	            			return;
	            			}
	            		} 
	            		catch (XPathParseException e) {
	            			MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
	            			close();
	            		} catch (XPathEvalException e) {
	            			MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
	            			close();
	            		} catch (NavException e) {
	            			MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
	            			close();			
	            		} 
	            		catch (ModifyException e) {
	            			MessageDialog.openError(getShell(), getResourceString("Modify_Exception_Title"), getResourceString("Modify_Exception_Message"));
	            			close();			
	            		}catch (TranscodeException e) {
	            			MessageDialog.openError(getShell(), getResourceString("Transcode_Exception_Title"), getResourceString("Transcode_Exception_Message"));
	            			close();
	            		} catch (IOException e) {
	            			MessageDialog.openError(getShell(), getResourceString("IO_Exception_Title"), getResourceString("IO_Exception_Message"));
	            			close();
	            		}
	            		
	            		selectedIndex = comboRoleName.getSelectionIndex();
	            		int oldPatternID = roles.get(selectedIndex).getPatternID();
	            		String oldPatternName = getPatternName(oldPatternID);
	            		String oldPatternCategoryName = getPatternCategoryName(oldPatternID);

	            		
	            		String pathName = testDesignPatternsDirectory + "/" + oldPatternCategoryName + "/" + oldPatternName + "/" + comboRoleName.getText();
	            		
	            		// Directory with old name
	            	    File file1 = new File(pathName);
	            	    
	            		int newPatternID = role.getPatternID();
	            		String newPatternName = getPatternName(newPatternID);
	            		String newPatternCategoryName = getPatternCategoryName(newPatternID);
	            	    
	            	    pathName = testDesignPatternsDirectory + "/" + newPatternCategoryName + "/" + newPatternName + "/" + textRoleName.getText();
	            	    
	            	    // Directory with new name
	            	    File file2 = new File(pathName);
	            		
	            		comboRoleName.remove(selectedIndex);
	                	comboRoleName.add(role.getRoleName(), selectedIndex);                	
	                	comboRoleName.setData(role.getRoleName(), role.getRoleID());
	                	comboRoleName.select(selectedIndex);
	                	
	                	roles.remove(selectedIndex);
	                	roles.add(selectedIndex, role);               	
	                	
		                MessageDialog.openInformation(getShell(), getResourceString("Inform_Save_Title"), getResourceString("Inform_Save_Message"));

	                	// Rename Directory
	                    boolean success = file1.renameTo(file2);
	                    if (!success) {
	                        // Directory was not successfully renamed
	            			MessageDialog.openError(getShell(), getResourceString("Directory_Rename_Failed_Title"), getResourceString("Directory_Rename_Failed_Message"));
	                    }
	            	}
		            mode = "view";
		            handleView();
    			}		
            }
        });
    }
    
    /**
     * Creates a button that deletes an existing role.
     */
    private void createButtonDeleteRole() {
    	buttonDeleteRole = new Button(optionsGroup, SWT.PUSH);
    	buttonDeleteRole.setText("Delete");
    	buttonDeleteRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonDeleteRole.addSelectionListener(new SelectionAdapter() {
            
    		public void widgetSelected(SelectionEvent arg0) {
                
    			if (comboRoleName.getItemCount()==0) return;
            	
            	boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Delete_Title"), getResourceString("Confirm_Delete_Message"));
                
	        	if (answer){
            		
	        		DARole daRole = new DARole();
	        		
            		selectedIndex = comboRoleName.getSelectionIndex();           		
            		int roleID = roles.get(selectedIndex).getRoleID();
	        		
	        		try {
            			if (!daRole.delete(roleID)){
	            			MessageDialog.openError(getShell(), getResourceString("Item_Linked_Error_Title"), getResourceString("Item_Linked_Error_Message"));
	            			comboRoleName.setFocus();
	            			return;
            			}
	        		} 
            		catch (XPathParseException e) {
            			MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
	            		close();
	            	} catch (XPathEvalException e) {
	            		MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
	           			close();
	           		} catch (NavException e) {
	           			MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
	           			close();			
	           		} 
	           		catch (ModifyException e) {
	           			MessageDialog.openError(getShell(), getResourceString("Modify_Exception_Title"), getResourceString("Modify_Exception_Message"));
	           			close();			
	           		}catch (TranscodeException e) {
            			MessageDialog.openError(getShell(), getResourceString("Transcode_Exception_Title"), getResourceString("Transcode_Exception_Message"));
            			close();
            		} catch (IOException e) {
            			MessageDialog.openError(getShell(), getResourceString("IO_Exception_Title"), getResourceString("IO_Exception_Message"));
            			close();
            		}
	        		
            		int patternID = roles.get(selectedIndex).getPatternID();
            		String patternName = getPatternName(patternID);
            		String patternCategoryName = getPatternCategoryName(patternID);
            		
            		String pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName + "/" + comboRoleName.getText();
            		File file = new File(pathName);
            		
            		int selectedIndex = comboRoleName.getSelectionIndex();
            		
            		comboRoleName.remove(selectedIndex);
	        		roles.remove(selectedIndex);
	        		
	        		if (selectedIndex == 0) selectedIndex++;
	        		
	        		if (comboRoleName.getItemCount()==0){
	        			comboPatternName.removeAll();
	        			textRoleDescription.setText("");
	        		
	        		}
	        		else {
	        			comboRoleName.setText (roles.get(selectedIndex-1).getRoleName());
	        			fillComboPatternName(roles.get(selectedIndex-1).getPatternID());
	        			comboPatternName.select(0);
	            		textRoleDescription.setText(roles.get(selectedIndex-1).getRoleDescription());
	        		}
	             
                    MessageDialog.openInformation(getShell(), getResourceString("Inform_Delete_Title"), getResourceString("Inform_Delete_Message"));

                	// Delete an empty directory
                	boolean success = file.delete();
                    if (!success) {
                        // Directory was not successfully deleted
            			MessageDialog.openError(getShell(), getResourceString("Directory_Deletion_Failed_Title"), getResourceString("Directory_Deletion_Failed_Message"));
                    }
                    
                    comboRoleName.setFocus();
            	}	
            }
        });
    }
    
    /**
     * Creates a button that cancels the existing agent role.
     */
    private void createButtonCancelRole() {
        buttonCancelRole = new Button(optionsGroup, SWT.PUSH);
        buttonCancelRole.setText("Cancel");
        buttonCancelRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonCancelRole.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	if (mode == "add" || mode == "edit"){
                	mode = "view";
                	handleView();
                }
                else
                	close();
           }
        });
    }
    
	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {

	}
}