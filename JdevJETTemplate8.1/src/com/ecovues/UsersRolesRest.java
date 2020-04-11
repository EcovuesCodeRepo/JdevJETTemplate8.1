package com.ecovues;

import java.security.Principal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Set;

import javax.security.auth.Subject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import weblogic.security.Security;
import weblogic.security.principal.WLSGroupImpl;
import weblogic.security.principal.WLSUserImpl;
import javax.naming.*;
import javax.management.MBeanInfo;

import org.apache.log4j.Logger;

import org.sql2o.tools.NamedParameterStatement;

import weblogic.jndi.Environment;
import weblogic.management.runtime.ServerRuntimeMBean;
import weblogic.security.providers.authentication.DefaultAuthenticatorMBean;
import weblogic.management.security.authentication.UserReaderMBean;
import weblogic.management.security.authentication.GroupReaderMBean;
import weblogic.management.MBeanHome;
//import weblogic.management.WebLogicMBean;
import weblogic.management.security.authentication.*;
@Path("userroles")
public class UsersRolesRest {
    private static final Logger logger = Logger.getLogger(UsersRolesRest.class);
    public UsersRolesRest() {
        super();
    }
    MBeanHome home = null;
    @GET
    @Path("/getUserName")
    @Produces("application/json")
    public Response getUserRoleInfo() {
    
            String userRoleInfo = "";
         ArrayList<String> roles = new ArrayList<String>();
             String user = null;
        
            Subject subject = Security.getCurrentSubject(); 
        
        Set<Principal> allPrincipals = subject.getPrincipals();
                for (Principal principal : allPrincipals) {
                    logger.info("Role: "+principal.getName());
                    if ( principal instanceof WLSGroupImpl ) {
                       
                        roles.add(principal.getName());
                    }
                    if ( principal instanceof WLSUserImpl ) {
                        logger.info("User: "+principal.getName());
                        user = principal.getName();
                        
                    }            
                }  
        
            userRoleInfo = "{\"userName\": \"" + user + "\",\"userDisplayName\": \"" + user
                    + "\",\"role\": [" + getEnterpriseRole(subject) + "]}";
            
        
    
        return Response.status(200)
                                          .entity( userRoleInfo )
                                          .header("Access-Control-Allow-Origin", "*")
                                          .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
                                    //      .allow("OPTIONS").build();
      //  return userRoleInfo;
    }
    private String getEnterpriseRole(Subject mySubject) {
        String roleDisplayName = "";
        Set<java.security.Principal> principals = mySubject.getPrincipals();
        int principalSize = principals.size();
        int i = 1;
        for (java.security.Principal principal : principals) {
            if (i != principalSize) {
                if (principal.getClass() == WLSGroupImpl.class) {
                    roleDisplayName += "{\"display\":\"" + principal.getName() + "\"},";
                }
            } else {
                roleDisplayName += "{\"display\":\"" + principal.getName() + "\"}";
            }
            i++;
        }
        return roleDisplayName;
    }
    
    public static JSONObject convertToJSON(ResultSet resultSet)
            throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject finalobj = new JSONObject();
        while (resultSet.next()) {
            int total_rows = resultSet.getMetaData().getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 0; i < total_rows; i++) {
               
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), resultSet.getObject(i + 1));
                
            }
            jsonArray.put(obj);
        }
        return finalobj.put("items",jsonArray);
            }
    @GET
           @Path("/configs")
           @Produces("application/json")
           public Response configs() throws Exception {
               logger.info("Entering configs method");
               Connection ebsconn = JdbcHelper.getJDBCConnectionFromDataSource(true);

               JSONObject jsonArray = new JSONObject();
               String query = "select * from ecoui_configs";
               
               logger.info("Configs Query: "+query);


               NamedParameterStatement p = new NamedParameterStatement(ebsconn, query, true);

               ResultSet rs = null;

               try {
                   rs = p.executeQuery();
                   jsonArray = JdbcHelper.convertToJSON(rs);
                   p.close();
                   rs.close();
               } catch (SQLException e) {
                   logger.error("Error while fetching configs info: "+e.toString());
                   return Response.status(200).entity(e.toString()).header("Access-Control-Allow-Origin",
                                                                           "*").header("Access-Control-Allow-Methods",
                                                                                       "GET, POST, DELETE, PUT").build();
               } finally {
                   logger.info("configs info: Closing connection");
                   ebsconn.close();
                   
               }


               return Response.status(200).entity(jsonArray.toString()).header("Access-Control-Allow-Origin",
                                                                               "*").header("Access-Control-Allow-Methods",
                                                                                           "GET, POST, DELETE, PUT").build();
           }

//    @GET
//    @Path("/m1")
//    
//    public String dbLink = JdbcHelper.dbLink;   
//    public String getName() {
//        try
//        {
//
//          Environment env = new Environment();
//          env.setProviderUrl("http://127.0.0.1:7101/");
//          env.setSecurityPrincipal("weblogic");
//          env.setSecurityCredentials("welcome1");
//          Context ctx = env.getInitialContext();
//            
//            
//
//          home = (MBeanHome)ctx.lookup(MBeanHome.ADMIN_JNDI_NAME);
//
//          weblogic.management.security.RealmMBean rmBean = 
//         home.getActiveDomain().getSecurityConfiguration().getDefaultRealm();
//
//          AuthenticationProviderMBean[] authenticationBeans = 
//          rmBean.getAuthenticationProviders();
//          DefaultAuthenticatorMBean defaultAuthenticationMBean = 
//          (DefaultAuthenticatorMBean)authenticationBeans[0];
//          UserReaderMBean userReaderMBean = 
//          (UserReaderMBean)defaultAuthenticationMBean;
//
//          String userCurName = userReaderMBean.listUsers("*", 100);
//
//          while (userReaderMBean.haveCurrent(userCurName) )
//          {
//            String user = userReaderMBean.getCurrentName(userCurName);
//            System.out.println("\n User: " + user);
//            userReaderMBean.advance(userCurName);
//          }
//
//        }
//        catch (Exception e)
//        {
//          e.printStackTrace();
//        }
//        return "Rest service working fine";
//        
//
//    }
    
    
    
    
  

}
