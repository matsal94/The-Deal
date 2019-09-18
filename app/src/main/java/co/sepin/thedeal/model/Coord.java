package co.sepin.thedeal.model;


import androidx.annotation.NonNull;

public class Coord {

    private double lon;
    private double lat;


    public Coord() {
    }


    public Coord(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }


    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder("[").append(this.lat).append(",").append(this.lon).append("]").toString();
    }
}




