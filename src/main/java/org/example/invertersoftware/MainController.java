package org.example.invertersoftware;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {
    @FXML
    private ComboBox<String> portComboBox;
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
    private Button connectButton;
    @FXML
    private TableView<Currents> currentTable;
    @FXML
    private TableView<voltages> voltageTable;
    @FXML
    private TableColumn<Currents, Float> phaseACurrent;
    @FXML
    private TableColumn<Currents, Float> phaseBCurrent;
    @FXML
    private TableColumn<Currents, Float> phaseCCurrent;
    @FXML
    private TableColumn<voltages, Float> phaseAVoltage;
    @FXML
    private TableColumn<voltages, Float> phaseBVoltage;
    @FXML
    private TableColumn<voltages, Float> phaseCVoltage;
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
        phaseACurrent.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IA, А"));
        phaseBCurrent.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IB, А"));
        phaseCCurrent.setCellValueFactory(new PropertyValueFactory<Currents, Float>("IC, А"));

        phaseAVoltage.setCellValueFactory(new PropertyValueFactory<voltages, Float>("UA, В"));
        phaseBVoltage.setCellValueFactory(new PropertyValueFactory<voltages, Float>("UB, В"));
        phaseCVoltage.setCellValueFactory(new PropertyValueFactory<voltages, Float>("UC, В"));

        portComboBox.getItems().addAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10",
                "COM11", "COM12", "COM13", "COM14", "COM15");
        baudRateComboBox.getItems().addAll(9600, 14400, 19200, 38400, 57600, 115200);
        dataBitsComboBox.getItems().addAll(7, 8);
        parityComboBox.getItems().addAll("None", "Even", "Odd");
        stopBitsComboBox.getItems().addAll(1, 2);
    }

    @FXML
    private void onConnectButtonClick() {
        if (isRunning) {
            stopProgram();
        } else {
            startProgram();
        }
    }
    @FXML
    private void onDisconnectButtonClick() {
        allowedToDisplayData = false;
    }
    @FXML
    private void onGetDataButtonClick() {
        allowedToDisplayData = true;
        new Thread(() -> {
            while (allowedToDisplayData) {
                displayOutputs();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void startProgram() {
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
            modbusConnection.startPolling();
            isRunning = true;
            connectButton.setText("Отключение");
        }
        assert modbusConnection != null;
        /*Currents currents = new Currents(modbusConnection.startPolling()[3],modbusConnection.startPolling()[4], modbusConnection.startPolling()[5]);
        voltages vltgs = new voltages(modbusConnection.startPolling()[0],modbusConnection.startPolling()[1], modbusConnection.startPolling()[2]);

        ObservableList<Currents> currentsList = currentTable.getItems();
        currentsList.add(currents);
        currentTable.setItems(currentsList);

        ObservableList<voltages> voltagesList = voltageTable.getItems();
        voltagesList.add(vltgs);
        voltageTable.setItems(voltagesList);*/
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
        for (int i = 0; i < outputs.length; i++) {
            System.out.println("Output " + (i + 1) + ": " + outputs[i]);
        }
    }
}

