/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.db;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josepadilla
 */
public abstract class Model implements CRUDS {
    
    private HashMap<String, String> errors;
    
    private final Db db = new Db();

    @Override
    public boolean create(Object object) {
        handleError(DbUtils.validateFields(this.getClass(), object));
        return db.create(this.getClass(), object);
    }

    @Override
    public ArrayList<Object> getAll() {
        return db.selectAll(this.getClass());
    }

    @Override
    public Object getOne(Object pk) {
        handleError(DbUtils.validatePk(this.getClass(), pk));
        return db.getOneByPk(this.getClass(), pk);
    }

    @Override
    public boolean delete(Object pk) {
        handleError(DbUtils.validatePk(this.getClass(), pk));
        return db.delete(this.getClass(), pk);
    }

    @Override
    public ArrayList<Object> search(HashMap<String, String> fields, Object object, Boolean equalOrLike) {
        return (equalOrLike)? db.searchEqual(this.getClass(), fields, object) : db.searchLike(this.getClass(), fields, object);
    }
    
    private void handleError(HashMap<String, String> errors) {
        if(!errors.isEmpty()) {
            this.errors = new HashMap<>();
        } else {
            this.errors = errors;
        }
    }
    
    public HashMap<String, String> errors() {
        return this.errors;
    }
    
    @Override
    public String toString() {
        try {
            return DbUtils.getPkField(this.getClass()).get(this).toString();
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public HashMap<String, String> getFields(String excludedFields, Object object) {
        HashMap<String, String> fields = new HashMap<>();
        Field[] dbFields = DbUtils.getFields(this.getClass());
        List<String> excluded = Arrays.asList(excludedFields.split(","));
        for (Field dbField : dbFields) {
            if (!excluded.contains(dbField.getName())) {
                try {
                    fields.put(dbField.getName(), dbField.get(object).toString());
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return fields;
    }
    
}
