package chat.server;

import chat.users.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ChatServer {

    private static final int PORT = 9001;

    private static HashSet<User> users = new HashSet<>();

    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {

        private User user;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {

                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("new connection received...");
                boolean created = false;
                while (true) {
                    out.println("LOGINUSERPASSWORD/SIGNUPUSERPASSWORD");
                    String input = in.readLine();

                    if (input == null) {
                        return;
                    }

                    String type = Character.toString(input.charAt(0));
                    String[] input2 = input.substring(1).split(":");

                    if (input2.length != 2) {
                        return;
                    }

                    String name = input2[0];
                    String password = input2[1];

                    user = new User(name, password);

                    if (!user.exists() && type.equalsIgnoreCase("S")) {
                        new User().create(user);
                        user = new User(name, password);
                        created = true;
                    }

                    synchronized (users) {
                        if (user.exists() && !users.contains(user) && user.signIn() && !type.equalsIgnoreCase("S")) {
                            System.out.println("Welcome to pichat " + user + "!");
                            users.add(user);
                            break;
                        } else {
                            if (created) {
                                out.println("USERCREATED");
                            } else {
                                out.println("USERREJECTED/USERTAKEN");
                            }
                        }
                    }
                }

                out.println("USERACCEPTED");
                writers.add(out);

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    System.out.println(user + " sent a message!");
                    for (PrintWriter writer : writers) {
                        if (writer != out) {
                            writer.println("MESSAGE " + user.getUsername() + ":" + input);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (user != null) {
                    users.remove(user);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
