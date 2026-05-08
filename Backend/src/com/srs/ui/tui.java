package com.srs.ui;

import com.srs.db.DBconnections;
import com.srs.api.DocumentGenerator;
import com.google.gson.*;
import java.sql.*;
import java.util.*;

public class tui{
    private DBconnections db;
    private Scanner scn;
    private String currentUser=null;
    private int currentUserId=-1;

    public tui(DBconnections db){
        this.db=db;
        this.scn=new Scanner(System.in);
    }

    public void start(){
        while(currentUser==null){
            authMenu();
        }
        dashboard();
    }

    private void authMenu(){
        while(currentUser==null){
            System.out.println("1.Login");
            System.out.println("2.Signup");
            System.out.println("3.Exit");
            String ch=scn.nextLine();

            if(ch.equals("1")) login();
            else if(ch.equals("2")) signup();
            else if(ch.equals("3")) System.exit(0);
        }
    }

    private void login(){
        System.out.print("Username:");
        String u=scn.nextLine();
        System.out.print("Password:");
        String p=scn.nextLine();

        try(Connection c=db.getConnection()){
            String q="select id,username from users where username=? and password=?";
            PreparedStatement ps=c.prepareStatement(q);
            ps.setString(1,u);
            ps.setString(2,p);

            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                currentUser=rs.getString("username");
                currentUserId=rs.getInt("id");
                System.out.println("Welcome "+currentUser);
            }else{
                System.out.println("Invalid");
            }
        }catch(Exception e){}
    }

    private void signup(){
        System.out.print("Name:");
        String n=scn.nextLine();
        System.out.print("Username:");
        String u=scn.nextLine();
        System.out.print("Password:");
        String p=scn.nextLine();

        try(Connection c=db.getConnection()){
            String q="insert into users(full_name,username,password) values(?,?,?)";
            PreparedStatement ps=c.prepareStatement(q);
            ps.setString(1,n);
            ps.setString(2,u);
            ps.setString(3,p);
            ps.executeUpdate();
            System.out.println("Created");
        }catch(Exception e){}
    }

    private void dashboard(){
        while(true){
            System.out.println("1.Create");
            System.out.println("2.View");
            System.out.println("3.Logout");
            String ch=scn.nextLine();

            if(ch.equals("1")) createProject();
            else if(ch.equals("2")) listProjects();
            else if(ch.equals("3")){
                currentUser=null;
                return;
            }
        }
    }

    private void createProject(){
        System.out.print("Project:");
        String name=scn.nextLine();

        try(Connection c=db.getConnection()){
            String q="insert into projects(user_id,project_name) values(?,?)";
            PreparedStatement ps=c.prepareStatement(q);
            ps.setInt(1,currentUserId);
            ps.setString(2,name);
            ps.executeUpdate();
        }catch(Exception e){}
    }

    private void listProjects(){
        try(Connection c=db.getConnection()){
            String q="select id,project_name from projects where user_id=?";
            PreparedStatement ps=c.prepareStatement(q);
            ps.setInt(1,currentUserId);
            ResultSet rs=ps.executeQuery();

            List<Integer> ids=new ArrayList<>();

            while(rs.next()){
                int id=rs.getInt("id");
                ids.add(id);
                System.out.println(id+" "+rs.getString("project_name"));
            }

            if(ids.isEmpty()) return;

            int pid=Integer.parseInt(scn.nextLine());

            if(ids.contains(pid)){
                projectMenu(pid);
            }

        }catch(Exception e){}
    }

    private void projectMenu(int pid){
        while(true){
            System.out.println("1.Edit");
            System.out.println("2.Preview");
            System.out.println("3.Export");
            System.out.println("4.Delete");
            System.out.println("5.Back");

            String ch=scn.nextLine();

            if(ch.equals("1")) edit(pid);
            else if(ch.equals("2")) preview(pid);
            else if(ch.equals("3")) export(pid);
            else if(ch.equals("4")) delete(pid);
            else if(ch.equals("5")) return;
        }
    }

    private void edit(int pid){
        try{
            String path="src/resources/srs_template.json";
            JsonObject template=JsonParser.parseString(
                java.nio.file.Files.readString(java.nio.file.Paths.get(path))
            ).getAsJsonObject();

            JsonObject result=new JsonObject();

            process(template,result,"");

            save(pid,result.toString());

        }catch(Exception e){}
    }

    private void process(JsonObject obj,JsonObject result,String prefix){
        for(Map.Entry<String,JsonElement> e:obj.entrySet()){
            String k=e.getKey();
            JsonElement v=e.getValue();

            if(v.isJsonObject()){
                JsonObject o=v.getAsJsonObject();

                if(o.has("content")){
                    String label=o.has("label")?o.get("label").getAsString():k;
                    System.out.println(label);
                    String input=scn.nextLine();
                    result.addProperty(prefix+k,input);
                }

                process(o,result,prefix+k+".");
            }
        }
    }

    private void preview(int pid){
        try(Connection c=db.getConnection()){
            String q="select content_json from srs_docs where project_id=?";
            PreparedStatement ps=c.prepareStatement(q);
            ps.setInt(1,pid);

            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                String json=rs.getString("content_json");

                if(json==null||json.equals("{}")){
                    System.out.println("No data");
                    return;
                }

                JsonObject obj=JsonParser.parseString(json).getAsJsonObject();

                String lastSection="";

                for(Map.Entry<String,JsonElement> e:obj.entrySet()){
                    String key=e.getKey();
                    String val=e.getValue().getAsString();

                    key=key.replace("sections.","");

                    String[] parts=key.split("\\.");

                    if(parts.length>=1){
                        String sec=parts[0];
                        String[] sp=sec.split("_",2);

                        String num=sp[0];
                        String name=sp.length>1?sp[1]:"";

                        String display=num+". "+name.replace("_"," ");

                        if(!display.equals(lastSection)){
                            System.out.println("\n"+display.toUpperCase());
                            lastSection=display;
                        }
                    }

                    String last=parts[parts.length-1];
                    String[] sub=last.split("_",2);

                    String subd=sub[0]+". "+(sub.length>1?sub[1]:"");

                    System.out.println("  "+subd);
                    System.out.println("    "+val+"\n");
                }
            }

        }catch(Exception e){}
    }

    private void export(int pid){
        try(Connection c=db.getConnection()){
            String q="select p.project_name,s.content_json from projects p left join srs_docs s on p.id=s.project_id where p.id=?";
            PreparedStatement ps=c.prepareStatement(q);
            ps.setInt(1,pid);

            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                String name=rs.getString("project_name");
                String json=rs.getString("content_json");

                StringBuilder sb=new StringBuilder();

                JsonObject obj=JsonParser.parseString(json).getAsJsonObject();

                for(Map.Entry<String,JsonElement> e:obj.entrySet()){
                    sb.append(e.getKey()).append("\n");
                    sb.append(e.getValue().getAsString()).append("\n\n");
                }

                byte[] pdf=DocumentGenerator.generateIEEEReport(name,currentUser,sb.toString());

                java.nio.file.Files.write(
                    java.nio.file.Paths.get(name+".pdf"),
                    pdf
                );

                System.out.println("Exported");
            }

        }catch(Exception e){}
    }

    private void delete(int pid){
        try(Connection c=db.getConnection()){
            String q1="delete from srs_docs where project_id=?";
            PreparedStatement ps1=c.prepareStatement(q1);
            ps1.setInt(1,pid);
            ps1.executeUpdate();

            String q2="delete from projects where id=?";
            PreparedStatement ps2=c.prepareStatement(q2);
            ps2.setInt(1,pid);
            ps2.executeUpdate();

        }catch(Exception e){}
    }

    private void save(int pid,String json){
        try(Connection c=db.getConnection()){
            String q="insert into srs_docs(project_id,content_json,version) values(?,?, '1.0') on conflict(project_id) do update set content_json=excluded.content_json";
            PreparedStatement ps=c.prepareStatement(q);
            ps.setInt(1,pid);
            ps.setString(2,json);
            ps.executeUpdate();
        }catch(Exception e){}
    }
}