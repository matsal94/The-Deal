package co.sepin.thedeal.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(database = AppDatabase.class)
public class Invitations extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = false)
    long id;

    @Column
    String phone;

    @Column
    String otp;


}
