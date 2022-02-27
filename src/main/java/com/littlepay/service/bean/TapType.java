package com.littlepay.service.bean;

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
