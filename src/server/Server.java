package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket = null;

        final int PORT = 5050;

        DataInputStream in;
        DataOutputStream out;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            while (true) {
                socket = server.accept();
                new ClientHandler(socket, this);

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void broadcastMessage(String msg, String sender) {
        for (ClientHandler c : clients) {
            c.sendMessage(sender + ": " + msg);
        }
    }

    public void privateMessage(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] private [ %s ] : %s", sender.getNick(), receiver, msg);
        for (ClientHandler c : clients) {
            if (c.getNick().equals(receiver)) {
                c.sendMessage(message);
                if (!c.getNick().equals(receiver)){
                    sender.sendMessage(message);
                }
                return;
            }
        }
        sender.sendMessage("Пользователь не найден: " + receiver);
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public boolean isLoginAuthorized(String login){
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)){
                return  true;
            }
        }
        return false;
    }

    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler c: clients) {
            sb.append(c.getNick()).append(" ");
        }
        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }
}
