import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.html.HTMLInputElement;

public class Main extends Application {

    private Scene scene;

    @Override
    public void start(Stage stage) throws Exception {
        scene = new Scene(new Chat(), 750, 500, Color.web("#666970"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("pichat");
        stage.show();
    }

}

class Chat extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    BufferedReader in;
    PrintWriter out;

    String username;
    String password;
    boolean authenticated = false;

    String serverAddress = "127.0.0.1";
    Socket socket;

    int messageCount = 0;

    public Chat() {
        try {
            socket = new Socket(serverAddress, 9001);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }

        // set listeners
        loadListeners();

        // load the web page
        webEngine.load(Chat.class.getResource("views/index.html").toExternalForm());

        //add the web view to the scene
        getChildren().add(browser);

    }

    private void loadListeners() {
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {

            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                // start firebug
                // webEngine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}"); 

                Document doc = webEngine.getDocument();
                if (newValue == Worker.State.SUCCEEDED) {
                    // note next classes are from org.w3c.dom domain
                    EventListener listener = (Event ev) -> {
                        switch (((Element) ev.getCurrentTarget()).getAttribute("id")) {
                            case "login-button":
                                HTMLInputElement usernameField = (HTMLInputElement) doc.getElementById("username");
                                username = usernameField.getValue();
                                HTMLInputElement passField = (HTMLInputElement) doc.getElementById("password");
                                password = passField.getValue();
                                processServerLogin();
                                break;
                            case "signup-button":
                                HTMLInputElement usernField = (HTMLInputElement) doc.getElementById("username");
                                username = usernField.getValue();
                                HTMLInputElement passwField = (HTMLInputElement) doc.getElementById("password");
                                password = passwField.getValue();
                                processServerSignup();
                                break;
                            case "logout-button":
                                logout();
                                break;
                            case "send-button":
                                HTMLInputElement textField = (HTMLInputElement) doc.getElementById("send-text");
                                if (!textField.getValue().isEmpty()) {
                                    sendMessage(textField.getValue());
                                }
                                break;
                        }
                    };

                    Element loginBtn = webEngine.getDocument().getElementById("login-button");
                    ((EventTarget) loginBtn).addEventListener("click", listener, false);

                    Element signupBtn = webEngine.getDocument().getElementById("signup-button");
                    ((EventTarget) signupBtn).addEventListener("click", listener, false);

                    Element logoutBtn = webEngine.getDocument().getElementById("logout-button");
                    ((EventTarget) logoutBtn).addEventListener("click", listener, false);

                    Element sendBtn = webEngine.getDocument().getElementById("send-button");
                    ((EventTarget) sendBtn).addEventListener("click", listener, false);
                }
            }

        });
    }

    private void sendMessage(String value) {
        out.println(value);
        addMessage(username, value, false);
        webEngine.executeScript("$('#send-text').val('');$('#send-text').focus();");
    }

    private void receiveMessage(String str) {
        String[] strs = str.split(":", 2);
        String text = "";
        for (int i = 1; i < strs.length; i++) {
            text += strs[i];
        }
        addMessage(strs[0], text, true);
    }

    private void addMessage(String user, String message, boolean side) {
        String sideStr = (side) ? "left" : "right";
        webEngine.executeScript("addMessage('" + user + "', '" + message + "', '" + sideStr + "');");
        webEngine.executeScript("$('#chat-box').scrollTop($('#" + messageCount++ + "').position().top+999999);");
    }

    private void loadChatUI() {
        webEngine.executeScript("$('#username').val(\"\");");
        webEngine.executeScript("$('#password').val(\"\");");
        webEngine.executeScript("$('#login').hide();");
        webEngine.executeScript("$('#chat').show();");

        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (authenticated) {
                    while (true) {
                        try {
                            String line = in.readLine();
                            System.out.println(line);
                            if (line.startsWith("MESSAGE")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        receiveMessage(line.substring(8));
                                    }
                                });

                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                return null;
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void loadLoginUI() {
        webEngine.executeScript("$('#chat').hide();");
        webEngine.executeScript("$('#login').show();");
    }

    private void processServerLogin() {
        if (!authenticated) {
            try {
                String line = in.readLine();
                System.out.println(line);

                if (line.startsWith("LOGINUSERPASSWORD/SIGNUPUSERPASSWORD")) {
                    out.println("L" + username + ":" + password);
                    line = in.readLine();
                    if (line.startsWith("USERACCEPTED")) {
                        authenticated = true;
                        loadChatUI();
                    } else if (line.startsWith("USERREJECTED")) {
                        webEngine.executeScript("$.snackbar({content: \"Username/password is incorrect!\", style: \"error\"});");
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Already authenticated!");
        }
    }

    private void processServerSignup() {
        if (!authenticated) {
            try {
                String line = in.readLine();
                System.out.println(line);

                if (line.startsWith("LOGINUSERPASSWORD/SIGNUPUSERPASSWORD")) {
                    out.println("S" + username + ":" + password);
                    line = in.readLine();
                    System.out.println(line);
                    if (line.startsWith("USERACCEPTED")) {
                        authenticated = true;
                        loadChatUI();
                    } else if (line.startsWith("USERCREATED")) {
                        webEngine.executeScript("$.snackbar({content: \"User created succesfully!\", style: \"success\"});");
                        loadChatUI();
                    } else if (line.startsWith("USERREJECTED/USERTAKEN")) {
                        webEngine.executeScript("$.snackbar({content: \"Username is already taken!\", style: \"error\"});");
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Already authenticated!");
        }
    }

    private void logout() {
        try {
            socket.close();
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 500;
    }
}
