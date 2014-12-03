package chat.users;

import chat.db.DbField;
import chat.db.Model;
import static chat.services.ServiceConstants.EQUAL;

public class User extends Model {
    
    @DbField(type="pk")
    public String username;
    
    @DbField(type="field")
    public String password;
    
    private Boolean exists = false;
    
    public User() {}
    
    public User (String username, String password) {
        this.username = username;
        this.password = password;
        
        if (getOne(this.username) != null) {
            exists = true;
        }
    }
    
    public Boolean exists() {
        return this.exists;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public boolean signIn() {
        return this.exists && checkPassword();
    }

    public boolean signUp() {
        if (username == null || password == null) {
            return false;
        } else return !this.exists() && create(this);
    }

    private boolean checkPassword() {
        return search(getFields("", this), this, EQUAL).size() == 1;
    }
}