package org.example.invertersoftware;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.ghgande.j2mod.modbus.ModbusException;

public class ModbusConnection {

    private final ModbusSerialMaster modbusMaster;
    private final int slaveId;
    private final int startAddress;
    private final int quantityOfRegisters;
    private boolean connected = false;
    private boolean running = false;
    private final long scanRate;

    public ModbusConnection(String port, int baudRate, int dataBits, String parity, int stopBits, int interval, int timeout, int retries) {
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
        slaveId = 5; // Укажите идентификатор устройства
        startAddress = 0;
        quantityOfRegisters = 60;
    }
    public void connect() {
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
    public void readRegisters() {
        if (!connected || !running) return;
        try {
            InputRegister[] registers;
            registers = modbusMaster.readMultipleRegisters(slaveId, startAddress, quantityOfRegisters);
            for (int i = 0; i < registers.length; i++) {
                if ((i + 1) % 2 != 0) {
                    System.out.println("Registers " + (startAddress + i) + "-" + (startAddress + i + 1) + " = " + (Float.intBitsToFloat(numbersConnector(registers[i + 1].getValue(), registers[i].getValue()))));
                }
            }
        } catch (ModbusException e) {
            System.out.println("Ошибка при чтении регистров: " + e.getMessage());
        }
    }
    public void startPolling() {
        new Thread(() -> {
            while (running) {
                readRegisters();
                try {
                    Thread.sleep(scanRate);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
    public void stop() {
        running = false;
        disconnect();
    }
    private int numbersConnector(int x, int y) {
        return x | (y << 16);
    }
}

