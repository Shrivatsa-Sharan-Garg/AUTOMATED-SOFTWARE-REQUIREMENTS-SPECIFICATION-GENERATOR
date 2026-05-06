package com.srs.ui;

import com.srs.db.DBconnections;
import com.srs.api.DocumentGenerator;
import com.google.gson.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class tui {
    private DBconnections db;
    private Scanner scn;
    private String currentUser=null;
    private int currentUserId=-1;

    public tui(DBconnections db){
        this.db=db;
        this.scn=new Scanner(System.in);
    }

    public void start(){
        System.out.println("\n=======================================");
        System.out.println("     SOVEREIGN SRS ENGINE (CLI)        ");
        System.out.println("=======================================\n");

        while(currentUser==null){
            showAuthMenu();
        }
        showDashboard();
    }

    private void showAuthMenu(){
        while(currentUser==null){
            System.out.println("\n------ User Authentication Menu ------");
            System.out.println("1. Login");
            System.out.println("2. Sign Up");
            System.out.println("3. Exit");
            System.out.print("Select option: ");

            String choice=scn.nextLine();

            if(choice.equals("1")){
                handleLogin();
            }
            else if(choice.equals("2")){
                handleSignup();
            }
            else if(choice.equals("3")){
                System.exit(0);
            }
            else{
                System.out.println("Invalid input.");
            }
        }
    }

    private void handleLogin(){
        System.out.print("\nUsername: ");
        String user=scn.nextLine();

        System.out.print("Password: ");
        String pass=scn.nextLine();

        try(Connection conn=db.getConnection()){
            String sql="SELECT id,username FROM users WHERE username=? AND password=?";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setString(1,user);
            ps.setString(2,pass);

            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                currentUserId=rs.getInt("id");
                currentUser=rs.getString("username");
                System.out.println("\nWelcome "+currentUser);
            }
            else{
                System.out.println("Invalid credentials.");
            }
        }
        catch(Exception e){
            System.out.println("Error.");
        }
    }

    private void handleSignup(){
        System.out.print("\nFull Name: ");
        String name=scn.nextLine();

        System.out.print("Username: ");
        String user=scn.nextLine();

        System.out.print("Password: ");
        String pass=scn.nextLine();

        try(Connection conn=db.getConnection()){
            String sql="INSERT INTO users (full_name,username,password) VALUES (?,?,?)";
            PreparedStatement ps=conn.prepareStatement(sql);

            ps.setString(1,name);
            ps.setString(2,user);
            ps.setString(3,pass);

            ps.executeUpdate();

            System.out.println("Account created.");
        }
        catch(Exception e){
            System.out.println("Signup failed.");
        }
    }

    private void showDashboard(){
        while(true){
            System.out.println("\n=======================================");
            System.out.println("Dashboard | "+currentUser);
            System.out.println("=======================================");

            System.out.println("1. Create Project");
            System.out.println("2. View Projects");
            System.out.println("3. Logout");

            System.out.print("Select: ");
            String choice=scn.nextLine();

            if(choice.equals("1")){
                createProject();
            }
            else if(choice.equals("2")){
                listProjects();
            }
            else if(choice.equals("3")){
                currentUser=null;
                currentUserId=-1;
                return;
            }
            else{
                System.out.println("Invalid choice.");
            }
        }
    }

    private void createProject(){
        System.out.print("\nProject name: ");
        String name=scn.nextLine();

        try(Connection conn=db.getConnection()){
            String sql="INSERT INTO projects (user_id,project_name) VALUES (?,?)";
            PreparedStatement ps=conn.prepareStatement(sql);

            ps.setInt(1,currentUserId);
            ps.setString(2,name);

            ps.executeUpdate();

            System.out.println("Project created.");
        }
        catch(Exception e){
            System.out.println("Error.");
        }
    }

    private void listProjects(){
        try(Connection conn=db.getConnection()){
            String sql="SELECT id,project_name FROM projects WHERE user_id=?";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setInt(1,currentUserId);

            ResultSet rs=ps.executeQuery();

            List<Integer> ids=new ArrayList<>();

            System.out.println("\nProjects:");
            while(rs.next()){
                int id=rs.getInt("id");
                ids.add(id);
                System.out.println(id+" -> "+rs.getString("project_name"));
            }

            if(ids.isEmpty()){
                System.out.println("No projects.");
                return;
            }

            System.out.print("Enter id (0 back): ");
            String input=scn.nextLine();

            if(input.equals("0")) return;

            try{
                int pid=Integer.parseInt(input);
                if(ids.contains(pid)){
                    openProject(pid);
                }
                else{
                    System.out.println("Invalid id.");
                }
            }
            catch(Exception e){
                System.out.println("Invalid input.");
            }
        }
        catch(Exception e){
            System.out.println("Error.");
        }
    }

    private void openProject(int pid){
        while(true){
            System.out.println("1. Edit SRS");
            System.out.println("2. Preview SRS");
            System.out.println("3. Export PDF");
            System.out.println("4. Delete Project");
            System.out.println("5. Back");

            System.out.print("Select: ");
            String choice=scn.nextLine();

            if(choice.equals("1")){
                handleFileEdit(pid);
            }
            else if(choice.equals("2")){
                handlePreview(pid);
            }
            else if(choice.equals("3")){
                handlePdfExport(pid);
            }
            else if(choice.equals("4")){
                handleDeleteProject(pid);
                return;
            }
            else if(choice.equals("5")){
                return;
            }
            else{
                System.out.println("Invalid choice.");
            }
        }
    }

    private void handleFileEdit(int pid){
        try{
            JsonObject obj=new JsonObject();

            System.out.println("\nSRS INPUT");

            System.out.print("Purpose: ");
            obj.addProperty("purpose",scn.nextLine());

            System.out.print("Scope: ");
            obj.addProperty("scope",scn.nextLine());

            System.out.print("Audience: ");
            obj.addProperty("audience",scn.nextLine());

            System.out.print("Functions: ");
            obj.addProperty("functions",scn.nextLine());

            System.out.print("Constraints: ");
            obj.addProperty("constraints",scn.nextLine());

            System.out.print("Assumptions: ");
            obj.addProperty("assumptions",scn.nextLine());

            System.out.print("Functional Requirements: ");
            obj.addProperty("functional_requirements",scn.nextLine());

            System.out.print("Non Functional Requirements: ");
            obj.addProperty("nonfunctional_requirements",scn.nextLine());

            System.out.print("Security: ");
            obj.addProperty("security",scn.nextLine());

            System.out.print("Other: ");
            obj.addProperty("other",scn.nextLine());

            saveToDb(pid,obj.toString());

            System.out.println("Saved.");
        }
        catch(Exception e){
            System.out.println("Error.");
        }
    }

    private void handlePreview(int pid){
        try(Connection conn=db.getConnection()){
            String sql="SELECT content_json FROM srs_docs WHERE project_id=?";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setInt(1,pid);

            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                String json=rs.getString("content_json");

                if(json==null||json.equals("{}")){
                    System.out.println("No data.");
                    return;
                }

                System.out.println("\n----- PREVIEW -----\n");

                JsonObject obj=JsonParser.parseString(json).getAsJsonObject();

                for(Map.Entry<String,JsonElement> entry:obj.entrySet()){
                    System.out.println(entry.getKey().toUpperCase());
                    System.out.println(entry.getValue().getAsString());
                    System.out.println();
                }
            }
        }
        catch(Exception e){
            System.out.println("Preview error.");
        }
    }

    private void handlePdfExport(int pid){
        try(Connection conn=db.getConnection()){
            String sql="SELECT p.project_name,s.content_json FROM projects p LEFT JOIN srs_docs s ON p.id=s.project_id WHERE p.id=?";
            PreparedStatement ps=conn.prepareStatement(sql);

            ps.setInt(1,pid);
            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                String name=rs.getString("project_name");
                String json=rs.getString("content_json");

                String formatted=formatJsonToText(json);

                byte[] pdf=DocumentGenerator.generateIEEEReport(name,currentUser,formatted);

                FileOutputStream fos=new FileOutputStream(name.replaceAll("\\s+","_")+".pdf");
                fos.write(pdf);
                fos.close();

                System.out.println("PDF exported.");
            }
        }
        catch(Exception e){
            System.out.println("Export failed.");
        }
    }

    private String formatJsonToText(String jsonStr){
        if(jsonStr==null||jsonStr.equals("{}")) return "No data.";

        StringBuilder sb=new StringBuilder();

        try{
            JsonObject obj=JsonParser.parseString(jsonStr).getAsJsonObject();

            for(Map.Entry<String,JsonElement> entry:obj.entrySet()){
                sb.append(entry.getKey().toUpperCase()).append("\n");
                sb.append(entry.getValue().getAsString()).append("\n\n");
            }
        }
        catch(Exception e){
            return jsonStr;
        }

        return sb.toString();
    }

    private void handleDeleteProject(int pid){
    System.out.print("Are you sure you want to delete this project? (yes/no): ");
    String confirm=scn.nextLine();

    if(!confirm.equalsIgnoreCase("yes")){
        System.out.println("Cancelled.");
        return;
    }

    try(Connection conn=db.getConnection()){
        String sql1="DELETE FROM srs_docs WHERE project_id=?";
        PreparedStatement ps1=conn.prepareStatement(sql1);
        ps1.setInt(1,pid);
        ps1.executeUpdate();

        String sql2="DELETE FROM projects WHERE id=?";
        PreparedStatement ps2=conn.prepareStatement(sql2);
        ps2.setInt(1,pid);
        ps2.executeUpdate();

        System.out.println("Project deleted successfully.");
    }
    catch(Exception e){
        System.out.println("Delete failed.");
    }
}

    private void saveToDb(int pid,String json){
        try(Connection conn=db.getConnection()){
            String sql="INSERT INTO srs_docs (project_id,content_json,version) VALUES (?,?, '1.0') ON CONFLICT(project_id) DO UPDATE SET content_json=excluded.content_json";
            PreparedStatement ps=conn.prepareStatement(sql);

            ps.setInt(1,pid);
            ps.setString(2,json);

            ps.executeUpdate();
        }
        catch(Exception e){
            System.out.println("DB error.");
        }
    }
}