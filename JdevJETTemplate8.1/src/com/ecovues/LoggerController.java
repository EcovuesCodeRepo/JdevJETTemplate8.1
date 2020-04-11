package com.ecovues;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@Path("logging")
public class LoggerController {
    public LoggerController() {
    }

    @GET
    public String getData() {

        // Provide method implementation.
        // TODO

        throw new UnsupportedOperationException();
    }
    @GET
        @Path("/loglevel/{level}")
           public String setLogLevel(@PathParam("level") String mode) {
            
              // Logger.getRootLogger().setLevel(Level.OFF);
             
               if(mode.equalsIgnoreCase("DEBUG")) {
                   Logger.getRootLogger().setLevel(Level.DEBUG);
               }
             
               /**** Setting Log4j Priority Mode As 'INFO' ****/
               else if(mode.equalsIgnoreCase("INFO")) {
                   Logger.getRootLogger().setLevel(Level.INFO);
               }
             
               /**** Setting Log4j Priority Mode As 'WARN' ****/
               else if(mode.equalsIgnoreCase("WARN")) {
                   Logger.getRootLogger().setLevel(Level.WARN);
               }
             
               /**** Setting Log4j Priority Mode As 'ERROR' ****/
               else if(mode.equalsIgnoreCase("ERROR")) {
                   Logger.getRootLogger().setLevel(Level.ERROR);
               }
             
               /**** Setting Log4j Priority Mode As 'OFF' ****/
               else {
                   Logger.getRootLogger().setLevel(Level.OFF);           
               }
              
             //  updateConfigs(mode,userName);
             
               return "Log level set to : "+mode;
           }
        public void updateConfigs(String value, String userName){
            String context="OFF";
            if(!value.equals("OFF")) {
                context=value;
                value="ON";
            }
        String sql = "update ecoui_configs set configs_value='"+value+"',configs_context='"+context+"' where configs_name = 'javadebug'";
        String truncateSql = "delete from ECOUI_LOG_MESSAGES where user_name = '"+userName+"'";
        try (Connection conn= JdbcHelper.getJDBCConnectionFromDataSource(true);
                Statement stmt = conn.createStatement();) {
             
              stmt.executeUpdate(sql);
              if(value.equals("OFF"))
              stmt.executeUpdate(truncateSql);
             // System.out.println("Database updated successfully ");
            } catch (SQLException e) {
              e.printStackTrace();
            }
        }

}