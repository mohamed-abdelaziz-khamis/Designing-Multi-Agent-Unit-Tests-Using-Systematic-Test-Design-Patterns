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
import mockagentdesigner.classes.PatternCategory;
import mockagentdesigner.dataaccess.DAPatternCategory;

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

public class DesignPatternCategory extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites*/
	Composite composite;
	Group patternCategoryGroup, patternCategoryNameGroup, patternCategoryDescriptionGroup, optionsGroup;
	
	/*Declare the used widgets */
	Label labelPatternCategoryName, labelPatternCategoryDescription;
	Combo comboPatternCategoryName;
	Text textPatternCategoryName, textPatternCategoryDescription, textPatternCategoryDescriptionAddEdit;
	Button buttonAddPatternCategory, buttonEditPatternCategory, buttonSavePatternCategory, buttonDeletePatternCategory, buttonCancelPatternCategory;
	ArrayList<PatternCategory> patternCategories = null;
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
	public DesignPatternCategory() {
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
       
        shell.setText("Design Pattern Category");
        shell.setSize(420,230);
        shell.setLocation(100, 100);
        
    }
        
    private void handleView(){
    	
    	if (mode == "view"){
    		
    		comboPatternCategoryName.setVisible(true);
    		textPatternCategoryName.setVisible(false);
    		
    		if (comboPatternCategoryName.getItemCount()>0){
    			selectedIndex = comboPatternCategoryName.getSelectionIndex();
    			textPatternCategoryDescription.setText(patternCategories.get(selectedIndex).getPatternCategoryDescription());
    		}else {
    			textPatternCategoryDescription.setText("");
    		}
    		
    		textPatternCategoryDescription.setVisible(true);
    		textPatternCategoryDescriptionAddEdit.setVisible(false);
    		
    		buttonAddPatternCategory.setEnabled(true);
    		buttonEditPatternCategory.setEnabled(true);
    		buttonSavePatternCategory.setEnabled(false);
    		buttonDeletePatternCategory.setEnabled(true);
    		
    		comboPatternCategoryName.setFocus();
    	}
    	
    	else if (mode == "add"){
    		
    		comboPatternCategoryName.setVisible(false);
    		textPatternCategoryName.setVisible(true);
    		textPatternCategoryName.setText("");

    		textPatternCategoryDescription.setVisible(false);
    		textPatternCategoryDescriptionAddEdit.setVisible(true);
    		textPatternCategoryDescriptionAddEdit.setText("");

    		buttonAddPatternCategory.setEnabled(false);
    		buttonEditPatternCategory.setEnabled(false);
    		buttonSavePatternCategory.setEnabled(true);
    		buttonDeletePatternCategory.setEnabled(false);
    	}
    	
    	else if (mode == "edit"){
    		
    		comboPatternCategoryName.setVisible(false);   		
    		textPatternCategoryName.setVisible(true);    		
    		textPatternCategoryName.setText(comboPatternCategoryName.getText());
    		
    		textPatternCategoryDescription.setVisible(false);
    		textPatternCategoryDescriptionAddEdit.setVisible(true);    		    		
    		textPatternCategoryDescriptionAddEdit.setText(textPatternCategoryDescription.getText());
    		
    		buttonAddPatternCategory.setEnabled(false);
    		buttonEditPatternCategory.setEnabled(false);
    		buttonSavePatternCategory.setEnabled(true);
    		buttonDeletePatternCategory.setEnabled(false);
    	}
    }
    
    /**
     * Overrides {@link org.eclipse.jface.window.Window#createContents(
     * org.eclipse.swt.widgets.Composite)}.
     */
    protected Control createContents(final Composite parent) {
    	composite = new Composite(parent, SWT.NONE);
    	composite.setLayout (new GridLayout (1, false));
		   	
		createPatternCategoryGroup();
        createOptionsGroup();
               
        return composite;
    }
    
	/**
	 * Creates the "PatternCategory" group.
	 */
    private void createPatternCategoryGroup () {		
	
		/*
		 * Create the "PatternCategory" group.  
		 * This is the group from which the user chooses the required PatternCategory.
		 */	

		patternCategoryGroup = new Group (composite, SWT.NONE);
		patternCategoryGroup.setLayout (new GridLayout (2, false));
		patternCategoryGroup.setLayoutData (new GridData(GridData.FILL_BOTH));		
		patternCategoryGroup.setText ("Choose Pattern Category");
		
		/* Create the widgets inside this group*/
	
        createLabelPatternCategoryName();        
       	createPatternCategoryNameGroup();     	
        createComboPatternCategoryName();
        createTextPatternCategoryName();
        
        createLabelPatternCategoryDescription();
        createPatternCategoryDescriptionGroup();      
		createTextPatternCategoryDescription();
		createTextPatternCategoryDescriptionAddEdit();		        
	}
	
    /**
     * Creates a label that represents the Pattern Category Name.
     */
    private void createLabelPatternCategoryName() {       
		labelPatternCategoryName = new Label (patternCategoryGroup, SWT.NONE);
		labelPatternCategoryName.setText ("Pattern Category Name");
    }
    
    private void createPatternCategoryNameGroup(){
		patternCategoryNameGroup = new Group (patternCategoryGroup, SWT.NONE);
		patternCategoryNameGroup.setLayout (new FormLayout ());		
	}
   
    
    private void fillComboPatternCategoryName(int patternCategoryID){
		comboPatternCategoryName.removeAll();
		    	
    	DAPatternCategory daPatternCategory = new DAPatternCategory();
		try {
			patternCategories = daPatternCategory.GetPatternCategories_ByPatternCategoryID(patternCategoryID);
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
		
        Iterator<PatternCategory> it = patternCategories.iterator();
                
        while(it.hasNext()){
        	PatternCategory patternCategory = it.next();            
        	comboPatternCategoryName.add(patternCategory.getPatternCategoryName());
        	comboPatternCategoryName.setData (patternCategory.getPatternCategoryName(), patternCategory.getPatternCategoryID());
        }
    }
    
    /**
     * Creates a combo that represents the Pattern Category Name.
     * @throws XPathEvalException 
     */
    private void createComboPatternCategoryName()  {       		
		comboPatternCategoryName = new Combo (patternCategoryNameGroup, SWT.READ_ONLY);
		fillComboPatternCategoryName(0);
		if (comboPatternCategoryName.getItemCount()>0) 
        	comboPatternCategoryName.select(0);
		comboPatternCategoryName.setLayoutData (new FormData(185,10));		
		comboPatternCategoryName.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	selectedIndex = comboPatternCategoryName.getSelectionIndex();
            	textPatternCategoryDescription.setText(patternCategories.get(selectedIndex).getPatternCategoryDescription());
            }
        });
    }
    
    /**
     * Creates a text box that represents the Pattern Category Name.
     */
    private void createTextPatternCategoryName() {	  			
    	textPatternCategoryName = new Text (patternCategoryNameGroup, SWT.BORDER);
    	textPatternCategoryName.setLayoutData (new FormData(200,15));
    }   
    
    /**
     * Creates a label that represents the Pattern Category Description.
     */
    private void createLabelPatternCategoryDescription() {       
		labelPatternCategoryDescription = new Label (patternCategoryGroup, SWT.NONE);
		labelPatternCategoryDescription.setText ("Pattern Category Description");
    }
    
	private void createPatternCategoryDescriptionGroup(){
		patternCategoryDescriptionGroup = new Group (patternCategoryGroup, SWT.NONE);
		patternCategoryDescriptionGroup.setLayout (new FormLayout ());		
	}
	
    /**
     * Creates a text box that represents the Pattern Category Description.
     */
    private void createTextPatternCategoryDescription() {    			
    	textPatternCategoryDescription = new Text (patternCategoryDescriptionGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP |SWT.READ_ONLY);		   
    	if (comboPatternCategoryName.getItemCount()>0)
    		textPatternCategoryDescription.setText (patternCategories.get(0).getPatternCategoryDescription());
    	textPatternCategoryDescription.setLayoutData (new FormData(200, 40));
    }    
	
    /**
     * Creates a text box that represents the Pattern Category Description in Add Edit Mode.
     */
    private void createTextPatternCategoryDescriptionAddEdit() {		
    	textPatternCategoryDescriptionAddEdit = new Text (patternCategoryDescriptionGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
    	textPatternCategoryDescriptionAddEdit.setText ("");
    	textPatternCategoryDescriptionAddEdit.setLayoutData (new FormData(200, 40));
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
        createButtonAddPatternCategory();
      	createButtonEditPatternCategory();
      	createButtonSavePatternCategory();
      	createButtonDeletePatternCategory();
      	createButtonCancelPatternCategory();       
	}
	
    /**
     * Creates a button that adds a new pattern category.
     */
    private void createButtonAddPatternCategory() {
        buttonAddPatternCategory = new Button(optionsGroup, SWT.PUSH);
        buttonAddPatternCategory.setText("Add");
        buttonAddPatternCategory.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonAddPatternCategory.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	mode = "add";
            	handleView();
            }
        });
    }
    
    /**
     * Creates a button that edits an existing pattern category.
     */
    private void createButtonEditPatternCategory() {
    	buttonEditPatternCategory = new Button(optionsGroup, SWT.PUSH);
    	buttonEditPatternCategory.setText("Edit");
    	buttonEditPatternCategory.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonEditPatternCategory.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	if (comboPatternCategoryName.getItemCount()==0) return;
            	mode = "edit";
            	handleView();
            }
        });
    }
    
    /**
     * Creates a button that saves the existing pattern category.
     */
    private void createButtonSavePatternCategory() {
    	
    	buttonSavePatternCategory = new Button(optionsGroup, SWT.PUSH);
    	buttonSavePatternCategory.setText("Save");
    	buttonSavePatternCategory.setEnabled(false);
    	buttonSavePatternCategory.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonSavePatternCategory.addSelectionListener(new SelectionAdapter() {
    		public void widgetSelected(SelectionEvent arg0) {
            	
    			if (textPatternCategoryName.getText().trim().equals(new String(""))){    			
    			 MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
    			 textPatternCategoryName.setFocus();
    			 return;
    			}
    			
                boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Save_Title"), getResourceString("Confirm_Save_Message"));
                
            	if (answer){
	                
            		DAPatternCategory daPatternCategory = new DAPatternCategory();
            		
            		PatternCategory patternCategory = new PatternCategory();
            		
            		patternCategory.setPatternCategoryName(textPatternCategoryName.getText());
            		patternCategory.setPatternCategoryDescription(textPatternCategoryDescriptionAddEdit.getText());
            		
            		if (mode == "add"){
	            		
	            		int newPatternCategoryID = 0;
	            		
	            		try {
	            			newPatternCategoryID = daPatternCategory.insert(patternCategory);	            			
	            			if (newPatternCategoryID == -1){
	            				MessageDialog.openError(getShell(), getResourceString("Name_Exists_Error_Title"), getResourceString("Name_Exists_Error_Message"));
	            				textPatternCategoryName.setFocus();
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
	            		
	            		patternCategory.setPatternCategoryID(newPatternCategoryID);
	            		patternCategories.add(patternCategory);
	            		
	                	comboPatternCategoryName.add(patternCategory.getPatternCategoryName());
	                	comboPatternCategoryName.setData(patternCategory.getPatternCategoryName(), newPatternCategoryID);	                	
	                	comboPatternCategoryName.setText(patternCategory.getPatternCategoryName()); 
	                		                	
		                MessageDialog.openInformation(getShell(), getResourceString("Inform_Save_Title"), getResourceString("Inform_Save_Message"));                
		                
	                	String pathName = testDesignPatternsDirectory + "/" + comboPatternCategoryName.getText();
	                	File file = new File(pathName);
	                	boolean success;
						success = file.mkdir();

	                    if (!success) {
	                        // Directory was not successfully created
	            			MessageDialog.openError(getShell(), getResourceString("Directory_Creation_Failed_Title"), getResourceString("Directory_Creation_Failed_Message"));
	                    }
            		}
	            	
	            	else if (mode == "edit"){
	            			            		
	            		patternCategory.setPatternCategoryID(Integer.parseInt(comboPatternCategoryName.getData(comboPatternCategoryName.getText()).toString()));
	            		
	            		try {
	            			if (!daPatternCategory.update(patternCategory)){
    	            			MessageDialog.openError(getShell(), getResourceString("Name_Exists_Error_Title"), getResourceString("Name_Exists_Error_Message"));
    	            			textPatternCategoryName.setFocus();
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
	            		
	            		selectedIndex = comboPatternCategoryName.getSelectionIndex();
	            		
	            		String pathName = testDesignPatternsDirectory + "/" + comboPatternCategoryName.getText();
	            		
	            		// Directory with old name
	            	    File file1 = new File(pathName);
	            	    
	            	    pathName = testDesignPatternsDirectory + "/" + textPatternCategoryName.getText();
	            	    
	            	    // Directory with new name
	            	    File file2 = new File(pathName);
	            			            	    
	            		comboPatternCategoryName.remove(selectedIndex);
	                	comboPatternCategoryName.add(patternCategory.getPatternCategoryName(), selectedIndex);                	
	                	comboPatternCategoryName.setData(patternCategory.getPatternCategoryName(), patternCategory.getPatternCategoryID());
	                	comboPatternCategoryName.select(selectedIndex);
	                	
	                	patternCategories.remove(selectedIndex);
	                	patternCategories.add(selectedIndex, patternCategory);
	                	                	
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
     * Creates a button that deletes an existing pattern category.
     */
    private void createButtonDeletePatternCategory() {
    	buttonDeletePatternCategory = new Button(optionsGroup, SWT.PUSH);
    	buttonDeletePatternCategory.setText("Delete");
    	buttonDeletePatternCategory.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonDeletePatternCategory.addSelectionListener(new SelectionAdapter() {
            
    		public void widgetSelected(SelectionEvent arg0) {
                
    			if (comboPatternCategoryName.getItemCount()==0) return;
    			
            	boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Delete_Title"), getResourceString("Confirm_Delete_Message"));
                
            	if (answer){
	        		
            		DAPatternCategory daPatternCategory = new DAPatternCategory();
	        		
            		selectedIndex = comboPatternCategoryName.getSelectionIndex();           		            		
            		int patternCategoryID = patternCategories.get(selectedIndex).getPatternCategoryID();
	        		
            		try {
            			if (!daPatternCategory.delete(patternCategoryID)){
	            			MessageDialog.openError(getShell(), getResourceString("Item_Linked_Error_Title"), getResourceString("Item_Linked_Error_Message"));
	            			comboPatternCategoryName.setFocus();
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
	        		
            		String pathName = testDesignPatternsDirectory + "/" + comboPatternCategoryName.getText();
            		File file = new File(pathName);
            		
	            	int selectedIndex = comboPatternCategoryName.getSelectionIndex();
	            	
	        		comboPatternCategoryName.remove(selectedIndex);	        		
	        		patternCategories.remove(selectedIndex);
	        		
	        		if (selectedIndex == 0) selectedIndex++;
	        		
	        		if (comboPatternCategoryName.getItemCount()==0){
	        			textPatternCategoryDescription.setText("");
	        		}
	        		else {
	        			comboPatternCategoryName.setText (patternCategories.get(selectedIndex-1).getPatternCategoryName()); 
	            		textPatternCategoryDescription.setText(patternCategories.get(selectedIndex-1).getPatternCategoryDescription());
	        		}

	                MessageDialog.openInformation(getShell(), getResourceString("Inform_Delete_Title"), getResourceString("Inform_Delete_Message"));

                	// Delete an empty directory
                	boolean success = file.delete();
                    if (!success) {
                        // Directory was not successfully deleted
            			MessageDialog.openError(getShell(), getResourceString("Directory_Deletion_Failed_Title"), getResourceString("Directory_Deletion_Failed_Message"));
                    }
                    
                    comboPatternCategoryName.setFocus();
            	}
    		}           
        });    	
    }
    
    /**
     * Creates a button that cancels the existing pattern category.
     */
    private void createButtonCancelPatternCategory() {
    	buttonCancelPatternCategory = new Button(optionsGroup, SWT.PUSH);
    	buttonCancelPatternCategory.setText("Cancel");
    	buttonCancelPatternCategory.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonCancelPatternCategory.addSelectionListener(new SelectionAdapter() {
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