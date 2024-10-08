package org.example.invertersoftware;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.ghgande.j2mod.modbus.ModbusException;

import java.util.Objects;

public class ModbusConnection {
    Register[] storingRegisters;
    InputRegister[] registers;
    private float[] controlSystemParameters = new float[4];
    private float[] outputs = new float[6];
    private final ModbusSerialMaster modbusMaster;
    private final int slaveId;
    private final int startAddress;
    private final int quantityOfRegisters;
    private boolean connected = false;
    private boolean running = false;
    private final long scanRate;

    public ModbusConnection(String port, int baudRate, int dataBits, String parity, int stopBits, int interval, int timeout, int retries, int slaveID) {
        // Настройки последовательного порта
        SerialParameters params = new SerialParameters();
        params.setPortName(port);
        params.setBaudRate(baudRate);
        params.setDatabits(dataBits);
        params.setParity(parity.equals("None") ? AbstractSerialConnection.NO_PARITY :
                parity.equals("Even") ? AbstractSerialConnection.EVEN_PARITY :
                        AbstractSerialConnection.ODD_PARITY);
        params.setStopbits(stopBits);
        params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
        params.setEcho(false);

        modbusMaster = new ModbusSerialMaster(params);
        modbusMaster.setTimeout(timeout);
        modbusMaster.setRetries(retries);
        scanRate = interval;
        slaveId = slaveID; // Укажите идентификатор устройства
        startAddress = 0;
        quantityOfRegisters = 112;
    }
    public void connect() throws ModbusException {
        try {
            modbusMaster.connect();
            connected = true;
            running = true;
            System.out.println("Соединение установлено.");
        } catch (ModbusException e) {
            System.out.println("Ошибка при подключении к устройству: " + e.getMessage());
            connected = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        createRegistersObject();
    }
    public void disconnect() {
        try {
            if (connected) {
                modbusMaster.disconnect();
                System.out.println("Соединение закрыто.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
        } finally {
            connected = false;
            running = false;
        }
    }
    public void createRegistersObject() throws ModbusException {
        storingRegisters = modbusMaster.readMultipleRegisters(slaveId, startAddress, quantityOfRegisters);
    }

    public void updateControlSystemParameters() throws ModbusException {
        registers = modbusMaster.readMultipleRegisters(slaveId, startAddress, quantityOfRegisters);

        controlSystemParameters[0] = Float.intBitsToFloat(numbersConnector(registers[11].getValue(), registers[10].getValue()));
        controlSystemParameters[1] = Float.intBitsToFloat(numbersConnector(registers[7].getValue(), registers[6].getValue()));
        controlSystemParameters[2] = Float.intBitsToFloat(numbersConnector(registers[31].getValue(), registers[30].getValue()));
        controlSystemParameters[3] = Float.intBitsToFloat(numbersConnector(registers[33].getValue(), registers[32].getValue()));
    }

    public void readRegisters() {
        try {
            registers = modbusMaster.readMultipleRegisters(slaveId, startAddress, quantityOfRegisters);
            for (int i = 0; i < registers.length; i++) {
                if ((i + 1) % 2 != 0) {
                    float value = Float.intBitsToFloat(numbersConnector(registers[i + 1].getValue(), registers[i].getValue()));
                    //System.out.println("Registers " + (startAddress + i) + "-" + (startAddress + i + 1) + " = " + value);

                    // Запись значений в глобальный массив
                    if (i == 80) outputs[0] = value;  // Пример для регистров 34-35
                    if (i == 82) outputs[1] = value;  // Пример для регистров 36-37
                    if (i == 84) outputs[2] = value;  // Пример для регистров 38-39
                    if (i == 86) outputs[3] = value;  // Пример для регистров 40-41
                    if (i == 88) outputs[4] = value;  // Пример для регистров 42-43
                    if (i == 90) outputs[5] = value;  // Пример для регистров 44-45
                }
            }
            controlSystemParameters[0] = Float.intBitsToFloat(numbersConnector(registers[11].getValue(), registers[10].getValue()));
            controlSystemParameters[1] = Float.intBitsToFloat(numbersConnector(registers[7].getValue(), registers[6].getValue()));
            controlSystemParameters[2] = Float.intBitsToFloat(numbersConnector(registers[31].getValue(), registers[30].getValue()));
            controlSystemParameters[3] = Float.intBitsToFloat(numbersConnector(registers[33].getValue(), registers[32].getValue()));

        } catch (ModbusException e) {
            System.out.println("Ошибка при чтении регистров: " + e.getMessage());
        }
    }
    public void writeRegisters(int slave, int offset, String option, float setValue) throws ModbusException {

        if (Objects.equals(option, "Sbase")) {
            storingRegisters[11].setValue(splitNumber(setValue)[1]);
            storingRegisters[10].setValue(splitNumber(setValue)[0]);
        } else if (Objects.equals(option, "Vacbase")) {
            storingRegisters[7].setValue(splitNumber(setValue)[1]);
            storingRegisters[6].setValue(splitNumber(setValue)[0]);
        } else if (Objects.equals(option, "KpPLL")) {
            storingRegisters[31].setValue(splitNumber(setValue)[1]);
            storingRegisters[30].setValue(splitNumber(setValue)[0]);
        } else if (Objects.equals(option, "KiPLL")) {
            storingRegisters[33].setValue(splitNumber(setValue)[1]);
            storingRegisters[32].setValue(splitNumber(setValue)[0]);
        }
        modbusMaster.writeMultipleRegisters(slave, offset, storingRegisters);
    }

    public float[] getOutputs() {
        return outputs;
    }

    public void stop() {
        running = false;
        disconnect();
    }

    public float showParameterValue(String parameter) {
        if (Objects.equals(parameter, "Sbase")) {
            return controlSystemParameters[0];
        } else if (Objects.equals(parameter, "Vacbase")) {
            return controlSystemParameters[1];
        } else if (Objects.equals(parameter, "KpPLL")) {
            return controlSystemParameters[2];
        } else if (Objects.equals(parameter, "KiPLL")) {
            return controlSystemParameters[3];
        } else {
            return 0;
        }
    }

    private int numbersConnector(int x, int y) {
        return x | (y << 16);
    }

    public static int[] splitNumber(float originalFloatNumber) {
        int[] outputValues = new int[2];
        outputValues[0] = Float.floatToIntBits(originalFloatNumber) >> 16;
        outputValues[1] = Float.floatToIntBits(originalFloatNumber) & 0xFFFF;
        return outputValues;
    }

}

