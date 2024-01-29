package mockagentdesigner.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.eclipse.swt.widgets.FileDialog;
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
public class MockAgentTemplate extends ApplicationWindow implements IWorkbenchWindowActionDelegate {
	
	/* Common groups and composites */
	Composite composite;
	Group mockAgentTypeGroup, designPatternFilesGroup, optionsGroup;
	
	/*Declare the used widgets */
	Label labelDesignPatternCategory, labelDesignPattern, labelAgentRole, labelInteractingRole;
	Label labelDesignPatternPath, labelResourceFilePath, labelTestDesignPatternPath, labelAgentUnderTestPath, labelAUTTestCasePath;
	Combo comboDesignPatternCategory, comboDesignPattern, comboAgentRole, comboInteractingRole;
	Button buttonBrowseDesignPattern, buttonBrowseResourceFile, buttonBrowseTestDesignPattern, buttonBrowseAgentUnderTest, buttonBrowseAUTTestCase; 
	Button buttonCreateDesignPattern, buttonCancel;
	Text textDesignPatternPath, textResourceFilePath, textTestDesignPatternPath, textAgentUnderTestPath, textAUTTestCasePath;
	String testDesignPatternsDirectory;
	
	/*
	 * textDesignPatternPath: Path of the template file MockAgent.java that contains some place holders that have different values 
	 * in the successfull and exceptional scenarios
	 * 
	 * textResourceFilePath: Path of the resource file AUT_MockAgent.properties that contains values of the placeholders in the 
	 * template file
	 * 
	 * textTestDesignPatternPath: Path of the test design pattern file MockAgent.XML that contains the test values of the placeholders
	 * that exist in the resource file in the successfull case and a sample exceptional case
	 * 
	 * textAgentUnderTestPath: Path of the agent under test file BidderAgent.java
	 * 
	 * textAUTTestCasePath: Path of the test case file AuctioneerAgentTestCase.java
	 */
	
	ArrayList<PatternCategory> patternCategories = null;
	ArrayList<Pattern> patterns = null;	
	ArrayList<Role> roles = null;
	ArrayList<Role> interactingRoles = null;
	
	int patternCategoryID;
	int patternID;
	int roleID;		
	int interactingRoleID;
		
	String patternCategoryName;
	String patternName;
	String roleName;
	String interactingRoleName;
	
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
	public MockAgentTemplate() {
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
        
        shell.setText("Add Mock Agent Template");
        shell.setSize(500,400);
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
        createDesignPatternFilesGroup();                    
        createOptionsGroup();
               
        return composite;
    }
    
