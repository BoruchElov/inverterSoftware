package org.example.invertersoftware;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class MainController {


    @FXML
    private LineChart<?, ?> currentChart;

    @FXML
    private CategoryAxis currentChartXAxis;

    @FXML
    private NumberAxis currentChartYAxis;
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

    private float currentTime;
    float[] outputs;

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

    private XYChart.Series currentSeries;

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


    /**
     * Method initialized() is used for initialization of some variables values and all the interface elements
     */
    @FXML
    public void initialize() {
        currentTime = 0;
        currentSeries = new XYChart.Series();

        //initialDataForTables();
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

        portComboBox.getItems().addAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10",
                "COM11", "COM12", "COM13", "COM14", "COM15");
        parametersComboBox.getItems().addAll("Sbase", "Vacbase", "KpPLL", "KiPLL");
        baudRateComboBox.getItems().addAll(9600, 14400, 19200, 38400, 57600, 115200);
        dataBitsComboBox.getItems().addAll(7, 8);
        parityComboBox.getItems().addAll("None", "Even", "Odd");
        stopBitsComboBox.getItems().addAll(1, 2);
        currentsData.add(new Currents(0,0,0));
        voltagesData.add(new Voltages(0,0,0));
    }

    /**
     * ACTION METHODS AREA STARTS FROM HERE.
     *
     * Method onConnectButtonClick() is called when user presses on the connection button.
     *
     * In this method variable referring to the connection time is reset. This method also starts application if it is
     * not running or stops in other case.
     *
     */
    @FXML
    private void onConnectButtonClick() throws ModbusException {
        currentTime = 0;
        if (isRunning) {
            stopProgram();
        } else {
            startProgram();
        }
    }

    /**
     * Method onGetDataButtonClick() gets data from controller via Thread.
     * At first, it checks if there is a connection, so it possible to obtain data.
     * If it is, method sends requests to the controller for reading holding registers, gets the response and displays it
     * in the tables using the method displayOutputs().
     *
     * If there is no connection method throws an exception.
     */
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

    /**
     * Method onStopDisplayingDataButtonClick() sets the variable allowedToDisplayData value to false, so it stops the
     * Thread of obtaining data.
     */

    @FXML
    private void onStopDisplayingDataButtonClick() {
        allowedToDisplayData = false;
    }

    /**
     * Method onApplyButtonClick() sends the request for changing the values of some of holding registers. To avoid
     * possible conflicts this method at first stops the thread of obtaining data from controller. Then it sends the
     * request. Constructor for this request contains the ID of slave device, offset value for registers and the value
     * that is set by user. After that actual values of control system parameters get updated using the method
     * updateControlSystemParameters() from ModbusConnection class and get shown using the
     * method initialValuesOfParameters()
     *
     */

    @FXML
    private void onApplyButtonClick() throws ModbusException {
        allowedToDisplayData = false;
        modbusConnection.writeRegisters(slaveID, 0, parametersComboBox.getValue(), Float.parseFloat(newParametervalue.getText()));
        modbusConnection.updateControlSystemParameters();
        initialValuesOfParameters();
    }

    @FXML
    private void initialValuesOfParameters() {
        actualParameterValue.setText(String.valueOf(modbusConnection.showParameterValue(parametersComboBox.getValue())));
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
        outputs = modbusConnection.getOutputs();
        currentTime += interval * 0.001f;

        currentsData.removeLast();
        currentsData.add(new Currents(outputs[3],outputs[4],outputs[5]));

        voltagesData.removeLast();
        voltagesData.add(new Voltages(outputs[0], outputs[1], outputs[2]));

        currentsTable.setItems(currentsData);
        voltagesTable.setItems(voltagesData);
    }
}

