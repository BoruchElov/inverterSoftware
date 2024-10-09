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
import java.util.Objects;


public class MainController {

    /**
     * В данной части объявляются элементы графика: LineChart (непосредственно график), CategoryAxis (ось абсцисс),
     * NumberAxis (ось ординат). Также здесь объявляется специальный тип данных, необходимый для построения графика -
     * XYChart.Series, содержащий узлы с координатами по осям X и Y. Такой объект создаётся для всех величин, выводимых
     * на график.
     */
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
    private XYChart.Series currentPhaseASeries;
    private XYChart.Series currentPhaseBSeries;
    private XYChart.Series currentPhaseCSeries;

    private XYChart.Series voltagePhaseASeries;
    private XYChart.Series voltagePhaseBSeries;
    private XYChart.Series voltagePhaseCSeries;

    /**
     * В данном блоке кода объявляются элементы интерфейса, позволяющие пользователю задать параметры соединения с
     * контроллером. Также здесь объявляются переменные, в которые записываются заданные пользователем значения данных
     * параметров.
     */
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
     * В данной части кода объявляется объект кнопки connectButton, по нажатии которой пользователь устанавливает
     * соединение с контроллером. Для установки соединения и осуществления дальнейших операций с контроллером
     * объявляется объект типа ModbusConnection - modbusConnection
     */
    @FXML
    private Button connectButton;
    private ModbusConnection modbusConnection;
    /**
     *
     */

    @FXML
    private TextField actualParameterValue;
    @FXML
    private TextField newParametervalue;
    @FXML
    private TextField writingTime;
    private int exportListLength;

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
    float[] outputsForChartsAndTables;
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
    private BufferedWriter writer;
    private int exportPointsCount;

    private float[][] export;
    private float[] time;

    private boolean isRunning = false;
    private boolean allowedToDisplayData = false;


    private boolean allowedToExportData = false;


    private int lineChartInterval;

    private float exportTime;

    /**
     * Method initialized() is used for initialization of some variables values and all the interface elements
     */
    @FXML
    public void initialize() {
        exportPointsCount = 0;

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

        intervalComboBox.getItems().addAll(50, 100, 500, 1000);
        exportConfigurationComboBox.getItems().addAll("Токи", "Напряжения", "Токи и напряжения");
        exportConfigurationComboBox.setValue("Токи");

        turnElementOff(exportConfigurationComboBox);
        turnElementOff(exportFileLocation);
        turnElementOff(exportApplyButton);

        portComboBox.getItems().addAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10",
                "COM11", "COM12", "COM13", "COM14", "COM15", "COM16", "COM17", "COM18", "COM19", "COM20");
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

        turnElementOff(stopGettingData);

