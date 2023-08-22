package com.example.SoundNet;

public class Frequency {
    private final int fMin;
    private final int fSync;
    private final int fEnd;

    Frequency(int fMin, int fSync, int fEnd) {
        this.fMin = fMin;
        this.fSync = fSync;
        this.fEnd = fEnd;
    }

    public int getFreMin() {
        return fMin;
    }

    public int getFreSync() {
        return fSync;
    }

    public int getFreEnd() {
        return fEnd;
    }
}
