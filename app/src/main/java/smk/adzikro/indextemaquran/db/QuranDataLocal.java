package smk.adzikro.indextemaquran.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 1/1/18.
 */

public class QuranDataLocal extends SQLiteOpenHelper {
    private static final String TAG = QuranDataLocal.class.getSimpleName() ;
    private Context context;

    public QuranDataLocal(Context context) {
        super(context, Fungsi.PATH_DATABASES(context)+File.separator+"Translation.db", null, 2);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
  //      Log.e(TAG,"onCreate");
        db.execSQL("CREATE TABLE quran (_id int, " +
                "displayName text, " +
                "translator text, " +
                "translator_asing text, " +
                "file_url text, " +
                "file_name text, " +
                "ada int, " +
                "type int, " +
                "active int, " +
                "languageCode text)");
        insertData(db,"Tafseer Adzikro (Indonesia)","","","https://www.dropbox.com/s/v35bglwwqn1s28w/quran.id.adzikro.db?dl=1","quran.id.adzikro.db",1,1001,0,"id");
        insertData(db,"Tafseer Ibnu Katsir (Indonesia)","","","https://www.dropbox.com/s/nvi6x23sk0ae0ki/quran.id.ibnukatir.db?dl=1","quran.id.ibnukatir.db",1,1002,0,"id");
        insertData(db,"Arabic I'rab Al-Quran","","","https://www.dropbox.com/s/5y295sny0mywnel/quran.ar.irab.db?dl=1","quran.ar.irab.db",1,1003,0,"ar");
        insertData(db,"Arabic Sharf  Al-Quran","","","https://www.dropbox.com/s/sun13kgsssbudws/quran.ar.sharf.db?dl=1","quran.ar.sharf.db",1,1004,0,"ar");
        insertData(db,"Arabic Balagha Al-Quran","","","https://www.dropbox.com/s/mu4wlsq3zav81ce/quran.ar.balagha.db?dl=1","quran.ar.balagha.db",1,1005,0,"ar");
        insertData(db,"Tafseer Jalalain (Indonesia)","","","https://www.dropbox.com/s/pd5b0druad2n1a8/quran.id.jalalain.db?dl=1","quran.id.jalalayn.db",1,1006,0,"id");
        insertData(db,"Tafseer Ibnu Katsir (English)","","","https://www.dropbox.com/s/wxhkhpsq0ecynr0/quran.en.kathir.db?dl=1","quran.en.ibnukatir.db",1,1007,0,"en");
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
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE quran");
        db.execSQL("CREATE TABLE quran (_id int, " +
                "displayName text, " +
                "translator text, " +
                "translator_asing text, " +
                "file_url text, " +
                "file_name text, " +
                "ada int, " +
                "type int, " +
                "active int, " +
                "languageCode text)");
        insertData(db,"Tafseer Adzikro (Indonesia)","","","https://www.dropbox.com/s/v35bglwwqn1s28w/quran.id.adzikro.db?dl=1","quran.id.adzikro.db",1,1001,0,"id");
        insertData(db,"Tafseer Ibnu Katsir (Indonesia)","","","https://www.dropbox.com/s/nvi6x23sk0ae0ki/quran.id.ibnukatir.db?dl=1","quran.id.ibnukatir.db",1,1002,0,"id");
        insertData(db,"Arabic I'rab Al-Quran","","","https://www.dropbox.com/s/5y295sny0mywnel/quran.ar.irab.db?dl=1","quran.ar.irab.db",1,1003,0,"ar");
        insertData(db,"Arabic Sharf  Al-Quran","","","https://www.dropbox.com/s/sun13kgsssbudws/quran.ar.sharf.db?dl=1","quran.ar.sharf.db",1,1004,0,"ar");
        insertData(db,"Arabic Balagha Al-Quran","","","https://www.dropbox.com/s/mu4wlsq3zav81ce/quran.ar.balagha.db?dl=1","quran.ar.balagha.db",1,1005,0,"ar");
        insertData(db,"Tafseer Jalalain (Indonesia)","","","https://www.dropbox.com/s/pd5b0druad2n1a8/quran.id.jalalain.db?dl=1","quran.id.jalalayn.db",1,1006,0,"id");
        insertData(db,"Tafseer Ibnu Katsir (English)","","","https://www.dropbox.com/s/wxhkhpsq0ecynr0/quran.en.kathir.db?dl=1","quran.en.ibnukatir.db",1,1007,0,"en");
        //  insertData(db,"Transliteration","","","", QuranFileConstants.LATIN_DATABASE,0,1007,1);

        try {
            JSONObject jsonObject = new JSONObject(getJson());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            if(jsonArray.length()>0){
                for(int xi=0; i<jsonArray.length();xi++){
                    insertData(db, jsonArray.getJSONObject(xi));
                }
            }
        }catch (JSONException e){
            Log.e(TAG,"aya errot Json "+e.getMessage());
        }
    }
    public void insertData(SQLiteDatabase db,String nama, String transaltor,
                           String asing, String url, String file, int type, int _id, int active, String code) {
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
        qr.put("languageCode",code);
        db.insert("quran", null, qr);
    }
    private void insertData(SQLiteDatabase db, JSONObject object){
        try {
            int id = object.getInt("id");
            String nama = object.getString("displayName");
            String translator=" ";
          //  if(!object.getString("translator").isEmpty()) {
           //     translator = object.getString("translator");
            //}

       //     Log.e(TAG,id+" "+nama+translator);
            String asing = " ";//object.getString("translatorForeign");
            String url = object.getString("fileUrl");
            String file = object.getString("fileName");
            String code = object.getString("languageCode");
            int type=0;
            if(nama.contains("Tafseer"))type=1;

            insertData(db,nama,translator,asing,url,file,type,id,0,code);
        }catch (JSONException e){
            Log.e(TAG,"eror json "+e.getMessage());
        }


    }
    private String getJson(){
      //  Log.e(TAG, "Ambil File Json");
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
    public List<QuranSource> getTranslations(){
        List<QuranSource> data = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String sql = "select * from quran where ada=1";
        Cursor cursor = db.rawQuery(sql,null);
    //    Log.e(TAG, "aya teu "+cursor.getCount());

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    QuranSource q= new QuranSource(cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(5),
                            cursor.getString(4));
                    q.setTranslator_asing(cursor.getString(3));
                    q.setAda(cursor.getInt(6));
                    q.setType(cursor.getInt(7));
                    q.setActive(cursor.getInt(8));
                    q.setId(cursor.getInt(0));
                    q.setLanguageCode(cursor.getString(9));
                    data.add(q);
                }
            }
        }finally {
            cursor.close();
            db.close();
        }
        return data;
    }
}
