package smk.adzikro.indextemaquran.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import smk.adzikro.indextemaquran.constans.QuranFileConstants;

/**
 * Created by server on 1/1/18.
 */

public class QuranDataLocal extends SQLiteOpenHelper {
    private static final String TAG = QuranDataLocal.class.getSimpleName() ;
    private Context context;

    public QuranDataLocal(Context context) {
        super(context, "dataquran", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE quran (_id int, displayName text, translator text, " +
                "translator_asing text, file_url text, file_name text, ada int, type int, active int)");
        insertData(db,"Tafseer Adzikro (Indonesia)","Dr. Deden Syamsul Hidayat, M.Ag","","","",1,1001,0);
        insertData(db,"Tafseer Ibnu Katsir (Indonesia)","","","","quran.id.ibnukatir.db",1,1002,0);
        insertData(db,"Arabic I'rab Al-Quran","","","","quran.ar.irab.db",1,1003,0);
        insertData(db,"Arabic Sharf  Al-Quran","","","","quran.ar.sharf.db",1,1004,0);
        insertData(db,"Arabic Balagha Al-Quran","","","","quran.ar.balagha.db",1,1005,0);
        insertData(db,"Tafseer Jalalain (Indonesia)","","","","quran.id.jalalayn.db",1,1006,0);
      //  insertData(db,"Transliteration","","","", QuranFileConstants.LATIN_DATABASE,0,1007,1);

        try {
            JSONObject jsonObject = new JSONObject(getJson());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            if(jsonArray.length()>0){
                for(int i=0; i<jsonArray.length();i++){
                    insertData(db, jsonArray.getJSONObject(i));
                }
            }
        }catch (JSONException e){
            Log.e(TAG,"aya errot Json "+e.getMessage());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void insertData(SQLiteDatabase db,String nama, String transaltor,
                           String asing, String url, String file, int type, int _id, int active) {
        ContentValues qr = new ContentValues();
        qr.put("_id", _id);
        qr.put("displayName", nama);
        qr.put("translator", transaltor);
        qr.put("translator_asing", asing);
        qr.put("file_url", url);
        qr.put("file_name", file);
        qr.put("ada", 0);
        qr.put("type", type);
        qr.put("active", active);
        db.insert("quran", null, qr);
    }
    private void insertData(SQLiteDatabase db, JSONObject object){
        try {
            int id = object.getInt("id");
            String nama = object.getString("displayName");
            String translator = object.getString("translator");
            String asing = object.getString("translator_foreign");
            String url = object.getString("fileUrl");
            String file = object.getString("fileName");
            int type=0;
            if(nama.contains("Tafseer"))type=1;
            insertData(db,nama,translator,asing,url,file,type,id,0);
        }catch (JSONException e){

        }


    }
    private String getJson(){
        String json = null;
        try {
            InputStream is = context.getAssets().open("translations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG,"aya errot IO "+ex.getMessage());
            return null;
        }
        return json;
    }

}
