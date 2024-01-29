package mockagentdesigner.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import mockagentdesigner.classes.Pattern;
import mockagentdesigner.classes.PatternCategory;
import mockagentdesigner.classes.Role;
import mockagentdesigner.dataaccess.DAPattern;
import mockagentdesigner.dataaccess.DAPatternCategory;
import mockagentdesigner.dataaccess.DARole;
import mockagentdesigner.dataaccess.DATestDesignPattern;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

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

public class TestDesignPattern extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites */
	Composite composite;
	Group scenarioTypeGroup, placeHoldersGroup, optionsGroup;
	
	/*Declare the used widgets */
	Button radioButtonSuccessfulScenario, radioButtonExceptionalScenario;
	Button buttonSaveTestDesignPattern, buttonCancelTestDesignPattern;
	
	private ArrayList<Label> lablePlaceHolders = new ArrayList<Label>();
	private ArrayList<Text> textPlaceHolders = new ArrayList<Text>();
	
	private Label lablePlaceHolder;
	private Text textPlaceHolder; 
	
	private ArrayList<String> placeHolderKeys = null;
	private ArrayList<String> placeHolderValues = null;
	private Hashtable<String, String> placeHoldersHashTable = new Hashtable<String, String>();
	
	private String placeHolderKey;
	private String placeHolderValue; 
		
	private ArrayList<PatternCategory> patternCategories = null;
	private ArrayList<Pattern> patterns = null;	
	private ArrayList<Role> roles = null;
		
	private int patternCategoryID;
	private int patternID;
	private int roleID;		
	private int interactingRoleID;
			
	private String patternCategoryName;
	private String patternName;
	private String roleName;
	private String interactingRoleName;
			
	private static ResourceBundle resActions = ResourceBundle.getBundle("mockagentdesigner_actions");	
	
	/**
	 * @param patternCategoryID the patternCategoryID to set
	 */
	public void setPatternCategoryID(int patternCategoryID) {
		this.patternCategoryID = patternCategoryID;
	}	
	/**
	 * @param patternID the patternID to set
	 */
	public void setPatternID(int patternID) {
		this.patternID = patternID;
	}
	/**
	 * @param roleID the roleID to set
	 */
	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}
	/**
	 * @param interactingRoleID the interactingRoleID to set
	 */
	public void setInteractingRoleID(int interactingRoleID) {
		this.interactingRoleID = interactingRoleID;
	}
	
	/**
	 * @param placeHolderKeys the placeHolderKeys to set
	 */
	public void setPlaceHolderKeys(ArrayList<String> placeHolderKeys) {
		this.placeHolderKeys = placeHolderKeys;
	}
	   	
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
	 * The constructor.
	 */
	public TestDesignPattern() {
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
        
        shell.setText("Test Design Pattern");
        
        if (placeHolderKeys!=null)
        	shell.setSize(450,placeHolderKeys.size()*25 + 150);
        else
        	shell.setSize(450, 150);
        
        shell.setLocation(150, 150);
        
    }
    
    /**
     * Overrides {@link org.eclipse.jface.window.Window#createContents(
     * org.eclipse.swt.widgets.Composite)}.
     */
    protected Control createContents(final Composite parent) {
        
    	composite = new Composite(parent, SWT.NONE);
    	composite.setLayout (new GridLayout (1, false));		
		             
        createScenarioTypeGroup();       
                
        createPlaceHoldersGroup();               
        createOptionsGroup();
               
        return composite;
    }
    
    private void setPlaceHolderValues(){    	
    	    	
    	patternCategoryName = getPatternCategoryName(patternCategoryID);
		patternName = getPatternName(patternID);
		roleName = getRoleName(roleID);
		interactingRoleName = getRoleName(interactingRoleID);
        
		DATestDesignPattern daTestDesignPattern = new DATestDesignPattern(patternCategoryName, patternName, roleName, interactingRoleName);
		
		String scenarioType = "SuccessfulScenario";

		if (radioButtonExceptionalScenario.getSelection())
			scenarioType = "ExceptionalScenario";
				
		try {
			placeHolderValues = daTestDesignPattern.GetPlaceHolderValues_ByScenarioType(scenarioType, placeHolderKeys);
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
	    
	/**
	 * Creates the "ScenarioType" group.
	 */
	private void createScenarioTypeGroup() {		
	
		/*
		 * Create the "ScenarioType" group.  
		 * This is the group from which the user determines wether the scenario is successful or exceptional.
		 */	

		scenarioTypeGroup = new Group (composite, SWT.NONE);
		scenarioTypeGroup.setLayout (new GridLayout (2, false));
		scenarioTypeGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		scenarioTypeGroup.setText ("Choose Scenario Type");
		
		/* Create the widgets inside this group*/
        createRadioButtonSuccessfulScenario();
        createRadioButtonExceptionalScenario();         
	}
	
    /**
     * Creates a radio button that represents the Successful scenario.
     */
    private void createRadioButtonSuccessfulScenario() {
        radioButtonSuccessfulScenario = new Button(scenarioTypeGroup, SWT.RADIO);
        radioButtonSuccessfulScenario.setText("Successful Scenario");
        radioButtonSuccessfulScenario.setSelection (true);
        radioButtonSuccessfulScenario.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	if (placeHolderKeys==null || placeHolderKeys.size()==0) return;
            	setPlaceHolderValues();        		
        		Iterator<Text> textIterator = textPlaceHolders.iterator();
        	    int index = 0;
        		while(textIterator.hasNext()){
        	    	textIterator.next().setText(placeHolderValues.get(index));       	    	
        	    	index++;
        	    }
            }
        });
    }
    
    /**
     * Creates a radio button that represents the Exceptional scenario.
     */
    private void createRadioButtonExceptionalScenario() {
        radioButtonExceptionalScenario = new Button(scenarioTypeGroup, SWT.RADIO);
        radioButtonExceptionalScenario.setText("Exceptional Scenario");
        radioButtonExceptionalScenario.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	if (placeHolderKeys==null || placeHolderKeys.size()==0) return;
            	setPlaceHolderValues();        		
        		Iterator<Text> textIterator = textPlaceHolders.iterator();
        	    int index = 0;
        		while(textIterator.hasNext()){
        	    	textIterator.next().setText(placeHolderValues.get(index));       	    	
        	    	index++;
        	    }
            }
        });
    }
    
	/**
	 * Creates the "PlaceHolders" group.
	 */
	void createPlaceHoldersGroup() {		
	
		/*
		 * Create the "PlaceHolders" group.  
		 * This is the group from which the user enters the values of the placeholders in the chosen scenario type.
		 */	

		placeHoldersGroup = new Group (composite, SWT.NONE);
		placeHoldersGroup.setLayout (new GridLayout (2, false));
		placeHoldersGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		placeHoldersGroup.setText ("Enter Placeholders Values");
		
		/* Create the widgets inside this group*/
		
		createPlaceHolders();
       
	}
	
    /**
     * Creates Labels for Placeholders Keys.
     * Creates Text Boxes for Placeholders Values.
     */
    private void createPlaceHolders() {       		
    	
    	if (placeHolderKeys==null || placeHolderKeys.size()==0) return;
		setPlaceHolderValues();	
		Iterator<String> it = placeHolderKeys.iterator();
	    int index = 0;
		while(it.hasNext()){
			
			placeHolderKey = it.next();
			
			lablePlaceHolder = new Label (placeHoldersGroup, SWT.NONE);
	     	lablePlaceHolder.setText (placeHolderKey);
	     	lablePlaceHolders.add(lablePlaceHolder);

	    	textPlaceHolder = new Text (placeHoldersGroup, SWT.BORDER);
	    	textPlaceHolder.setText (placeHolderValues.get(index));
	    	textPlaceHolder.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
	    	textPlaceHolders.add(textPlaceHolder);
	    
	    	index++;
	    }
    }
    
	/**
	 * Creates the "Options" group.
	 */
	void createOptionsGroup() {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can either save the placeholder values or cancel Test Design Pattern.
		 */	

		optionsGroup = new Group (composite, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (2, true));
		optionsGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		optionsGroup.setText ("Choose Option");
		
		/* Create the widgets inside this group*/
        createButtonSaveTestDesignPattern();
      	createButtonCancelTestDesignPattern();
	}
	

    /**
     * Creates a button that saves the placeholders values.
     */
    private void createButtonSaveTestDesignPattern() {
    	buttonSaveTestDesignPattern = new Button(optionsGroup, SWT.PUSH);
    	buttonSaveTestDesignPattern.setText("Save");
    	buttonSaveTestDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonSaveTestDesignPattern.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	
            	if (placeHolderKeys==null || placeHolderKeys.size()==0) return;
            	
        		boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Save_Title"), getResourceString("Confirm_Save_Message"));
                        		
            	if (answer){
            		
                	patternCategoryName = getPatternCategoryName(patternCategoryID);
            		patternName = getPatternName(patternID);
            		roleName = getRoleName(roleID);
            		interactingRoleName = getRoleName(interactingRoleID);
	                
            		DATestDesignPattern daTestDesignPattern = new DATestDesignPattern(patternCategoryName, patternName, roleName, interactingRoleName);
            		
            		String scenarioType = "SuccessfulScenario";
 
            		if (radioButtonExceptionalScenario.getSelection())
            			scenarioType = "ExceptionalScenario";

            	    Iterator<Label> it = lablePlaceHolders.iterator();
            		int index = 0;            		
            		while(it.hasNext()){            			
            			placeHolderKey = it.next().getText();
            			placeHolderValue = textPlaceHolders.get(index).getText();            			
            			placeHoldersHashTable.put(placeHolderKey, placeHolderValue);
            	    	index ++;
            	    }
	            	
            		try {
	            		daTestDesignPattern.update(scenarioType, placeHoldersHashTable);
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
		            MessageDialog.openInformation(getShell(), getResourceString("Inform_Save_Title"), getResourceString("Inform_Save_Message"));                	                	
            	}
            }
        });
    }    
     
    /**
     * Creates a button that cancels the Test Design Pattern Form.
     */
    private void createButtonCancelTestDesignPattern() {
    	buttonCancelTestDesignPattern = new Button(optionsGroup, SWT.PUSH);
    	buttonCancelTestDesignPattern.setText("Cancel");
    	buttonCancelTestDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    	buttonCancelTestDesignPattern.addSelectionListener(new SelectionAdapter() {
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