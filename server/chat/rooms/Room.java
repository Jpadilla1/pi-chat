package chat.rooms;


import chat.db.DbField;
import chat.db.Model;
import chat.services.ServiceConstants;
import chat.users.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author josepadilla
 */
public class Room extends Model {
    
    @DbField(type="pk")
    private String name;
    
    @DbField(type="field")
    private String password;
    
    private User user;
    
    public Room(String name) {
        this.name = name;
    }
    
    public boolean isMember(User user) {
        return isMember(user, this.name);
    }
    
    public ArrayList<String> getMembers(User user) {
        return getRoomMembers(user);
    }
    
    public boolean createRoom() {
        return makeRoom();
    }
    
    public boolean addUser(User user) {
        return addUserToRoom(user, this.password);
    }
    
    public void getRoom() {
        if (this.name == null) {
            return;
        }
        Room object = (Room) getOne(this.name);
        this.name = object.name;
        this.password = object.password;
    }
    
    private ArrayList<String> getRoomMembers(User user) {
        ArrayList<String> data = new ArrayList<>();
        if (this.name == null) {
            return null;
        }
        try {
            Class.forName(ServiceConstants.SQLITE_CLASS_NAME);
            Connection conn = DriverManager.getConnection(ServiceConstants.CONNECTION_STRING);
            
            String sql = "SELECT * FROM ROOM_MEMBERS WHERE name = " + this.name + ";";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                data.add(rs.getString("username"));
            }
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
        return data;
    }

    private boolean isMember(User user, String name) {
        if (user == null ||  name == null) {
            return false;
        }
        try {
            Class.forName(ServiceConstants.SQLITE_CLASS_NAME);
            Connection conn = DriverManager.getConnection(ServiceConstants.CONNECTION_STRING);
            
            String sql = "SELECT * FROM ROOM_MEMBERS WHERE username = "
                    + user.getUsername() +" AND title = " + name + ";";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) {
                return true;
            }
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        }
        return false;
    }
    
    private boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    private boolean addUserToRoom(User user, String password) {
        if (password == null) {
            return false;
        }
        if (checkPassword(password)) {
            try {
                Class.forName(ServiceConstants.SQLITE_CLASS_NAME);
                Connection conn = DriverManager.getConnection(ServiceConstants.CONNECTION_STRING);

                String sql = "INSERT INTO ROOM_MEMBER (USERNAME, NAME) " 
                        + "VALUES ('" + user.getUsername()  + "', '" + this.name + "');";
                conn.createStatement().executeUpdate(sql);
                conn.close();
                return true;
            } catch (ClassNotFoundException | SQLException e) {
                return false;
            }
        }
        return false;
    }
    
    private boolean removeUserFromRoom(User user) {
        if (user == null) {
            return false;
        }
        try {
            Class.forName(ServiceConstants.SQLITE_CLASS_NAME);
            Connection conn = DriverManager.getConnection(ServiceConstants.CONNECTION_STRING);

            String sql = "DELETE ROOM_MEMBER WHERE username = '" 
                    + user.getUsername() + "' AND name = '" + this.name + "');";
            conn.createStatement().executeUpdate(sql);
            conn.close();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        }
    }

    private boolean makeRoom() {
        try {
            Class.forName(ServiceConstants.SQLITE_CLASS_NAME);
            Connection conn = DriverManager.getConnection(ServiceConstants.CONNECTION_STRING);
            
            String sql = "INSERT INTO ROOM (NAME, PASSWORD) " 
                    + "VALUES ('" + this.name + "', '" + this.password + "');";
            conn.createStatement().executeUpdate(sql);
            conn.close();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        }
    }
    
}
