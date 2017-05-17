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

    public TextField messageName;
    public TextField mtiField;
    public TableView<Message.BitMessage> messageEditTable;
    public TextField bitField;
    public TextField valueField;
    private ObjectProperty<Message> selectedMessage = new SimpleObjectProperty<>();
    private ObjectProperty<Message.BitMessage> selectedBitMessage = new SimpleObjectProperty<>();

    public static VBox create(ObjectProperty<Message> selectedMessage){
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

    public void bind(Message oldMessage, Message newMessage){
        if(oldMessage != null){
            this.messageName.textProperty().unbindBidirectional(oldMessage.nameProperty());
            this.mtiField.textProperty().bindBidirectional(oldMessage.mtiProperty());
        }

        if(newMessage != null){
            this.messageName.textProperty().bindBidirectional(newMessage.nameProperty());
            this.mtiField.textProperty().bindBidirectional(newMessage.mtiProperty());
            this.messageEditTable.setItems(newMessage.bits());
        }


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

    private void initData(ObjectProperty<Message> message){
        this.selectedMessage.bindBidirectional(message);
        this.selectedMessage.addListener((observable, oldValue, newValue) -> {
            bind(oldValue, newValue);
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

    public void saveMessage(ActionEvent actionEvent) {
        Message message = this.selectedMessage.get();
        if (message != null) {
            message.save();
        }
    }
}
