package smk.adzikro.indextemaquran.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.db.QuranDataLocal;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.object.VerseRange;
import smk.adzikro.indextemaquran.services.QuranDownloadService;
import smk.adzikro.indextemaquran.services.utils.ServiceIntentHelper;


/**
 * Created by server on 1/28/16.
 */
public class Fungsi {
    public static final String SETTING = "setting";
    private static final String TAG = "Fungsi";
    public static URL url=null;
    private static Context context;

    public static String getAyahWithoutBasmallah(int sura, int ayah, String ayahText) {
        // note that ayahText.startsWith check is always true for now - but it's explicitly here so
        // that if we update quran.ar.db one day to fix this issue and older clients get a new copy of
        // the database, their code continues to work as before.
        if (ayah == 1 && sura != 9 && sura != 1 && ayahText.startsWith(AR_BASMALLAH)) {
            return ayahText.substring(AR_BASMALLAH.length() + 1);
        }
        return ayahText;
    }

    public static List<QuranSource> getDataSourceQuran(Context context){
        SQLiteDatabase db = new QuranDataLocal(context).getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from quran order by type, ada desc",null);
        List<QuranSource> data = new ArrayList<>();
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    QuranSource q= new QuranSource(cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(5),
                            cursor.getString(4));
                    q.setTranslator_asing(cursor.getString(3));
                    File file = new File(Fungsi.PATH_DATABASE()+cursor.getString(5));
                    if(file.exists()){
                        db.execSQL("update quran set ada=1 where _id="+cursor.getInt(0));
                    }
                    q.setAda(cursor.getInt(6));
                    q.setType(cursor.getInt(7));
                    q.setActive(cursor.getInt(8));
                    q.setId(cursor.getInt(0));
                    data.add(q);
                }
            }
        }finally {
            cursor.close();
            db.close();
        }
        return data;
    }

    public static void share(Context context,CharSequence isi){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Ulum Al-Qur'an");
            String sAux = (String) isi;
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            context.startActivity(Intent.createChooser(i, "Pilih.."));
        } catch (Exception e) { //e.toString();
        }
    }


    public static void createPolder(){
        String PATH_DATABASE = "/data/data/smk.adzikro.indextemaquran/databases/";

        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/.adzikro");
        if(!file.exists()) file.mkdir();
        file = new File(PATH_DATABASE);
        if(!file.exists()) file.mkdir();
        file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/.adzikro/indexQuran");
        if(!file.exists()) file.mkdir();
        file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/.adzikro/indexQuran/database");
        if(!file.exists()) file.mkdir();
        file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/.adzikro/indexQuran/images");
        if(!file.exists()) file.mkdir();
    }

    public static boolean isFileImageExist(){
        boolean ada = false;
        File file = new File(PATH_IMAGES()+"page604.png");
        if(file.exists()){
            ada = true;
        }else {
            file = new File(PATH_OLD_IMAGES() + "page604.png");
            if (file.exists()) {
                ada = true;
            } else {
                file = new File(PATH_QURAN_IMAGES_1260() + "page604.png");
                if (file.exists()) {
                    ada = true;
                } else {
                    file = new File(PATH_QURAN_IMAGES_1024() + "page604.png");
                    if (file.exists()) {
                        ada = true;
                    } else {
                        file = new File(PATH_QURAN_IMAGES_800() + "page604.png");
                        if (file.exists()) {
                            ada = true;
                        }else {
                            file = new File(PATH_QURAN_IMAGES_480() + "page604.png");
                            if (file.exists()) {
                                ada = true;
                            }
                        }
                    }
                }
            }
        }
        return  ada;
    }
    public static String getPathImage(){
        String path = "";
        File file = new File(PATH_IMAGES()+"page604.png");
        if(file.exists()){
            path = PATH_IMAGES();
        }else {
            file = new File(PATH_OLD_IMAGES() + "page604.png");
            if (file.exists()) {
                path = PATH_OLD_IMAGES();
            } else {
                file = new File(PATH_QURAN_IMAGES_1260() + "page604.png");
                if (file.exists()) {
                    path = PATH_QURAN_IMAGES_1260();
                } else {
                    file = new File(PATH_QURAN_IMAGES_1024() + "page604.png");
                    if (file.exists()) {
                        path = PATH_QURAN_IMAGES_1024();
                    } else {
                        file = new File(PATH_QURAN_IMAGES_800() + "page604.png");
                        if (file.exists()) {
                            path = PATH_QURAN_IMAGES_800();
                        }else {
                            file = new File(PATH_QURAN_IMAGES_480() + "page604.png");
                            if (file.exists()) {
                                path = PATH_QURAN_IMAGES_480();
                            }
                        }
                    }
                }
            }
        }
        return path;
    }

    public static boolean isFileDataExist(){
        File file = new File(PATH_DATABASE()+"index3");
        return file.exists();
    }
    final static private int WRITE_ESCDARD=1;
    public static void cekPermision(Activity compat){
        if (ContextCompat.checkSelfPermission(compat,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(compat,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(compat,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_ESCDARD);
            } else {

                ActivityCompat.requestPermissions(compat,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_ESCDARD);
            }
        } else {
            createPolder();
            Log.e("Fungsi", "cek self fermision false");
            Log.e(TAG,"Load Service");
            Intent intent = new Intent(compat, QuranDownloadService.class);
            intent.putExtra("url", QuranFileConstants.DATABASE_BASE_URL+QuranFileConstants.ARABIC_DATABASE);
            intent.putExtra("file_name",Fungsi.PATH_DATABASE()+QuranFileConstants.ARABIC_DATABASE);
            compat.startService(intent);
        }
    }

    public static String PATH_DATABASE(){

        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/.adzikro/indexQuran/databases/";
    }
    public static String PATH_IMAGES(){

        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/.adzikro/indexQuran/images/";
    }
    public static String PATH_OLD_IMAGES(){

        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/indexQuran/image/";
    }
    public static String PATH_QURAN_IMAGES_800(){
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/quran_android/width_800/";
    }
    public static String PATH_QURAN_IMAGES_1024(){
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/quran_android/width_1024/";
    }
    public static String PATH_QURAN_IMAGES_1260(){
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/quran_android/width_1260/";
    }
    public static String PATH_QURAN_IMAGES_480(){
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/quran_android/width_480/";
    }
    public static String PATH_QURAN_IMAGES_320(){
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()+"/quran_android/width_320/";
    }
    public static void ShowMessage(Context context, String judul, String isi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(judul);
        builder.setMessage(isi)
                .setCancelable(false)
                .setPositiveButton("Ok ", new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        }).show();

    }
    public static String getTafsirAdzikro(){
        return PATH_DATABASE()+ Constants.TAFSIR_ADZIKRO;
    }
    public static String getTafsirIbnuKatsir(){
        return PATH_DATABASE()+Constants.TAFSIR_IBNU_KATSIR;
    }
    public static String getTafsirLafdzi(){
        return PATH_DATABASE()+Constants.TERJEMAH_LAFDZI;
    }
    public static String getKunci() {
        Encryption sokAcak = Encryption.getDefault("!!Al-Qorut", "Salto", new byte[16]);
        String hasilAcak = sokAcak.encryptOrNull("KunciNaCanBener");
        return hasilAcak;
    }
    public static String getVersi(Context context){
        PackageManager manager = context.getPackageManager();
        String versi="0";
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versi = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versi;
    }
    public static Boolean getInstalasi(Context context) {
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        Boolean install = set.getBoolean(getVersi(context), false);
        return install;
    }

    public static void setInstalasi(Context context, Boolean selesai){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
        editor.putBoolean(getVersi(context), selesai);
        editor.commit();
    }

   public static Boolean getBaru(Context context){
       SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
       Boolean mimiti = set.getBoolean("pertama", true);
       return mimiti;
   }
    public static Boolean getTafsirIbnuKastir(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        Boolean tafsir = set.getBoolean("tafsir_ibnu_katsir", false);
        return tafsir;
    }
    public static Boolean getTafsirIrab(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        Boolean tafsir = set.getBoolean("tafsir_irab_quran", false);
        return tafsir;
    }
    public static Boolean getTafsirSharf(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        Boolean tafsir = set.getBoolean("tafsir_sharf_quran", false);
        return tafsir;
    }
    public static Boolean getTafsirBalagha(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        Boolean tafsir = set.getBoolean("tafsir_balagha_quran", false);
        return tafsir;
    }

    public static String getNamaPengguna(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        String nama= set.getString("namapengguna", "free");
        return nama;
    }

    public static void setNamaPengguna(Context context, String nama){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
        editor.putString("namapengguna", nama);
        editor.commit();
    }
  public static void setBaru(Context context, Boolean baru){
      SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
      editor.putBoolean("pertama", baru);
      editor.commit();
  }
    public static void setLastAksi(Context context, int aksi){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
        editor.putInt("aksi", aksi);
        editor.commit();
    }
    public static void setLastPage(Context context, int page){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
        editor.putInt("page", page);
        editor.commit();
    }
    public static int getLastAksi(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        int aksi = set.getInt("aksi", 0);
        return aksi;
    }
    public static int getLastPage(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        int aksi = set.getInt("page", 1);
        return aksi;
    }

  public static Typeface getHurufArab(Context context){
      final Typeface face;
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      final String  value = sharedPreferences.getString("huruf", null);
      if (value == null) {
          face = Typeface.createFromAsset(context.getAssets(), "font/quran.ttf");
      } else {
          face = Typeface.createFromAsset(context.getAssets(), "font/" + value);
      }
      return face;
  }

  public static int getSizeHuruf(Context context){
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      final int sizeHuruf = sharedPreferences.getInt("size", 18);
      return sizeHuruf;
  }

  public static Boolean getModeView(Context context){
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      Boolean mode_terjemah = sharedPreferences.getBoolean("mode_view", false);
      return mode_terjemah;
  }
    public static Boolean getRegAwal(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean mode_terjemah = sharedPreferences.getBoolean("regAwal", true);
        return mode_terjemah;
    }
    public static void setRegAwal(Context context, boolean aksi){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
        editor.putBoolean("regAwal", aksi);
        editor.commit();
    }
