package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Controller {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;

    public void sendMessage() {
        textArea.setText(textField.getText() + "\n" + textArea.getText());
        textField.setText("");
    }

    public void pressEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER)
            sendMessage();
    }
}
