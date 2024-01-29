package mockagentdesigner.actions;

import java.io.File;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */

public class CreateNewFolderDialog extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites */
	Composite composite;
	Group newFolderNameGroup, optionsGroup;
	
	/* Declare the used widgets */
	Text textNewFolderName;
	Button buttonOk, buttonCancel;
		
	private static ResourceBundle resActions = ResourceBundle.getBundle("mockagentdesigner_actions");
	
	private String newFolderLocation;
	
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
	
	/**
	 * @param newFolderLocation the newFolderLocation to set
	 */
	public void setNewFolderLocation(String newFolderLocation) {
		this.newFolderLocation = newFolderLocation;
	}	
	
	/**
	 * The constructor.
	 */
	public CreateNewFolderDialog() {		
		super(null);
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
       
        shell.setText("Create New Folder Dialog");
        shell.setSize(420,150);
        shell.setLocation(100, 100);
        
    }
        
   
    /**
     * Overrides {@link org.eclipse.jface.window.Window#createContents(
     * org.eclipse.swt.widgets.Composite)}.
     */
    protected Control createContents(final Composite parent) {
    	composite = new Composite(parent, SWT.NONE);
    	composite.setLayout (new GridLayout (1, false));
		   	
		createNewFolderNameGroup();
        createOptionsGroup();
               
        return composite;
    }
    
	/**
	 * Creates the "NewFolderName" group.
	 */
    private void createNewFolderNameGroup () {		
	
		/*
		 * Create the "NewFolderName" group.  
		 * This is the group from which the user enters the NewFolderName.
		 */	

    	newFolderNameGroup = new Group (composite, SWT.NONE);
    	newFolderNameGroup.setLayout (new GridLayout (1, false));
    	newFolderNameGroup.setLayoutData (new GridData(GridData.FILL_BOTH));		
    	newFolderNameGroup.setText ("Enter New Folder Name");
		
		/* Create the widgets inside this group*/
        createTextNewFolderName();        
	}
	   
    /**
     * Creates a text box that represents the NewFolderName.
     */
    private void createTextNewFolderName() {	  			
    	textNewFolderName = new Text (newFolderNameGroup, SWT.BORDER);
    	textNewFolderName.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }         
    
    /**
	 * Creates the "Options" group.
	 */
	private void createOptionsGroup() {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can either confirm or cancel
		 */	

		optionsGroup = new Group (composite, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (2, true));
		optionsGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		optionsGroup.setText ("Choose Option");
		
		/* Create the widgets inside this group*/
        createButtonOk();
      	createButtonCancel();       
	}
	
    /**
     * Creates the OK button.
     */
    private void createButtonOk() {
        buttonOk = new Button(optionsGroup, SWT.PUSH);
        buttonOk.setText("Ok");
        buttonOk.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonOk.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	
    			if (textNewFolderName.getText().trim().equals(new String(""))){    			
	       			 MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
	       			 textNewFolderName.setFocus();
	       			 return;
       			}
            	
            	if (newFolderLocation!=null && !newFolderLocation.trim().equals(new String(""))){

            		// Create a directory; all ancestor directories must exist
                    boolean success = (new File(newFolderLocation + "/" + textNewFolderName.getText())).mkdir();
                    if (!success) {
                        // Directory creation failed
                    	MessageDialog.openError(getShell(), getResourceString("Directory_Creation_Failed_Title"), getResourceString("Directory_Creation_Failed_Message"));
                    }
            	}
            	
            	close();
            }
        });
    }
    
    /**
     * Creates the Cancel button.
     */
    private void createButtonCancel() {
    	buttonCancel = new Button(optionsGroup, SWT.PUSH);
    	buttonCancel.setText("Cancel");
    	buttonCancel.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonCancel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
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