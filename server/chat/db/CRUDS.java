/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.db;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author josepadilla
 */
public interface CRUDS {
    
    boolean create(Object object);
    
    ArrayList<Object> getAll();
    
    Object getOne(Object pk);
    
    boolean delete(Object pk);
    
    ArrayList<Object> search(HashMap<String, String> fields, Object object, Boolean equalOrLike);
    
}
