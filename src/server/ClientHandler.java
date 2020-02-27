package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;
    Server server;
    private String nick;
    private String login;

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            throw new RuntimeException("Закрыли крестиком");
                        }
                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");
                            if (token.length < 3){
                                continue;
                            }
                            String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1];
                                if(!server.isLoginAuthorized(login)){
                                    sendMessage("/authok " + newNick);
                                    nick = newNick;
                                    server.subscribe(this);
                                    System.out.println("Клиент " + nick + " подключился");
                                    break;
                                }else{
                                    sendMessage("С этим логином уже авторизовались");
                                }
                            } else {
                                sendMessage("Неверный логин / пароль");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if(str.startsWith("/")){
                            if (str.equals("/end")) {
                                out.writeUTF("/end");
                                break;
                            }
                            if (str.startsWith("/w")){
                                String[] token = str.split(" ", 3);
                                if(token.length == 3){
                                    server.privateMessage(this, token[1], token[2]);
                                }
                            }
                        }else {
                            server.broadcastMessage(str, nick);
                        }
                    }
                } catch (RuntimeException e){
                    System.out.println("Закрыли крестиком");
                }
                catch (IOException e) {
                    e.getStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент " + nick + " отключился");
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
