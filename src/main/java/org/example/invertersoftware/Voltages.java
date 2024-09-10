package org.example.invertersoftware;

public class Voltages {
    private float UA;
    private float UB;
    private float UC;

    public Voltages(float UA, float UB, float UC) {
        this.UA = UA;
        this.UB = UB;
        this.UC = UC;
    }
    public Voltages() {
    }

    public float getUA() {
        return UA;
    }

    public void setUA(float UA) {
        this.UA = UA;
    }

    public float getUB() {
        return UB;
    }

    public void setUB(float UB) {
        this.UB = UB;
    }

    public float getUC() {
        return UC;
    }

    public void setUC(float UC) {
        this.UC = UC;
    }
}
