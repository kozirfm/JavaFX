package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 5050;

    private boolean authenticated;
    private String nickname;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        if (!authenticated) {
            nickname = "";
        }
        textArea.clear();
        setTitle("Messenger");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authenticated = false;
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/authok ")) {
                            setAuthenticated(true);
                            nickname = str.split(" ")[1];
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }

                    setTitle("Messenger : " + nickname);

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        //Меняет собственное имя на "Я: " для более понятного отображения
                        if (str.startsWith(nickname)) {
                            String[] token = str.split(" ", 2);
                            textArea.appendText("Я: " + token[1] + "\n");
                        } else {
                            textArea.appendText(str + "\n");
                        }
                        if (str.equals("/end")) {
                            setAuthenticated(false);
                            break;
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("Сервер отключился");
                    setAuthenticated(false);
                } catch (IOException e) {
                    e.getStackTrace();
                } finally {
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

    public void sendMessage() {
        if (textField.getText().trim().length() > 0) {
            try {
                out.writeUTF(textField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }

            textField.clear();
            textField.requestFocus();
        }

    }

    public void pressEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)
            sendMessage();
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            passwordField.clear();

        } catch (NullPointerException e) {
            System.out.println("Сервер не доступен");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setTitle(String title) {
        Platform.runLater(() -> {
            ((Stage) textField.getScene().getWindow()).setTitle(title);
        });
    }
}
