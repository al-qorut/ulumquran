package smk.adzikro.indextemaquran.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 12/11/17.
 */

public class CreateDb extends SQLiteOpenHelper {
    public static final String FIELD_SURAT ="sura";
    public static final String FIELD_AYAT ="ayah";
    public static final String FIELD_TEXT ="text";
    public static final String TABLE_NAME ="verses";

    public CreateDb(Context context, String name) {
        super(context, Fungsi.PATH_DATABASE()+name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE VIRTUAL TABLE "+TABLE_NAME+" using fts3("+FIELD_SURAT+" Integer, "+FIELD_AYAT+" Integer, "+FIELD_TEXT+" text, primary key("+FIELD_SURAT+", "+FIELD_AYAT+"))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
