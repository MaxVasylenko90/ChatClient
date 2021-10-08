package ua.kiev.prog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Chat {
    private static Scanner scanner = new Scanner(System.in);
    private static User user;

    public static void start() throws IOException {
        System.out.println("Enter command:");
        String command = scanner.nextLine();
        if (command.equals("login")) {
            int resLog = login();
            if (resLog != 200) { // 200 OK
                System.out.println("HTTP error occurred: " + resLog);
                return;
            }
            startChat();
        } else if (command.equals("reg")) {
            int resReg = registration();
            if (resReg != 200) { // 200 OK
                System.out.println("HTTP error occurred: " + resReg);
                return;
            }
            login();
            startChat();
        } else if (command.equals("get users list")) {
            getUsersList();
            System.out.println("Choose user to write him a private message");
            String userName = scanner.nextLine();
            startChat(userName);
        }
        else {
            System.out.println("Wrong command!");
            return;
        }
    }

    public static int registration() throws IOException {
        System.out.println("Enter new login: ");
        String login = scanner.nextLine();
        System.out.println("Enter new password: ");
        String pass = scanner.nextLine();
        user = new User(login, pass);
        URL url = new URL(Utils.getURL() + "/registration");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            String json = user.toJSON();
            os.write(json.getBytes(StandardCharsets.UTF_8));
            return connection.getResponseCode();
        }
    }

    public static int login() throws IOException {
        System.out.println("Enter your login: ");
        String login = scanner.nextLine();
        System.out.println("Enter your password: ");
        String pass = scanner.nextLine();
        user = new User(login, pass);
        URL url = new URL(Utils.getURL() + "/login");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try(OutputStream os = connection.getOutputStream()) {
            String json = user.toJSON();
            os.write(json.getBytes(StandardCharsets.UTF_8));
            return connection.getResponseCode();
        }
    }

    public static void getUsersList() throws IOException {
        URL url = new URL(Utils.getURL() + "/usersList");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream is = connection.getInputStream()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte [] buf = new byte[10240];
            int r;
            do {
                r = is.read(buf);
                if(r > 0)
                    baos.write(buf, 0 , r);
            } while (r != -1);
            byte[] buf2 = baos.toByteArray();
            String strBuf = new String(buf2, StandardCharsets.UTF_8);
            System.out.println(strBuf);
        }
    }

    public static void startChat() throws IOException {
        String userName = user.getLogin();
        Thread th = new Thread(new GetThread());
        th.setDaemon(true);
        th.start();
        System.out.println("Enter your message: ");
        while (true) {
            String text = scanner.nextLine();
            if (text.isEmpty()) break;
            else if(text.equals("/command"))
                start();
            Message m = new Message(userName, text);
            int res = m.send(Utils.getURL() + "/add");
            if (res != 200) { // 200 OK
                System.out.println("HTTP error occured: " + res);
                return;
            }
        }
    }
    public static void startChat(String toUserName) throws IOException {
        String userName = user.getLogin();
        Thread th = new Thread(new GetThread());
        th.setDaemon(true);
        th.start();
        System.out.println("Enter your message: ");
        while (true) {
            String text = scanner.nextLine();
            if (text.isEmpty()) break;
            else if(text.equals("/command"))
                start();
            Message m = new Message(userName, toUserName, text);
            int res = m.send(Utils.getURL() + "/add");
            if (res != 200) { // 200 OK
                System.out.println("HTTP error occured: " + res);
                return;
            }
        }
    }
}