public static void showInfo(Context context, CharSequence info){
    Toast.makeText(context, info,Toast.LENGTH_LONG).show();
}
    public static void setInetTersedia(Context context,Boolean ada){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
        editor.putBoolean("internet", ada);
        editor.commit();
    }


    public static Boolean getInetTersedia(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        Boolean ada= set.getBoolean("internet", false);
        return ada;
    }

    public static String getNamaAksi(Context context,int aksi){
        if (aksi==12){
            return "Tadarrus Mode Mushaf";
        }else if(aksi==11){
            return "Tadarrus Mode Text";
        }else{
            return context.getResources().getStringArray(R.array.aksi)[aksi];

        }
    }

  public static final String AR_BASMALLAH = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ";

    public static String getQuery(Integer[] mAyahBounds){
      int minSura = mAyahBounds[0];
      int maxSura = mAyahBounds[2];
      String COL_SURA = "ALQURAN.surat";
      String COL_AYAH = "ALQURAN.ayat";
      String minAyah = String.valueOf(mAyahBounds[1]);
      String maxAyah = String.valueOf(mAyahBounds[3]);

      StringBuilder whereQuery = new StringBuilder();
      whereQuery.append("(");

      if (minSura == maxSura) {
          whereQuery.append(COL_SURA)
                  .append("=").append(minSura)
                  .append(" and ").append(COL_AYAH)
                  .append(">=").append(minAyah)
                  .append(" and ").append(COL_AYAH)
                  .append("<=").append(maxAyah);
      } else {
    //      Log.e (TAG,"sura = minSura and ayah >= minAyah)");
          whereQuery.append("(").append(COL_SURA).append("=")
                  .append(minSura).append(" and ")
                  .append(COL_AYAH).append(">=").append(minAyah).append(")");
      //    Log.e("Hasil Query1 = ",String.valueOf(whereQuery));
          whereQuery.append(" or ");
      //    Log.e("Hasil Query2 = ",String.valueOf(whereQuery));
          // (sura = maxSura and ayah <= maxAyah)
          whereQuery.append("(").append(COL_SURA).append("=")
                  .append(maxSura).append(" and ")
                  .append(COL_AYAH).append("<=").append(maxAyah).append(")");
       //   Log.e("Hasil Query3 = ",String.valueOf(whereQuery));
          whereQuery.append(" or ");
        //  Log.e("Hasil Query4 = ",String.valueOf(whereQuery));
          // (sura > minSura and sura < maxSura)
          whereQuery.append("(").append(COL_SURA).append(">")
                  .append(minSura).append(" and ")
                  .append(COL_SURA).append("<")
                  .append(maxSura).append(")");
        //  Log.e("Hasil Query5 = ",String.valueOf(whereQuery));
      }

      whereQuery.append(")");
      return String.valueOf(whereQuery);
  }
    public static String getQueryTafsir(Integer[] mAyahBounds){
        int minSura = mAyahBounds[0];
        int maxSura = mAyahBounds[2];
        String COL_SURA = "surat";
        String COL_AYAH = "ayat";
        String minAyah = String.valueOf(mAyahBounds[1]);
        String maxAyah = String.valueOf(mAyahBounds[3]);

        StringBuilder whereQuery = new StringBuilder();
        whereQuery.append("(");

        if (minSura == maxSura) {
            whereQuery.append(COL_SURA)
                    .append("=").append(minSura)
                    .append(" and ").append(COL_AYAH)
                    .append(">=").append(minAyah)
                    .append(" and ").append(COL_AYAH)
                    .append("<=").append(maxAyah);
        } else {
            //      Log.e (TAG,"sura = minSura and ayah >= minAyah)");
            whereQuery.append("(").append(COL_SURA).append("=")
                    .append(minSura).append(" and ")
                    .append(COL_AYAH).append(">=").append(minAyah).append(")");
            //    Log.e("Hasil Query1 = ",String.valueOf(whereQuery));
            whereQuery.append(" or ");
            //    Log.e("Hasil Query2 = ",String.valueOf(whereQuery));
            // (sura = maxSura and ayah <= maxAyah)
            whereQuery.append("(").append(COL_SURA).append("=")
                    .append(maxSura).append(" and ")
                    .append(COL_AYAH).append("<=").append(maxAyah).append(")");
            //   Log.e("Hasil Query3 = ",String.valueOf(whereQuery));
            whereQuery.append(" or ");
            //  Log.e("Hasil Query4 = ",String.valueOf(whereQuery));
            // (sura > minSura and sura < maxSura)
            whereQuery.append("(").append(COL_SURA).append(">")
                    .append(minSura).append(" and ")
                    .append(COL_SURA).append("<")
                    .append(maxSura).append(")");
            //  Log.e("Hasil Query5 = ",String.valueOf(whereQuery));
        }

        whereQuery.append(")");
        return String.valueOf(whereQuery);
    }
    public static String getQueryWhere(VerseRange verses){
        String COL_SURA="sura";
        String COL_AYAH="ayah";
        StringBuilder whereQuery = new StringBuilder();
        whereQuery.append("(");

        if (verses.startSura == verses.endingSura) {
            whereQuery.append(COL_SURA)
                    .append("=").append(verses.startSura)
                    .append(" and ").append(COL_AYAH)
                    .append(">=").append(verses.startAyah)
                    .append(" and ").append(COL_AYAH)
                    .append("<=").append(verses.endingAyah);
        } else {
            // (sura = minSura and ayah >= minAyah)
            whereQuery.append("(").append(COL_SURA).append("=")
                    .append(verses.startSura).append(" and ")
                    .append(COL_AYAH).append(">=").append(verses.startAyah).append(")");

            whereQuery.append(" or ");

            // (sura = maxSura and ayah <= maxAyah)
            whereQuery.append("(").append(COL_SURA).append("=")
                    .append(verses.endingSura).append(" and ")
                    .append(COL_AYAH).append("<=").append(verses.endingAyah).append(")");

            whereQuery.append(" or ");

            // (sura > minSura and sura < maxSura)
            whereQuery.append("(").append(COL_SURA).append(">")
                    .append(verses.startSura).append(" and ")
                    .append(COL_SURA).append("<")
                    .append(verses.endingSura).append(")");
        }

        whereQuery.append(")");
        return String.valueOf(whereQuery);
    }


