package org.example.invertersoftware;

public class voltages {
    private float phaseAVoltage;
    private float phaseBVoltage;
    private float phaseCVoltage;

    public voltages(float phaseAVoltage, float phaseBVoltage, float phaseCVoltage) {
        this.phaseAVoltage = phaseAVoltage;
        this.phaseBVoltage = phaseBVoltage;
        this.phaseCVoltage = phaseCVoltage;
    }

    public float getPhaseAVoltage() {
        return phaseAVoltage;
    }

    public void setPhaseAVoltage(float phaseAVoltage) {
        this.phaseAVoltage = phaseAVoltage;
    }

    public float getPhaseBVoltage() {
        return phaseBVoltage;
    }

    public void setPhaseBVoltage(float phaseBVoltage) {
        this.phaseBVoltage = phaseBVoltage;
    }

    public float getPhaseCVoltage() {
        return phaseCVoltage;
    }

    public void setPhaseCVoltage(float phaseCVoltage) {
        this.phaseCVoltage = phaseCVoltage;
    }
}
