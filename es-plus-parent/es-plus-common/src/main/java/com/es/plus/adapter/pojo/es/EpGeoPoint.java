package com.es.plus.adapter.pojo.es;

/**
 * 自定义GeoPoint类
 */
public class EpGeoPoint {
    private double lat;
    private double lon;

    public EpGeoPoint() {
    }

    public EpGeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return lat + "," + lon;
    }
}