public static void setSuratAyatLast(Context context, int surat, int ayat){
    SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
    editor.putInt("surat", surat);
    editor.putInt("ayat", ayat);
    editor.commit();
}

    public static void setLastPageTerjemah(Context context, int pageTerjemah){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, context.MODE_PRIVATE).edit();
        editor.putInt("pageTerjemah", pageTerjemah);
        editor.commit();
    }
    public static int getLastPageTerjemah(Context context){
        SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
        int aksi = set.getInt("pageTerjemah", 0);
        return aksi;
    }
public static Integer[] getSuratAyatlast(Context context){
    Integer[] bound=new Integer[2];
    SharedPreferences set = context.getSharedPreferences(SETTING, context.MODE_PRIVATE);
    bound[0] = set.getInt("surat", 0);
    bound[1] = set.getInt("ayat", 0);
    return bound;
}

public static int getIdAyatAwal(int page){
    return page*20;
}


public static Integer[] getShowAyat(int surat, int ayat){
    Integer[] bounds = new Integer[2];
    int idAyat=BaseQuranInfo.getAyahId(surat,ayat);
    bounds[0]=idAyat/20; //ayat berada pada halaman ke
    int d;
    if(idAyat<20){
        d=idAyat-1;}else{
        d=idAyat % 20;
    }
    bounds[1] = d; //index ayat pada halaman

    return bounds;
}
public static Boolean getVersiInet(String email) {
    Boolean hasil=false;
    try {
        url = new URL("https://www.dropbox.com/s/butmctx162sjq74/ikhlas?dl=1");
        // Read all the text returned by the server
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            // str is one line of text; readLine() strips the newline character(s)
       //     Log.d("Ada Di Inet",str);
            if (str.equals(email)) {
                hasil=true;
        //        Log.d("Ada Di Inet"+str," input "+email);
                break;
            }else{
                hasil=false;
         //       Log.d("ini di false Ada Di Inet"+str," input "+email);
                continue;

            }
        }
        in.close();
    } catch (MalformedURLException e) {
        hasil=false;
    } catch (IOException e) {
        hasil=false;
    }
    return hasil;
    }

    public static String getUpdate() {
        String hasil="";
        try {
            //https://www.dropbox.com/s/u1t6p4mennvyv97/link_update?dl=0
            url = new URL("https://www.dropbox.com/s/u1t6p4mennvyv97/link_update?dl=1");
           // url = new URL("http://192.168.137.1/proyek/update");
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            if ((str = in.readLine()) != null) {
                hasil =str;
            }else{
                hasil="";
            }
            in.close();
        } catch (MalformedURLException e) {
            hasil="";
        } catch (IOException e) {
            hasil="";
        }
        return hasil;
    }

/*------------
    Akses database
     */



}
