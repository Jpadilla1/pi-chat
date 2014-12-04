/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josepadilla
 */
public class Db {

    private ResultSet rs = null;
    private Connection conn = null;

    private String SQLITE_CLASS_NAME = "org.sqlite.JDBC";
    private String SQLITE_CONNECTION = "jdbc:sqlite:";
    private String DB_NAME = "chat.db";
    private String CONNECTION_STRING = SQLITE_CONNECTION + DB_NAME;

    public Db() {
        try {
            Class.forName(SQLITE_CLASS_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getConnection() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING);
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void closeConnection() {
        try {
            if (rs != null) {
                rs.close();
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<Object> selectAll(Class model) {
        ArrayList<Object> objects = new ArrayList<>();
        String sql = "SELECT * FROM " + model.getSimpleName() + ";";
        getConnection();
        try {
            objects = DbUtils.rsToArrayOfObjects(conn.createStatement().executeQuery(sql), model);
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        closeConnection();
        return objects;
    }
    
    public ArrayList<Object> searchEqual(Class model, HashMap<String, String> fields, Object object) {
        ArrayList<Object> objects = new ArrayList<>();
        String sql = "SELECT * FROM " + model.getSimpleName()+ " WHERE ";
        int i = 0;
        for (Map.Entry<String, String> entrySet : fields.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            if (i > 0) {
                sql += " AND ";
            }
            sql += key + " = '" + value + "'";
            i++;
        }
        sql += ";";
        getConnection();
        try {
            objects = DbUtils.rsToArrayOfObjects(conn.createStatement().executeQuery(sql), model);
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        closeConnection();
        return objects;
    }
    
    public ArrayList<Object> searchLike(Class model, HashMap<String, String> fields, Object object) {
        ArrayList<Object> objects = new ArrayList<>();
        String sql = "SELECT * FROM " + model.getSimpleName()+ " WHERE ";
        int i = 0;
        for (Map.Entry<String, String> entrySet : fields.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            if (i > 0) {
                sql += " AND ";
            }
            sql += key + " LIKE %'" + value + "'";
            i++;
        }
        sql += ";";
        getConnection();
        try {
            objects = DbUtils.rsToArrayOfObjects(conn.createStatement().executeQuery(sql), model);
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        closeConnection();
        return objects;
    }

    public Object getOneByPk(Class model, Object pk) {
        Object object = null;
        Field f = DbUtils.getPkField(model);
        if (f == null) {
            return object;
        }
        String sql = "SELECT * FROM " + model.getSimpleName() + " WHERE " + f.getName() + " = '" + pk + "';";
        
        getConnection();
        try {
            object = DbUtils.rsToObject(conn.createStatement().executeQuery(sql), model);
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        closeConnection();

        return object;
    }

    public boolean create(Class model, Object object) {
        try {
            Field[] fields = DbUtils.getFields(model);
            String sql = "INSERT INTO " + model.getSimpleName()+ " (";
            for (int i = 0; i < fields.length - 1; i++) {
                sql += fields[i].getName() + ", ";
            }
            sql += fields[fields.length - 1].getName() + ") VALUES (";
            for (int i = 0; i < fields.length - 1; i++) {
                sql += "'" + fields[i].get(object) + "', ";
            }
            sql += "'" + fields[fields.length - 1].get(object) + "');";
            getConnection();
            conn.createStatement().executeUpdate(sql);
            closeConnection();
            return true;
        } catch (IllegalArgumentException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean delete(Class model, Object pk) {
        Field f = DbUtils.getPkField(model);
        if (f == null) {
            return false;
        }
        String sql = "DELETE FROM " + model.getSimpleName()+ " WHERE " + f.getName() + " = '" + pk.toString() + "';";
        try {
            getConnection();
            conn.createStatement().executeUpdate(sql);
            closeConnection();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
