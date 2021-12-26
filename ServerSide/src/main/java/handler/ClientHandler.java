package handler;


import Services.MyServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nickName;

    public ClientHandler(MyServer myServer, Socket socket) throws IOException {

        this.myServer = myServer;
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        sendMessage("Привет." + "\n" + "Пройдите аутентификацию.");
        Thread thread = new Thread(() -> {
            try {
                authentication();
                receiveMessage();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        thread.start();


    }

    public void authentication() throws Exception {
        while (true) {
            String message = dis.readUTF();
            if (message.startsWith("/start")) {
                String[] arr = message.split("-", 3);
                if (arr.length != 3) {
                    throw new IllegalAccessException();
                }
                final String nick = myServer
                        .getAuthenticationServices()
                        .getNickNameByLoginAndPassword(arr[1].trim(), arr[2].trim());
                if (nick != null) {
                    if (!myServer.nickNameIsBusy(nick)) {
                        sendMessage("/start " + nick);
                        this.nickName = nick;
                        myServer.sendMessageToClients(nickName + " зашел в чат.");
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMessage("Ник занят.");
                    }
                } else {
                    sendMessage("Ошибка в логине или в пароле.");
                }
            }
            if (message.startsWith("/close")) {
                closeConnection();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage() throws IOException {
        while (true) {
            String message = dis.readUTF();
            if (message.startsWith("/")) {
                if (message.startsWith("/close")) {
                    myServer.sendMessageToClients(nickName + " вышел.");
                    break;
                }
                if (message.startsWith("/nick")) {
                    String[] arr = message.split("-", 3);
                    String toNick = arr[1].trim();
                    String toMessage = arr[2].trim();
                    myServer.sendMessageToClient(this, toNick, toMessage);
                }
                continue;
            }
            myServer.sendMessageToClients(nickName + ": -> " + message);
        }
    }

    private void closeConnection() {
        myServer.unSubscribe(this);
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName() {
        return nickName;
    }
}
