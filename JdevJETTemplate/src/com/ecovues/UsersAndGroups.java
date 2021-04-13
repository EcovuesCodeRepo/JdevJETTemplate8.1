package com.ecovues;

import com.ecovues.JdbcHelper;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.sql2o.tools.NamedParameterStatement;

@Path("securityrealm")
public class UsersAndGroups {
    public UsersAndGroups() {
    }


    private static MBeanServerConnection mBeanServerConnection;
    private static ObjectName userEditor;
    private static ObjectName[] authenticationProviders;
    private static String authenticatorName = "DefaultAuthenticator";
    static Gson gson = new Gson();
    
    public static ConfigData configs() throws Exception {
        String username="";
        String password="";
        String imagingHost="";
        String imagingPort="";
        Connection ebsconn = JdbcHelper.getJDBCConnectionFromDataSource(true);
        
        ConfigData config = new ConfigData(username,password,imagingHost, imagingPort);
        String query = "select * from ecoui_configs";//xxcv_img_configs


        NamedParameterStatement p = new NamedParameterStatement(ebsconn, query, true);

        ResultSet rs = null;

        try {
            rs = p.executeQuery();
           
            while(rs.next()){  
          
                
           if(rs.getString("configs_name").equalsIgnoreCase("imagingHost"))
            imagingHost =  rs.getString("configs_value");
                
                if(rs.getString("configs_name").equalsIgnoreCase("imagingPort"))
                 imagingPort =  rs.getString("configs_value");
                               
                               if(rs.getString("configs_name").equalsIgnoreCase("USERNAME"))
                                  username= rs.getString("configs_value");
                               
                               if(rs.getString("configs_name").equalsIgnoreCase("PASSWORD"))
                                  password = rs.getString("configs_value");
                
                 config = new ConfigData(username,password,imagingHost,imagingPort);
            }
            p.close();
            rs.close();
        } catch (SQLException e) {
            
            
        } finally {
            
            ebsconn.close();
        }


        return config;
    }
    
    static class ConfigData {
        String userName, password, imagingHost, imagingPort;

        public ConfigData(String userName, String password, String imagingHost, String imagingPort) {
            this.userName = userName;
            this.password = password;
            this.imagingHost = imagingHost;
            this.imagingPort = imagingPort;
            
        }
    }

    static {
        try {
            ConfigData config = configs();
            
            String username   = config.userName;
            String password   = config.password;
            String imagingHost = config.imagingHost;
            String imagingPort = config.imagingPort;
            
         //   String host = "eco-imaging-com";
         //   String port = "7001";
         //   String username = "weblogic";
        //   String password = "weblogic1";
            
            Hashtable h = new Hashtable();
            JMXServiceURL serviceURL;
            System.out.println("Inside Static");
            serviceURL =
                new JMXServiceURL("t3", imagingHost, Integer.valueOf(imagingPort).intValue(),
                                  "/jndi/weblogic.management.mbeanservers.domainruntime");
            h.put("java.naming.security.principal", username);
            h.put("java.naming.security.credentials", password);
            h.put("jmx.remote.protocol.provider.pkgs", "weblogic.management.remote");
            //Creating a JMXConnector to connect to JMX
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, h);
            mBeanServerConnection = connector.getMBeanServerConnection();

            // ObjectName configurationMBeans=
            //                 new ObjectName(DOMAIN_MBEAN_NAME);

            ObjectName configurationMBeans =
                new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");

            ObjectName domain =
                (ObjectName) mBeanServerConnection.getAttribute(configurationMBeans, "DomainConfiguration");
            ObjectName security = (ObjectName) mBeanServerConnection.getAttribute(domain, "SecurityConfiguration");
            ObjectName realm = (ObjectName) mBeanServerConnection.getAttribute(security, "DefaultRealm");
            authenticationProviders =
                (ObjectName[]) mBeanServerConnection.getAttribute(realm, "AuthenticationProviders");
            for (int i = 0; i < authenticationProviders.length; i++) {
                String name = (String) mBeanServerConnection.getAttribute(authenticationProviders[i], "Name");
                System.out.println("Authenticator Name: " + name);
                if (name.equals(authenticatorName))
                    userEditor = authenticationProviders[i];
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/m2")
    public String getData() {
        System.out.println("Latest");
        // invoke();
        getListOfUsers();
        return "Success";
    }


    @GET
    @Path("/getListOfUsers")
    @Produces("application/json")
    public static Response getListOfUsers() throws RuntimeException {
        try {
            List allUsers = new ArrayList();
            String cursor =
                (String) mBeanServerConnection.invoke(userEditor, "listUsers",
                                                      new Object[] { "*", Integer.valueOf(9999) },
                                                      new String[] { "java.lang.String", "java.lang.Integer" });
            boolean haveCurrent =
                ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                        new String[] { "java.lang.String" })).booleanValue();
            while (haveCurrent) {
                String currentName =
                    (String) mBeanServerConnection.invoke(userEditor, "getCurrentName", new Object[] { cursor },
                                                          new String[] { "java.lang.String" });
                System.out.println("User: " + currentName);
                allUsers.add(currentName);
                mBeanServerConnection.invoke(userEditor, "advance", new Object[] { cursor },
                                             new String[] { "java.lang.String" });
                haveCurrent =
                    ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                            new String[] { "java.lang.String" })).booleanValue();
            }