	/**
	 * Creates the "MockAgentType" group.
	 */
	void createMockAgentTypeGroup () {		
	
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
        	comboDesignPatternCategory.setData(patternCategory.getPatternCategoryName(), patternCategory.getPatternCategoryID());
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
        	Role interactingRole =  it.next();            
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
     * Creates a label that represents the Interacting Agent. 
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
	 * Creates the "DesignPatternFiles" group.
	 */
	private void createDesignPatternFilesGroup() {		
	
		/*
		 * Create the "DesignPatternFiles" group.  
		 * This is the group from which the user browses the design pattern files.
		 */	

		designPatternFilesGroup = new Group (composite, SWT.NONE);
		designPatternFilesGroup.setLayout (new GridLayout (3, false));
		designPatternFilesGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		designPatternFilesGroup.setText ("Browse Design Pattern Files");
		
		/* Create the widgets inside this group*/

		createLabelDesignPatternPath();
        createTextDesignPatternPath();
        createButtonBrowseDesignPattern();
        
		createLabelResourceFilePath();
        createTextResourceFilePath();
        createButtonBrowseResourceFile();        

		createLabelTestDesignPatternPath();
        createTextTestDesignPatternPath();
        createButtonBrowseTestDesignPattern();
        
		createLabelAgentUnderTestPath();
        createTextAgentUnderTestPath();
        createButtonBrowseAgentUnderTest();
        
		createLabelAUTTestCasePath();
        createTextAUTTestCasePath();
        createButtonBrowseAUTTestCase();

	}
	
    /**
     * Creates a label that represents the Design Pattern Path.
     */
    private void createLabelDesignPatternPath() {       
		labelDesignPatternPath = new Label (designPatternFilesGroup, SWT.NONE);
		labelDesignPatternPath.setText ("Design Pattern Path");
    }
    
    /**
     * Creates a text box that represents the Design Pattern Path.
     */
    private void createTextDesignPatternPath() {
    	textDesignPatternPath = new Text (designPatternFilesGroup, SWT.BORDER);    	
    	textDesignPatternPath.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
        
    private void browseDesignPattern() {	
    	FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

    	fileDialog.setFilterExtensions(new String[] {"*.java;", "*.*"});
    	fileDialog.setFilterNames(new String[] {getResourceString("Design_Pattern_Filter_Name") + " (*.java)", 
    											getResourceString("All_Filter_Name") + " (*.*)"});
    	String name = fileDialog.open();

    	if(name == null) return;
    	File file = new File(name);
    	if (!file.exists()) {
			MessageDialog.openError(getShell(), getResourceString("File_Does_Not_Exist_Title"), getResourceString("File_Does_Not_Exist_Message"));
    		return;
    	}
    	textDesignPatternPath.setText(name);
    }
    
    /**
     * Creates a button that opens a browse dialog for the Design Pattern.
     */
    private void createButtonBrowseDesignPattern() {
        buttonBrowseDesignPattern = new Button(designPatternFilesGroup, SWT.PUSH);
        buttonBrowseDesignPattern.setText("Browse");
        buttonBrowseDesignPattern.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	browseDesignPattern();
            }
        });       
    }
    
    /**
     * Creates a label that represents the Resource File Path.
     */
    private void createLabelResourceFilePath() {       
		labelResourceFilePath = new Label (designPatternFilesGroup, SWT.NONE);
		labelResourceFilePath.setText ("Resource File Path");
    }
    
    /**
     * Creates a text box that represents the Resource File Path.
     */
    private void createTextResourceFilePath() {
    	textResourceFilePath = new Text (designPatternFilesGroup, SWT.BORDER);    	
    	textResourceFilePath.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
        
    private void browseResourceFile() {	
    	FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

    	fileDialog.setFilterExtensions(new String[] {"*.properties;", "*.*"});
    	fileDialog.setFilterNames(new String[] {getResourceString("Resource_File_Filter_Name") + " (*.properties)", 
    											getResourceString("All_Filter_Name") + " (*.*)"});
    	String name = fileDialog.open();

    	if(name == null) return;
    	File file = new File(name);
    	if (!file.exists()) {
			MessageDialog.openError(getShell(), getResourceString("File_Does_Not_Exist_Title"), getResourceString("File_Does_Not_Exist_Message")); 
    		return;
    	}
    	textResourceFilePath.setText(name);
    }
    
    /**
     * Creates a button that opens a browse dialog for the Resource File.
     */
    private void createButtonBrowseResourceFile() {
        buttonBrowseResourceFile = new Button(designPatternFilesGroup, SWT.PUSH);
        buttonBrowseResourceFile.setText("Browse");
        buttonBrowseResourceFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	browseResourceFile();
            }
        });       
    }
    
    /**
     * Creates a label that represents the Test Design Pattern Path.
     */
    private void createLabelTestDesignPatternPath() {       
		labelTestDesignPatternPath = new Label (designPatternFilesGroup, SWT.NONE);
		labelTestDesignPatternPath.setText ("Test Design Pattern Path");
    }
    
    /**
     * Creates a text box that represents the Test Design Pattern Path.
     */
    private void createTextTestDesignPatternPath() {
    	textTestDesignPatternPath = new Text (designPatternFilesGroup, SWT.BORDER);    	
    	textTestDesignPatternPath.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
        
    private void browseTestDesignPattern() {	
    	FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

    	fileDialog.setFilterExtensions(new String[] {"*.xml;", "*.*"});
    	fileDialog.setFilterNames(new String[] {getResourceString("Test_Design_Pattern_Filter_Name") + " (*.xml)", 
    											getResourceString("All_Filter_Name") + " (*.*)"});
    	String name = fileDialog.open();

    	if(name == null) return;
    	File file = new File(name);
    	if (!file.exists()) {
			MessageDialog.openError(getShell(), getResourceString("File_Does_Not_Exist_Title"), getResourceString("File_Does_Not_Exist_Message"));
    		return;
    	}
    	textTestDesignPatternPath.setText(name);
    }
    
    /**
     * Creates a button that opens a browse dialog for the Test Design Pattern.
     */
    private void createButtonBrowseTestDesignPattern() {
        buttonBrowseTestDesignPattern = new Button(designPatternFilesGroup, SWT.PUSH);
        buttonBrowseTestDesignPattern.setText("Browse");
        buttonBrowseTestDesignPattern.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	browseTestDesignPattern();
            }
        });       
    }
    
    /**
     * Creates a label that represents the Agent Under Test Path.
     */
    private void createLabelAgentUnderTestPath() {       
		labelAgentUnderTestPath = new Label (designPatternFilesGroup, SWT.NONE);
		labelAgentUnderTestPath.setText ("Agent Under Test Path");
    }
    
    /**
     * Creates a text box that represents the Agent Under Test Path.
     */
    private void createTextAgentUnderTestPath() {
    	textAgentUnderTestPath = new Text (designPatternFilesGroup, SWT.BORDER);    	
    	textAgentUnderTestPath.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
        
    private void browseAgentUnderTest() {	
    	FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

    	fileDialog.setFilterExtensions(new String[] {"*.java;", "*.*"});
    	fileDialog.setFilterNames(new String[] {getResourceString("Agent_Under_Test_Filter_Name") + " (*.java)", 
    											getResourceString("All_Filter_Name") + " (*.*)"});
    	String name = fileDialog.open();

    	if(name == null) return;
    	File file = new File(name);
    	if (!file.exists()) {
			MessageDialog.openError(getShell(), getResourceString("File_Does_Not_Exist_Title"), getResourceString("File_Does_Not_Exist_Message"));
    		return;
    	}
    	textAgentUnderTestPath.setText(name);
    }
    
    /**
     * Creates a button that opens a browse dialog for the Agent Under Test.
     */
    private void createButtonBrowseAgentUnderTest() {
        buttonBrowseAgentUnderTest = new Button(designPatternFilesGroup, SWT.PUSH);
        buttonBrowseAgentUnderTest.setText("Browse");
        buttonBrowseAgentUnderTest.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	browseAgentUnderTest();
            }
        });       
    }
    
    /**
     * Creates a label that represents the AUT Test Case Path.
     */
    private void createLabelAUTTestCasePath() {       
		labelAUTTestCasePath = new Label (designPatternFilesGroup, SWT.NONE);
		labelAUTTestCasePath.setText ("AUT Test Case Path");
    }
    
    /**
     * Creates a text box that represents the AUT Test Case Path.
     */
    private void createTextAUTTestCasePath() {
    	textAUTTestCasePath = new Text (designPatternFilesGroup, SWT.BORDER);    	
    	textAUTTestCasePath.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
        
    private void browseAUTTestCase() {	
    	FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

    	fileDialog.setFilterExtensions(new String[] {"*.java;", "*.*"});
    	fileDialog.setFilterNames(new String[] {getResourceString("AUT_Test_Case_Filter_Name") + " (*.java)", 
    											getResourceString("All_Filter_Name") + " (*.*)"});
    	String name = fileDialog.open();

    	if(name == null) return;
    	File file = new File(name);
    	if (!file.exists()) {
			MessageDialog.openError(getShell(), getResourceString("File_Does_Not_Exist_Title"), getResourceString("File_Does_Not_Exist_Message"));
    		return;
    	}
    	textAUTTestCasePath.setText(name);
    }
    
    /**
     * Creates a button that opens a browse dialog for the AUT Test Case.
     */
    private void createButtonBrowseAUTTestCase() {
        buttonBrowseAUTTestCase = new Button(designPatternFilesGroup, SWT.PUSH);
        buttonBrowseAUTTestCase.setText("Browse");
        buttonBrowseAUTTestCase.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	browseAUTTestCase();
            }
        });       
    }
    
	/**
	 * Creates the "Options" group.
	 */
	private void createOptionsGroup() {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can create the design pattern placeholder values.
		 */	

		optionsGroup = new Group (composite, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (3, false));
		optionsGroup.setLayoutData (new GridData(GridData.FILL_BOTH));
		optionsGroup.setText ("Choose Option");
		
		/* Create the widgets inside this group*/
        createButtonCreateDesignPattern();
      	createButtonCancel();       
	}
	
	
	// Copies src file to dst file.
    // If the dst file does not exist, it is created
    private void copy(String srcFilename, String dstFilename) throws IOException{
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
	
    /**
     * Creates a button that uploads the design pattern files.
     */
    private void createButtonCreateDesignPattern() {
        buttonCreateDesignPattern = new Button(optionsGroup, SWT.PUSH);
        buttonCreateDesignPattern.setText("Create Design Pattern");
        buttonCreateDesignPattern.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonCreateDesignPattern.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	
    			if (comboInteractingRole.getText().trim().equals(new String(""))){    			
       			  	MessageDialog.openError(getShell(), getResourceString("Invalid_Name_Title"), getResourceString("Invalid_Name_Message"));
       			  	comboInteractingRole.setFocus();
       			  	return;
    			}
            	
            	boolean answer = MessageDialog.openConfirm(getShell(), getResourceString("Confirm_Upload_Title"), getResourceString("Confirm_Upload_Message"));
                
	            if (answer){

	            	patternCategoryID = Integer.parseInt(comboDesignPatternCategory.getData(comboDesignPatternCategory.getText()).toString());
	            	patternID = Integer.parseInt(comboDesignPattern.getData(comboDesignPattern.getText()).toString());
	        		roleID =  Integer.parseInt(comboAgentRole.getData(comboAgentRole.getText()).toString());
	        		interactingRoleID = Integer.parseInt(comboInteractingRole.getData(comboInteractingRole.getText()).toString());
	
	       			patternCategoryName = getPatternCategoryName(patternCategoryID);
	       			patternName = getPatternName(patternID);
	       			roleName = getRoleName(roleID);
	       			interactingRoleName = getRoleName(interactingRoleID);
	            	
	       			String path  = testDesignPatternsDirectory + "/" + patternCategoryName + "/" + patternName 
					+ "/" + roleName + "/Mock" + interactingRoleName;                
	       			
	                /***************************Begin: Upload Design Pattern file*************************************/
	            	// File to be copied
	                File file = new File(textDesignPatternPath.getText());
	                String designPatternPath = path + "/" + file.getName();
	                                              
	                try{
	                	copy(file.getPath() , designPatternPath);                
	                }catch (IOException e) {
	                    // File was not successfully uploaded
	        			MessageDialog.openError(getShell(), getResourceString("File_Upload_Failed_Title"), getResourceString("File_Upload_Failed_Message"));
	        			textDesignPatternPath.setFocus();
	        			return;
	        		}
	                /***************************End: Upload Design Pattern file*************************************/
	                
	                /***************************Begin: Upload Resource file*****************************************/
	                // File to be copied
	                file = new File(textResourceFilePath.getText());
	                String resourceFilePath = path + "/" + file.getName();
	                       		                
	                try{
	                	copy(file.getPath(), resourceFilePath);                
	                }catch (IOException e) {
	                    // File was not successfully uploaded
	        			MessageDialog.openError(getShell(), getResourceString("File_Upload_Failed_Title"), getResourceString("File_Upload_Failed_Message"));
	        			textResourceFilePath.setFocus();
	        			return;
	        		}
	                /***************************End: Upload Resource file****************************************/
	                
	                /***************************Begin: Upload Test Design Pattern file*****************************************/
	                // File to be copied
	                file = new File(textTestDesignPatternPath.getText());
	                String testDesignPatternPath = path + "/" + file.getName();
	                       		                
	                try{
	                	copy(file.getPath(), testDesignPatternPath);                
	                }catch (IOException e) {
	                    // File was not successfully uploaded
	        			MessageDialog.openError(getShell(), getResourceString("File_Upload_Failed_Title"), getResourceString("File_Upload_Failed_Message"));
	        			textTestDesignPatternPath.setFocus();
	        			return;
	        		}
	                /***************************End: Upload Test Design Pattern file****************************************/
	                
	                /***************************Begin: Upload Agent Under Test file*****************************************/
	                // File to be copied
	                file = new File(textAgentUnderTestPath.getText());
	                String testAgentUnderTestPath = new File(path).getParent() + "/" + file.getName();
	                       		                
	                try{
	                	copy(file.getPath(), testAgentUnderTestPath);                
	                }catch (IOException e) {
	                    // File was not successfully uploaded
	        			MessageDialog.openError(getShell(), getResourceString("File_Upload_Failed_Title"), getResourceString("File_Upload_Failed_Message"));
	        			textAgentUnderTestPath.setFocus();
	        			return;
	        		}
	                /***************************End: Upload Agent Under Test file****************************************/
	                
	                /***************************Begin: Upload AUT Test Case file*****************************************/
	                // File to be copied
	                file = new File(textAUTTestCasePath.getText());
	                String testAUTTestCasePath = path + "/" + file.getName();
		                
	                try{
	                	copy(file.getPath(), testAUTTestCasePath);                
	                }catch (IOException e) {
	                    // File was not successfully uploaded
	        			MessageDialog.openError(getShell(), getResourceString("File_Upload_Failed_Title"), getResourceString("File_Upload_Failed_Message"));
	        			textAUTTestCasePath.setFocus();
	        			return;
	        		}
	                /***************************End: Upload AUT Test Case file****************************************/
	            
	            	MessageDialog.openInformation(getShell(), getResourceString("Inform_Upload_Title"), getResourceString("Inform_Upload_Message"));
            	}
            }
        });
    }
        
    /**
     * Creates a button that Cancel the Mock Agent Template Form.
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