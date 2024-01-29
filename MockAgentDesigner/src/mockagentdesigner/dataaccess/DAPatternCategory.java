package mockagentdesigner.dataaccess;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import mockagentdesigner.Activator;
import mockagentdesigner.classes.PatternCategory;

//xpath without name space

public class DAPatternCategory
{	
	File file;
	String fileName;
	
	public DAPatternCategory() {
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path("repository/PatternCategories.xml"); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
		URL fileUrl = null;
		try {
		fileUrl = FileLocator.toFileURL(url);
		}
		catch (IOException e) {
		// Will happen if the file cannot be read for some reason
		e.printStackTrace();
		}
		file = new File(fileUrl.getPath());	
		fileName = file.getPath();
	}
	
	public ArrayList<PatternCategory> GetPatternCategories_ByPatternCategoryID(int patternCategoryID) throws XPathParseException, XPathEvalException, NavException {		   
		
	   ArrayList<PatternCategory> patternCategories = new ArrayList<PatternCategory>();	   
	   VTDGen vg = new VTDGen();

       AutoPilot ap0 = new AutoPilot();
       AutoPilot ap1 = new AutoPilot();
       AutoPilot ap2 = new AutoPilot();
       AutoPilot ap3 = new AutoPilot();
        
       ap0.selectXPath("/PatternCategories/PatternCategory[PatternCategoryID = " + patternCategoryID + " or " + patternCategoryID + " = 0]");
       ap1.selectXPath("PatternCategoryID"); 
       ap2.selectXPath("PatternCategoryName"); 
       ap3.selectXPath("PatternCategoryDescription");
        
       if (vg.parseFile(fileName, false)){
            
    	   	VTDNav vn = vg.getNav();
    	   	
            ap0.bind(vn);
            ap1.bind(vn);
            ap2.bind(vn);
            ap3.bind(vn);

            while(ap0.evalXPath()!=-1){
            	PatternCategory patternCategory = new PatternCategory();
            	patternCategory.setPatternCategoryID((int)ap1.evalXPathToNumber());
            	patternCategory.setPatternCategoryName(ap2.evalXPathToString());
            	patternCategory.setPatternCategoryDescription(ap3.evalXPathToString());
               
            	patternCategories.add(patternCategory);
            }
            ap0.resetXPath();
            
        }
        return patternCategories;
                    	
	}	
	
	public boolean update(PatternCategory patternCategory) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException  {        
    				
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap0 = new AutoPilot();	    
	    
	    AutoPilot ap1 = new AutoPilot();  
	    AutoPilot ap2 = new AutoPilot();
	    AutoPilot ap3 = new AutoPilot();	    
	       
	    ap0.selectXPath("/PatternCategories/PatternCategory[PatternCategoryID != " + patternCategory.getPatternCategoryID() + " and PatternCategoryName = '" + patternCategory.getPatternCategoryName() + "' ]");
	    
	    ap1.selectXPath("/PatternCategories/PatternCategory[PatternCategoryID = " + patternCategory.getPatternCategoryID() + " ]");
	    ap2.selectXPath("PatternCategoryName"); 
	    ap3.selectXPath("PatternCategoryDescription"); 
	       
	    if (vg.parseFile(fileName, false)){
        	
	    	VTDNav vn = vg.getNav();
        
	    	ap0.bind(vn);
	    	
	    	ap1.bind(vn);
	    	ap2.bind(vn);
            ap3.bind(vn);

            
			if (ap0.evalXPath() !=-1) return false;

            XMLModifier xm = new XMLModifier(vn);
			
			while(ap1.evalXPath()!=-1){

	            vn.push();	            
	            ap2.evalXPath();
	            xm.updateToken (vn.getText(), patternCategory.getPatternCategoryName());
	            ap2.resetXPath();	            
	            vn.pop();
	            
	            vn.push();
	            ap3.evalXPath();
	            xm.updateToken (vn.getText(), patternCategory.getPatternCategoryDescription());
	            ap3.resetXPath();	            
	            vn.pop();				
			}
			
	        ap1.resetXPath();
	        xm.output(fileName);
        }
	    return true;
    }
    
	public int insert(PatternCategory patternCategory) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException {        
		
		VTDGen vg = new VTDGen();
		
		int newPatternCategoryID = 0;
		
		AutoPilot ap0 = new AutoPilot();
		AutoPilot ap = new AutoPilot();
		
	    ap0.selectXPath("/PatternCategories/PatternCategory[PatternCategoryName = '" + patternCategory.getPatternCategoryName() + "' ]");
		ap.selectXPath("PatternCategoryID");
		    
	    if (vg.parseFile(fileName, false)){
        	
	    	VTDNav vn = vg.getNav();
	    	
	    	ap0.bind(vn);
	    	ap.bind(vn);
	    	
	    	if (ap0.evalXPath() !=-1) return -1;
	    	
	    	XMLModifier xm =  new XMLModifier(vn);
	    	vn.toElement(VTDNav.ROOT);
	    	if (vn.toElement(VTDNav.LC)){
	    		vn.push();
	    		ap.evalXPath();
	    		newPatternCategoryID = vn.parseInt(vn.getText()) + 1;
	            ap.resetXPath();	            
	            vn.pop();
	            xm.insertAfterElement("\n\t<PatternCategory> \n\t\t<PatternCategoryID>" + newPatternCategoryID + "</PatternCategoryID>" +
	    				"\n\t\t<PatternCategoryName>" + patternCategory.getPatternCategoryName() + "</PatternCategoryName>" +
	    				"\n\t\t<PatternCategoryDescription>" + patternCategory.getPatternCategoryDescription() + "</PatternCategoryDescription> \n\t</PatternCategory>");
	    	}
	    	else{
	    		newPatternCategoryID = 1;
	    		xm.insertAfterHead("\n\t<PatternCategory> \n\t\t<PatternCategoryID>" + newPatternCategoryID + "</PatternCategoryID>" +
	    				"\n\t\t<PatternCategoryName>" + patternCategory.getPatternCategoryName() + "</PatternCategoryName>" +
	    				"\n\t\t<PatternCategoryDescription>" + patternCategory.getPatternCategoryDescription() + "</PatternCategoryDescription> \n\t</PatternCategory>");
	    	}
	        xm.output(fileName);
        }
		return newPatternCategoryID;                        
    }
	
