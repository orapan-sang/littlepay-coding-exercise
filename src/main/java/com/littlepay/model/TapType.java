package com.littlepay.model;

public enum TapType {
    ON,
    OFF;

    public boolean isTapOn() {
        return this.equals(ON);
    }

    public boolean isTapOff() {
        return this.equals(OFF);
    }
}
