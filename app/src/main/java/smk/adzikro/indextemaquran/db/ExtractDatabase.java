package smk.adzikro.indextemaquran.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by server on 11/28/16.
 */

public class ExtractDatabase extends SQLiteOpenHelper {

    public ExtractDatabase(Context context, String data) {
        super(context, data, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
