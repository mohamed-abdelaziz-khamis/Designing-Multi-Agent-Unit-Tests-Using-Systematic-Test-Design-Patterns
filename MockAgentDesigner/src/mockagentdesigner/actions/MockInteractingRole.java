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
import mockagentdesigner.classes.InteractingRole;
import mockagentdesigner.classes.Pattern;
import mockagentdesigner.classes.PatternCategory;
import mockagentdesigner.classes.Role;
import mockagentdesigner.dataaccess.DAInteractingRole;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
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

public class MockInteractingRole extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites */
	Composite composite;
	Group interactingRoleGroup, optionsGroup;
	
	/*Declare the used widgets */
	Label labelDesignPatternName, labelRoleName, labelInteractingRoleName;
	Combo comboDesignPatternName, comboRoleName, comboInteractingRoleName ;
	Button buttonAddInteractingRole, buttonEditInteractingRole, buttonSaveInteractingRole, buttonDeleteInteractingRole, buttonCancelInteractingRole;
	
	ArrayList<Pattern> patterns = null;
	ArrayList<Role> roles = null;
	ArrayList<Role> interactingRoles = null;
	ArrayList<Role> nonInteractingRoles = null;
	
	int patternID;
	int roleID;		
	int oldInteractingRoleID, newInteractingRoleID;
		
	String patternCategoryName;
	String patternName;
	String roleName;
	String oldInteractingRoleName, newInteractingRoleName;
	
	String mode = "view";
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
	public MockInteractingRole() {
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
        
        shell.setText("Mock Interacting Role");
        shell.setSize(400, 200);
        shell.setLocation(100, 100);
        
    }
    
    private void handleView(){    	
    	if (mode == "view"){
    		   		
    		buttonAddInteractingRole.setEnabled(true);
    		buttonEditInteractingRole.setEnabled(true);
    		buttonSaveInteractingRole.setEnabled(false);
    		buttonDeleteInteractingRole.setEnabled(true);
    		
    		comboInteractingRoleName.setFocus();
    	}
    	else if (mode == "add"){
    		buttonAddInteractingRole.setEnabled(false);
    		buttonEditInteractingRole.setEnabled(false);
    		buttonSaveInteractingRole.setEnabled(true);
    		buttonDeleteInteractingRole.setEnabled(false);
    	}
    	else if (mode == "edit"){    		   	   		
    		buttonAddInteractingRole.setEnabled(false);   		
    		buttonEditInteractingRole.setEnabled(false);
    		buttonSaveInteractingRole.setEnabled(true);
    		buttonDeleteInteractingRole.setEnabled(false);
    	}
    }
    
    /**
     * Overrides {@link org.eclipse.jface.window.Window#createContents(
     * org.eclipse.swt.widgets.Composite)}.
     */
    protected Control createContents(final Composite parent) {
        
    	composite = new Composite(parent, SWT.NONE);
    	composite.setLayout (new GridLayout (1, false));		
		             
        createInteractingRoleGroup();    	            
        createOptionsGroup();
               
        return composite;
    }
    
	/**
	 * Creates the "InteractingRole" group.
	 */
	private void createInteractingRoleGroup () {		
	
		/*
		 * Create the "InteractingRole" group.  
		 * This is the group from which the user chooses the required InteractingRole.
		 */	

		interactingRoleGroup = new Group (composite, SWT.NONE);
		interactingRoleGroup.setLayout (new GridLayout (2, false));
		interactingRoleGroup.setLayoutData (new GridData(GridData.FILL_BOTH));		
		interactingRoleGroup.setText ("Choose Mock Interacting Role");
		
		/* Create the widgets inside this group*/
	
        createLabelDesignPatternName();
        createComboDesignPatternName();

		createLabelRoleName();
        createComboRoleName();

        createLabelInteractingRoleName();
        createComboInteractingRoleName();       
        
	}

    /**
     * Creates a label that represents the Design Pattern Name.
     */
    private void createLabelDesignPatternName() {       
		labelDesignPatternName = new Label (interactingRoleGroup, SWT.NONE);
		labelDesignPatternName.setText ("Design Pattern Name");
    }
    
	private void fillComboDesignPatternName(int patternID){
		comboDesignPatternName.removeAll();
				
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
	    	comboDesignPatternName.add(pattern.getPatternName());
	    	comboDesignPatternName.setData (pattern.getPatternName(), pattern.getPatternID());
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
	
	private void fillComboRoleName(int patternID){
		comboRoleName.removeAll();
				
		DARole daRole = new DARole();
		try {
			roles = daRole.GetRoles_ByPatternID(patternID);
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
        	Role role = (Role) it.next();            
        	comboRoleName.add(role.getRoleName());
        	comboRoleName.setData (role.getRoleName(), role.getRoleID());
        }
		
	}
    
	private String getRoleName(int roleID){
				
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
		
		return roles.get(0).getRoleName();		
	}
	
	private void fillComboInteractingRoleName(int roleID){
		comboInteractingRoleName.removeAll();
				
		DAInteractingRole daInteractingRole = new DAInteractingRole();
		try {
			interactingRoles = daInteractingRole.GetInteractingRoles_ByRoleID(roleID);
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
		
        Iterator<Role> it = interactingRoles.iterator();
        
        while(it.hasNext()){
        	Role interactingRole = it.next();            
        	comboInteractingRoleName.add(interactingRole.getRoleName());
        	comboInteractingRoleName.setData(interactingRole.getRoleName(), interactingRole.getRoleID());
        }		
	}
	
	private void fillComboInteractingRoleNameInAddEditMode(int roleID){
		comboInteractingRoleName.removeAll();
				
		DAInteractingRole daInteractingRole = new DAInteractingRole();
		try {
			nonInteractingRoles = daInteractingRole.GetNonInteractingRoles_ByRoleID(roleID);
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
		
        Iterator<Role> it = nonInteractingRoles.iterator();
        
        while(it.hasNext()){
        	Role role = it.next();            
        	comboInteractingRoleName.add(role.getRoleName());
        	comboInteractingRoleName.setData(role.getRoleName(), role.getRoleID());
        }		
	}
	
    /**
     * Creates a combo that represents the Design Pattern Name.
     */
    private void createComboDesignPatternName() {       
		comboDesignPatternName = new Combo (interactingRoleGroup, SWT.READ_ONLY);		
		fillComboDesignPatternName(0);
		if (comboDesignPatternName.getItemCount()>0) 
			comboDesignPatternName.select(0);			
		comboDesignPatternName.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));		
		comboDesignPatternName.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	
       			patternID = Integer.parseInt(comboDesignPatternName.getData(comboDesignPatternName.getText()).toString());        		            	
            	fillComboRoleName(patternID);
            	
            	if (comboRoleName.getItemCount()>0){ 
            		
            		comboRoleName.select(0);           		
            		roleID =  Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());
	            	if (mode == "view")            		
	            			fillComboInteractingRoleName(roleID);	            	
	            	else             		
	            			fillComboInteractingRoleNameInAddEditMode(roleID);                	
        			if (comboInteractingRoleName.getItemCount()>0)
        				comboInteractingRoleName.select(0);
            	}
            	else comboInteractingRoleName.removeAll();
            }
        });
    }
    
    /**
     * Creates a label that represents the Role Name.
     */
    private void createLabelRoleName() {       
		labelRoleName = new Label (interactingRoleGroup, SWT.NONE);
		labelRoleName.setText ("Role Name");
    }
       
    /**
     * Creates a combo that represents the Role Name.
     */
    private void createComboRoleName() {       
		comboRoleName = new Combo (interactingRoleGroup, SWT.READ_ONLY);
		if (comboDesignPatternName.getItemCount()>0){
			patternID = Integer.parseInt(comboDesignPatternName.getData(comboDesignPatternName.getText()).toString());
			fillComboRoleName(patternID);
			if (comboRoleName.getItemCount()>0)	
				comboRoleName.select(0);
		}
    	comboRoleName.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));				
		comboRoleName.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {          	
            	
            	roleID =  Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());
            	if (mode == "view")
	            	fillComboInteractingRoleName(roleID);
            	else
                	fillComboInteractingRoleNameInAddEditMode(roleID);

    			if (comboInteractingRoleName.getItemCount()>0)
    				comboInteractingRoleName.select(0);
            }
        });
    }
   
    /**
     * Creates a label that represents the Interacting Role Name.
     */
    private void createLabelInteractingRoleName() {       
		labelInteractingRoleName = new Label (interactingRoleGroup, SWT.NONE);
		labelInteractingRoleName.setText ("Interacting Role Name");
    }
    
    /**
     * Creates a combo that represents the Interacting Role Name.
     */
    private void createComboInteractingRoleName() {       
		comboInteractingRoleName = new Combo (interactingRoleGroup, SWT.READ_ONLY);
		if (comboRoleName.getItemCount()>0){
			roleID =  Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());
			fillComboInteractingRoleName(roleID);
			if (comboInteractingRoleName.getItemCount()>0)
				comboInteractingRoleName.select(0);
		}
		comboInteractingRoleName.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
    
	
    /**
	 * Creates the "Options" group.
	*/
	private void createOptionsGroup() {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can either add, edit or delete
		 * the pattern category or close this form.
		 */	

		optionsGroup = new Group (composite, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (5, true));
		optionsGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		optionsGroup.setText ("Choose Option");
		
		/* Create the widgets inside this group*/
        createButtonAddInteractingRole();
      	createButtonEditInteractingRole();
      	createButtonSaveInteractingRole();
      	createButtonDeleteInteractingRole();
      	createButtonCancelInteractingRole();       
	}
	
    /**
     * Creates a button that adds a new interacting role.
     */
    private void createButtonAddInteractingRole() {
        buttonAddInteractingRole = new Button(optionsGroup, SWT.PUSH);
        buttonAddInteractingRole.setText("Add");
        buttonAddInteractingRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonAddInteractingRole.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	if (comboRoleName.getItemCount()>0){
            		roleID =  Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());
            		fillComboInteractingRoleNameInAddEditMode(roleID);
            	}
            	mode = "add";
            	handleView();
            }
        });
    }
    
    /**
     * Creates a button that edits an existing interacting role.
     */
    private void createButtonEditInteractingRole() {
    	buttonEditInteractingRole = new Button(optionsGroup, SWT.PUSH);
    	buttonEditInteractingRole.setText("Edit");
    	buttonEditInteractingRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonEditInteractingRole.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	
            	if (comboInteractingRoleName.getItemCount()==0) return;
            	
            	oldInteractingRoleID = Integer.parseInt(comboInteractingRoleName.getData(comboInteractingRoleName.getText()).toString());         	            	
          		roleID = Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());
          		
           		fillComboInteractingRoleNameInAddEditMode(roleID);
           		if (comboInteractingRoleName.getItemCount()>0)
           			comboInteractingRoleName.select(0);
            	
            	mode = "edit";
            	handleView();
            }
        });
    }
    
    /**
     * Creates a button that saves the existing interacting role.
     */
    private void createButtonSaveInteractingRole() {
    	
    	buttonSaveInteractingRole = new Button(optionsGroup, SWT.PUSH);
    	buttonSaveInteractingRole.setText("Save");
    	buttonSaveInteractingRole.setEnabled(false);
    	buttonSaveInteractingRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonSaveInteractingRole.addSelectionListener(new SelectionAdapter() {
    		
    		public void widgetSelected(SelectionEvent arg0) {
            	
    			if (comboInteractingRoleName.getText().trim().equals(new String(""))){    			
         			  MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
         			  comboInteractingRoleName.setFocus();
         			  return;
    			}
    			
    			boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Save_Title"), getResourceString("Confirm_Save_Message"));
                
	            if (answer){
            		
            		patternID = Integer.parseInt(comboDesignPatternName.getData(comboDesignPatternName.getText()).toString());
            		roleID =  Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());         		            		
            		newInteractingRoleID = Integer.parseInt(comboInteractingRoleName.getData(comboInteractingRoleName.getText()).toString());
           			
           			String patternCategoryName = getPatternCategoryName(patternID);
           			String patternName = getPatternName(patternID);
           			String roleName = getRoleName(roleID);
           			newInteractingRoleName = getRoleName(newInteractingRoleID);
           			
	            	DAInteractingRole daInteractingRole = new DAInteractingRole();
            		
            		InteractingRole interactingRole = new InteractingRole();
            		
           			interactingRole.setRoleID(roleID);
           			
                	if (mode == "add"){	            		

                		interactingRole.setInteractingRoleID(newInteractingRoleID);

                		try {
	            			daInteractingRole.insert(interactingRole);            			
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
	            		
            			
	            		fillComboInteractingRoleName(roleID);           			
                    	
            			comboInteractingRoleName.setText(newInteractingRoleName);
            				            		
	            		MessageDialog.openInformation(getShell(), getResourceString("Inform_Save_Title"), getResourceString("Inform_Save_Message"));

	                	String pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName 
	                					+ "/" + roleName + "/Mock" + newInteractingRoleName;
	                	
	                	File file = new File(pathName);
	                	boolean success;
						success = file.mkdir();

	                    if (!success) {
	                        // Directory was not successfully created
	            			MessageDialog.openError(getShell(), getResourceString("Directory_Creation_Failed_Title"), getResourceString("Directory_Creation_Failed_Message"));
	                    }
	            	}
	            	
	            	else if (mode == "edit"){
	            		
	            		interactingRole.setInteractingRoleID(oldInteractingRoleID);	            		            		
	            		
	            		try {
	            			daInteractingRole.update(interactingRole, newInteractingRoleID);
	            		 } catch (XPathParseException e) {
		            			MessageDialog.openError(getShell(), getResourceString("XPathParse_Exception_Title"), getResourceString("XPathParse_Exception_Message"));
		            			close();
		            	} catch (XPathEvalException e) {
		            			MessageDialog.openError(getShell(), getResourceString("XPathEval_Exception_Title"), getResourceString("XPathEval_Exception_Message"));
		            			close();
		            	} catch (NavException e) {
		            			MessageDialog.openError(getShell(), getResourceString("Nav_Exception_Title"), getResourceString("Nav_Exception_Message"));
		            			close();			
		            	} catch (ModifyException e) {
		            			MessageDialog.openError(getShell(), getResourceString("Modify_Exception_Title"), getResourceString("Modify_Exception_Message"));
		            			close();			
		            	}catch (TranscodeException e) {
		            			MessageDialog.openError(getShell(), getResourceString("Transcode_Exception_Title"), getResourceString("Transcode_Exception_Message"));
		            			close();
		            	} catch (IOException e) {
		            			MessageDialog.openError(getShell(), getResourceString("IO_Exception_Title"), getResourceString("IO_Exception_Message"));
		            			close();
		            	}
		            		
	            		fillComboInteractingRoleName(roleID);
	            		
	            		comboInteractingRoleName.setText(newInteractingRoleName); 
		            		
		            	MessageDialog.openInformation(getShell(), getResourceString("Inform_Save_Title"), getResourceString("Inform_Save_Message"));

		                 
		            	oldInteractingRoleName = getRoleName(oldInteractingRoleID);
		            	
		                String pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName 
        								+ "/" + roleName + "/Mock" + oldInteractingRoleName;

		                // Directory with old name
		            	File file1 = new File(pathName);
		            	    
		            	 pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName 
        								+ "/" + roleName + "/Mock" + newInteractingRoleName;
		            	    
		            	 // Directory with new name
		            	 File file2 = new File(pathName);
		            	    
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
    
    
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Creates a button that deletes an existing interacting role.
     */
    private void createButtonDeleteInteractingRole() {
    	buttonDeleteInteractingRole = new Button(optionsGroup, SWT.PUSH);
    	buttonDeleteInteractingRole.setText("Delete");
    	buttonDeleteInteractingRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonDeleteInteractingRole.addSelectionListener(new SelectionAdapter() {
    		
            public void widgetSelected(SelectionEvent arg0) {
                
            	if (comboInteractingRoleName.getItemCount()==0) return;
            	
            	boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Delete_Title"), getResourceString("Confirm_Delete_Message"));
                
	        	if (answer){
	        		        		
           			patternID = Integer.parseInt(comboDesignPatternName.getData(comboDesignPatternName.getText()).toString());
	        		roleID =  Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());
            		oldInteractingRoleID = Integer.parseInt(comboInteractingRoleName.getData(comboInteractingRoleName.getText()).toString());

           			String patternCategoryName = getPatternCategoryName(patternID);
           			String patternName = getPatternName(patternID);
           			String roleName = getRoleName(roleID);
           			String interactingRoleName = getRoleName(oldInteractingRoleID);
           			
                	DAInteractingRole daInteractingRole = new DAInteractingRole();
                	
	        		InteractingRole interactingRole = new InteractingRole();        		    	

           			interactingRole.setRoleID(roleID);
	        		interactingRole.setInteractingRoleID(oldInteractingRoleID);
	
	        		try {
	        			daInteractingRole.delete(interactingRole);	        			
	        		}catch (XPathParseException e) {
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
	        		
            		String pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName 
									+ "/" + roleName + "/Mock" + interactingRoleName;
            		
            		File file = new File(pathName);
            		
	            	int selectedIndex = comboInteractingRoleName.getSelectionIndex();            	
	        		comboInteractingRoleName.remove(selectedIndex);	        		
	        		
	        		if (selectedIndex==0) selectedIndex++;
	        		
	        		if (comboInteractingRoleName.getItemCount()>0)
	        			comboInteractingRoleName.select(selectedIndex-1);
	        		
	                MessageDialog.openInformation(getShell(), getResourceString("Inform_Delete_Title"), getResourceString("Inform_Delete_Message"));
	                
                	// Delete an empty directory
                	boolean success = deleteDir(file);
                    if (!success) {
                        // Directory was not successfully deleted
            			MessageDialog.openError(getShell(), getResourceString("Directory_Deletion_Failed_Title"), getResourceString("Directory_Deletion_Failed_Message"));
                    }
                    
                    comboInteractingRoleName.setFocus();
            	}	
            }
        });
    }
    
    /**
     * Creates a button that cancels the exiting interacting role.
     */
    private void createButtonCancelInteractingRole() {
        buttonCancelInteractingRole = new Button(optionsGroup, SWT.PUSH);
        buttonCancelInteractingRole.setText("Cancel");
        buttonCancelInteractingRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
		buttonCancelInteractingRole.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	if (mode == "add" || mode == "edit"){
            		if (comboRoleName.getItemCount()>0){
                		roleID =  Integer.parseInt(comboRoleName.getData(comboRoleName.getText()).toString());
                		fillComboInteractingRoleName(roleID);
                		if (comboInteractingRoleName.getItemCount()>0)
                			comboInteractingRoleName.select(0);
                	}
	            	mode = "view";
	            	handleView();
            	}
            	else{
            		close();
            	}
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