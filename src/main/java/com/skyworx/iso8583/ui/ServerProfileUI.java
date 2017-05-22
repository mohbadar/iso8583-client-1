package com.skyworx.iso8583.ui;

import com.skyworx.iso8583.EventBus;
import com.skyworx.iso8583.domain.Message;
import com.skyworx.iso8583.domain.ServerProfile;
import com.skyworx.iso8583.domain.ServerSettingClosed;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServerProfileUI {

    public TableView<ServerProfile> tableProfiles;
    public TextField txtHost;
    public TextField txtPort;
    public ChoiceBox<ServerProfile.Packager> cbPackager;
    public ChoiceBox<ServerProfile.Channel> cbChannel;

    public ObjectProperty<ServerProfile> selectedProfile = new SimpleObjectProperty<>();
    public TextField txtName;
    public TextField txtGeneric;
    public TextField txtHeader;
    private Map<UUID, ServerProfile> serverProfileMaps;

    public void initialize(){
        cbPackager.setItems(FXCollections.observableArrayList(ServerProfile.Packager.values()));
        cbChannel.setItems(FXCollections.observableArrayList(ServerProfile.Channel.values()));
        cbPackager.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == ServerProfile.Packager.GENERIC){
                txtGeneric.disableProperty().set(false);
            } else {
                txtGeneric.disableProperty().set(true);
            }
        });
        List<ServerProfile> all = ServerProfile.findAll();
        this.serverProfileMaps = all.stream().collect(Collectors.toMap(ServerProfile::getId, Function.identity()));
        this.tableProfiles.setItems(FXCollections.observableList(all));

        this.tableProfiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                selectedProfile.set(Optional.ofNullable(newValue).map(ServerProfile::copy).orElse(new ServerProfile())));

        this.selectedProfile.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                txtName.textProperty().unbindBidirectional(oldValue.nameProperty());
                txtHost.textProperty().unbindBidirectional(oldValue.hostProperty());
                txtPort.textProperty().unbindBidirectional(oldValue.portProperty());
                cbPackager.valueProperty().unbindBidirectional(oldValue.packagerProperty());
                cbChannel.valueProperty().unbindBidirectional(oldValue.channelProperty());
                txtGeneric.textProperty().unbindBidirectional(oldValue.genericFileNameProperty());
                txtHeader.textProperty().unbindBidirectional(oldValue.channelHeaderProperty());
            }

            if(newValue != null){
                txtName.textProperty().bindBidirectional(newValue.nameProperty());
                txtHost.textProperty().bindBidirectional(newValue.hostProperty());
                txtPort.textProperty().bindBidirectional(newValue.portProperty(), new IntegerStringConverter());
                cbPackager.valueProperty().bindBidirectional(newValue.packagerProperty());
                cbChannel.valueProperty().bindBidirectional(newValue.channelProperty());
                txtGeneric.textProperty().bindBidirectional(newValue.genericFileNameProperty());
                txtHeader.textProperty().bindBidirectional(newValue.channelHeaderProperty());
            }
        });

        this.selectedProfile.set(new ServerProfile());
    }

    public void removeSelected(ActionEvent actionEvent) {
        ServerProfile serverProfile = this.selectedProfile.get();
        UUID id = serverProfile.getId();
        ServerProfile.delete(id);
        this.tableProfiles.getItems().remove(serverProfileMaps.get(id));
        this.serverProfileMaps.remove(id);
    }

    public void addNew(ActionEvent actionEvent) {
        this.selectedProfile.set(new ServerProfile());
    }

    public void saveSelected(ActionEvent actionEvent) {
        ServerProfile serverProfile = this.selectedProfile.get();
        UUID existingId = serverProfile.getId();
        serverProfile.save();
        if(existingId == null){
            this.serverProfileMaps.put(serverProfile.getId(), serverProfile);
            this.tableProfiles.getItems().add(serverProfile);
        } else {
            ServerProfile existing = this.serverProfileMaps.get(existingId);
            existing.update(serverProfile);
        }
        this.selectedProfile.set(new ServerProfile());
    }

    public void cancel(ActionEvent actionEvent) {
        this.selectedProfile.set(new ServerProfile());
    }

    public static void show(){
        try {
            HBox serverProfileUI = FXMLLoader.load(ServerProfileUI.class.getResource("ServerProfileUI.fxml"));
            Scene scene = new Scene(serverProfileUI);
            Stage stage = new Stage();
            stage.setOnCloseRequest(event -> {
                EventBus.post(new ServerSettingClosed());
            });
            stage.setScene(scene);
            stage.setTitle("Server Profile Setting");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