	public boolean delete(int patternCategoryID) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException{        
		
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap = new AutoPilot();
	       
        ap.selectXPath("/PatternCategories/PatternCategory[PatternCategoryID = " + patternCategoryID + " ]");
	    
        DAPattern daPattern = new DAPattern();
        
        if (daPattern.GetPatterns_ByPatternCategoryID(patternCategoryID).size()>=1)
        	return false;
        	
	    if (vg.parseFile(fileName, false)){
        	
	    	VTDNav vn = vg.getNav();
            ap.bind(vn);
         
			XMLModifier xm = new XMLModifier(vn);
			
			while(ap.evalXPath()!=-1){

                // remove the cursor element in the embedded VTDNav object 
                xm.remove();				
			}
			
	        ap.resetXPath();
	        xm.output(fileName);
        }
	    
	    return true;
    }
	
	
	private static void find(){
			
		DAPatternCategory daPatternCategory = new DAPatternCategory();
		
		ArrayList<PatternCategory> patternCategories = null;
		try {
			patternCategories = daPatternCategory.GetPatternCategories_ByPatternCategoryID(0);
		} catch (XPathParseException e) {
			System.out.println(" Exception during parsing XPath "+e);
			return;
		} catch (XPathEvalException e) {
			System.out.println(" Exception during evaluating XPath "+e);
			return;
		} catch (NavException e) {			
			System.out.println(" Exception during navigation "+e);
			return;
		}
        Iterator<PatternCategory> it = patternCategories.iterator();
        
        while(it.hasNext()){
        	PatternCategory patternCategory = it.next();            
            System.out.println("===================");
            System.out.println("PatternCategoryID:  ==>  "+patternCategory.getPatternCategoryID());
            System.out.println("PatternCategoryName: ==>  "+patternCategory.getPatternCategoryName());
            System.out.println("PatternCategoryDescription:  ==>  "+patternCategory.getPatternCategoryDescription());            
        }		
	}
	
	private static void edit(){
					
		DAPatternCategory daPatternCategory = new DAPatternCategory();
		
		PatternCategory patternCategory = new PatternCategory();
		
		patternCategory.setPatternCategoryID(1);
		patternCategory.setPatternCategoryName("updatedName");
		patternCategory.setPatternCategoryDescription("updatedDescription");
		
		
		try {
			if (!daPatternCategory.update(patternCategory)){
				System.out.println("Name is already existing. Please enter another name.");
			    return;
			}
		} catch (XPathParseException e) {
			System.out.println(" Exception during parsing XPath "+e);
		} catch (XPathEvalException e) {
			System.out.println(" Exception during evaluating XPath "+e);
		} catch (NavException e) {
			System.out.println(" Exception during navigation "+e);			
		} catch (ModifyException e) {
			System.out.println(" There is an exception condition during modification of XML "+e);
		} catch (TranscodeException e) {
			System.out.println(" There is an exception condition (in XMLModifier) for transcoding characters "+e);
		} catch (IOException e) {
			System.out.println(" An I/O exception produced by failed or interrupted I/O operations "+e);
		}
	}
	
	
	private static void add(){				
		
		DAPatternCategory daPatternCategory = new DAPatternCategory();
		
		PatternCategory patternCategory = new PatternCategory();

		patternCategory.setPatternCategoryName("patternCategoryName");
		patternCategory.setPatternCategoryDescription("patternCategoryDescription");		
		
		try {
			if (daPatternCategory.insert(patternCategory) == -1){
				System.out.println("Name is already existing. Please enter another name.");
				return;
			}
		} catch (XPathParseException e) {
			System.out.println(" Exception during parsing XPath "+e);
		} catch (XPathEvalException e) {
			System.out.println(" Exception during evaluating XPath "+e);
		} catch (NavException e) {
			System.out.println(" Exception during navigation "+e);			
		} catch (ModifyException e) {
			System.out.println(" There is an exception condition during modification of XML "+e);
		} catch (TranscodeException e) {
			System.out.println(" There is an exception condition (in XMLModifier) for transcoding characters "+e);
		} catch (IOException e) {
			System.out.println(" An I/O exception produced by failed or interrupted I/O operations "+e);
		}
	}
	
	private static void remove(){
					
		DAPatternCategory daPatternCategory = new DAPatternCategory();
		
		int patternCategoryID = 3;
		try {
			if (!daPatternCategory.delete(patternCategoryID)){
				System.out.println("Item can not be deleted. Please delete linked items first.");
			    return;
			}
		} catch (XPathParseException e) {
			System.out.println(" Exception during parsing XPath "+e);
		} catch (XPathEvalException e) {
			System.out.println(" Exception during evaluating XPath "+e);
		} catch (NavException e) {
			System.out.println(" Exception during navigation "+e);			
		} catch (ModifyException e) {
			System.out.println(" There is an exception condition during modification of XML "+e);
		} catch (TranscodeException e) {
			System.out.println(" There is an exception condition (in XMLModifier) for transcoding characters "+e);
		} catch (IOException e) {
			System.out.println(" An I/O exception produced by failed or interrupted I/O operations "+e);
		}
     }    
	
	public static void main(String[] args) {		
		edit();
		add();
		remove();
		find();
    }
}
