package Services;


import Services.interfaces.AuthenticationServices;
import handler.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private static final Integer PORT = 8808;
    private AuthenticationServices authenticationServices;
    private List<ClientHandler> handlerList;

    public MyServer() {
        System.out.println("Запуск сервера.");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            authenticationServices = new AuthenticationServicesImpl();
            authenticationServices.start();
            handlerList = new ArrayList<>();
            while (true) {
                System.out.println("Ожидаем подключения.");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился.");
                new ClientHandler(this, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            authenticationServices.stop();
        }
    }

    public synchronized boolean nickNameIsBusy(String nickName) {
        return handlerList
                .stream()
                .anyMatch(clientHandler -> clientHandler.getNickName().equalsIgnoreCase(nickName));

    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        handlerList.add(clientHandler);

    }

    public synchronized void unSubscribe(ClientHandler clientHandler) {
        handlerList.remove(clientHandler);
    }

    public synchronized void sendMessageToClients(String message) {
        handlerList.forEach(clientHandler -> clientHandler.sendMessage(message));
    }

    public synchronized void sendMessageToClient(ClientHandler fromNick, String toNick, String message) {
        for (ClientHandler clientHandler : handlerList) {
            if (clientHandler.getNickName().equals(toNick)) {
                clientHandler.sendMessage("От: " + fromNick.getNickName() + " -> " + toNick + ": " + message);
                fromNick.sendMessage(fromNick.getNickName() + " -> " + toNick + ": " + message);
                return;
            }
        }
        fromNick.sendMessage("Не возможно отправить личное сообщение " + toNick);
    }

    public AuthenticationServices getAuthenticationServices() {
        return this.authenticationServices;
    }
}