            //  return allUsers;
            return Response.status(200)
                           .entity(gson.toJson(allUsers))
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                           .build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GET
    @Path("/getUserGroups")
    @Produces("application/json")
    public static Response getUserGroups(@QueryParam("username") String username) throws RuntimeException {
        try {
            List allUserGroups = new ArrayList();
            String cursor =
                (String) mBeanServerConnection.invoke(userEditor, "listMemberGroups", new Object[] { username },
                                                      new String[] { "java.lang.String" });
            boolean haveCurrent =
                ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                        new String[] { "java.lang.String" })).booleanValue();
            while (haveCurrent) {
                String currentName =
                    (String) mBeanServerConnection.invoke(userEditor, "getCurrentName", new Object[] { cursor },
                                                          new String[] { "java.lang.String" });
                allUserGroups.add(currentName);
                mBeanServerConnection.invoke(userEditor, "advance", new Object[] { cursor },
                                             new String[] { "java.lang.String" });
                haveCurrent =
                    ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                            new String[] { "java.lang.String" })).booleanValue();
            }
            return Response.status(200)
                           .entity(gson.toJson(allUserGroups))
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                           .build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GET
    @Path("/getGroupMembers")
    @Produces("application/json")
    public static Response getGroupMembers(@QueryParam("groupName") String groupName) throws RuntimeException {
        try {
            List allGroupMembers = new ArrayList();
            String cursor =
                (String) mBeanServerConnection.invoke(userEditor, "listGroupMembers",
                                                      new Object[] { groupName, "*", new java.lang.Integer(0) },
                                                      new String[] { "java.lang.String", "java.lang.String",
                                                                     "java.lang.Integer" });
            boolean haveCurrent =
                ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                        new String[] { "java.lang.String" })).booleanValue();
            while (haveCurrent) {
                String currentName =
                    (String) mBeanServerConnection.invoke(userEditor, "getCurrentName", new Object[] { cursor },
                                                          new String[] { "java.lang.String" });
                allGroupMembers.add(currentName);
                mBeanServerConnection.invoke(userEditor, "advance", new Object[] { cursor },
                                             new String[] { "java.lang.String" });
                haveCurrent =
                    ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                            new String[] { "java.lang.String" })).booleanValue();
            }
            return Response.status(200)
                           .entity(gson.toJson(allGroupMembers))
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                           .build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GET
    @Path("/getListOfGroups")
    @Produces("application/json")
    public static Response getListOfGroups() throws RuntimeException {
        try {
            List allUsers = new ArrayList();
            String cursor =
                (String) mBeanServerConnection.invoke(userEditor, "listGroups",
                                                      new Object[] { "*", Integer.valueOf(9999) },
                                                      new String[] { "java.lang.String", "java.lang.Integer" });
            boolean haveCurrent =
                ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                        new String[] { "java.lang.String" })).booleanValue();
            while (haveCurrent) {
                String currentName =
                    (String) mBeanServerConnection.invoke(userEditor, "getCurrentName", new Object[] { cursor },
                                                          new String[] { "java.lang.String" });
                allUsers.add(currentName);
                mBeanServerConnection.invoke(userEditor, "advance", new Object[] { cursor },
                                             new String[] { "java.lang.String" });
                haveCurrent =
                    ((Boolean) mBeanServerConnection.invoke(userEditor, "haveCurrent", new Object[] { cursor },
                                                            new String[] { "java.lang.String" })).booleanValue();
            }
            return Response.status(200)
                           .entity(gson.toJson(allUsers))
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                           .build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @POST
    @Path("/createUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public static String createUser(String JsonPayload) {
        JSONParser parser = new JSONParser();
        try {
            org.json.simple.JSONObject preferenceObj;

            preferenceObj = (org.json.simple.JSONObject) parser.parse(JsonPayload);

            String username = (String) preferenceObj.get("userName");
            String psw = (String) preferenceObj.get("password");
            String desc = (String) preferenceObj.get("description");

            try {
                mBeanServerConnection.invoke(userEditor, "createUser", new Object[] { username, psw, desc },
                                             new String[] { "java.lang.String", "java.lang.String",
                                                            "java.lang.String" });
                return username+" created successfully";
            } catch (Exception ex) {
                ex.printStackTrace();

            }

        } catch (ParseException e) {
            return "Error while creating User";
        }
        return "Error while creating User";
    }

    public static boolean removeUser(String username) {
        try {
            if (!username.equalsIgnoreCase("weblogic")) {
                mBeanServerConnection.invoke(userEditor, "removeUser", new Object[] { username },
                                             new String[] { "java.lang.String" });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean resetUserPassword(String username, String newPassword) {
        try {
            if (!username.equalsIgnoreCase("weblogic")) {
                mBeanServerConnection.invoke(userEditor, "resetUserPassword", new Object[] { username, newPassword },
                                             new String[] { "java.lang.String", "java.lang.String" });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isUserExists(String currentUser) throws RuntimeException {
        try {
            boolean userExists =
                ((Boolean) mBeanServerConnection.invoke(userEditor, "userExists", new Object[] { currentUser },
                                                        new String[] { "java.lang.String" })).booleanValue();
            return userExists;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isGroupExists(String currentGroup) throws RuntimeException {
        try {
            boolean gourpExists =
                ((Boolean) mBeanServerConnection.invoke(userEditor, "groupExists", new Object[] { currentGroup },
                                                        new String[] { "java.lang.String" })).booleanValue();
            return gourpExists;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
  //  public static final String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
 //   public static final String MBEAN_SERVER = "weblogic.management.mbeanservers.domainruntime";
 //   public static final String JNDI_ROOT = "/jndi/";
 //   public static final String DEFAULT_PROTOCOL = "t3";
 //   public static final String PROTOCOL_PROVIDER_PACKAGES = "weblogic.management.remote";