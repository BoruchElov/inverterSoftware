package org.example.invertersoftware;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {

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
    private TextField intervalTextField;
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

    private ObservableList<Currents> currentsData = FXCollections.observableArrayList();
    private ObservableList<Voltages> voltagesData = FXCollections.observableArrayList();

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
    private boolean isRunning = false;
    private boolean allowedToDisplayData = false;
    private ModbusConnection modbusConnection;

    private String port;
    private int baudRate;
    private int dataBits;
    private String parity;
    private int stopBits;
    private int interval;
    private int timeout;
    private int retries;
    private int slaveID;

    @FXML
    public void initialize() {
        initialDataForTables();

        phaseACurrentColumn.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IA"));
        phaseBCurrentColumn.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IB"));
        phaseCCurrentColumn.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IC"));

        phaseAVoltageColumn.setCellValueFactory(new PropertyValueFactory<Voltages, Float>("UA"));
        phaseBVoltageColumn.setCellValueFactory(new PropertyValueFactory<Voltages, Float>("UB"));
        phaseCVoltageColumn.setCellValueFactory(new PropertyValueFactory<Voltages, Float>("UC"));

        currentsTable.setItems(currentsData);
        voltagesTable.setItems(voltagesData);

        portComboBox.getItems().addAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10",
                "COM11", "COM12", "COM13", "COM14", "COM15");
        parametersComboBox.getItems().addAll("Sbase", "Vacbase", "KpPLL", "KiPLL");
        baudRateComboBox.getItems().addAll(9600, 14400, 19200, 38400, 57600, 115200);
        dataBitsComboBox.getItems().addAll(7, 8);
        parityComboBox.getItems().addAll("None", "Even", "Odd");
        stopBitsComboBox.getItems().addAll(1, 2);
    }
    private void initialDataForTables () {
        currentsData.add(new Currents(0,0,0));
        voltagesData.add(new Voltages(0,0,0));
    }

    @FXML
    private void onConnectButtonClick() throws ModbusException {
        if (isRunning) {
            stopProgram();
        } else {
            startProgram();
        }
    }
    @FXML
    private void onStopDisplayingDataButtonClick() {
        allowedToDisplayData = false;
    }

    @FXML
    private void onGetDataButtonClick() {
        allowedToDisplayData = modbusConnection != null;
        new Thread(() -> {
            while (allowedToDisplayData) {
                modbusConnection.readRegisters();
                displayOutputs();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
    @FXML
    private void initialValuesOfParameters() {
        actualParameterValue.setText(String.valueOf(modbusConnection.showParameterValue(parametersComboBox.getValue())));
    }
    @FXML
    private void onApplyButtonClick() throws ModbusException {
        allowedToDisplayData = false;
        modbusConnection.writeRegisters(slaveID, 0, parametersComboBox.getValue(), Float.parseFloat(newParametervalue.getText()));
    }

    private void startProgram() throws ModbusException {

        port = portComboBox.getValue();
        baudRate = baudRateComboBox.getValue();
        dataBits = dataBitsComboBox.getValue();
        parity = parityComboBox.getValue();
        stopBits = stopBitsComboBox.getValue();
        interval = Integer.parseInt(intervalTextField.getText());
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

    private void stopProgram() {
        if (modbusConnection != null) {
            modbusConnection.stop();
        }
        isRunning = false;
        connectButton.setText("Соединение");
        allowedToDisplayData = false;
    }

    public void displayOutputs() {
        float[] outputs = modbusConnection.getOutputs();

        currentsData.removeLast();
        currentsData.add(new Currents(outputs[3],outputs[4],outputs[5]));

        voltagesData.removeLast();
        voltagesData.add(new Voltages(outputs[0], outputs[1], outputs[2]));

        currentsTable.setItems(currentsData);
        voltagesTable.setItems(voltagesData);
    }
}

