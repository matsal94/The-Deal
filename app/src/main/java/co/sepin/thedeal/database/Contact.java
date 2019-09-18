package co.sepin.thedeal.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(database = AppDatabase.class, useBooleanGetterSetters = true)
public class Contact extends BaseModel {

    @Column
    @Expose//(serialize = false, deserialize = true)
    @SerializedName("uid")
    private String userId;

    @Column
    private int avatar;

    @Column
    @Expose
    @SerializedName("avatar_uri")
    private String avatarUri;

    @Column
    @Expose
    private String name;

    @Column
    @PrimaryKey(autoincrement = false)
    @Expose
    @SerializedName("tel_num")
    private String telephone;

    @Column
    @Expose
    @SerializedName("about_me")
    private String aboutMe;

    @Column
    @Expose
    private String email;

    @Column
    @Expose
    private String status;

    @Column
    @Expose
    @SerializedName("last_seen")
    private String lastSeen;

    @Column
    private int isUser;


    public Contact() {
    }


    public Contact(String name, String telephone, int isUser) {

        this.name = name;
        this.telephone = telephone;
        this.isUser = isUser;
    }


    public Contact(String userId, String avatarUri, String name, String telephone, String aboutMe, String email, String status, String lastSeen) {

        this.userId = userId;
        this.avatarUri = avatarUri;
        this.name = name;
        this.telephone = telephone;
        this.aboutMe = aboutMe;
        this.email = email;
        this.status = status;
        this.lastSeen = lastSeen;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setIsUser(int user) {
        isUser = user;
    }

    public int getIsUser() {
        return isUser;
    }
}