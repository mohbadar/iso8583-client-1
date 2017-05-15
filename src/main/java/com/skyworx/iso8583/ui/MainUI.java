package com.skyworx.iso8583.ui;

import com.skyworx.iso8583.domain.Message;
import com.skyworx.iso8583.repository.MessageRepository;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOResponseListener;
import org.jpos.iso.MUX;
import org.jpos.util.NameRegistrar;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainUI {
    private final MessageRepository messageRepository = MessageRepository.getInstance();
    public ListView<Message> messageList;
    private ObjectProperty<Message> messageProperty = new SimpleObjectProperty<>();
    public ComboBox<String> muxList;
    public TextField newMessage;
    public TextArea consoleArea;
    public TextField newMti;
    public VBox selectedMessageContainer;

    public void initialize(){
        reloadMuxList();


        messageList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        messageList.setItems(FXCollections.observableList(Message.findAll()));
        messageList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            messageProperty.set(newValue);
        });

        messageProperty.addListener((observable, oldValue, newValue) -> {

            if(newValue != null){
                VBox vBox = SelectedMessageUI.create(newValue);
                selectedMessageContainer.getChildren().clear();
                selectedMessageContainer.getChildren().add(vBox);
            }
        });



    }

    public void saveMessage(ActionEvent actionEvent) {
        Message selectedItem = messageProperty.get();
        if(selectedItem != null){
            selectedItem.save();
            ObservableList<Message> items = FXCollections.observableList(this.messageList.getItems());
            this.messageList.setItems(null); //Redraw listview
            this.messageList.setItems(items);
        }
    }

    public void removeMessage(ActionEvent actionEvent) {
        Message message = this.messageProperty.get();
        this.messageList.getItems().remove(message);
        UUID id = message.getId();
        if(id != null){
            Message.delete(id);
        }
    }

    public void reloadMuxList(){
        List<String> collect = NameRegistrar.getAsMap().keySet().stream().filter(s -> s.indexOf("mux.") == 0).collect(Collectors.toList());
        muxList.setItems(FXCollections.observableList(collect));
    }

    public void addNewMessage(ActionEvent actionEvent) {
        Message e = new Message();
        e.setName(newMessage.getText());
        e.setMti(newMti.getText());
        this.messageList.getItems().add(e);
        e.save();
    }

    public void kirimPesan(ActionEvent actionEvent) {
        String muxName = muxList.getValue();
        if(muxName == null || "".equals(muxName.trim())){
            showAlert("Silahkan pilih mux tersedia");
            return;
        }

        Message message = this.messageProperty.get();
        if(message == null){
            showAlert("Tidak ada pesan yang dipilih");
            return;
        }

        try {
            MUX mux = (MUX) NameRegistrar.get(muxName);
            mux.request(message.toIso(), 10000L, (isoMsg, o) -> {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                isoMsg.dump(new PrintStream(bos),"");
                Platform.runLater(() -> {
                    consoleArea.appendText(new String(bos.toByteArray()));
                });
            },null);
        } catch (NameRegistrar.NotFoundException e) {
            showAlert(e.getMessage());
        } catch (ISOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String contentText) {
        new Alert(Alert.AlertType.ERROR, contentText).show();
    }
}
