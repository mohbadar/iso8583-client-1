package com.skyworx.iso8583.ui;

import com.skyworx.iso8583.domain.Message;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;


public class SelectedMessageUI {

    private Message selectedMessage;
    public TextField selectedMessageName;
    public TableView<Message.BitMessage> messageEditTable;
    public TextField bitField;
    public TextField valueField;
    private ObjectProperty<Message.BitMessage> selectedBitMessage = new SimpleObjectProperty<>();

    public static VBox create(Message selectedMessage){
        try {
            FXMLLoader loader = new FXMLLoader(SelectedMessageUI.class.getResource("SelectedMessageUI.fxml"));
            VBox box = loader.load();
            SelectedMessageUI messageController = loader.getController();
            messageController.initData(selectedMessage);
            return box;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initData(Message message){
        this.selectedMessage = message;
        this.selectedMessageName.textProperty().bindBidirectional(this.selectedMessage.nameProperty());
        this.messageEditTable.setItems(this.selectedMessage.bits());

        this.selectedBitMessage.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                bitField.textProperty().unbindBidirectional(oldValue.bitProperty());
                valueField.textProperty().unbindBidirectional(oldValue.valueProperty());
            }

            if(newValue != null){
                bitField.textProperty().bindBidirectional(newValue.bitProperty(), new IntegerStringConverter());
                valueField.textProperty().bindBidirectional(newValue.valueProperty());
            }
        });

        messageEditTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedBitMessage.set(newValue);
        });

    }

    public void clearSelectedBitMessage(ActionEvent actionEvent) {
        this.selectedBitMessage.set(new Message.BitMessage());
    }

    public void saveSelectedBitMessage(ActionEvent actionEvent) {
        Message.BitMessage bitMessage = this.selectedBitMessage.get();
        if(!messageEditTable.getItems().contains(bitMessage)){
            messageEditTable.getItems().add(bitMessage);
        }
    }

}
