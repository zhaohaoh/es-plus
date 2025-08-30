package com.es.plus.adapter.pojo.es;

/**
 * 自定义距离单位枚举
 */
public enum EpDistanceUnit {
    MILES(1609.344, "mi"),
    YARDS(0.9144, "yd"),
    FEET(0.3048, "ft"),
    INCHES(0.0254, "in"),
    KILOMETERS(1000.0, "km"),
    METERS(1.0, "m"),
    CENTIMETERS(0.01, "cm"),
    MILLIMETERS(0.001, "mm"),
    NAUTICAL_MILES(1852.0, "nmi");

    private final double meters;
    private final String unit;

    EpDistanceUnit(double meters, String unit) {
        this.meters = meters;
        this.unit = unit;
    }

    public double getMeters() {
        return meters;
    }

    public String getUnit() {
        return unit;
    }

    public double toMeters(double distance) {
        return distance * meters;
    }

    public double fromMeters(double meters) {
        return meters / this.meters;
    }

    @Override
    public String toString() {
        return unit;
    }
}