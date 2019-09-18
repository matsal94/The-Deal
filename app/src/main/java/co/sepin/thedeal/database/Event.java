package co.sepin.thedeal.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;


@Table(database = AppDatabase.class)
public class Event extends BaseModel implements Serializable {

    @Column
    @PrimaryKey(autoincrement = false)
    @Expose(serialize = false, deserialize = true)
    public String id;

    @Column
    @SerializedName("uid")
    @Expose
    public String userId;

    @Column
    @Expose
    public String name;

    @Column
    @SerializedName("date_time")
    @Expose
    public long dateTime;

    @Column
    @Expose
    public String address;

    @Column
    @Expose
    public double lat;

    @Column
    @Expose
    public double lon;

    @Column
    @Expose
    public String comment;

    @Column
    @Expose
    public String description;

    @Column
    @Expose
    public int color;


    public Event(String userId, String name, long dateTime, String localization, double lat, double lon, String comment, String description, int color) {

        this.userId = userId;
        this.name = name;
        this.dateTime = dateTime;
        this.address = localization;
        this.lat = lat;
        this.lon = lon;
        this.comment = comment;
        this.description = description;
        this.color = color;
    }


    public Event() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}