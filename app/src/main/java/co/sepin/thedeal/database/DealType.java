package co.sepin.thedeal.database;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(database = AppDatabase.class)
public class DealType extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = false)
    @Expose(serialize = false, deserialize = true) public long id;

    @Column
    @Expose public String description;


    public DealType(long id, String description) {

        this.id = id;
        this.description = description;
    }

    public DealType() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
