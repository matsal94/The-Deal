package co.sepin.thedeal.database;


import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import co.sepin.thedeal.model.Chat;


@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

    static final String NAME = "AppDatabase";
    static final int VERSION = 32;

    // każda tabela musi zawierać atrybut PrimaryKey!
    // przy dodawaniu nowej tabeli zwiększyć numer wersji
    // przy dodawaniu nowej kolumny stworzyć migrację i zwiekszyć numer wersji
    // przy zmianie typu kolumny usunąć tabelę przez migracje, po pomyślnym usunięciu zwiększyć dodatkowo numer wersji i jeszcze raz uruchomić app
    // przy usuwaniu tabeli stworzyć migracje i zwiekszyc numer wersji

    public String getDBName() {
        return NAME;
    }


    @Migration(version = 2, database = AppDatabase.class)
    public static class Migration2 extends AlterTableMigration<Event> {

        public Migration2(Class<Event> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            addColumn(SQLiteType.TEXT, "comment");
        }
    }


    @Migration(version = 3, database = AppDatabase.class)
    public static class Migration3 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {
            database.execSQL("DROP TABLE IF EXISTS UserImage");
        }
    }


    @Migration(version = 6, database = AppDatabase.class)
    public static class Migration5 extends AlterTableMigration<User> {

        public Migration5(Class<User> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.INTEGER, "userId");
            addColumn(SQLiteType.TEXT, "email");
            addColumn(SQLiteType.TEXT, "aboutMe");
        }
    }


    @Migration(version = 8, database = AppDatabase.class)
    public static class Migration8 extends AlterTableMigration<Event> {

        public Migration8(Class<Event> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.REAL, "lat");
            addColumn(SQLiteType.REAL, "lon");
        }
    }


    @Migration(version = 11, database = AppDatabase.class)
    public static class Migration11 extends AlterTableMigration<Contact> {

        public Migration11(Class<Contact> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.TEXT, "avatarUri");
            addColumn(SQLiteType.TEXT, "aboutMe");
        }
    }


    @Migration(version = 12, database = AppDatabase.class)
    public static class Migration12 extends AlterTableMigration<Deal> {

        public Migration12(Class<Deal> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.TEXT, "phone");
        }
    }


    @Migration(version = 15, database = AppDatabase.class)
    public static class Migration15 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {

            database.execSQL("DROP TABLE IF EXISTS Deal");
        }
    }


    @Migration(version = 17, database = AppDatabase.class)
    public static class Migration17 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {

            database.execSQL("DROP TABLE IF EXISTS UserImage");
        }
    }


    @Migration(version = 19, database = AppDatabase.class)
    public static class Migration19 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {

            database.execSQL("DROP TABLE IF EXISTS Contact");
        }
    }


    @Migration(version = 21, database = AppDatabase.class)
    public static class Migration21 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {

            database.execSQL("DROP TABLE IF EXISTS UserImage");
        }
    }


    @Migration(version = 22, database = AppDatabase.class)
    public static class Migration22 extends AlterTableMigration<User> {

        public Migration22(Class<User> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.TEXT, "avatarUri");
        }
    }


    @Migration(version = 24, database = AppDatabase.class)
    public static class Migration24 extends AlterTableMigration<Chat> {

        public Migration24(Class<Chat> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.TEXT, "receiverTelephone");
        }
    }


    @Migration(version = 25, database = AppDatabase.class)
    public static class Migration25 extends AlterTableMigration<Chat> {

        public Migration25(Class<Chat> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.TEXT, "senderTelephone");
        }
    }


    @Migration(version = 26, database = AppDatabase.class)
    public static class Migration26 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {

            database.execSQL("DROP TABLE IF EXISTS Invitations");
        }
    }


    @Migration(version = 27, database = AppDatabase.class)
    public static class Migration27 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {

            database.execSQL("DROP TABLE IF EXISTS Chat");
        }
    }


    @Migration(version = 30, database = AppDatabase.class)
    public static class Migration28 extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {

            database.execSQL("DROP TABLE IF EXISTS Event");
        }
    }


    @Migration(version = 32, database = AppDatabase.class)
    public static class Migration32 extends AlterTableMigration<Contact> {

        public Migration32(Class<Contact> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {

            addColumn(SQLiteType.TEXT, "status");
            addColumn(SQLiteType.TEXT, "lastSeen");
        }
    }
}