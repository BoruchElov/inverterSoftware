package org.example.invertersoftware;

public class Currents {
    private float IA;
    private float IB;
    private float IC;

    public Currents(float IA, float IB, float IC) {
        this.IA = IA;
        this.IB = IB;
        this.IC = IC;
    }
    public Currents() {
    }

    public float getIA() {
        return IA;
    }

    public void setIA(float IA) {
        this.IA = IA;
    }

    public float getIB() {
        return IB;
    }

    public void setIB(float IB) {
        this.IB = IB;
    }

    public float getIC() {
        return IC;
    }

    public void setIC(float IC) {
        this.IC = IC;
    }
}
