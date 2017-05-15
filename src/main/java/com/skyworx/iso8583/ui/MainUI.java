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
    public TableView<Message.BitMessage> messageEditTable;
    public ObjectProperty<Message> messageProperty = new SimpleObjectProperty<>();
    public TextField selectedMessageName;
    public ObjectProperty<Message.BitMessage> selectedBitMessage = new SimpleObjectProperty<>();
    public ComboBox<Integer> listBits;
    public TextField bitValue;
    public ComboBox<String> muxList;
    public TextField newMessage;
    public TextArea consoleArea;
    public TextField newMti;

    public void initialize(){
        reloadMuxList();
        List<Integer> collect = IntStream.range(1, 128).boxed().collect(Collectors.toList());
        listBits.setItems(FXCollections.observableList(collect));

        this.selectedBitMessage.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                listBits.valueProperty().unbindBidirectional(oldValue.bitProperty());
                bitValue.textProperty().unbindBidirectional(oldValue.valueProperty());
            }

            if(newValue != null){
                listBits.valueProperty().bindBidirectional(newValue.bitProperty());
                bitValue.textProperty().bindBidirectional(newValue.valueProperty());
            }
        });
        messageList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        messageList.setItems(FXCollections.observableList(Message.findAll()));
        messageList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            messageProperty.set(newValue);
        });

        messageProperty.addListener((observable, oldValue, newValue) -> {
            selectedBitMessage.set(new Message.BitMessage());
            if(oldValue != null){
                selectedMessageName.textProperty().unbindBidirectional(oldValue.nameProperty());
                messageEditTable.setItems(FXCollections.observableList(new ArrayList<>()));
            }
            if(newValue != null){
                selectedMessageName.textProperty().bindBidirectional(newValue.nameProperty());
                messageEditTable.setItems(FXCollections.observableList(newValue.getBits()));
            } else {
                this.selectedBitMessage.set(new Message.BitMessage());
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

    public void saveMessage(ActionEvent actionEvent) {
        Message selectedItem = messageProperty.get();
        if(selectedItem != null){
            selectedItem.setBits(new ArrayList<>(messageEditTable.getItems()));
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
