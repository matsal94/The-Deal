package co.sepin.thedeal.model;

public class WeatherUvi {

    private double lat;
    private double lon;
    private String date_iso;
    private int date;
    private double value;


    public WeatherUvi() {
    }


    public WeatherUvi(double lat, double lon, String date_iso, int date, double value) {

        this.lat = lat;
        this.lon = lon;
        this.date_iso = date_iso;
        this.date = date;
        this.value = value;
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

    public String getDate_iso() {
        return date_iso;
    }

    public void setDate_iso(String date_iso) {
        this.date_iso = date_iso;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
