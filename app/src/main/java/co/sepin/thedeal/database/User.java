package co.sepin.thedeal.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(database = AppDatabase.class)
public class User extends BaseModel {
/*
    @Column
    @Expose (serialize = false, deserialize = true) public long id;*/

    @Column
    @PrimaryKey(autoincrement = false)
    @SerializedName("uid")
    @Expose public String userId;

    @Column
    @SerializedName("avatar_uri")
    @Expose public String avatarUri;

    @Column
    @SerializedName("tel_num")
    @Expose public String phone;

    @Column
    @Expose public String email;

    @Column
    @SerializedName("about_me")
    @Expose public String aboutMe;

    @Column
    @Expose public String token;


    public User() {
    }


    public User(String phone, String email, String aboutMe) {

        this.phone = phone;
        this.email = email;
        this.aboutMe = aboutMe;
    }


    public User(String userId, String avatarUri, String phone, String email, String aboutMe, String token) {

        this.userId = userId;
        this.avatarUri = avatarUri;
        this.phone = phone;
        this.email = email;
        this.aboutMe = aboutMe;
        this.token = token;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}