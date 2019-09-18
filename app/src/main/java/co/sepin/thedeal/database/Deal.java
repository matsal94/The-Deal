package co.sepin.thedeal.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(database = AppDatabase.class)
public class Deal extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = false)
    @Expose(serialize = false, deserialize = true)
    public String id;

    @Column
    @SerializedName("uid")
    @Expose
    public String userId;

    @Column
    @SerializedName("tel_num")
    @Expose
    public String phone;

    @Column
    @Expose
    public String title;

    @Column
    @Expose
    public String description;

    @Column
    @SerializedName("date_time")
    @Expose
    public long createDate;

    @Column
    @SerializedName("category_id")
    @Expose
    public long categoryId;
    @ForeignKey(saveForeignKeyModel = false)
    DealType dealtype;


    public Deal() {
    }


    public Deal(String id, String userId, String title, String phone, String desc, long createDate, long categoryId) {

        this.id = id;
        this.userId = userId;
        this.title = title;
        this.phone = phone;
        this.description = desc;
        this.createDate = createDate;
        this.categoryId = categoryId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
}