package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.CloudSettings;
import model.DAOFileItem;
import util.AppSettings;
import util.CloudSettingsManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Anton on 02.07.2016.
 */
public class SettingsController implements Initializable{
    CloudSettingsManager settingsMananger;
    Stage settingsStage;
    DAOFileItem dataManager;
    AppSettings appSettings;

    @FXML
    AnchorPane accountsTreePane;

    @FXML
    private Slider cloudRaidSpaceSlider;

    @FXML
    private Label totalSpaceLable;

    @FXML
    private Label availableSpaceLable;

    @FXML
    private Label cloudRaidSpaceLable;

    @FXML
    private PieChart chart;

    @FXML
    private Label cloudNameLable;

    @FXML
    private Label syncFolderPathLable;

    @FXML
    private void addAccountHandler(ActionEvent event){
        System.out.println("open dialog");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/addAccountDialog.fxml"));
            AnchorPane pane = loader.load();


            Stage stage = new Stage();
            ((AddAccountController)loader.getController()).init(settingsMananger, stage);
            stage.setTitle("Settings");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(settingsStage);
            Scene scene = new Scene(pane, 300, 350);
            stage.setScene(scene);
            stage.showAndWait();
            initAccountsTreeView();



        } catch (IOException e){

        }
    }

    /**
     * Метод открывает диалог выбора синхронизуемых папок из EFS на диск
     */
    @FXML
    void openSyncManagerHandler(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/syncManager.fxml"));
            AnchorPane pane = loader.load();


            Stage stage = new Stage();
            ((SyncManagerController)loader.getController()).setDataManager(dataManager);
            ((SyncManagerController)loader.getController()).init();
            ((SyncManagerController)loader.getController()).setStage(stage);
            stage.setTitle("Sync settings");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(settingsStage);
            Scene scene = new Scene(pane, 500, 600);
            stage.setScene(scene);
            stage.showAndWait();



        } catch (IOException e){

        }
    }

    /**
     * Метод открывает диалолговое окно для выбора папки синхронизации на диске
     */
    @FXML
    public void openSyncFileBrowser(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Synchronization folder");
        //fileChooser.getExtensionFilters().addAll();
        File selectedFile = directoryChooser.showDialog(settingsStage);
        if (selectedFile != null) {
            setSyncFolder(selectedFile);
            setSyncFolderPathLable();
        }
    }


    /**
     * метод сохраняет настройки
     * @param event
     *
     */
    @FXML
    void applySettingsHandler(ActionEvent event) {
        appSettings.saveProperties();
    }

    /**
     * Метод закрывает окно настроек и отменяет изменения
     * @param event
     */
    @FXML
    void cancelSettingsHandler(ActionEvent event) {
        settingsStage.close();
        appSettings.reload();
    }


    /**
     * Метод сохраняет настройки и закрывает окно
     * @param event
     */
    @FXML
    void saveSettingsHandler(ActionEvent event) {
        applySettingsHandler(event);
        settingsStage.close();
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        appSettings = AppSettings.getInstance();
        settingsMananger = new CloudSettingsManager();
        initAccountsTreeView();
        setSyncFolderPathLable();


    }

    private void initAccountsTreeView(){

        TreeItem<CloudSettings> root = new TreeItem<>(new CloudSettings());
        for (CloudSettings setting:settingsMananger.getCloudAccounts()) {
            TreeItem<CloudSettings> item = new TreeItem<>(setting, new ImageView(setting.getImage()));
            root.getChildren().add(item);
        }
        TreeView<CloudSettings> accountsTree = new TreeView<>(root);
        accountsTree.setShowRoot(false);
        accountsTreePane.getChildren().add(accountsTree);

        accountsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            CloudSettings set = newValue.getValue();

            updateChart(set);
            cloudNameLable.setGraphic(new ImageView(set.getBigImage()));
            cloudNameLable.setText(set.getName());


            totalSpaceLable.setText(String.valueOf(set.getTotalSpace()));
            availableSpaceLable.setText(String.valueOf(set.getAvailableSpace()));
            cloudRaidSpaceLable.setText(String.valueOf(set.getCloudRaidSpace()));
            cloudRaidSpaceSlider.setMax(set.getTotalSpace());
            cloudRaidSpaceSlider.setMin(0);
            cloudRaidSpaceSlider.adjustValue(set.getCloudRaidSpace());
            cloudRaidSpaceSlider.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                cloudRaidSpaceLable.setText(String.valueOf(newValue1.longValue()));
            });
            cloudRaidSpaceSlider.setOnMouseReleased(event -> {
                set.setCloudRaidSpace((long) cloudRaidSpaceSlider.getValue());
                availableSpaceLable.setText(String.valueOf(set.getAvailableSpace()));
                updateChart(set);
            });





        });


    }
    private void updateChart(CloudSettings set){
        PieChart.Data availableData = new PieChart.Data("Available", set.getAvailableSpace());
        PieChart.Data usingData = new PieChart.Data("Using", set.getUsingSpace());
        PieChart.Data totalData = new PieChart.Data("Empty", set.getTotalSpace()-set.getCloudRaidSpace());


        ObservableList<PieChart.Data> list = FXCollections.observableArrayList(
                availableData, usingData, totalData
        );
        chart.setData(list);

        applyCustomColorSequence(
                list,
                "bisque",
                "red",
                "aqua"
        );
    }
    private void applyCustomColorSequence(ObservableList<PieChart.Data> pieChartData, String... pieColors) {
        int i = 0;
        for (PieChart.Data data : pieChartData) {
            data.getNode().setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
            i++;
        }
    }

    private void setSyncFolder(File fileOnDisc){
        appSettings.setProperty(AppSettings.PROPERTIES_KEYS.SINCHRONIZATION_PATH, fileOnDisc.toString());
    }

    private void setSyncFolderPathLable(){
        syncFolderPathLable.setText(appSettings.getProperty(AppSettings.PROPERTIES_KEYS.SINCHRONIZATION_PATH));
    }


    public void setSettingsStage(Stage stage){
        settingsStage = stage;
    }
    public void setDataManager(DAOFileItem dataManager){
        this.dataManager = dataManager;
    }

}
