package org.example.invertersoftware;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainController {


    @FXML
    private LineChart<String, Number> currentChart;
    @FXML
    private LineChart<String, Number> voltageChart;

    @FXML
    private CategoryAxis currentChartXAxis;

    @FXML
    private NumberAxis currentChartYAxis;

    @FXML
    private CategoryAxis voltageChartXAxis;

    @FXML
    private NumberAxis voltageChartYAxis;
    @FXML
    private ComboBox<String> portComboBox;
    @FXML
    private ComboBox<String> parametersComboBox;
    @FXML
    private ComboBox<Integer> baudRateComboBox;
    @FXML
    private ComboBox<Integer> dataBitsComboBox;
    @FXML
    private ComboBox<String> parityComboBox;
    @FXML
    private ComboBox<Integer> stopBitsComboBox;
    @FXML
    private ComboBox<Integer> intervalComboBox;
    @FXML
    private TextField timeoutTextField;
    @FXML
    private TextField slaveIdentificationNumber;
    @FXML
    private TextField retriesTextField;
    @FXML
    private TextField actualParameterValue;
    @FXML
    private TextField newParametervalue;
    @FXML
    private Button connectButton;
    @FXML
    private Button getData;
    @FXML
    private Button stopGettingData;
    @FXML
    private Button setNewParameterValue;
    @FXML
    private Button exportApplyButton;
    @FXML
    private ComboBox<String> exportConfigurationComboBox;
    @FXML
    private TextField exportFileLocation;
    @FXML
    private CheckBox isExported;

    private ObservableList<Currents> currentsData = FXCollections.observableArrayList();
    private ObservableList<Voltages> voltagesData = FXCollections.observableArrayList();

    private float currentTime;
    float[] outputsForTables;
    float[] outputsForCharts;
    float[] outputs;
    private String exportFileConfiguration;
    private String pathToExportFile;

    PauseTransition pause = new PauseTransition(Duration.seconds(5)); // 5 seconds delay
    PauseTransition pauseForChangingParameters = new PauseTransition(Duration.seconds(5)); // 5 seconds delay

    @FXML
    private TableView<Currents> currentsTable;
    @FXML
    private TableView<Voltages> voltagesTable;

    @FXML
    private TableColumn<Currents, Float> phaseACurrentColumn;
    @FXML
    private TableColumn<Currents, Float> phaseBCurrentColumn;
    @FXML
    private TableColumn<Currents, Float> phaseCCurrentColumn;
    @FXML
    private TableColumn<Voltages, Float> phaseAVoltageColumn;
    @FXML
    private TableColumn<Voltages, Float> phaseBVoltageColumn;
    @FXML
    private TableColumn<Voltages, Float> phaseCVoltageColumn;
    @FXML
    private Slider currentsChartSlider;
    @FXML
    private Slider voltagesChartSlider;

    private XYChart.Series currentPhaseASeries;
    private XYChart.Series currentPhaseBSeries;
    private XYChart.Series currentPhaseCSeries;

    private XYChart.Series voltagePhaseASeries;
    private XYChart.Series voltagePhaseBSeries;
    private XYChart.Series voltagePhaseCSeries;
    
    private List<Float> currentPhaseAExportList;
    private List<Float> currentPhaseBExportList;
    private List<Float> currentPhaseCExportList;
    private List<Float> voltagePhaseAExportList;
    private List<Float> voltagePhaseBExportList;
    private List<Float> voltagePhaseCExportList;
    private List<Float> timeExportList;

    private String filePath;

    private boolean isRunning = false;
    private boolean allowedToDisplayData = false;
    private ModbusConnection modbusConnection;

    private boolean allowedToExportData = false;

    private String port;
    private int baudRate;
    private int dataBits;
    private String parity;
    private int stopBits;
    private int interval;
    private int timeout;
    private int retries;
    private int slaveID;
    private int lineChartInterval;

    private float exportTime;

    /**
     * Method initialized() is used for initialization of some variables values and all the interface elements
     */
    @FXML
    public void initialize() {
        //Объект файла
        //BufferedWriter writer = new BufferedWriter(new FileWriter(new File(exportFileLocation.getText())));
        currentPhaseAExportList = new ArrayList<>();
        currentPhaseBExportList = new ArrayList<>();
        currentPhaseCExportList = new ArrayList<>();
        voltagePhaseAExportList = new ArrayList<>();
        voltagePhaseBExportList = new ArrayList<>();
        voltagePhaseCExportList = new ArrayList<>();
        timeExportList = new ArrayList<>();

        currentChart.setCreateSymbols(false);
        currentChart.getXAxis().setAnimated(false);
        currentChart.getYAxis().setAnimated(false);

        voltageChart.setCreateSymbols(false);
        voltageChart.getXAxis().setAnimated(false);
        voltageChart.getYAxis().setAnimated(false);

        currentTime = 0;
        lineChartInterval = 0;

        currentPhaseASeries = new XYChart.Series();
        currentPhaseBSeries = new XYChart.Series();
        currentPhaseCSeries = new XYChart.Series();

        voltagePhaseASeries = new XYChart.Series();
        voltagePhaseBSeries = new XYChart.Series();
        voltagePhaseCSeries = new XYChart.Series();

        currentsTable.setFocusTraversable(false);
        currentsTable.addEventFilter(MouseEvent.ANY, Event::consume);

        voltagesTable.setFocusTraversable(false);
        voltagesTable.addEventFilter(MouseEvent.ANY, Event::consume);

        phaseACurrentColumn.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IA"));
        phaseBCurrentColumn.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IB"));
        phaseCCurrentColumn.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IC"));

        phaseAVoltageColumn.setCellValueFactory(new PropertyValueFactory<Voltages, Float>("UA"));
        phaseBVoltageColumn.setCellValueFactory(new PropertyValueFactory<Voltages, Float>("UB"));
        phaseCVoltageColumn.setCellValueFactory(new PropertyValueFactory<Voltages, Float>("UC"));

        currentsTable.setItems(currentsData);
        voltagesTable.setItems(voltagesData);

        intervalComboBox.getItems().addAll(1, 10, 100, 1000);
        exportConfigurationComboBox.getItems().addAll("Токи", "Напряжения", "Токи и напряжения");
        exportConfigurationComboBox.setValue("Токи");

        turnElementOff(exportConfigurationComboBox);
        turnElementOff(exportFileLocation);
        turnElementOff(exportApplyButton);

        portComboBox.getItems().addAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10",
                "COM11", "COM12", "COM13", "COM14", "COM15");
        portComboBox.setValue("COM1");
        parametersComboBox.getItems().addAll("Sbase", "Vacbase", "KpPLL", "KiPLL");
        parametersComboBox.setValue("Sbase");
        baudRateComboBox.getItems().addAll(9600, 14400, 19200, 38400, 57600, 115200);
        baudRateComboBox.setValue(115200);
        dataBitsComboBox.getItems().addAll(7, 8);
        dataBitsComboBox.setValue(8);
        parityComboBox.getItems().addAll("None", "Even", "Odd");
        parityComboBox.setValue("None");
        stopBitsComboBox.getItems().addAll(1, 2);
        stopBitsComboBox.setValue(1);
        currentsData.add(new Currents(0, 0, 0));
        voltagesData.add(new Voltages(0, 0, 0));
        turnElementOff(setNewParameterValue);
    }

    /**
     * Method onConnectButtonClick() is called when user presses on the connection button.
     * <p>
     * In this method variable referring to the connection time is reset. This method also starts application if it is
     * not running or stops in other case.
     */
    @FXML
    private void onConnectButtonClick() throws ModbusException, IOException {
        exportTime = 0;
        currentTime = 0;
        if (isRunning) {
            onStopDisplayingDataButtonClick();
            stopProgram();
            turnElementOff(setNewParameterValue);
            allowedToExportData = false;
        } else {
            startProgram();
            turnElementOn(setNewParameterValue);
            allowedToExportData = true;
        }
    }

    /**
     * Method onGetDataButtonClick() gets data from controller via Thread.
     * At first, it checks if there is a connection, so it possible to obtain data.
     * If it is, method sends requests to the controller for reading holding registers, gets the response and displays it
     * in the tables using the method displayOutputs().
     * <p>
     * If there is no connection method throws an exception.
     */
    @FXML
    private void onGetDataButtonClick() {
        turnElementOn(stopGettingData);
        turnElementOff(exportConfigurationComboBox);
        turnElementOff(exportFileLocation);
        turnElementOff(exportApplyButton);
        turnElementOff(isExported);

        getData.setDisable(true);
        if (interval <= 100) {
            lineChartInterval = 100;
        } else {
            lineChartInterval = interval;
        }

        currentTime = 0;

        allowedToDisplayData = modbusConnection != null;
        if (!currentChart.getData().contains(currentPhaseASeries)) {
            currentChart.getData().add(currentPhaseASeries);
        }
        if (!currentChart.getData().contains(currentPhaseBSeries)) {
            currentChart.getData().add(currentPhaseBSeries);
        }
        if (!currentChart.getData().contains(currentPhaseCSeries)) {
            currentChart.getData().add(currentPhaseCSeries);
        }
        if (!voltageChart.getData().contains(voltagePhaseASeries)) {
            voltageChart.getData().add(voltagePhaseASeries);
        }
        if (!voltageChart.getData().contains(voltagePhaseBSeries)) {
            voltageChart.getData().add(voltagePhaseBSeries);
        }
        if (!voltageChart.getData().contains(voltagePhaseCSeries)) {
            voltageChart.getData().add(voltagePhaseCSeries);
        }
        new Thread(() -> {
            while (allowedToDisplayData) {
                modbusConnection.readRegisters();
                addDataToArrayList();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        new Thread(() -> {
            while (allowedToDisplayData) {
                currentTime += lineChartInterval * 0.001f;
                displayOutputs();
                plotOutPuts();
                try {
                    Thread.sleep(lineChartInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Platform.runLater(() -> {
                    voltagePhaseASeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForCharts[0]));
                    if (voltagePhaseASeries.getData().size() > 50) {
                        voltagePhaseASeries.getData().remove(0);
                    }
                    voltagePhaseBSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForCharts[1]));
                    if (voltagePhaseBSeries.getData().size() > 50) {
                        voltagePhaseBSeries.getData().remove(0);
                    }
                    voltagePhaseCSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForCharts[2]));
                    if (voltagePhaseCSeries.getData().size() > 50) {
                        voltagePhaseCSeries.getData().remove(0);
                    }
                    currentPhaseASeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForCharts[3]));
                    if (currentPhaseASeries.getData().size() > 50) {
                        currentPhaseASeries.getData().remove(0);
                    }
                    currentPhaseBSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForCharts[4]));
                    if (currentPhaseBSeries.getData().size() > 50) {
                        currentPhaseBSeries.getData().remove(0);
                    }
                    currentPhaseCSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForCharts[5]));
                    if (currentPhaseCSeries.getData().size() > 50) {
                        currentPhaseCSeries.getData().remove(0);
                    }
                });
            }
        }).start();
    }

    public void exportEnable() {
        if (allowedToExportData) {
            if (isExported.isSelected()) {
                turnElementOn(exportConfigurationComboBox);
                turnElementOn(exportFileLocation);
                turnElementOn(exportApplyButton);
            } else {
                turnElementOff(exportConfigurationComboBox);
                turnElementOff(exportFileLocation);
                turnElementOff(exportApplyButton);
            }
        } else {
            turnElementOff(exportConfigurationComboBox);
            turnElementOff(exportFileLocation);
            turnElementOff(exportApplyButton);
        }
    }

    public void addDataToArrayList() {
        outputs = modbusConnection.getOutputs();

        timeExportList.add(exportTime);

        currentPhaseAExportList.add(outputs[3]);
        currentPhaseBExportList.add(outputs[4]);
        currentPhaseCExportList.add(outputs[5]);

        voltagePhaseAExportList.add(outputs[0]);
        voltagePhaseBExportList.add(outputs[1]);
        voltagePhaseCExportList.add(outputs[2]);

        exportTime += interval * 0.001f;
    }
    public void saveExportParameters() {
        exportFileConfiguration = exportConfigurationComboBox.getValue();
        pathToExportFile = exportFileLocation.getText();

    }

    /**
     * Method onStopDisplayingDataButtonClick() sets the variable allowedToDisplayData value to false, so it stops the
     * Thread of obtaining data.
     */

    @FXML
    private void onStopDisplayingDataButtonClick() throws IOException {
        pause.setOnFinished(event ->
                turnElementOn(getData));
        pause.play();
        currentPhaseASeries.getData().clear();
        currentPhaseBSeries.getData().clear();
        currentPhaseCSeries.getData().clear();
        currentChart.getData().clear();
        currentChartXAxis.getCategories().clear();

        voltagePhaseASeries.getData().clear();
        voltagePhaseBSeries.getData().clear();
        voltagePhaseCSeries.getData().clear();
        voltageChart.getData().clear();
        voltageChartXAxis.getCategories().clear();

        allowedToDisplayData = false;
        turnElementOff(stopGettingData);
        if(isExported.isSelected()) {
            writeToTxt(timeExportList, currentPhaseAExportList, currentPhaseBExportList,
                    currentPhaseCExportList, voltagePhaseAExportList, voltagePhaseBExportList,
                    voltagePhaseCExportList, pathToExportFile, exportFileConfiguration);
        }
        turnElementOn(exportConfigurationComboBox);
        turnElementOn(exportFileLocation);
        turnElementOn(exportApplyButton);
        turnElementOn(isExported);
        exportTime = 0;
    }

    /**
     * Method onApplyButtonClick() sends the request for changing the values of some of holding registers. To avoid
     * possible conflicts this method at first stops the thread of obtaining data from controller. Then it sends the
     * request. Constructor for this request contains the ID of slave device, offset value for registers and the value
     * that is set by user. After that actual values of control system parameters get updated using the method
     * updateControlSystemParameters() from ModbusConnection class and get shown using the
     * method initialValuesOfParameters()
     */

    @FXML
    private void onApplyButtonClick() throws ModbusException, IOException {
        onStopDisplayingDataButtonClick();


        modbusConnection.writeRegisters(slaveID, 0, parametersComboBox.getValue(), Float.parseFloat(newParametervalue.getText()));
        modbusConnection.updateControlSystemParameters();

        initialValuesOfParameters();
        turnElementOff(setNewParameterValue);
        pauseForChangingParameters.setOnFinished(event -> {
            turnElementOn(setNewParameterValue);
        });
        pauseForChangingParameters.play();
    }

    /**
     * Method initialValuesOfParameters() sets text to the text field that shows actual value of the parameter chosen
     * by user
     */
    @FXML
    private void initialValuesOfParameters() {
        actualParameterValue.setText(String.valueOf(modbusConnection.showParameterValue(parametersComboBox.getValue())));
    }

    /**
     * Method startProgram() gets the data from the text fields and sets the values of the connection parameters.
     * It also sends a request for connection, sets the status of application as "Running" and changes the text of
     * connection button from "Connect" to "Disconnect", so it can be used for both connection and disconnection
     */
    private void startProgram() throws ModbusException {

        port = portComboBox.getValue();
        baudRate = baudRateComboBox.getValue();
        dataBits = dataBitsComboBox.getValue();
        parity = parityComboBox.getValue();
        stopBits = stopBitsComboBox.getValue();
        interval = intervalComboBox.getValue();
        timeout = Integer.parseInt(timeoutTextField.getText());
        retries = Integer.parseInt(retriesTextField.getText());
        slaveID = Integer.parseInt(slaveIdentificationNumber.getText());
        modbusConnection = new ModbusConnection(port, baudRate, dataBits, parity, stopBits, interval, timeout, retries, slaveID);
        modbusConnection.connect();
        if (modbusConnection != null) {
            isRunning = true;
            connectButton.setText("Отключение");
        }
        assert modbusConnection != null;
    }

    /**
     * Method stopProgram() works reversely to the method startProgram()
     */
    private void stopProgram() {
        if (modbusConnection != null) {
            modbusConnection.stop();
        }
        isRunning = false;
        connectButton.setText("Соединение");
        allowedToDisplayData = false;
    }



    /**
     * Method displayOutputs() gets the value obtained through readMultipleRegisters request and puts it in the tables
     */

    public void displayOutputs() {
        outputsForTables = modbusConnection.getOutputs();

        currentsData.removeLast();
        currentsData.add(new Currents(outputsForTables[3], outputsForTables[4], outputsForTables[5]));

        voltagesData.removeLast();
        voltagesData.add(new Voltages(outputsForTables[0], outputsForTables[1], outputsForTables[2]));

        currentsTable.setItems(currentsData);
        voltagesTable.setItems(voltagesData);
    }

    public void plotOutPuts() {
        outputsForCharts = modbusConnection.getOutputs();
    }

    public void turnElementOff(@NotNull Node element) {
        element.setDisable(true);
    }

    public void turnElementOn(@NotNull Node element) {
        element.setDisable(false);
    }
    public static void writeToTxt (List<Float> timeList, List<Float> pACList, List<Float> pBCList, List<Float> pCCList,
                                   List<Float> pAVList, List<Float> pBVList, List<Float> pCVList, String filePathName,
                                   String Configuration) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePathName)));
        if (Objects.equals(Configuration, "Токи")) {
            writer.write("Время, с");
            writer.write("; ");
            writer.write("Ток фазы А, А");
            writer.write("; ");
            writer.write("Ток фазы В, А");
            writer.write("; ");
            writer.write("Ток фазы С, А");
            writer.write("\r\n");

            for (int i = 0; i < timeList.size(); i++) {
                writer.write(String.valueOf(timeList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pACList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pBCList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pCCList.get(i)));
                writer.write("\r\n");
            }
            writer.flush();
        } else if (Objects.equals(Configuration, "Напряжения")) {
            writer.write("Время, с");
            writer.write("; ");
            writer.write("Напряжение фазы А, В");
            writer.write("; ");
            writer.write("Напряжение фазы В, В");
            writer.write("; ");
            writer.write("Напряжение фазы С, В");
            writer.write("\r\n");

            for (int i = 0; i < timeList.size(); i++) {
                writer.write(String.valueOf(timeList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pAVList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pBVList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pCVList.get(i)));
                writer.write("\r\n");
            }
            writer.flush();
        } else if (Objects.equals(Configuration, "Токи и напряжения")) {
            writer.write("Время, с");
            writer.write("; ");
            writer.write("Ток фазы А, А");
            writer.write("; ");
            writer.write("Ток фазы В, А");
            writer.write("; ");
            writer.write("Ток фазы С, А");
            writer.write("; ");
            writer.write("Напряжение фазы А, В");
            writer.write("; ");
            writer.write("Напряжение фазы В, В");
            writer.write("; ");
            writer.write("Напряжение фазы С, В");
            writer.write("\r\n");

            for (int i = 0; i < timeList.size(); i++) {
                writer.write(String.valueOf(timeList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pACList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pBCList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pCCList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(timeList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pAVList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pBVList.get(i)));
                writer.write("; ");
                writer.write(String.valueOf(pCVList.get(i)));
                writer.write("\r\n");
            }
            writer.flush();
        }

        timeList.clear();
        pACList.clear();
        pBCList.clear();
        pCCList.clear();
        pAVList.clear();
        pBVList.clear();
        pCVList.clear();
    }

}

