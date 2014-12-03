/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.db;

import chat.services.ServiceConstants;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josepadilla
 */
public class DbUtils {
    
    static ArrayList<Object> rsToArrayOfObjects(ResultSet rs, Class model) {
        ArrayList<Object> objects = new ArrayList<>();
        try {
            while (rs.next()) {
                Object obj = model.newInstance();
                Field[] fields = getFields(model);
                for (int i = 0; i < rs.getMetaData().getColumnCount() && i < fields.length; i++) {
                    fields[i].set(obj, rs.getObject(i+1));
                }
                objects.add(obj);
            }
        } catch (InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return objects;
    }

    static Object rsToObject(ResultSet rs, Class model) {
        Object object = null;
        try {
            if (rs.next()) {
                Object obj = model.newInstance();
                Field[] fields = getFields(model);
                for (int i = 0; i < rs.getMetaData().getColumnCount() && i < fields.length; i++) {
                    fields[i].set(obj, rs.getObject(i+1));
                }
                object = obj;
            }
        } catch (InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }
    
    static HashMap<String, String> validateFields(Class model, Object object) {
        HashMap<String, String> errors = new HashMap<>();
        Field[] fields = getFields(model);
        for (Field f : fields) {
            try {
                if (f.get(object) == null) {
                    errors.put(f.getName(), ServiceConstants.FIELD_MUST_NOT_BE_NULL);
                } else if (!f.getClass().isAssignableFrom(f.get(object).getClass())) {
                    errors.put(f.getName(), "Value assigned to field is not applicable.");
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return errors;
    }
    
    static HashMap<String, String> validatePk(Class model, Object pk) {
        HashMap<String, String> errors = new HashMap<>();
        Field f = getPkField(model);
        if (pk == null) {
            errors.put(f.getName(), ServiceConstants.FIELD_MUST_NOT_BE_NULL);
        } else if (!f.getClass().isAssignableFrom(pk.getClass())) {
            errors.put(f.getName(), "Value assigned to field is not applicable.");
        }
        return errors;
    }
    
    static Field getPkField(Class model) {
        Field f = null;
        for (Field field : getFields(model)) {
            Annotation annotation = field.getAnnotation(DbField.class);
            if (annotation instanceof DbField) {
                DbField pf = (DbField) annotation;
                if (pf.type().equals("pk")) {
                    f = field;
                    break;
                }
            }
        }
        return f;
    }
    
    static Field[] getFields(Class model) {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : model.getFields()) {
            Annotation annotation = field.getAnnotation(DbField.class);
            if (annotation != null) {
                fields.add(field);
            }
        }
        Field[] dbFields = new Field[fields.size()];
        for (int i = 0; i < dbFields.length; i++) {
            dbFields[i] = fields.get(i);
        }
        return dbFields;
    }
     
}
