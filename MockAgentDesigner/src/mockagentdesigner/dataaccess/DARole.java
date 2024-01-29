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
import mockagentdesigner.classes.Role;

//xpath without name space

public class DARole
{
	File file;
	String fileName;
	
	public DARole() {
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path("repository/AgentRoles.xml"); //$NON-NLS-1$
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
	
	public ArrayList<Role> GetRoles_ByRoleID(int roleID) throws XPathParseException, XPathEvalException, NavException {		   
		
	   ArrayList<Role> roles = new ArrayList<Role>();	   
	   VTDGen vg = new VTDGen();

       AutoPilot ap0 = new AutoPilot();
       AutoPilot ap1 = new AutoPilot();
       AutoPilot ap2 = new AutoPilot();
       AutoPilot ap3 = new AutoPilot();
       AutoPilot ap4 = new AutoPilot();
        
       ap0.selectXPath("/AgentRoles/AgentRole[RoleID = " + roleID + " or " + roleID + " = 0]");
       ap1.selectXPath("RoleID"); 
       ap2.selectXPath("RoleName"); 
       ap3.selectXPath("PatternID");
       ap4.selectXPath("RoleDescription");
       
       if (vg.parseFile(fileName, false)){
            
    	   	VTDNav vn = vg.getNav();
            
    	   	ap0.bind(vn);
            ap1.bind(vn);
            ap2.bind(vn);
            ap3.bind(vn);
            ap4.bind(vn);

            while(ap0.evalXPath()!=-1){
            	Role role = new Role();
            	role.setRoleID((int)ap1.evalXPathToNumber());
            	role.setRoleName(ap2.evalXPathToString());
            	role.setPatternID((int)ap3.evalXPathToNumber());
            	role.setRoleDescription(ap4.evalXPathToString());
               
            	roles.add(role);
            }
            ap0.resetXPath();
            
        }
        return roles;
                    	
	}		
	
	public ArrayList<Role> GetRoles_ByPatternID(int patternID) throws XPathParseException, XPathEvalException, NavException {		   
		
		   ArrayList<Role> roles = new ArrayList<Role>();	   
		   VTDGen vg = new VTDGen();

	       AutoPilot ap0 = new AutoPilot();
	       AutoPilot ap1 = new AutoPilot();
	       AutoPilot ap2 = new AutoPilot();
	       AutoPilot ap3 = new AutoPilot();
	       AutoPilot ap4 = new AutoPilot();
	        
	       ap0.selectXPath("/AgentRoles/AgentRole[PatternID = " + patternID + " or " + patternID + " = 0]");
	       ap1.selectXPath("RoleID"); 
	       ap2.selectXPath("RoleName"); 
	       ap3.selectXPath("PatternID");
	       ap4.selectXPath("RoleDescription");
	        
	       if (vg.parseFile(fileName, false)){
	            
	    	   	VTDNav vn = vg.getNav();
	            
	    	   	ap0.bind(vn);
	            ap1.bind(vn);
	            ap2.bind(vn);
	            ap3.bind(vn);
	            ap4.bind(vn);

	            while(ap0.evalXPath()!=-1){
	            	Role role = new Role();
	            	role.setRoleID((int)ap1.evalXPathToNumber());
	            	role.setRoleName(ap2.evalXPathToString());
	            	role.setPatternID((int)ap3.evalXPathToNumber());
	            	role.setRoleDescription(ap4.evalXPathToString());
	               
	            	roles.add(role);
	            }
	            ap0.resetXPath();
	            
	        }
	        return roles;
	                    	
		}	
	
	public boolean update(Role role) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException  {        
    				
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap0 = new AutoPilot();
	    
	    AutoPilot ap1 = new AutoPilot();
	    AutoPilot ap2 = new AutoPilot();
	    AutoPilot ap3 = new AutoPilot();
	    AutoPilot ap4 = new AutoPilot();
	    
	    ap0.selectXPath("/AgentRoles/AgentRole[RoleID != " + role.getRoleID() + " and RoleName = '" + role.getRoleName() + "' ]");
	    
        ap1.selectXPath("/AgentRoles/AgentRole[RoleID = " + role.getRoleID() + " ]");	    
        ap2.selectXPath("RoleName"); 
        ap3.selectXPath("PatternID");
	    ap4.selectXPath("RoleDescription"); 
	       
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
	            xm.updateToken (vn.getText(), role.getRoleName());
	            ap2.resetXPath();	            
	            vn.pop();
	            
	            vn.push();	            
	            ap3.evalXPath();
	            xm.updateToken (vn.getText(), Integer.toString(role.getPatternID()));
	            ap3.resetXPath();	            
	            vn.pop();

	            vn.push();
	            ap4.evalXPath();
	            xm.updateToken (vn.getText(), role.getRoleDescription());
	            ap4.resetXPath();	            
	            vn.pop();				
			}
			
	        ap1.resetXPath();
	        xm.output(fileName);
        }
	    return true;
    }
    
	public int insert(Role role) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException {        
		
		VTDGen vg = new VTDGen();
		
		int newRoleID = 0;
		
		AutoPilot ap0 = new AutoPilot();
		AutoPilot ap = new AutoPilot();
		
		ap0.selectXPath("/AgentRoles/AgentRole[RoleName = '" + role.getRoleName() + "' ]");
		ap.selectXPath("RoleID"); 
		    
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
	    		newRoleID = vn.parseInt(vn.getText()) + 1;
	            ap.resetXPath();	            
	            vn.pop();	            
	            xm.insertAfterElement("\n\t<AgentRole> \n\t\t<RoleID>" + newRoleID + "</RoleID>" +
	    				"\n\t\t<RoleName>" + role.getRoleName() + "</RoleName>" +
	    				"\n\t\t<PatternID>" + role.getPatternID() + "</PatternID>" +
	    				"\n\t\t<RoleDescription>" + role.getRoleDescription() + "</RoleDescription> \n\t</AgentRole>");
	    	}
	    	else{
	    		newRoleID = 1;
	    		xm.insertAfterHead("\n\t<AgentRole> \n\t\t<RoleID>" + newRoleID + "</RoleID>" +
	    				"\n\t\t<RoleName>" + role.getRoleName() + "</RoleName>" +
	    				"\n\t\t<PatternID>" + role.getPatternID() + "</PatternID>" +
	    				"\n\t\t<RoleDescription>" + role.getRoleDescription() + "</RoleDescription> \n\t</AgentRole>");
	    	}
	        xm.output(fileName);
        }
		return newRoleID;                        
    }
	
	public boolean delete(int roleID) throws XPathParseException, XPathEvalException, NavException, ModifyException, TranscodeException, IOException{        
		
		VTDGen vg = new VTDGen();
		
	    AutoPilot ap = new AutoPilot();
	       
        ap.selectXPath("/AgentRoles/AgentRole[RoleID = " + roleID + " ]");
	    
        DAInteractingRole daInteractingRole = new DAInteractingRole();
        
        if (daInteractingRole.GetInteractingRoles_ByRoleID(roleID).size()>=1)
        	return false;
        
        if (daInteractingRole.GetRoles_ByInteractingRoleID(roleID).size()>=1)
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
		
		DARole daRole = new DARole();
		
		ArrayList<Role> roles = null;
		try {
			roles = daRole.GetRoles_ByRoleID(0);
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
            System.out.println("RoleID:  ==>  "+role.getRoleID());
            System.out.println("RoleName: ==>  "+role.getRoleName());
            System.out.println("PatternID: ==>  "+role.getPatternID());
            System.out.println("RoleDescription:  ==>  "+role.getRoleDescription());            
        }		
	}
	
	private static void edit(){
		
		DARole daRole = new DARole();
		
		Role role = new Role();
		
		role.setRoleID(1);
		role.setRoleName("updatedName");
		role.setPatternID(1);
		role.setRoleDescription("updatedDescription");
				
		try {
			if (!daRole.update(role)){
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
		
		DARole daRole = new DARole();
		
		Role role = new Role();

		role.setRoleName("roleName");
		role.setPatternID(1);
		role.setRoleDescription("roleDescription");
		
		
		try {
			if (daRole.insert(role) == -1){
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
		
		DARole daRole = new DARole();
		
		int roleID = 25;		
		try {
			if (!daRole.delete(roleID)){
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
