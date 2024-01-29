package mockagentdesigner.dataaccess;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import mockagentdesigner.Activator;
import mockagentdesigner.classes.Pattern;
import mockagentdesigner.classes.PatternCategory;

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

//xpath without name space

public class DAPattern
{
	File file;
	String fileName;
	
	public DAPattern() {
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path("repository/Patterns.xml"); //$NON-NLS-1$
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
	
	public ArrayList<Pattern> GetPatterns_ByPatternID(int patternID) throws XPathParseException, XPathEvalException, NavException {		   
		
	   ArrayList<Pattern> patterns = new ArrayList<Pattern>();	   
	   VTDGen vg = new VTDGen();

       AutoPilot ap0 = new AutoPilot();
       AutoPilot ap1 = new AutoPilot();
       AutoPilot ap2 = new AutoPilot();
       AutoPilot ap3 = new AutoPilot();
       AutoPilot ap4 = new AutoPilot();
        
       ap0.selectXPath("/Patterns/Pattern[PatternID = " + patternID + " or " + patternID + " = 0]");
       ap1.selectXPath("PatternID"); 
       ap2.selectXPath("PatternName"); 
       ap3.selectXPath("PatternCategoryID");
       ap4.selectXPath("PatternDescription");
       
       if (vg.parseFile(fileName, false)){
            
    	   	VTDNav vn = vg.getNav();
            
    	   	ap0.bind(vn);
            ap1.bind(vn);
            ap2.bind(vn);
            ap3.bind(vn);
            ap4.bind(vn);

            while(ap0.evalXPath()!=-1){
            	Pattern pattern = new Pattern();
            	pattern.setPatternID((int)ap1.evalXPathToNumber());
            	pattern.setPatternName(ap2.evalXPathToString());
            	pattern.setPatternCategoryID((int)ap3.evalXPathToNumber());
            	pattern.setPatternDescription(ap4.evalXPathToString());
               
            	patterns.add(pattern);
            }
            ap0.resetXPath();
            
        }
        return patterns;
                    	
	}	
	
	public ArrayList<Pattern> GetPatterns_ByPatternCategoryID(int patternCategoryID) throws XPathParseException, XPathEvalException, NavException {		   
		
		   ArrayList<Pattern> patterns = new ArrayList<Pattern>();	   
		   VTDGen vg = new VTDGen();

	       AutoPilot ap0 = new AutoPilot();
	       AutoPilot ap1 = new AutoPilot();
	       AutoPilot ap2 = new AutoPilot();
	       AutoPilot ap3 = new AutoPilot();
	       AutoPilot ap4 = new AutoPilot();
	        
	       ap0.selectXPath("/Patterns/Pattern[PatternCategoryID = " + patternCategoryID + " or " + patternCategoryID + " = 0]");
	       ap1.selectXPath("PatternID"); 
	       ap2.selectXPath("PatternName"); 
	       ap3.selectXPath("PatternCategoryID");
	       ap4.selectXPath("PatternDescription");
	        
	       if (vg.parseFile(fileName, false)){
	            
	    	   	VTDNav vn = vg.getNav();
	            
	    	   	ap0.bind(vn);
	            ap1.bind(vn);
	            ap2.bind(vn);
	            ap3.bind(vn);
	            ap4.bind(vn);

	            while(ap0.evalXPath()!=-1){	               
	            	Pattern pattern = new Pattern();
	            	pattern.setPatternID((int)ap1.evalXPathToNumber());
	            	pattern.setPatternName(ap2.evalXPathToString());
	            	pattern.setPatternCategoryID((int)ap3.evalXPathToNumber());
	            	pattern.setPatternDescription(ap4.evalXPathToString());
	            	
	            	patterns.add(pattern);
	            }
	            ap0.resetXPath();
	            
	        }
	        return patterns;	                    	
		}	
	
	public PatternCategory GetPatternCategory_ByPatternID(int patternID) throws XPathParseException, XPathEvalException, NavException {		   
		
		   PatternCategory patternCategory = new PatternCategory();
		   VTDGen vg = new VTDGen();

	       AutoPilot ap0 = new AutoPilot();
	       AutoPilot ap1 = new AutoPilot();
	        
	       ap0.selectXPath("/Patterns/Pattern[PatternID = " + patternID + " ]");
	       ap1.selectXPath("PatternCategoryID"); 
	       
	       DAPatternCategory daPatternCategory = new DAPatternCategory();       
	       if (vg.parseFile(fileName, false)){
	            
	    	   	VTDNav vn = vg.getNav();
	            
	    	   	ap0.bind(vn);
	            ap1.bind(vn);

	            while(ap0.evalXPath()!=-1){	            	
	            	patternCategory.setPatternCategoryID((int)ap1.evalXPathToNumber());               
	            	patternCategory.setPatternCategoryName(daPatternCategory.GetPatternCategories_ByPatternCategoryID(patternCategory.getPatternCategoryID()).get(0).getPatternCategoryName());
	            }
	            ap0.resetXPath();            
	        }	        
	        return patternCategory;                    	
		}
	
	public boolean update(Pattern pattern) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException  {        
    				
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap0 = new AutoPilot();
	    
	    AutoPilot ap1 = new AutoPilot();
	    AutoPilot ap2 = new AutoPilot();
	    AutoPilot ap3 = new AutoPilot();
	    AutoPilot ap4 = new AutoPilot();
	    
	    ap0.selectXPath("/Patterns/Pattern[PatternID != " + pattern.getPatternID() + " and PatternName = '" + pattern.getPatternName() + "' ]");
	    
        ap1.selectXPath("/Patterns/Pattern[PatternID = " + pattern.getPatternID() + " ]");	    
        ap2.selectXPath("PatternName"); 
        ap3.selectXPath("PatternCategoryID");
	    ap4.selectXPath("PatternDescription"); 
	       
	    if (vg.parseFile(fileName, false)){
        	
	    	VTDNav vn = vg.getNav();
	    	
            ap0.bind(vn);
            
            ap1.bind(vn);
            ap2.bind(vn);
            ap3.bind(vn);
            ap4.bind(vn);
            
            if (ap0.evalXPath() !=-1) return false;
            
            XMLModifier xm = new XMLModifier(vn);
			
			while(ap1.evalXPath()!=-1){

	            vn.push();	            
	            ap2.evalXPath();
	            xm.updateToken (vn.getText(), pattern.getPatternName());
	            ap2.resetXPath();	            
	            vn.pop();
	            
	            vn.push();	            
	            ap3.evalXPath();
	            xm.updateToken (vn.getText(), Integer.toString(pattern.getPatternCategoryID()));
	            ap3.resetXPath();	            
	            vn.pop();

	            vn.push();
	            ap4.evalXPath();
	            xm.updateToken (vn.getText(), pattern.getPatternDescription());
	            ap4.resetXPath();	            
	            vn.pop();				
			}
			
	        ap1.resetXPath();
	        xm.output(fileName);
        }
	    return true;
    }
    
	public int insert(Pattern pattern) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException {        
		
		VTDGen vg = new VTDGen();
		
		int newPatternID = 0;
		
		AutoPilot ap0 = new AutoPilot();
		AutoPilot ap = new AutoPilot();
		
	    ap0.selectXPath("/Patterns/Pattern[PatternName = '" + pattern.getPatternName() + "' ]");
		ap.selectXPath("PatternID"); 
		    
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
	    		newPatternID = vn.parseInt(vn.getText()) + 1;
	            ap.resetXPath();	            
	            vn.pop();	            
	            xm.insertAfterElement("\n\t<Pattern>\n\t\t<PatternID>" + newPatternID + "</PatternID>" +
	    				"\n\t\t<PatternName>" + pattern.getPatternName() + "</PatternName>" +
	    				"\n\t\t<PatternCategoryID>" + pattern.getPatternCategoryID() + "</PatternCategoryID>" +
	    				"\n\t\t<PatternDescription>" + pattern.getPatternDescription() + "</PatternDescription> \n\t</Pattern>");
	    	}
	    	else{
	    		newPatternID = 1;
	    		xm.insertAfterHead("\n\t<Pattern>\n\t\t<PatternID>" + newPatternID + "</PatternID>" +
	    				"\n\t\t<PatternName>" + pattern.getPatternName() + "</PatternName>" +
	    				"\n\t\t<PatternCategoryID>" + pattern.getPatternCategoryID() + "</PatternCategoryID>" +
	    				"\n\t\t<PatternDescription>" + pattern.getPatternDescription() + "</PatternDescription> \n\t</Pattern>");
	    	}
	        xm.output(fileName);
        }
		return newPatternID;                        
    }
	
	public boolean delete(int patternID) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException{        
		
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap = new AutoPilot();
	       
        ap.selectXPath("/Patterns/Pattern[PatternID = " + patternID + " ]");
	    
        DARole daRole = new DARole();
        
        if (daRole.GetRoles_ByPatternID(patternID).size()>=1)
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
		
		DAPattern daPattern = new DAPattern();
		
		ArrayList<Pattern> patterns = null;
		try {
			patterns = daPattern.GetPatterns_ByPatternID(0);
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
        
		Iterator<Pattern> it = patterns.iterator();
        
        while(it.hasNext()){
        	Pattern pattern =  it.next();            
            System.out.println("===================");
            System.out.println("PatternID:  ==>  "+pattern.getPatternID());
            System.out.println("PatternName: ==>  "+pattern.getPatternName());
            System.out.println("PatternCategoryID: ==>  "+pattern.getPatternCategoryID());
            System.out.println("PatternDescription:  ==>  "+pattern.getPatternDescription());            
        }		
	}
	
	private static void edit(){
		
		DAPattern daPattern = new DAPattern();
		
		Pattern pattern = new Pattern();
		
		pattern.setPatternID(1);
		pattern.setPatternName("updatedName");
		pattern.setPatternCategoryID(1);
		pattern.setPatternDescription("updatedDescription");
				
		try {
			if (!daPattern.update(pattern)){
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
		
		DAPattern daPattern = new DAPattern();
		
		Pattern pattern = new Pattern();

		pattern.setPatternName("patternName");
		pattern.setPatternCategoryID(1);
		pattern.setPatternDescription("patternDescription");		
		
		try {
			if (daPattern.insert(pattern) == -1){
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
		
		DAPattern daPattern = new DAPattern();
		
		int patternID = 11;
		try {
			if (!daPattern.delete(patternID)){
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
