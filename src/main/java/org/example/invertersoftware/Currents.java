package org.example.invertersoftware;

public class Currents {
    private float phaseACurrent;
    private float phaseBCurrent;
    private float phaseCCurrent;

    public Currents(float phaseACurrent, float phaseBCurrent, float phaseCCurrent) {
        this.phaseACurrent = phaseACurrent;
        this.phaseBCurrent = phaseBCurrent;
        this.phaseCCurrent = phaseCCurrent;
    }

    public float getPhaseACurrent() {
        return phaseACurrent;
    }

    public void setPhaseACurrent(float phaseACurrent) {
        this.phaseACurrent = phaseACurrent;
    }

    public float getPhaseBCurrent() {
        return phaseBCurrent;
    }

    public void setPhaseBCurrent(float phaseBCurrent) {
        this.phaseBCurrent = phaseBCurrent;
    }

    public float getPhaseCCurrent() {
        return phaseCCurrent;
    }

    public void setPhaseCCurrent(float phaseCCurrent) {
        this.phaseCCurrent = phaseCCurrent;
    }
}
