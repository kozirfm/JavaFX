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
                System.out.println("Клиент подключился");
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

    //Метод приватных сообщений. В параметрах получатель, отправитель и сообщение.
    public void privateMessage(String recipient, String sender, String msg) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(recipient) || c.getNick().equals(sender)) {
                c.sendMessage(sender + ": " + msg);
            }
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

}
