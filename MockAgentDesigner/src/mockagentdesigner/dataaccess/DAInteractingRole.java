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
import mockagentdesigner.classes.InteractingRole;
import mockagentdesigner.classes.Role;

//xpath without name space

public class DAInteractingRole
{
	File file;
	String fileName;
	
	public DAInteractingRole() {
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path("repository/InteractingRoles.xml"); //$NON-NLS-1$
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
	
	public ArrayList<Role> GetInteractingRoles_ByRoleID(int roleID) throws XPathParseException, XPathEvalException, NavException {		   
		
	   ArrayList<Role> roles = new ArrayList<Role>();	   
	   VTDGen vg = new VTDGen();

       AutoPilot ap0 = new AutoPilot();
       AutoPilot ap1 = new AutoPilot();
        
       ap0.selectXPath("/InteractingRoles/InteractingRole[RoleID = " + roleID + " or " + roleID + " = 0]");
       ap1.selectXPath("InteractingRoleID"); 
       
       DARole daRole = new DARole();       
       if (vg.parseFile(fileName, false)){
            
    	   	VTDNav vn = vg.getNav();
            
    	   	ap0.bind(vn);
            ap1.bind(vn);

            while(ap0.evalXPath()!=-1){
            	Role role = new Role();
            	role.setRoleID((int)ap1.evalXPathToNumber());               
            	role.setRoleName(daRole.GetRoles_ByRoleID(role.getRoleID()).get(0).getRoleName());
            	roles.add(role);
            }
            ap0.resetXPath();            
        }        
        return roles;                    	
	}	
	
	public ArrayList<Role> GetRoles_ByInteractingRoleID(int interactingRoleID) throws XPathParseException, XPathEvalException, NavException {		   
		
		   ArrayList<Role> roles = new ArrayList<Role>();	   
		   VTDGen vg = new VTDGen();

	       AutoPilot ap0 = new AutoPilot();
	       AutoPilot ap1 = new AutoPilot();
	        
	       ap0.selectXPath("/InteractingRoles/InteractingRole[InteractingRoleID = " + interactingRoleID + " or " + interactingRoleID + " = 0]");
	       ap1.selectXPath("RoleID"); 
	       
	       DARole daRole = new DARole();       
	       if (vg.parseFile(fileName, false)){
	            
	    	   	VTDNav vn = vg.getNav();
	            
	    	   	ap0.bind(vn);
	            ap1.bind(vn);

	            while(ap0.evalXPath()!=-1){
	            	Role role = new Role();
	            	role.setRoleID((int)ap1.evalXPathToNumber());               
	            	role.setRoleName(daRole.GetRoles_ByRoleID(role.getRoleID()).get(0).getRoleName());
	            	roles.add(role);
	            }
	            ap0.resetXPath();            
	        }	        
	        return roles;                    	
		}	
	
	public ArrayList<Role> GetNonInteractingRoles_ByRoleID(int roleID) throws XPathParseException, XPathEvalException, NavException {		   
		   	       
	       DARole daRole = new DARole();
	       ArrayList<Role> roles = daRole.GetRoles_ByPatternID(daRole.GetRoles_ByRoleID(roleID).get(0).getPatternID());
	       
	       Iterator<Role> it = roles.iterator();
	       Role role;
           
           while(it.hasNext()){
           	role = it.next();            
           	if (role.getRoleID() == roleID){
           		roles.remove(role);
           		break;
           	}	                		
           }
	       
		   VTDGen vg = new VTDGen();

	       AutoPilot ap0 = new AutoPilot();
	       AutoPilot ap1 = new AutoPilot();
	        
	       ap0.selectXPath("/InteractingRoles/InteractingRole[RoleID = " + roleID + " ]");
	       ap1.selectXPath("InteractingRoleID"); 
	       
	       if (vg.parseFile(fileName, false)){
	            
	    	   	VTDNav vn = vg.getNav();
	            
	    	   	ap0.bind(vn);
	            ap1.bind(vn);
	            
	            int interactingRoleID;
	            
	            while(ap0.evalXPath()!=-1){
	            	
	            	interactingRoleID = (int)ap1.evalXPathToNumber();            	
	                it = roles.iterator();
	                
	                while(it.hasNext()){
	                	role = it.next();            
	                	if (role.getRoleID() == interactingRoleID){
	                		roles.remove(role);
	                		break;
	                	}	                		
	                }			            	
	            }
	            ap0.resetXPath();            
	        }
	        
	        return roles;                    	
		}
	
	public void insert(InteractingRole interactingRole) throws ModifyException, NavException, TranscodeException, IOException {        
		
		VTDGen vg = new VTDGen();
		    
	    if (vg.parseFile(fileName, false)){
        	
	    	VTDNav vn = vg.getNav();	    	
	    	XMLModifier xm =  new XMLModifier(vn);
	    	vn.toElement(VTDNav.ROOT);
	    	if (vn.toElement(VTDNav.LC))    	            
	    		xm.insertAfterElement("\n\t<InteractingRole>\n\t\t<RoleID>" + interactingRole.getRoleID() + "</RoleID>" +
	    				"\n\t\t<InteractingRoleID>" + interactingRole.getInteractingRoleID() + "</InteractingRoleID> \n\t</InteractingRole>");	    	
	    	else
	    		xm.insertAfterHead("\n\t<InteractingRole>\n\t\t<RoleID>" + interactingRole.getRoleID() + "</RoleID>" +
	    				"\n\t\t<InteractingRoleID>" + interactingRole.getInteractingRoleID() + "</InteractingRoleID> \n\t</InteractingRole>");
	        xm.output(fileName);
        }                        
    }
	
	public void update(InteractingRole interactingRole, int newInteractingRoleID) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException  {        
    				
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap0 = new AutoPilot();
	    AutoPilot ap1 = new AutoPilot();
	       
        ap0.selectXPath("/InteractingRoles/InteractingRole[RoleID = " + interactingRole.getRoleID() + " and InteractingRoleID = " + interactingRole.getInteractingRoleID() + " ]");              
        ap1.selectXPath("InteractingRoleID");
	       
	    if (vg.parseFile(fileName, false)){
        	
	    	VTDNav vn = vg.getNav();
	    	
            ap0.bind(vn);
            ap1.bind(vn);
            
			XMLModifier xm = new XMLModifier(vn);
			
			while(ap0.evalXPath()!=-1){
	            vn.push();	            
	            ap1.evalXPath();
	            xm.updateToken (vn.getText(), Integer.toString(newInteractingRoleID));
	            ap1.resetXPath();	            
	            vn.pop();	            
			}
			
	        ap0.resetXPath();
	        xm.output(fileName);
        }                        
    }
    	
	public void delete(InteractingRole interactingRole) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException{        
		
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap = new AutoPilot();
	       
	    ap.selectXPath("/InteractingRoles/InteractingRole[RoleID = " + interactingRole.getRoleID() + " and InteractingRoleID = " + interactingRole.getInteractingRoleID() + " ]");
	       
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
    }
	
	
	private static void find(){
		DAInteractingRole daInteractingRole = new DAInteractingRole();
		
		ArrayList<Role> roles = null;
		try {
			roles = daInteractingRole.GetInteractingRoles_ByRoleID(0);
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
        Iterator<Role> it = roles.iterator();
        
        while(it.hasNext()){
        	Role role = it.next();            
            System.out.println("===================");
            System.out.println("InteractingRoleID:  ==>  " + role.getRoleID());
            System.out.println("InteractingRoleName: ==>  " + role.getRoleName());           
        }		
	}
	
	private static void edit(){		
		
		DAInteractingRole daInteractingRole = new DAInteractingRole();
		
		InteractingRole interactingRole = new InteractingRole();
		
   		interactingRole.setRoleID(1);
		interactingRole.setInteractingRoleID(2);
		            		
		int newInteractingRoleID = 3;
		
		try {
			daInteractingRole.update(interactingRole, newInteractingRoleID);
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
		DAInteractingRole daInteractingRole = new DAInteractingRole();
		
		InteractingRole interactingRole = new InteractingRole();

		interactingRole.setRoleID(25);
		interactingRole.setInteractingRoleID(26);
		
		try {
			daInteractingRole.insert(interactingRole);
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
	
		DAInteractingRole daInteractingRole = new DAInteractingRole();

		InteractingRole interactingRole = new InteractingRole();        		    	
		
   		interactingRole.setRoleID(25);
		interactingRole.setInteractingRoleID(26);

		try {
			daInteractingRole.delete(interactingRole);
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
