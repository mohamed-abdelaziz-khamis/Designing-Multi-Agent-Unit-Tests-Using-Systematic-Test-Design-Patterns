package mockagentdesigner.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import mockagentdesigner.Activator;
import mockagentdesigner.classes.Pattern;
import mockagentdesigner.classes.PatternCategory;
import mockagentdesigner.classes.Role;
import mockagentdesigner.dataaccess.DAInteractingRole;
import mockagentdesigner.dataaccess.DAPattern;
import mockagentdesigner.dataaccess.DAPatternCategory;
import mockagentdesigner.dataaccess.DARole;
import mockagentdesigner.dataaccess.DATestDesignPattern;
import mockagentdesigner.fileviewer.FileViewer;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.Bundle;

import com.ximpleware.NavException;
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
public class MockAgentDesigner extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites */
	Composite composite;
	Group mockAgentTypeGroup, scenarioTypeGroup, mockAgentPackageGroup, optionsGroup;
	
	/*Declare the used widgets */
	Label labelDesignPatternCategory, labelDesignPattern, labelAgentRole, labelInteractingRole, labelGeneratedMockAgentPackage;
	Combo comboDesignPatternCategory, comboDesignPattern, comboAgentRole, comboInteractingRole;
	Button radioButtonSuccessfulScenario, radioButtonExceptionalScenario;
	Button buttonBrowse, buttonEditTestDesignPattern, buttonGenerate, buttonCancel;
	Text textGeneratedMockAgentPackage;
	String testDesignPatternsDirectory;
	
	private ArrayList<String> placeHolderKeys = null;
	private ArrayList<String> placeHolderValues = null;
	
	private String placeHolderKey;
	private String placeHolderValue; 
	
	private ArrayList<PatternCategory> patternCategories = null;
	private ArrayList<Pattern> patterns = null;	
	private ArrayList<Role> roles = null;
	private ArrayList<Role> interactingRoles = null;
	
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
	public MockAgentDesigner() {
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
        
        shell.setText("Mock Agent Designer");
        shell.setSize(500,350);
        shell.setLocation(100, 100);
        
    }
    
    /**
     * Overrides {@link org.eclipse.jface.window.Window#createContents(
     * org.eclipse.swt.widgets.Composite)}.
     */
    protected Control createContents(final Composite parent) {
        
    	composite = new Composite(parent, SWT.NONE);
    	composite.setLayout (new GridLayout (1, false));		
		             
        createMockAgentTypeGroup();    	
        createScenarioTypeGroup();        
        createMockAgentPackageGroup();               
        createOptionsGroup();
               
        return composite;
    }
    
	/**
	 * Creates the "MockAgentType" group.
	 */
	private void createMockAgentTypeGroup () {		
	
		/*
		 * Create the "MockAgentType" group.  
		 * This is the group from which the user chooses the required Mock Agent Type.
		 */	

		mockAgentTypeGroup = new Group (composite, SWT.NONE);
		mockAgentTypeGroup.setLayout (new GridLayout (2, false));
		mockAgentTypeGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		
		mockAgentTypeGroup.setText ("Choose Mock Agent Type");
		
		/* Create the widgets inside this group*/
	
        createLabelDesignPatternCategory();
        createComboDesignPatternCategory();
        
        createLabelDesignPattern();
        createComboDesignPattern();
        
        createLabelAgentRole();
        createComboAgentRole();
        
        createLabelInteractingRole();
        createComboInteractingRole();
	}
	
    private void fillComboPatternCategory(int patternCategoryID){
    	comboDesignPatternCategory.removeAll();
		    	
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
        	comboDesignPatternCategory.add(patternCategory.getPatternCategoryName());
        	comboDesignPatternCategory.setData (patternCategory.getPatternCategoryName(), patternCategory.getPatternCategoryID());
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
    
	private void fillComboDesignPattern(int patternCategoryID){
		comboDesignPattern.removeAll();
				
		DAPattern daPattern = new DAPattern();
		try {
			patterns = daPattern.GetPatterns_ByPatternCategoryID(patternCategoryID);
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
        	comboDesignPattern.add(pattern.getPatternName());
        	comboDesignPattern.setData (pattern.getPatternName(), pattern.getPatternID());
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
	
	private void fillComboAgentRole(int patternID){
		comboAgentRole.removeAll();
				
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
        	Role role = it.next();            
        	comboAgentRole.add(role.getRoleName());
        	comboAgentRole.setData(role.getRoleName(), role.getRoleID());
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
	
	private void fillComboInteractingRole(int roleID){
		comboInteractingRole.removeAll();
				
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
        	comboInteractingRole.add(interactingRole.getRoleName());
        	comboInteractingRole.setData(interactingRole.getRoleName(), interactingRole.getRoleID());
        }		
	}
	
    /**
     * Creates a label that represents the Design Pattern Category.
     */
    private void createLabelDesignPatternCategory() {       
		labelDesignPatternCategory = new Label (mockAgentTypeGroup, SWT.NONE);
		labelDesignPatternCategory.setText ("Design Pattern Category");
    }
    	
    /**
     * Creates a combo that represents the Design Pattern Category.
     */
    private void createComboDesignPatternCategory() {       
		comboDesignPatternCategory = new Combo (mockAgentTypeGroup, SWT.READ_ONLY);
		fillComboPatternCategory(0);
		if (comboDesignPatternCategory.getItemCount()>0)
			comboDesignPatternCategory.select(0);				
		comboDesignPatternCategory.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
		comboDesignPatternCategory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
        	
	        	patternCategoryID = Integer.parseInt(comboDesignPatternCategory.getData(comboDesignPatternCategory.getText()).toString());	        	
	        	fillComboDesignPattern(patternCategoryID);
	        	
	        	if (comboDesignPattern.getItemCount()>0){ 
		        	comboDesignPattern.select(0);
		        	
		        	patternID = Integer.parseInt(comboDesignPattern.getData(comboDesignPattern.getText()).toString());
		        	fillComboAgentRole(patternID);
		        	
		        	if (comboAgentRole.getItemCount()>0){
		        		comboAgentRole.select(0);		        	
		        		
		        		roleID =  Integer.parseInt(comboAgentRole.getData(comboAgentRole.getText()).toString());
		        		fillComboInteractingRole(roleID);
		        		
		        		if (comboInteractingRole.getItemCount()>0)
		        			comboInteractingRole.select(0);
		        	}else{
		           		comboInteractingRole.removeAll();
		        	}		           	
		        }else{
		        	comboAgentRole.removeAll();
	            	comboInteractingRole.removeAll();
	            }	
            }			
        });
    }
    
    /**
     * Creates a label that represents the Design Pattern.
     */
    private void createLabelDesignPattern() {       
		labelDesignPattern = new Label (mockAgentTypeGroup, SWT.NONE);
		labelDesignPattern.setText ("Design Pattern");
    }
    
	
    /**
     * Creates a combo that represents the Design Pattern.
     */
    private void createComboDesignPattern() {       
		comboDesignPattern = new Combo (mockAgentTypeGroup, SWT.READ_ONLY);
		if (comboDesignPatternCategory.getItemCount()>0){
        	
			patternCategoryID = Integer.parseInt(comboDesignPatternCategory.getData(comboDesignPatternCategory.getText()).toString());	        	
        	fillComboDesignPattern(patternCategoryID);
        	
        	if (comboDesignPattern.getItemCount()>0) 
	        	comboDesignPattern.select(0);
		}
		comboDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
		comboDesignPattern.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
	        	
	        	patternID = Integer.parseInt(comboDesignPattern.getData(comboDesignPattern.getText()).toString());
	        	fillComboAgentRole(patternID);
	        	
	        	if (comboAgentRole.getItemCount()>0){
	        		comboAgentRole.select(0);		        	
	        		
	        		roleID =  Integer.parseInt(comboAgentRole.getData(comboAgentRole.getText()).toString());
	        		fillComboInteractingRole(roleID);
	        		
	        		if (comboInteractingRole.getItemCount()>0)
	        			comboInteractingRole.select(0);
	        	}else{
	           		comboInteractingRole.removeAll();
	        	}		           	
            }
	    });
    }    
    
    /**
     * Creates a label that represents the Agent Role. 
     */
    private void createLabelAgentRole() {       
		labelAgentRole = new Label (mockAgentTypeGroup, SWT.NONE);
		labelAgentRole.setText ("Agent Under Test");		
    }
    
    /**
     * Creates a combo that represents the Agent Role. 
     */
    private void createComboAgentRole() {       
		comboAgentRole = new Combo (mockAgentTypeGroup, SWT.READ_ONLY);
		if (comboDesignPattern.getItemCount()>0){
			
			patternID = Integer.parseInt(comboDesignPattern.getData(comboDesignPattern.getText()).toString());
        	fillComboAgentRole(patternID);        	
        	
        	if (comboAgentRole.getItemCount()>0)
        		comboAgentRole.select(0);	
		}
		comboAgentRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
		comboAgentRole.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent arg0) {
	        	
        		roleID =  Integer.parseInt(comboAgentRole.getData(comboAgentRole.getText()).toString());
        		fillComboInteractingRole(roleID);
        		
        		if (comboInteractingRole.getItemCount()>0)
        			comboInteractingRole.select(0);
	            }
	    });
    }
       
    
    /**
     * Creates a label that represents the Interacting Role. 
     */
    private void createLabelInteractingRole() {       
		labelInteractingRole = new Label (mockAgentTypeGroup, SWT.NONE);
		labelInteractingRole.setText ("Mock Agent");
    }    
	
    /**
     * Creates a combo that represents the Interacting Role.
     */
    private void createComboInteractingRole() {       
		comboInteractingRole = new Combo (mockAgentTypeGroup, SWT.READ_ONLY);
    	if (comboAgentRole.getItemCount()>0){
    		
    		roleID =  Integer.parseInt(comboAgentRole.getData(comboAgentRole.getText()).toString());
    		fillComboInteractingRole(roleID);
    		
    		if (comboInteractingRole.getItemCount()>0)
    			comboInteractingRole.select(0);
    	}
    	comboInteractingRole.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
    
	/**
	 * Creates the "ScenarioType" group.
	 */
	void createScenarioTypeGroup() {		
	
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
    }
    
    /**
     * Creates a radio button that represents the Exceptional scenario.
     */
    private void createRadioButtonExceptionalScenario() {
        radioButtonExceptionalScenario = new Button(scenarioTypeGroup, SWT.RADIO);
        radioButtonExceptionalScenario.setText("Exceptional Scenario");      
    }
    
	/**
	 * Creates the "MockAgentPackage" group.
	 */
	void createMockAgentPackageGroup() {		
	
		/*
		 * Create the "MockAgentPackage" group.  
		 * This is the group from which the user determines the generated mock agent Package.
		 */	

		mockAgentPackageGroup = new Group (composite, SWT.NONE);
		mockAgentPackageGroup.setLayout (new GridLayout (3, false));
		mockAgentPackageGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		mockAgentPackageGroup.setText ("Choose Mock Agent Package");
		
		/* Create the widgets inside this group*/
        createLabelGeneratedMockAgentPackage();
        createTextGeneratedMockAgentPackage();
        createButtonBrowse();        
	}
	
    /**
     * Creates a label that represents the Generated Mock Agent Package.
     */
    private void createLabelGeneratedMockAgentPackage() {       
		labelGeneratedMockAgentPackage = new Label (mockAgentPackageGroup, SWT.NONE);
		labelGeneratedMockAgentPackage.setText ("Generated Mock Agent Package");
    }
    
    /**
     * Creates a text box that represents the Generated Mock Agent Package.
     */
    private void createTextGeneratedMockAgentPackage() {
    	textGeneratedMockAgentPackage = new Text (mockAgentPackageGroup, SWT.BORDER);    	
    	textGeneratedMockAgentPackage.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
       
    private void browseMockAgentPackage() {	
		
    	FileViewer application = new FileViewer();		
		Display display = Display.getCurrent();	
		Shell shell = application.open(display);
		
		while (! shell.isDisposed()) {
			if (! display.readAndDispatch()) 
				display.sleep();			
		}
		
		String packageName = application.getDirectoryPath();
		application.close();

    	if(packageName == null) return;
    	File file = new File(packageName);
    	if (!file.exists()) { 
		    MessageDialog.openError(getShell(), getResourceString("Directory_Does_Not_Exist_Title"), getResourceString("Directory_Does_Not_Exist_Message"));
    		return;
    	}
    	
    	textGeneratedMockAgentPackage.setText(packageName);
    }
    
    /**
     * Creates a button that opens a browse dialog.
     */
    private void createButtonBrowse() {
        buttonBrowse = new Button(mockAgentPackageGroup, SWT.PUSH);
        buttonBrowse.setText("Browse");
        buttonBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	browseMockAgentPackage();
            }
        });       
    }
    
	/**
	 * Creates the "Options" group.
	 */
	void createOptionsGroup() {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can either edit the design pattern placeholder values
		 * or generate the mock agent code.
		 */	

		optionsGroup = new Group (composite, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (3, true));
		optionsGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		optionsGroup.setText ("Choose Option");
		
		/* Create the widgets inside this group*/
        createButtonEditTestDesignPattern();
      	createButtonGenerate();
      	createButtonCancel();       
	}
	
	/*
	The StreamTokenizer can be used for simple parsing of a Resource file into tokens.
	*/ 
    public void resourceFileTokenizer(String resourceFileName){
	
    	try {
	        // Create the tokenizer to read from a file
	        FileReader rd = new FileReader(resourceFileName);
	        StreamTokenizer st = new StreamTokenizer(rd);
	    
	        // Prepare the tokenizer for Resource File-style tokenizing rules	               

	        // If whitespace is not to be discarded, make this call
	        st.ordinaryChars(0, ' ');
	        
	        st.wordChars(' ', '<');	        
	        st.wordChars('>', '~');	      
	        
	        // These calls caused comments to be discarded
	        st.slashSlashComments(true);
	        st.slashStarComments(true);
	        
	        boolean newKeyValuePair = true;
	        char ch = 0;
	        
	        // Parse the file
	        int token = st.nextToken();
	        placeHolderKeys = new ArrayList<String>();
	        while (token != StreamTokenizer.TT_EOF) {
	        	// End of file has not been reached yet
	            
	        	switch (token) {
		            case StreamTokenizer.TT_WORD:
		                // A word was found; the value is in sval		                
		                if (newKeyValuePair) {
		                	placeHolderKey = st.sval;
		                	newKeyValuePair = false;
		                }
		                else {
		                	if (placeHolderValue == null) placeHolderValue = st.sval;
		                	else placeHolderValue += ch + st.sval;
		                }
		                break;
		            case StreamTokenizer.TT_EOL:
		                // End of line character found
		            	if(placeHolderKey!=null && !placeHolderKey.trim().startsWith("#")) {
		            		if (placeHolderValue==null) placeHolderValue = " "; 
		            		//placeHoldersHashTable.put(placeHolderKey.trim(), placeHolderValue.trim());
		            		placeHolderKeys.add(placeHolderKey.trim());
		            	}
		            	placeHolderKey = null;
		            	placeHolderValue = null;
		            	newKeyValuePair = true;
		                break;
		            default:
		                // A regular character was found; the value is the token itself
		                ch = (char)st.ttype;
		                break;
		            }
		            token = st.nextToken();
	        }
        	if(placeHolderKey!=null && !placeHolderKey.trim().startsWith("#")) {
        		if (placeHolderValue==null) placeHolderValue = " "; 
        		//placeHoldersHashTable.put(placeHolderKey.trim(), placeHolderValue.trim());
        		placeHolderKeys.add(placeHolderKey.trim());
        	}
	        rd.close();
    	} catch (IOException e) {
			MessageDialog.openError(getShell(), getResourceString("IO_Exception_Title"), getResourceString("IO_Exception_Message"));
			close();
    	}
    }
    
    /**
     * Creates a button that opens a form for editing the Test Design Pattern.
     */
    private void createButtonEditTestDesignPattern() {
        buttonEditTestDesignPattern = new Button(optionsGroup, SWT.PUSH);
        buttonEditTestDesignPattern.setText("Edit Test Design Pattern");
        buttonEditTestDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonEditTestDesignPattern.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {            	           
                
    			if (comboInteractingRole.getText().trim().equals(new String(""))){    			
       			  	MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
       			  	comboInteractingRole.setFocus();
       			  	return;
    			}
            	
            	/***********************Begin: Building Resource File Package Path*****************/                
            	
                patternCategoryID = Integer.parseInt(comboDesignPatternCategory.getData(comboDesignPatternCategory.getText()).toString());
            	patternID = Integer.parseInt(comboDesignPattern.getData(comboDesignPattern.getText()).toString());
        		roleID =  Integer.parseInt(comboAgentRole.getData(comboAgentRole.getText()).toString());
        		interactingRoleID = Integer.parseInt(comboInteractingRole.getData(comboInteractingRole.getText()).toString());

       			patternCategoryName = getPatternCategoryName(patternCategoryID);
       			patternName = getPatternName(patternID);
       			roleName = getRoleName(roleID);
       			interactingRoleName = getRoleName(interactingRoleID);
            	       			
       			String resourceFileName  = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName + "/" + roleName + "/Mock" + interactingRoleName + "/Mock" + interactingRoleName + ".properties";
       			resourceFileTokenizer(resourceFileName);
       			                
                /***********************End: Building Resource File Package Path*******************/

                TestDesignPattern testDesignPattern = new TestDesignPattern();
                testDesignPattern.setPatternCategoryID(patternCategoryID);
                testDesignPattern.setPatternID(patternID);
                testDesignPattern.setRoleID(roleID);
                testDesignPattern.setInteractingRoleID(interactingRoleID);
                testDesignPattern.setPlaceHolderKeys(placeHolderKeys);
                testDesignPattern.open();
            }
        });
    }
    
    // Copies all files under srcDir to dstDir.
    // If dstDir does not exist, it will be created.
    public void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
    
            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                                     new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir.getPath(), dstDir.getPath());
        }
    }

	// Copies src file to dst file.
    // If the dst file does not exist, it is created
    private void copyFile(String srcFilename, String dstFilename) throws IOException{
            
    		// Create channel on the source
            FileChannel srcChannel = new FileInputStream(srcFilename).getChannel();
        
            // Create channel on the destination
            FileChannel dstChannel = new FileOutputStream(dstFilename).getChannel();
        
            // Copy file contents from source to destination
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        
            // Close the channels
            srcChannel.close();
            dstChannel.close();
    }
       
    private void setPlaceHolderValues(){    	
    	       
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
    
    private void createResourceFile(String resourceFilePath){

    	try{
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(resourceFilePath, false));
	
	    	Iterator<String> placeHolderKeysIterator =  placeHolderKeys.iterator();
	    	Iterator<String> placeHolderValuesIterator =  placeHolderValues.iterator();
				    	
		    while(placeHolderKeysIterator.hasNext()){
			   	bw.write(placeHolderKeysIterator.next() + " = " + placeHolderValuesIterator.next());
			   	bw.newLine();
			   	/*From the newLine() Javadoc: Write a line separator. 
			   	 * The line separator string is defined by the system property line.separator, 
			   	 * and is not necessarily a single newline ('\n') character.
			    */
			}
		
		    bw.close(); 
    	}
    	catch (IOException e) {
			MessageDialog.openError(getShell(), getResourceString("IO_Exception_Title"), getResourceString("IO_Exception_Message"));
			close();
    	}
    }
    
    /**
     * Creates a button that generates the Mock Agent Code.
     */
    private void createButtonGenerate() {
    	
        buttonGenerate = new Button(optionsGroup, SWT.PUSH);
        buttonGenerate.setText("Generate");
        buttonGenerate.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
		buttonGenerate.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
       
    			if (comboInteractingRole.getText().trim().equals(new String(""))){    			
       			  	MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
       			  	comboInteractingRole.setFocus();
       			  	return;
    			}
            	
            	boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Generate_Title"), getResourceString("Confirm_Generate_Message"));
                
	            if (answer){
            	       			
	       			/********************************Begin: MASUnitTesting Package Generation***********************************/
	            	//This Package represents the MASUnitTesting Framework and is independent from the generated Mock Agent Type
	            	
	            	File srcDir = new File(testDesignPatternsDirectory + "/MASUnitTesting");
	       			File dstDir = new File(textGeneratedMockAgentPackage.getText() + "/MASUnitTesting");
	       			
	                try{
	                	copyDirectory(srcDir, dstDir);                
	                }catch (IOException e) {
	                    // MAS Unit Testing Package was not successfully generated
	        			MessageDialog.openError(getShell(), getResourceString("MASUnitTesting_Package_Generation_Failed_Title"), getResourceString("MASUnitTesting_Package_Generation_Failed_Message"));
	        			textGeneratedMockAgentPackage.setFocus();
	        			return;
	        		}
	                /********************************End: MASUnitTesting Package Generation***********************************/
	                
	            	
	                /***********************Begin: Building Mock Agent Package Non-Existing Ancestor Directories*****************/
	                
	                patternCategoryID = Integer.parseInt(comboDesignPatternCategory.getData(comboDesignPatternCategory.getText()).toString());
	            	patternID = Integer.parseInt(comboDesignPattern.getData(comboDesignPattern.getText()).toString());
	        		roleID =  Integer.parseInt(comboAgentRole.getData(comboAgentRole.getText()).toString());
	        		interactingRoleID = Integer.parseInt(comboInteractingRole.getData(comboInteractingRole.getText()).toString());
	
	       			patternCategoryName = getPatternCategoryName(patternCategoryID);
	       			patternName = getPatternName(patternID);
	       			roleName = getRoleName(roleID);
	       			interactingRoleName = getRoleName(interactingRoleID);
	            	       			
	       			String srcMockAgentDirectoryPath  = testDesignPatternsDirectory + "/" + patternCategoryName + "/" 
											+ patternName + "/" + roleName + "/Mock" + interactingRoleName;
	       			      			
	       			String dstMockAgentDirectoryPath  = textGeneratedMockAgentPackage.getText() + "/" + patternCategoryName + "/" 
						+ patternName + "/" + roleName + "/Mock" + interactingRoleName;  
	       			
	                // Create the mock agent directory; all non-existent ancestor directories are automatically created
	                boolean success = (new File(dstMockAgentDirectoryPath)).mkdirs();
	                if (!success) {
	                    // Mock agent package was not successfully created
	        			MessageDialog.openError(getShell(), getResourceString("MockAgent_Package_Generation_Failed_Title"), getResourceString("MockAgent_Package_Generation_Failed_Message"));
	        			textGeneratedMockAgentPackage.setFocus();
	        			return;
	                }
	                
	                /***********************End: Building Mock Agent Package Non-Existing Ancestor Directories*******************/
	                
	                
	                /***************************Begin: Generate Design Pattern file*************************************/
	       			String srcDesignPatternPath = srcMockAgentDirectoryPath + "/Mock" + interactingRoleName + ".java";
	                String dstDesignPatternPath = dstMockAgentDirectoryPath + "/Mock" + interactingRoleName + ".java";	       			
                                              
	                try{
	                	copyFile(srcDesignPatternPath , dstDesignPatternPath);                
	                }catch (IOException e) {
	                    // File was not successfully generated
	        			MessageDialog.openError(getShell(), getResourceString("DesignPattern_File_Generation_Failed_Title"), getResourceString("DesignPattern_File_Generation_Failed_Message"));
	        			textGeneratedMockAgentPackage.setFocus();
	        			return;
	        		}
	                /***************************End: Generate Design Pattern file*************************************/
		                
	                /***************************Begin: Generate Resource file*******************************************/       			
	                String srcResourceFilePath = srcMockAgentDirectoryPath + "/Mock" + interactingRoleName + ".properties";
	                String dstResourceFilePath = dstMockAgentDirectoryPath + "/Mock" + interactingRoleName + ".properties";
	                	       			
	       			resourceFileTokenizer(srcResourceFilePath);
	       			
	       			if (placeHolderKeys!=null && placeHolderKeys.size()>0){
	       				setPlaceHolderValues();
	       				createResourceFile(srcResourceFilePath);
	       			}
	                
	                try{
	                	copyFile(srcResourceFilePath , dstResourceFilePath);                
	                }catch (IOException e) {
	                    // File was not successfully generated
	        			MessageDialog.openError(getShell(), getResourceString("Resource_File_Generation_Failed_Title"), getResourceString("Resource_File_Generation_Failed_Message"));
	        			textGeneratedMockAgentPackage.setFocus();
	        			return;
	        		}
	                /***************************End: Generate Resource file**********************************************/

	                
	                /***************************Begin: Generate Agent Under Test file*****************************************/                
	       			String srcAgentUnderTestPath = new File(srcMockAgentDirectoryPath).getParent() + "/" + roleName + ".java";
	                String dstAgentUnderTestPath = new File(dstMockAgentDirectoryPath).getParent() + "/" + roleName + ".java";	
	                       		                
	                try{
	                	copyFile(srcAgentUnderTestPath , dstAgentUnderTestPath);                
	                }catch (IOException e) {
	                    // File was not successfully generated
	        			MessageDialog.openError(getShell(), getResourceString("AUT_File_Generation_Failed_Title"), getResourceString("AUT_File_Generation_Failed_Message"));
	        			textGeneratedMockAgentPackage.setFocus();
	        			return;
	        		}
	                /***************************End: Generate Agent Under Test file****************************************/
	                
	                /***************************Begin: Generate AUT Test Case file*****************************************/	                
	       			String srcAUTTestCasePath = srcMockAgentDirectoryPath + "/" + roleName + "TestCase.java";
	                String dstAUTTestCasePath = dstMockAgentDirectoryPath + "/" + roleName + "TestCase.java";	
		                
	                try{
	                	copyFile(srcAUTTestCasePath , dstAUTTestCasePath);                
	                }catch (IOException e) {
	                    // File was not successfully generated
	        			MessageDialog.openError(getShell(), getResourceString("AUT_TestCase_File_Generation_Failed_Title"), getResourceString("AUT_TestCase_File_Generation_Failed_Message"));
	        			textGeneratedMockAgentPackage.setFocus();
	        			return;
	        		}
	                /***************************End: Generate AUT Test Case file****************************************/
	            
	            	MessageDialog.openInformation(getShell(), getResourceString("Inform_Generation_Title"), getResourceString("Inform_Generation_Message"));
            	}
            }
        });
    }
    
    /**
     * Creates a button that cancels the Mock Agent Designer Form.
     */
    private void createButtonCancel() {
        buttonCancel = new Button(optionsGroup, SWT.PUSH);
        buttonCancel.setText("Close");
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