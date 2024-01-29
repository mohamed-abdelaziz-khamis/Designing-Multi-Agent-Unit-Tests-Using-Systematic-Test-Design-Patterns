package mockagentdesigner.dataaccess;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import mockagentdesigner.Activator;

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

public class DATestDesignPattern {

	File file;
	String fileName;
	
	public DATestDesignPattern(String patternCategoryName, String patternName, String roleName, String interactingRoleName) {
		
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path("testDesignPatterns/" + patternCategoryName + "/" + patternName + "/" + roleName + 
							"/Mock" + interactingRoleName + "/Mock" + interactingRoleName +".xml"); //$NON-NLS-1$
		
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
	
	public ArrayList<String> GetPlaceHolderValues_ByScenarioType(String scenarioType, ArrayList<String> placeHolderKeys) throws XPathParseException, XPathEvalException, NavException {		   
				   	   
		   VTDGen vg = new VTDGen();
       
		   ArrayList<String> placeHolderValues = new ArrayList<String>();
		   
		   AutoPilot ap0 = new AutoPilot();
		   ArrayList<AutoPilot> apList = new ArrayList<AutoPilot>();
		   AutoPilot ap = null;		   		          
	       
		   ap0.selectXPath("/TestDesignPattern/" + scenarioType);
		    
		   Iterator<String> it = placeHolderKeys.iterator();		
		   while(it.hasNext()){
				ap = new AutoPilot();
				ap.selectXPath(it.next());
				apList.add(ap);
		   }
			
	       if (vg.parseFile(fileName, false)){
	            
	    	   	VTDNav vn = vg.getNav();
	    	   	
		    	ap0.bind(vn);
		    	
		    	Iterator<AutoPilot> apIterator = apList.iterator();
	    		while(apIterator.hasNext()){
	    			apIterator.next().bind(vn);
	    	    }

	            while(ap0.evalXPath()!=-1){	            	
	            	apIterator = apList.iterator();
		    		while(apIterator.hasNext()){		            			           
			            placeHolderValues.add(apIterator.next().evalXPathToString());			            
		    	    }	    		
	            }
	            ap0.resetXPath();	            
	        }
	        return placeHolderValues;	                    
		}
	
	public void update(String scenarioType, Hashtable<String, String> placeHoldersHashTable) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException  {        
		
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap0 = new AutoPilot();	    
	    
	    ArrayList<AutoPilot> apList = new ArrayList<AutoPilot>();
	    AutoPilot ap = null;
	    	       
	    ap0.selectXPath("/TestDesignPattern/" + scenarioType);

	    Enumeration<String> e = placeHoldersHashTable.keys();
	    while(e.hasMoreElements()) {
	    	ap = new AutoPilot();
	    	ap.selectXPath(e.nextElement());
	    	apList.add(ap);
	     }
    	       
	    if (vg.parseFile(fileName, false)){
        	
	    	VTDNav vn = vg.getNav();
        
	    	ap0.bind(vn);
	    	
	    	Iterator<AutoPilot> apIterator = apList.iterator();
    		while(apIterator.hasNext()){
    			apIterator.next().bind(vn);
    	    }
            
            XMLModifier xm = new XMLModifier(vn);

            while(ap0.evalXPath()!=-1){
	    		
            	apIterator = apList.iterator();            	
            	Iterator<String> placeHolderValuesInterator = placeHoldersHashTable.values().iterator();
	    		while(apIterator.hasNext()){	
		            
		            ap = new AutoPilot();
		            ap = apIterator.next();
		            
		            vn.push();		            
		            ap.evalXPath();
		            xm.updateToken (vn.getText(), placeHolderValuesInterator.next());
		            ap.resetXPath();	            
		            vn.pop();
		            
	    	    }	   
			}			
	        ap0.resetXPath();
	        xm.output(fileName);
        }
    }
}
