package chat.services;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author josepadilla
 */
public interface ServiceConstants {
    String TYPE = "type";
    String ERROR = "error";
    String SUCCESS = "success";
    String MESSAGE = "message";
    String USER = "user";
    String USERNAME = "username";
    String PASSWORD = "password";
    String SQLITE_CLASS_NAME = "org.sqlite.JDBC";
    String SQLITE_CONNECTION = "jdbc:sqlite:";
    String DB_NAME = "chat.db";
    String CONNECTION_STRING = SQLITE_CONNECTION + DB_NAME;
    String FIELD_MUST_NOT_BE_NULL = "Field must not be null.";
    
    Boolean EQUAL = true;
    Boolean LIKE = false;
}
