package co.sepin.thedeal.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(database = AppDatabase.class)
public class OneTimePassword extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    int id;

    @Column
    String password;

    public void insertData(String Password) {
        this.password = Password;
    }


}
