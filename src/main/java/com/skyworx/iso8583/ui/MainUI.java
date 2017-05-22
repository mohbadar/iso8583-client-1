package com.skyworx.iso8583.ui;

import com.google.common.eventbus.Subscribe;
import com.skyworx.iso8583.EventBus;
import com.skyworx.iso8583.domain.*;
import com.skyworx.iso8583.repository.MessageRepository;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.LogListener;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainUI {
    public TableView<Message> messageList;
    public TableView<Message> historyList;
    public ComboBox<ServerProfile> cbServerProfiles;
    public Button connectButton;
    public MenuItem settingMenu;
    public Button btnSend;

    private ObjectProperty<Message> messageProperty = new SimpleObjectProperty<>();
    public TextArea consoleArea;
    public VBox selectedMessageContainer;

    public BooleanProperty disableBtnConnect = new SimpleBooleanProperty(true);
    public StringProperty btnConnectName = new SimpleStringProperty("Connect");
    public ObjectProperty<ServerProfile> selectedServerProfileProperty = new SimpleObjectProperty<>();

    private ObservableList<ServerProfile> serverProfiles = FXCollections.observableArrayList();

    private ISOMUX mux;

    public void initialize(){
        EventBus.register(this);
        loadServerProfiles();
        historyList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        historyList.setItems(FXCollections.observableList(Message.findAllHistories()));
        historyList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> messageProperty.set(newValue));

        messageList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        messageList.setItems(FXCollections.observableList(Message.findAll()));
        messageList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            messageProperty.set(newValue);
        });



        attachSelectedMessageUI();

        messageProperty.set(new Message());

        connectButton.textProperty().bindBidirectional(btnConnectName);
        connectButton.disableProperty().bindBidirectional(disableBtnConnect);
        selectedServerProfileProperty.addListener((observable, oldValue, newValue) -> {
            disableBtnConnect.set(newValue == null);
        });

        settingMenu.disableProperty().bind(cbServerProfiles.disableProperty());
        btnSend.disableProperty().bind(cbServerProfiles.disableProperty().not());
        selectedServerProfileProperty.bind(cbServerProfiles.getSelectionModel().selectedItemProperty());
    }

    public void loadServerProfiles(){
        cbServerProfiles.setItems(null);
        serverProfiles.clear();
        serverProfiles.addAll(ServerProfile.findAll());
        cbServerProfiles.setItems(serverProfiles);
    }

    public void attachSelectedMessageUI(){
        VBox vBox = SelectedMessageUI.create(this.messageProperty);
        selectedMessageContainer.getChildren().clear();
        selectedMessageContainer.getChildren().add(vBox);
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

    public void kirimPesan(ActionEvent actionEvent) {
        Message selectedMessage = messageProperty.get();
        if(this.mux != null && selectedMessage != null){
            consoleArea.clear();
            ISOMsg m = selectedMessage.toIso();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            m.dump(new PrintStream(bos), "out |");
            consoleArea.appendText(new String(bos.toByteArray()));
            consoleArea.appendText("\r\n");
            this.mux.send(m);
            selectedMessage.createHistory(this.selectedServerProfileProperty.get().toString());
        }
    }

    private void showAlert(String contentText) {
        new Alert(Alert.AlertType.ERROR, contentText).show();
    }

    @Subscribe
    public void handleMessageSave(MessageSaved event){
        if(event.isNew()){
            Platform.runLater(() -> this.messageList.getItems().add(event.getMessage()));
        }
    }

    @Subscribe
    public void handleMessageHistory(MessageHistoryCreated event){
        Platform.runLater(() -> {
            historyList.getItems().add(0,event.getMessage());
        });
    }

    @Subscribe
    public void handleSettingClosed(ServerSettingClosed event){
        Platform.runLater(this::loadServerProfiles);
    }
    public void toggleServerConnection(ActionEvent actionEvent) {
        try {
            if(this.mux != null){
                mux.setConnect(false);
                this.disableBtnConnect.set(false);
                this.btnConnectName.set("Connect");
                cbServerProfiles.disableProperty().set(false);
                mux = null;
            } else {
                BaseChannel channel = selectedServerProfileProperty.get().createChannel();
                Logger logger = new Logger();
                logger.addListener(ev -> {
                    if("error".equals(ev.getTag())){
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ev.dump(new PrintStream(bos), ev.getTag()+" |");
                        Platform.runLater(() -> {
                            consoleArea.appendText(new String(bos.toByteArray()));
                            consoleArea.appendText("\r\n");
                        });
                    }
                    return ev;
                });
                mux = new ISOMUX(channel, logger,"iso-client");
                mux.setISORequestListener((source, m) -> {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    m.dump(new PrintStream(bos), "in |");
                    Platform.runLater(() -> {
                        consoleArea.appendText(new String(bos.toByteArray()));
                        consoleArea.appendText("\r\n");
                    });
                    return false;
                });
                this.btnConnectName.set("Connecting...");
                this.disableBtnConnect.set(true);
                cbServerProfiles.disableProperty().set(true);
                new Thread(mux).start();
                new Thread(() -> {
                    int tryCount = 0;
                    while (true){
                        if(mux.isConnected()){
                            Platform.runLater(() -> {
                                btnConnectName.set("Disconnect");
                                disableBtnConnect.set(false);
                            });
                            break;
                        } else if(++tryCount == 5){
                            Platform.runLater(() -> {
                                cbServerProfiles.disableProperty().set(false);
                                disableBtnConnect.set(false);
                                btnConnectName.set("Connect");
                                mux = null;
                            });
                            break;
                        }
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }).start();
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            showAlert("Error connecting to server, cause by: "+e.getMessage());
        }
    }

    public void openSettings(ActionEvent actionEvent) {
        ServerProfileUI.show();
    }

    public void close(ActionEvent actionEvent) {
        System.exit(1);
    }
}
