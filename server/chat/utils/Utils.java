package chat.utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author josepadilla
 */
public class Utils {
    
    public static ArrayList<HashMap<String, String>> rsToArray(ResultSet rs) {
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        try {
            HashMap<String, String> temp;
            while (rs.next()) {
                temp = new HashMap<>();
                int nColumns = rs.getMetaData().getColumnCount();
                for (int i = 0; i < nColumns; i++) {
                    temp.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                }
                data.add(temp);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return data;
    }
    
    public static HashMap<String, String> rsToMap(ResultSet rs) {
        HashMap<String, String> data = new HashMap<>();
        try {
            if (rs.next()) {
                int nColumns = rs.getMetaData().getColumnCount();
                for (int i = 0; i < nColumns; i++) {
                    data.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return data;
    }
}