        if(isExported.isSelected()) {
            saveExportParameters();
        } else {
            turnElementOn(stopGettingData);
        }

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
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
        new Thread(() -> {
            while (allowedToDisplayData & isExported.isSelected()) {
                if (exportPointsCount < exportListLength) {
                    saveExportData(exportPointsCount);
                    exportPointsCount++;
                    exportTime += interval * 0.001f;
                } else {
                    turnElementOn(stopGettingData);
                }
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
                try {
                    Thread.sleep(lineChartInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Platform.runLater(() -> {
                    voltagePhaseASeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForChartsAndTables[0]));
                    if (voltagePhaseASeries.getData().size() > 50) {
                        voltagePhaseASeries.getData().remove(0);
                    }
                    voltagePhaseBSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForChartsAndTables[1]));
                    if (voltagePhaseBSeries.getData().size() > 50) {
                        voltagePhaseBSeries.getData().remove(0);
                    }
                    voltagePhaseCSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForChartsAndTables[2]));
                    if (voltagePhaseCSeries.getData().size() > 50) {
                        voltagePhaseCSeries.getData().remove(0);
                    }
                    currentPhaseASeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForChartsAndTables[3]));
                    if (currentPhaseASeries.getData().size() > 50) {
                        currentPhaseASeries.getData().remove(0);
                    }
                    currentPhaseBSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForChartsAndTables[4]));
                    if (currentPhaseBSeries.getData().size() > 50) {
                        currentPhaseBSeries.getData().remove(0);
                    }
                    currentPhaseCSeries.getData().add(new XYChart.Data<>(String.format("%.3f", currentTime), outputsForChartsAndTables[5]));
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

    public void saveExportParameters() {
        if(Integer.parseInt(writingTime.getText()) <= 0 || interval == 0) {
            exportListLength = 10;
        } else {
            exportListLength = Integer.parseInt(writingTime.getText()) * 1000 / interval;
        }
        export = new float[exportListLength][6];
        time = new float[exportListLength];

        exportFileConfiguration = exportConfigurationComboBox.getValue();
        pathToExportFile = exportFileLocation.getText();
    }

    public void saveExportData(int position) {
        for (int i = 0; i < 6; i++) {
            export[position][i] = modbusConnection.getOutputs()[i];
        }
        time[position] = exportTime;
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

        turnElementOn(isExported);
        if (isExported.isSelected()) {
            turnElementOn(exportConfigurationComboBox);
            turnElementOn(exportFileLocation);
            turnElementOn(exportApplyButton);
        }
        exportTime = 0;
        exportPointsCount = 0;
        if (isExported.isSelected()) {
            writeToTxt(exportFileConfiguration);
        }
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
     * Метод displayOutputs() необходим для отображения данных в таблицах.
     * В начале он записывает данные, полученные от контроллера, в таблицу, используя массив outputsForTables и метод
     * getOutputs() класса modbusConnection. Затем удаляет последний элемент списка значений токов и записывает в него
     * новые, беря, соответственно, 3, 4 и 5 элементы из массива outputsForTables.
     * Для напряжений действия аналогичные, но берутся элементы 0,1 и 2.
     * В конце данный метод помещает актуальные значения в таблицы.
     **/

    public void displayOutputs() {
        outputsForChartsAndTables = modbusConnection.getOutputs();

        currentsData.removeLast();
        currentsData.add(new Currents(outputsForChartsAndTables[3], outputsForChartsAndTables[4], outputsForChartsAndTables[5]));

        voltagesData.removeLast();
        voltagesData.add(new Voltages(outputsForChartsAndTables[0], outputsForChartsAndTables[1], outputsForChartsAndTables[2]));

        currentsTable.setItems(currentsData);
        voltagesTable.setItems(voltagesData);
    }

    public void turnElementOff(@NotNull Node element) {
        element.setDisable(true);
    }

    public void turnElementOn(@NotNull Node element) {
        element.setDisable(false);
    }
    public void writeToTxt (String Configuration) throws IOException{
        writer = new BufferedWriter(new FileWriter(new File(pathToExportFile)));
        if (Objects.equals(Configuration, "Токи")) {
            writer.write("Время, с");
            writer.write("; ");
            writer.write("Ток фазы А, А");
            writer.write("; ");
            writer.write("Ток фазы В, А");
            writer.write("; ");
            writer.write("Ток фазы С, А");
            writer.write("\r\n");

            for (int i = 0; i < time.length; i++) {
                    writer.write(String.valueOf(time[i]));
                    writer.write("; ");
                    writer.write(String.valueOf(export[i][3]));
                    writer.write("; ");
                    writer.write(String.valueOf(export[i][4]));
                    writer.write("; ");
                    writer.write(String.valueOf(export[i][5]));
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

            for (int i = 0; i < time.length; i++) {
                writer.write(String.valueOf(time[i]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][0]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][1]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][2]));
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

            for (int i = 0; i < time.length; i++) {
                writer.write(String.valueOf(time[i]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][3]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][4]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][5]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][0]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][1]));
                writer.write("; ");
                writer.write(String.valueOf(export[i][2]));
                writer.write("\r\n");
            }
            writer.flush();
        }
    }

}

