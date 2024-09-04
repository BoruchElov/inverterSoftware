package org.example.invertersoftware;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    private TextField retriesTextField;
    @FXML
    private Button connectButton;
    @FXML
    private TableView<?> currentTable;
    @FXML
    private TableView<?> voltageTable;
    private boolean isRunning = false;
    private ModbusConnection modbusConnection;
    @FXML
    public void initialize() {
        portComboBox.getItems().addAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8");
        baudRateComboBox.getItems().addAll(9600, 14400, 19200, 38400, 57600, 115200);
        dataBitsComboBox.getItems().addAll(5, 6, 7, 8);
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
    private void startProgram() {
        String port = portComboBox.getValue();
        int baudRate = baudRateComboBox.getValue();
        int dataBits = dataBitsComboBox.getValue();
        String parity = parityComboBox.getValue();
        int stopBits = stopBitsComboBox.getValue();
        int interval = Integer.parseInt(intervalTextField.getText());
        int timeout = Integer.parseInt(timeoutTextField.getText());
        int retries = Integer.parseInt(retriesTextField.getText());
        modbusConnection = new ModbusConnection(port, baudRate, dataBits, parity, stopBits, interval, timeout, retries);
        modbusConnection.connect();
        if (modbusConnection != null) {
            modbusConnection.startPolling();
            isRunning = true;
            connectButton.setText("Отключение");
        }
    }
    private void stopProgram() {
        if (modbusConnection != null) {
            modbusConnection.stop();
        }
        isRunning = false;
        connectButton.setText("Соединение");
    }
}

