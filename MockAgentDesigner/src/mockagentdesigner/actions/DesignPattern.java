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
import mockagentdesigner.dataaccess.DAPattern;
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
public class DesignPattern extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites */
	Composite composite;
	Group designPatternGroup, designPatternNameGroup, patternCategoryNameGroup, designPatternDescriptionGroup, optionsGroup;
	
	/*Declare the used widgets */
	Label labelDesignPatternName, labelPatternCategoryName,labelDesignPatternDescription;
	Combo comboDesignPatternName, comboPatternCategoryName;
	Text textDesignPatternName, textDesignPatternDescription, textDesignPatternDescriptionAddEdit;
	Button buttonAddDesignPattern, buttonEditDesignPattern, buttonSaveDesignPattern, buttonDeleteDesignPattern, buttonCancelDesignPattern;
	ArrayList<Pattern> patterns = null;
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
	public DesignPattern() {
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
        
        shell.setText("Design Pattern");
        shell.setSize(420,290);
        shell.setLocation(100, 100);
        
    }
    
    private void handleView(){
    	
    	if (mode == "view"){    		
    		
    		comboDesignPatternName.setVisible(true);
    		textDesignPatternName.setVisible(false);
    		    		    		
    		if (comboDesignPatternName.getItemCount()>0){
    			selectedIndex = comboDesignPatternName.getSelectionIndex();
    			fillComboPatternCategoryName(patterns.get(selectedIndex).getPatternCategoryID());
    			comboPatternCategoryName.select(0);
    			textDesignPatternDescription.setText(patterns.get(selectedIndex).getPatternDescription());
    		}else {
    			comboPatternCategoryName.removeAll();
    			textDesignPatternDescription.setText("");
    		}
    		
    		comboPatternCategoryName.setEnabled(false);
    		textDesignPatternDescription.setVisible(true);
    		textDesignPatternDescriptionAddEdit.setVisible(false);
    		
    		buttonAddDesignPattern.setEnabled(true);
    		buttonEditDesignPattern.setEnabled(true);
    		buttonSaveDesignPattern.setEnabled(false);
    		buttonDeleteDesignPattern.setEnabled(true);
    		
    		comboDesignPatternName.setFocus();
    	}
    	
    	else if (mode == "add"){
    		
    		comboDesignPatternName.setVisible(false);
    		textDesignPatternName.setVisible(true);
    		textDesignPatternName.setText("");
    		
    		comboPatternCategoryName.setEnabled(true);
    		fillComboPatternCategoryName(0);

    		textDesignPatternDescription.setVisible(false);
    		textDesignPatternDescriptionAddEdit.setVisible(true);
    		textDesignPatternDescriptionAddEdit.setText("");

    		buttonAddDesignPattern.setEnabled(false);
    		buttonEditDesignPattern.setEnabled(false);
    		buttonSaveDesignPattern.setEnabled(true);
    		buttonDeleteDesignPattern.setEnabled(false);
    	}
    	
    	else if (mode == "edit"){    		   	   		
    		
    		comboDesignPatternName.setVisible(false);   		
    		textDesignPatternName.setVisible(true);    		
    		textDesignPatternName.setText(comboDesignPatternName.getText());
    		
    		comboPatternCategoryName.setEnabled(true);
    		
			selectedIndex = comboDesignPatternName.getSelectionIndex();
        	fillComboPatternCategoryName(0);       	
    		comboPatternCategoryName.setText(getPatternCategoryName(patterns.get(selectedIndex).getPatternCategoryID()));
    		
    		textDesignPatternDescription.setVisible(false);
    		textDesignPatternDescriptionAddEdit.setVisible(true);    		    		
    		textDesignPatternDescriptionAddEdit.setText(textDesignPatternDescription.getText());
    		
    		buttonAddDesignPattern.setEnabled(false);
    		buttonEditDesignPattern.setEnabled(false);
    		buttonSaveDesignPattern.setEnabled(true);
    		buttonDeleteDesignPattern.setEnabled(false);
    	}
    }
    
    /**
     * Overrides {@link org.eclipse.jface.window.Window#createContents(
     * org.eclipse.swt.widgets.Composite)}.
     */
    protected Control createContents(final Composite parent) {        
    	composite = new Composite(parent, SWT.NONE);
    	composite.setLayout (new GridLayout (1, false));		
		             
        createDesignPatternGroup();    	            
        createOptionsGroup();
               
        return composite;
    }
    
	/**
	 * Creates the "DesignPattern" group.
	 */
    private void createDesignPatternGroup () {		
	
		/*
		 * Create the "DesignPattern" group.  
		 * This is the group from which the user chooses the required DesignPattern.
		 */	

		designPatternGroup = new Group (composite, SWT.NONE);
		designPatternGroup.setLayout (new GridLayout (2, false));
		designPatternGroup.setLayoutData (new GridData(GridData.FILL_BOTH));		
		designPatternGroup.setText ("Choose Design Pattern");
		
		/* Create the widgets inside this group*/
	
        createLabelDesignPatternName();
        createDesignPatternNameGroup();
        createComboDesignPatternName();
        createTextDesignPatternName();
        
        createLabelPatternCategoryName();
        createPatternCategoryNameGroup();
        createComboPatternCategoryName();
        
        createLabelDesignPatternDescription();
        createDesignPatternDescriptionGroup();
        createTextDesignPatternDescription();
        createTextDesignPatternDescriptionAddEdit();
	}
	
    /**
     * Creates a label that represents the Design Pattern Name.
     */
    private void createLabelDesignPatternName() {       
		labelDesignPatternName = new Label (designPatternGroup, SWT.NONE);
		labelDesignPatternName.setText ("Design Pattern Name");
    }
    
	private void createDesignPatternNameGroup(){
		designPatternNameGroup = new Group (designPatternGroup, SWT.NONE);
		designPatternNameGroup.setLayout (new FormLayout ());		
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
	    	comboDesignPatternName.setData(pattern.getPatternName(), pattern.getPatternID());
	    }
	    	    
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
        	comboPatternCategoryName.setData(patternCategory.getPatternCategoryName(), patternCategory.getPatternCategoryID());
        }

	}
	
	private String getPatternCategoryName(int patternCategoryID){
				
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
		
		return patternCategories.get(0).getPatternCategoryName();
	}
	
	/**
     * Creates a combo that represents the Design Pattern Name.
     */
    private void createComboDesignPatternName() {       
		comboDesignPatternName = new Combo (designPatternNameGroup, SWT.READ_ONLY);
		fillComboDesignPatternName(0);
		if (comboDesignPatternName.getItemCount()>0) 
			comboDesignPatternName.select(0);			
		comboDesignPatternName.setLayoutData (new FormData(195,10));		
		comboDesignPatternName.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	selectedIndex = comboDesignPatternName.getSelectionIndex();
            	fillComboPatternCategoryName(patterns.get(selectedIndex).getPatternCategoryID());
            	comboPatternCategoryName.select(0);
            	textDesignPatternDescription.setText(patterns.get(selectedIndex).getPatternDescription());
            }
        });
    }
    
    /**
     * Creates a text box that represents the Design Pattern Name.
     */
    private void createTextDesignPatternName() {	  			
    	textDesignPatternName = new Text(designPatternNameGroup, SWT.BORDER);
    	textDesignPatternName.setLayoutData(new FormData(210,15));
    }
    
    /**
     * Creates a label that represents the Pattern Category Name.
     */
    private void createLabelPatternCategoryName() {       
		labelPatternCategoryName = new Label (designPatternGroup, SWT.NONE);
		labelPatternCategoryName.setText ("Pattern Category Name");
    }
    
	private void createPatternCategoryNameGroup(){
		patternCategoryNameGroup = new Group (designPatternGroup, SWT.NONE);
		patternCategoryNameGroup.setLayout (new FormLayout ());		
	}	
	
    /**
     * Creates a combo that represents the Pattern Category Name.
     */
    private void createComboPatternCategoryName() {       
		comboPatternCategoryName = new Combo (patternCategoryNameGroup, SWT.READ_ONLY);
		if (comboDesignPatternName.getItemCount()>0){
			fillComboPatternCategoryName(patterns.get(0).getPatternCategoryID());
			comboPatternCategoryName.select(0);
		}
        comboPatternCategoryName.setEnabled(false);        
        comboPatternCategoryName.setLayoutData(new FormData(195,10));
    }
    
    /**
     * Creates a label that represents the Design Pattern Description.
     */
    private void createLabelDesignPatternDescription() {       
		labelDesignPatternDescription = new Label (designPatternGroup, SWT.NONE);
		labelDesignPatternDescription.setText ("Design Pattern Description");
    }
    
	private void createDesignPatternDescriptionGroup(){
		designPatternDescriptionGroup = new Group (designPatternGroup, SWT.NONE);
		designPatternDescriptionGroup.setLayout (new FormLayout ());		
	}
	
    /**
     * Creates a text box that represents the Design Pattern Description.
     */
    private void createTextDesignPatternDescription() {
    	textDesignPatternDescription = new Text (designPatternDescriptionGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP |SWT.READ_ONLY);
    	if (comboDesignPatternName.getItemCount()>0)
    		textDesignPatternDescription.setText(patterns.get(0).getPatternDescription());
    	textDesignPatternDescription.setLayoutData (new FormData(210, 60));
    }    
	
    /**
     * Creates a text box that represents the Design Pattern Description in Add Edit Mode.
     */
    private void createTextDesignPatternDescriptionAddEdit() {		
    	textDesignPatternDescriptionAddEdit = new Text (designPatternDescriptionGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
    	textDesignPatternDescriptionAddEdit.setText ("");
    	textDesignPatternDescriptionAddEdit.setLayoutData (new FormData(210, 60));
    }
    
    /**
	 * Creates the "Options" group.
	*/
    private void createOptionsGroup() {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can either add, edit or delete
		 * the design pattern or close this form.
		 */	

		optionsGroup = new Group (composite, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (5, true));
		optionsGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		optionsGroup.setText ("Choose Option");
		
		/* Create the widgets inside this group*/
        createButtonAddDesignPattern();
      	createButtonEditDesignPattern();
      	createButtonSaveDesignPattern();
      	createButtonDeleteDesignPattern();
      	createButtonCancelDesignPattern();       
	}
	
    /**
     * Creates a button that adds a new design pattern.
     */
    private void createButtonAddDesignPattern() {
        buttonAddDesignPattern = new Button(optionsGroup, SWT.PUSH);
        buttonAddDesignPattern.setText("Add");
        buttonAddDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonAddDesignPattern.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {            	
            	mode = "add";
            	handleView();
            }
        });
    }
       
    /**
     * Creates a button that edits an existing design pattern.
     */
    private void createButtonEditDesignPattern() {
    	buttonEditDesignPattern = new Button(optionsGroup, SWT.PUSH);
    	buttonEditDesignPattern.setText("Edit");
    	buttonEditDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonEditDesignPattern.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {            	
            	if (comboDesignPatternName.getItemCount()==0) return;
            	mode = "edit";
            	handleView();
            }
        });
    }
    
    /**
     * Creates a button that saves the existing design pattern.
     */
    private void createButtonSaveDesignPattern() {
    	
    	buttonSaveDesignPattern = new Button(optionsGroup, SWT.PUSH);
    	buttonSaveDesignPattern.setText("Save");
    	buttonSaveDesignPattern.setEnabled(false);
    	buttonSaveDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonSaveDesignPattern.addSelectionListener(new SelectionAdapter() {
    	
    		public void widgetSelected(SelectionEvent arg0) {
            	
    			if (textDesignPatternName.getText().trim().equals(new String(""))){    			
       			  MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
       			  textDesignPatternName.setFocus();
       			  return;
       			}
    			
    			if (comboPatternCategoryName.getText().trim().equals(new String(""))){    			
            	  MessageDialog.openError(getShell(), getResourceString("Invalid_Parent_Title"), getResourceString("Invalid_Parent_Message"));
            	  comboPatternCategoryName.setFocus();
            	  return;
            	}
    			
    			boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Save_Title"), getResourceString("Confirm_Save_Message"));
                
    			if (answer){
	            	
    				DAPattern daPattern = new DAPattern();
            		
            		Pattern pattern = new Pattern();

            		pattern.setPatternName(textDesignPatternName.getText());            		
            		pattern.setPatternCategoryID(Integer.parseInt(comboPatternCategoryName.getData(comboPatternCategoryName.getText()).toString()));             		            		            
            		pattern.setPatternDescription(textDesignPatternDescriptionAddEdit.getText());
            		
    				if (mode == "add"){
	            			            		
	            		int newPatternID = 0;
	            			            		
	            		try {	                			            			
	            			newPatternID = daPattern.insert(pattern);	            			
	            			if (newPatternID == -1){
	            				MessageDialog.openError(getShell(), getResourceString("Name_Exists_Error_Title"), getResourceString("Name_Exists_Error_Message"));
	            				textDesignPatternName.setFocus();
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
	            		
	            		pattern.setPatternID(newPatternID);
	                	patterns.add(pattern);
	                	
	                	comboDesignPatternName.add(pattern.getPatternName());
	                	comboDesignPatternName.setData(pattern.getPatternName(), newPatternID);
	                	comboDesignPatternName.setText(pattern.getPatternName());
	                	
	                    MessageDialog.openInformation(getShell(), getResourceString("Inform_Save_Title"), getResourceString("Inform_Save_Message"));

	            		int patternCategoryID = pattern.getPatternCategoryID();
	            		String patternCategoryName = getPatternCategoryName(patternCategoryID);
	                    
	                    String pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + comboDesignPatternName.getText();
	                	File file = new File(pathName);
	                	boolean success;
						success = file.mkdir();

	                    if (!success) {
	                        // Directory was not successfully created
	            			MessageDialog.openError(getShell(), getResourceString("Directory_Creation_Failed_Title"), getResourceString("Directory_Creation_Failed_Message"));
	                    }
	            	}
	            	
	            	else if (mode == "edit"){
	            		
	            		pattern.setPatternID(Integer.parseInt(comboDesignPatternName.getData(comboDesignPatternName.getText()).toString()));
	            		
	            		try {
	            			if (!daPattern.update(pattern)){
    	            			MessageDialog.openError(getShell(), getResourceString("Name_Exists_Error_Title"), getResourceString("Name_Exists_Error_Message"));
    	            			textDesignPatternName.setFocus();
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
	            		
	            		selectedIndex = comboDesignPatternName.getSelectionIndex();
	            		int oldPatternCategoryID = patterns.get(selectedIndex).getPatternCategoryID();
	            		String oldPatternCategoryName = getPatternCategoryName(oldPatternCategoryID);
	            		
	            		String pathName = testDesignPatternsDirectory + "/" + oldPatternCategoryName + "/" + comboDesignPatternName.getText();
	            		
	            		// Directory with old name
	            	    File file1 = new File(pathName);	            	    
	            	    	            		
	            		int newPatternCategoryID = pattern.getPatternCategoryID();
	            		String newPatternCategoryName = getPatternCategoryName(newPatternCategoryID);
	            	    
	            	    pathName = testDesignPatternsDirectory + "/" + newPatternCategoryName + "/" + textDesignPatternName.getText();
	            	    
	            	    // Directory with new name
	            	    File file2 = new File(pathName);
	            		
	            		comboDesignPatternName.remove(selectedIndex);
	                	comboDesignPatternName.add(pattern.getPatternName(), selectedIndex);                	
	                	comboDesignPatternName.setData(pattern.getPatternName(), pattern.getPatternID());
	                	comboDesignPatternName.select(selectedIndex);
	                	
	                	patterns.remove(selectedIndex);
	                	patterns.add(selectedIndex, pattern);
	                	 	                	
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
     * Creates a button that deletes an existing design pattern.
     */
    private void createButtonDeleteDesignPattern() {
    	buttonDeleteDesignPattern = new Button(optionsGroup, SWT.PUSH);
    	buttonDeleteDesignPattern.setText("Delete");
    	buttonDeleteDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonDeleteDesignPattern.addSelectionListener(new SelectionAdapter() {
            
    		public void widgetSelected(SelectionEvent arg0) {
                
    			if (comboDesignPatternName.getItemCount()==0) return;
            	
            	boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Delete_Title"), getResourceString("Confirm_Delete_Message"));
                
            	if (answer){
	        		
            		DAPattern daPattern = new DAPattern();
	        		
            		selectedIndex = comboDesignPatternName.getSelectionIndex();           		
            		int patternID = patterns.get(selectedIndex).getPatternID();
	        		
            		try {
            			if (!daPattern.delete(patternID)){
	            			MessageDialog.openError(getShell(), getResourceString("Item_Linked_Error_Title"), getResourceString("Item_Linked_Error_Message"));
	            			comboDesignPatternName.setFocus();
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
	        		
            		int patternCategoryID = patterns.get(selectedIndex).getPatternCategoryID();
            		String patternCategoryName = getPatternCategoryName(patternCategoryID);
            		
            		String pathName = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + comboDesignPatternName.getText();
            		File file = new File(pathName);
            		
	            	int selectedIndex = comboDesignPatternName.getSelectionIndex();
	        		
	            	comboDesignPatternName.remove(selectedIndex);	        		
	        		patterns.remove(selectedIndex);
	        		
	        		if (selectedIndex == 0) selectedIndex++;
	        		
	        		if (comboDesignPatternName.getItemCount()==0){
	        			comboPatternCategoryName.removeAll();
	        			textDesignPatternDescription.setText("");
	        		}	        		
	        		else {
	        			comboDesignPatternName.setText(patterns.get(selectedIndex-1).getPatternName());
	        			fillComboPatternCategoryName(patterns.get(selectedIndex-1).getPatternCategoryID());
	        			comboPatternCategoryName.select(0);
	            		textDesignPatternDescription.setText(patterns.get(selectedIndex-1).getPatternDescription());
	        		}
	                	        			        		
	        		MessageDialog.openInformation(getShell(), getResourceString("Inform_Delete_Title"), getResourceString("Inform_Delete_Message"));
	        		
                	// Delete an empty directory
                	boolean success = file.delete();
                    if (!success) {
                        // Directory was not successfully deleted
            			MessageDialog.openError(getShell(), getResourceString("Directory_Deletion_Failed_Title"), getResourceString("Directory_Deletion_Failed_Message"));
                    }
                    
                    comboDesignPatternName.setFocus();
            	}
            }
        });
    }
    
    /**
     * Creates a button that cancels the existing design pattern.
     */
    private void createButtonCancelDesignPattern() {
        buttonCancelDesignPattern = new Button(optionsGroup, SWT.PUSH);
        buttonCancelDesignPattern.setText("Cancel");
        buttonCancelDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonCancelDesignPattern.addSelectionListener(new SelectionAdapter() {
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