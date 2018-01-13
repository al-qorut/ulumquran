package smk.adzikro.indextemaquran.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
//import net.sqlcipher.database.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import smk.adzikro.indextemaquran.adapter.ListLafdziAdapter;
import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.db.CreateDb;
import smk.adzikro.indextemaquran.db.DatabaseHandler;
import smk.adzikro.indextemaquran.db.QuranDatabase;
import smk.adzikro.indextemaquran.object.VerseRange;
import smk.adzikro.indextemaquran.services.QuranDownloadService;
import smk.adzikro.indextemaquran.services.utils.ServiceIntentHelper;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.db.ExtractDatabase;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.util.QuranFileUtils;
import smk.adzikro.indextemaquran.widgets.GridAutofitLayoutManager;

/**
 * Created by server on 11/26/16.
 */

public class InstalasiActivity extends AppCompatActivity implements
        View.OnClickListener{

    ListView listView;
    ProgressBar progressBar;

  //  CustomListAdapter adapter;
    public String[] proses = new String[12];
    public String[] selesai = new String[12];
    public String[] aksi = new String[12];
    String TAG="InstalasiActivity";
    Button start, info;
    final private int WRITE_ESCDARD=1;
    int control;
    QuranSettings settings;
    ListLafdziAdapter adapter;
    RecyclerView recyclerView;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // handler.sendMessage(Message.obtain(handler, 3));
        /*setContentView(R.layout.quran_list);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,1));
        adapter = new ListLafdziAdapter(this, getData());
        if(Build.VERSION.SDK_INT>17)
        recyclerView.setLayoutDirection(ViewCompat.LAYOUT_DIRECTION_RTL);
        ViewTreeObserver viewTreeObserver = recyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateSize();
            }
        });
        recyclerView.setAdapter(adapter); */
       // Fungsi.setInstalasi(InstalasiActivity.this, true);
      //  handler.sendMessage(Message.obtain(handler, 3));
        settings = QuranSettings.getInstance(this);
        if(Fungsi.getInstalasi(this)){
            Log.d(TAG,"Instalasi sudah");
            startActivity(new Intent(InstalasiActivity.this,MainActivity.class));
            finish();
        }else {
            setContentView(R.layout.activity_instalasi);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "checkSelfPermission ");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.e(TAG, "show request permision ");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_ESCDARD);
                } else {

                    // No explanation needed, we can request the permission.
                    Log.e(TAG, "tidak butuh show request permision ");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_ESCDARD);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                Log.e(TAG, "cek self fermision false");
                if (!Fungsi.getInstalasi(this)) {
                    Fungsi.createPolder();
                   // copyDatabase();
                    createTableLatin();
                 //   donlot();
                }
            }
             start = findViewById(R.id.mulai);
             info = findViewById(R.id.infoInstall);
             info.setOnClickListener(this);
             start.setOnClickListener(this);
             progressBar = (ProgressBar) findViewById(R.id.progress);

        }
    }
    private static final int sColumnWidth = 120; // assume cell width of 120dp
    private void calculateSize() {
        int spanCount = (int) Math.floor(recyclerView.getWidth() / convertDPToPixels(sColumnWidth));
        ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanCount(spanCount);
    }

    private float convertDPToPixels(int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return dp * logicalDensity;
    }

    private ArrayList<HashMap<String, String>> getData(){
                ArrayList<HashMap<String, String>> hasil = new ArrayList<>();
                DatabaseHandler db=DatabaseHandler.getDatabaseHandler(InstalasiActivity.this, QuranFileConstants.ARABIC_DATABASE);
                VerseRange verseRange = new VerseRange(1,1,1,7);
                List<String> arab = db.getLafadzArabic(verseRange,DatabaseHandler.TextType.ARABIC);
                db=DatabaseHandler.getDatabaseHandler(InstalasiActivity.this, QuranFileConstants.LATIN_DATABASE);
                List<String> arti = db.getLafadzArabic(verseRange,DatabaseHandler.TextType.TRANSLATION);
                for (int i=0; i<arab.size();i++){
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("arab",arab.get(i));
                    hashMap.put("arti",arti.get(i));
                    hasil.add(hashMap);
                }
                return hasil;
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    progressBar.setVisibility(View.GONE);
                    start.setVisibility(View.VISIBLE);
                    Fungsi.setInstalasi(InstalasiActivity.this, true);
                    break;
                case 1:
                    start.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    selesai[control]="1";
                    aksi[control]="1";
                    proses[control]="0";
                    progressBar.setVisibility(View.GONE);
                    //TampilList();
                    control++;
                    if(control==3){
                        copyTema();
                    }else if(control==9) {
                        handler.sendMessage(Message.obtain(handler, 3));
                    }else{
                        //copyData(control);
                    }
                    break;
                case 3:
                    startActivity(new Intent(InstalasiActivity.this,MainActivity.class));
                    Fungsi.setInstalasi(InstalasiActivity.this,true);
                    finish();
                    break;
                case 4:
                   // createDb();
                   // finish();
                    startActivity(new Intent(InstalasiActivity.this,MainActivity.class));
                    Fungsi.setInstalasi(InstalasiActivity.this,true);
                    finish();
                    break;
                case 5:
                    finish();
                    break;
        }
            super.handleMessage(msg);
        }
    };
    private void donwloadImage(){
        if(!Fungsi.isFileImageExist()) {
            settings.setDowloadImage(true);
            new DownloadZipfromInternet().execute("https://www.dropbox.com/s/5xgul9c98bcgzv3/quran.zip?dl=1");
        }
    }


    private void donlot(){
        Toast.makeText(this,"Hayu donlot",Toast.LENGTH_SHORT).show();
        String notificationTitle = "Notif donlot";
        String url = QuranFileUtils.getArabicSearchDatabaseUrl();
        String destination = Fungsi.PATH_DATABASE();
        Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
                destination, notificationTitle, Constants.TRANSLATION_DOWNLOAD_KEY,
                QuranDownloadService.DOWNLOAD_TYPE_TRANSLATION);
        String filename = QuranFileConstants.ARABIC_DATABASE;
        if (url.endsWith("zip")) {
            filename += ".zip";
        }
        intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME, filename);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_ESCDARD: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!Fungsi.getInstalasi(this)) {
                        Fungsi.createPolder();
                       // copyDatabase();
                        createTableLatin();
                        donlot();
                        Fungsi.setInstalasi(this,true);
                    }
                } else {
                    Log.e(TAG,"ditolak :(");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //   setPertama(true);
                 //   this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void insertTema(SQLiteDatabase db,String _id, String parent, String tema) {
        ContentValues qr = new ContentValues();
        qr.put(QuranDatabase.FIELD_ID_, _id);
        qr.put(QuranDatabase.FIELD_PARENT, parent);
        qr.put(QuranDatabase.FIELD_TEMA, tema);
        db.insert("TEMA", null, qr);
    }
    public void insertAyatTema(SQLiteDatabase db, String id, String surat, String ayat) {
        ContentValues qr = new ContentValues();
        qr.put(QuranDatabase.FIELD_ID, id);
        qr.put(QuranDatabase.FIELD_SURAT, surat);
        qr.put(QuranDatabase.FIELD_AYAT, ayat);
        db.insert(QuranDatabase.TABLE_AYAT_TEMA, null, qr);
    }

    private void createTableLatin(){
       new Thread(new Runnable() {
           @Override
           public void run() {
       Resources res = getResources();
       InputStream inputStream = null;
       BufferedReader buff = null;
       SQLiteDatabase db = new CreateDb(InstalasiActivity.this, QuranFileConstants.LATIN_DATABASE).getWritableDatabase();
       String sql = "INSERT INTO "+CreateDb.TABLE_NAME+"("+
               CreateDb.FIELD_SURAT+","+CreateDb.FIELD_AYAT+","+CreateDb.FIELD_TEXT+") VALUES(?,?,?)";
       db.beginTransaction();
       SQLiteStatement query = db.compileStatement(sql);
       try{
           for(int surat=1; surat<=114; surat++) {
               inputStream = res.getAssets().open("latin/f"+surat+".dat");
               buff = new BufferedReader(new InputStreamReader(inputStream));
               String line;
               int ayat=1;
               while ((line = buff.readLine())!=null){
                   query.bindLong(1, surat);
                   query.bindLong(2, ayat);
                   query.bindString(3, line);
                   query.execute();
                   query.clearBindings();
                   ayat++;
               }

           }
           db.setTransactionSuccessful();
           db.endTransaction();
           db.close();
           handler.sendMessage(Message.obtain(handler,4));
       }catch (IOException e){

       }
           }
       }).start();
    }

    private void copyTema() {
        progressBar.setMax(28378);
        proses[3]="1";
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //SQLiteDatabase database = QuranDatabase.open(InstalasiActivity.this);
                android.database.sqlite.SQLiteDatabase database = QuranDatabase.open(InstalasiActivity.this);
                android.database.sqlite.SQLiteDatabase dataasal = new ExtractDatabase(InstalasiActivity.this, Fungsi.PATH_DATABASE() + "indexquran3").getReadableDatabase();
                final Resources resources2 = getResources();
                InputStream inputStream3 = resources2.openRawResource(R.raw.tema);
                InputStream inputStream4 = resources2.openRawResource(R.raw.ayat_tema);
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputStream3));
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream4));
                database.beginTransaction();
                try {
                    int lineNumber = 1;
                    String line;
                    //data Tema
                    while ((line = reader1.readLine()) != null) {
                        lineNumber++;
                        final int counter=lineNumber;
                        String[] kata = TextUtils.split(line, ";");
                        if (kata.length < 3) continue;
                        insertTema(database,kata[0].trim(), kata[1].trim(), kata[2].trim());
                        Log.e(TAG,"Input tema "+kata[0].trim()+kata[1].trim()+kata[2].trim());
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                               progressBar.setProgress(counter);
                            }
                        });
                    }
                    while ((line = reader2.readLine()) != null) {
                        lineNumber++;
                        final int counter=lineNumber;
                        String[] kata = TextUtils.split(line, ";");
                        if (kata.length < 3) continue;
                        insertAyatTema(database,kata[0].trim(), kata[1].trim(), kata[2].trim());
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(counter);
                            }
                        });
                    }
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    dataasal.close();
                    database.close();
                    handler.sendMessage(Message.obtain(handler, 2));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }





    private void copyDatabase(){
        new Thread(new Runnable() {
            public void run() {
                int count;
                try {
                        URL url = new URL(QuranFileConstants.DATABASE_BASE_URL+QuranFileConstants.ARABIC_DATABASE);
                        URLConnection conection = url.openConnection();
                        conection.connect();
                        InputStream input = new BufferedInputStream(url.openStream(),10*1024);
                        OutputStream output;
                        output = new FileOutputStream(Fungsi.PATH_DATABASE()+QuranFileConstants.ARABIC_DATABASE);
                        byte data[] = new byte[1024];
                        while ((count = input.read(data)) != -1) {
                            output.write(data, 0, count);
                        }
                        output.flush();
                        output.close();
                        input.close();
                    handler.sendMessage(Message.obtain(handler, 0)); // extract dari asset selesai
                    Log.e(TAG,"Extract dari asset selesai");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }






    @Override
    public void onClick(View view) {
      //  control =0;
        if(view.getId()==R.id.mulai){
            startActivity(new Intent(this, MainActivity.class));//copyData(control);
        }else if(view.getId()==R.id.infoInstall)Fungsi.setInstalasi(this,true);//donlot();
      //  start.setVisibility(View.GONE);

    }

    class CustomListAdapter extends ArrayAdapter<HashMap<String, String>> {
        Context context;
        int textViewResourceId;
        ArrayList<HashMap<String, String>> alist;

        public CustomListAdapter(Context context, int textViewResourceId,
                                 ArrayList<HashMap<String, String>> alist) {
            super(context, textViewResourceId);
            this.context = context;
            this.alist = alist;
            this.textViewResourceId = textViewResourceId;

        }

        public int getCount() {

            return alist.size();
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context)
                    .getLayoutInflater();
            convertView = inflater.inflate(R.layout.detail_instalasi,
                    parent, false);
            holder = new Holder();
            holder.pilihan = (CheckBox) convertView
                    .findViewById(R.id.pilihan);
            holder.judul = (TextView) convertView
                    .findViewById(R.id.judul);
            holder.detail = (TextView) convertView
                    .findViewById(R.id.info);
            holder.ok = (ImageView) convertView
                    .findViewById(R.id.image);
            holder.progressBar =(ProgressBar) convertView
                    .findViewById(R.id.loading);
            holder.lin_background = (LinearLayout) convertView
                    .findViewById(R.id.linear);
            convertView.setTag(holder);


            holder = (Holder) convertView.getTag();

            holder.judul.setText(alist.get(pos).get("judul"));
            holder.detail.setText(alist.get(pos).get("detail"));
            if(alist.get(pos).get("aksi").equals("1")){
                holder.pilihan.setChecked(true);
            }else{
                holder.pilihan.setChecked(false);
            }
            if (alist.get(pos).get("proses").equals("1")) {
                holder.ok.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);

            } else{
               // holder.ok.setImageResource(R.drawable.ic_accept);
                if (alist.get(pos).get("ok").equals("0")) {
                    holder.ok.setImageResource(R.drawable.ic_download);
                }else {
                    holder.ok.setImageResource(R.drawable.ic_accept);
                }
            }

            return convertView;

        }

        class Holder {
            CheckBox pilihan;
            ProgressBar progressBar;
            TextView judul, detail;
            ImageView ok;
            LinearLayout lin_background;
        }
    }

    class DownloadZipfromInternet extends AsyncTask<String, String, String> {
       // private ProgressDialog prgDialog=null;
        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        // Download Music File from Internet
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // Get Music file length
                int lenghtOfFile = conection.getContentLength();
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(),10*1024);
                // Output stream to write file in SD card
                OutputStream output;
                output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/.adzikro/indexQuran/images/quran.zip");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Publish the progress which triggers onProgressUpdate method
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // Write data to file
                    output.write(data, 0, count);
                }
                // Flush output
                output.flush();
                // Close streams
                output.close();
                input.close();
                unpackZip(Fungsi.PATH_IMAGES(), "quran.zip");
            } catch (Exception e) {
                settings.setDowloadImage(false);
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        // While Downloading Music File
        protected void onProgressUpdate(String... progress) {
        }

        // Once Music File is downloaded
        @Override
        protected void onPostExecute(String file_url) {
            //unzip
            settings.setDowloadImage(false);
        }
    }

    private boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
            handler.sendMessage(Message.obtain(handler, 4));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            settings.setDowloadImage(false);
            return false;
        }

        return true;
    }
    public void insertInto(SQLiteDatabase db, String text, int ayat,int surat) {
        ContentValues qr = new ContentValues();
        qr.put(CreateDb.FIELD_SURAT, surat);
        qr.put(CreateDb.FIELD_AYAT, ayat);
        qr.put(CreateDb.FIELD_TEXT, text);
        db.insertWithOnConflict(CreateDb.TABLE_NAME,null,qr,SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void createDb(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase dbAsal = QuranDatabase.open(InstalasiActivity.this);
                //ibnu Katsir
                SQLiteDatabase db = new CreateDb(InstalasiActivity.this, "quran.id.ibnukatir.db").getWritableDatabase();
                Cursor cursor = dbAsal.rawQuery("select * from  tafsir_ibnu_katsir", null);

                db.beginTransaction();
                  if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                     insertInto(db,cursor.getString(3),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();

                // irab
                db = new CreateDb(InstalasiActivity.this, "quran.ar.irab.db").getWritableDatabase();
                cursor = dbAsal.rawQuery("select * from  nahwusharf", null);
                db.beginTransaction();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        insertInto(db,cursor.getString(3),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();

                // sharaf
                db = new CreateDb(InstalasiActivity.this, "quran.ar.sharf.db").getWritableDatabase();
                cursor = dbAsal.rawQuery("select * from  nahwusharf", null);
                db.beginTransaction();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        insertInto(db,cursor.getString(4),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();

                // balagha
                db = new CreateDb(InstalasiActivity.this, "quran.ar.balagha.db").getWritableDatabase();
                cursor = dbAsal.rawQuery("select * from  nahwusharf", null);
                db.beginTransaction();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        insertInto(db,cursor.getString(5),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
                handler.sendMessage(Message.obtain(handler,5));
            }
        }).start();
    }


}
