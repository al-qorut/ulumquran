package smk.adzikro.indextemaquran.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
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
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.adapter.TemaListAdapter;
import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.db.QuranDataLocal;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.services.QuranDownloadService;


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
        // that if we update quran.ar one day to fix this issue and older clients get a new copy of
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
    public static void showMessage(Context context, String judul, String isi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(judul);
        builder.setMessage(isi)
                .setCancelable(false)
                .setPositiveButton("Ok ", (dialog, id) -> dialog.cancel()).show();

    }
    public static void ShowMessage(Context context, List<Ayah> data, String title) {
        Activity activity = (Activity) context;
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_ayat_tema);
        dialog.setTitle(title);
        RecyclerView recyclerView = dialog.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new TemaListAdapter(context, data));
        ImageView button = dialog.findViewById(R.id.exit);
        String text = ShareUtil.getShareTextFromAyah(context, data);
        button.setOnClickListener(view -> dialog.dismiss());
        ImageView copy = dialog.findViewById(R.id.copy);
        copy.setOnClickListener(view ->
                ShareUtil.copyToClipboard(activity, text));
        ImageView share = dialog.findViewById(R.id.share);
        share.setOnClickListener(view ->
                ShareUtil.shareViaIntent(activity, text, R.string.app_name));

        dialog.show();
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

  public static Typeface getHurufArab(Context context){
      final Typeface face;
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      final String  value = sharedPreferences.getString("huruf", null);
      if (value == null) {
          face = Typeface.createFromAsset(context.getAssets(), "font/qalam.ttf");
      } else {
          face = Typeface.createFromAsset(context.getAssets(), "font/" + value);
      }
      return face;
  }

  public static Boolean getModeView(Context context){
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      Boolean mode_terjemah = sharedPreferences.getBoolean("mode_view", false);
      return mode_terjemah;
  }
    public static void setModeView(Context context, boolean mode){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean("mode_view", mode).apply();
    }


  public static final String AR_BASMALLAH = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ";



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
