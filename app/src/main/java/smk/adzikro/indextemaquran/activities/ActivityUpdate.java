package smk.adzikro.indextemaquran.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import smk.adzikro.indextemaquran.db.ExtractDatabase;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.db.QuranDatabase;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.R;

/**
 * Created by server on 12/26/16.
 */

public class ActivityUpdate extends AppCompatActivity implements
        View.OnClickListener{
    ProgressBar loading;
    TextView info;
    Button cek;
    String update="", url_, versi_update;
    QuranSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        loading = (ProgressBar)findViewById(R.id.loading);
        info =(TextView)findViewById(R.id.infoupdate);
        cek =(Button)findViewById(R.id.cekupdate);
        cek.setOnClickListener(this);
        settings = QuranSettings.getInstance(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.cekupdate){
            if(cek.getText().toString().equals("Selesai")){
                finish();
            }
            loading.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    update = Fungsi.getUpdate();
                    loading.post(new Runnable() {
                        @Override
                        public void run() {
                            if(update.equals("")||update.equals("belum")){
                                info.setText("Update belum ada");
                                loading.setVisibility(View.GONE);
                            }else{
                                String[] kata = TextUtils.split(update, ";");
                                url_ =kata[0];
                                String infox = kata[2];
                                versi_update = kata[1];
                                if(settings.isUpdate(versi_update)){
                                    Toast.makeText(ActivityUpdate.this,"Data yang dimiliki terbaru",Toast.LENGTH_SHORT).show();
                                    info.setText("Data sudah terupdate");
                                }else {
                                    messageBox("Update", "Ada update " + infox + "\nlanjutkan ?");
                                }
                            }
                        }
                    });
                }
            }).start();
        }
    }

    private void messageBox(String judul, String isi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(judul);
        builder.setMessage(isi)
                .setCancelable(false)
                .setPositiveButton("Ok ", new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                               // getProsesUpdate();
                                cek.setVisibility(View.GONE);
                                new DownloadData().execute(url_);
                            }
                        })
                .setNegativeButton("Tidak", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int arg1) {
                                //countDownTimer.cancel();
                                loading.setVisibility(View.GONE);
                                dialog.cancel();
                            }
                        }).show();
    }


    class DownloadData extends AsyncTask<String, String, String> {
        ProgressDialog prgDialog;
        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Shows Progress Bar Dialog and then call doInBackground method
            prgDialog = new ProgressDialog(ActivityUpdate.this);
            prgDialog.setMessage("Downloading update data. Please wait...");
            prgDialog.setIndeterminate(false);
            prgDialog.setMax(100);
            prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            prgDialog.setCancelable(false);
            prgDialog.show();
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
                //   output = new FileOutputStream(path + "/indexQuran/database/data.zip");
                output = new FileOutputStream(Fungsi.PATH_DATABASE()+"update.zip");
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
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        // While Downloading Music File
        protected void onProgressUpdate(String... progress) {
            // Set progress percentage
            prgDialog.setProgress(Integer.parseInt(progress[0]));
        }

        // Once Music File is downloaded
        @Override
        protected void onPostExecute(String file_url) {
            unpackZip(Fungsi.PATH_DATABASE(),"update.zip");
            prgDialog.dismiss();
        }
    }
    private String getQuery(int index){
            String query="";
            switch (index){
                case 0:
                    query = "select * from lafdzi";//1
                    break;
                case 1:
                    query = "select * from tafsir_adzikro";//2
                    break;
                case 2:
                    query = "select * from ibnu_katsir";//6
                    break;
                case 3:
                    query = "select * from ulum";//8
                    break;
            }
            return query;
    }
    public void addLafdzi(SQLiteDatabase db,String arti, int ayat,int surat, int id, String arab) {
        ContentValues qr = new ContentValues();
        qr.put(QuranDatabase.FIELD_SURAT, surat);
        qr.put(QuranDatabase.FIELD_AYAT, ayat);
        qr.put(QuranDatabase.FIELD_ARTI, arti);
        qr.put("_id", id);
        qr.put("KATAARAB", arab);
        db.insert("lafdzi",null,qr);

    }
    public void addAdzikro(SQLiteDatabase db,String tafsir, int ayat,int surat) {
        ContentValues qr = new ContentValues();
        qr.put(QuranDatabase.FIELD_SURAT, surat);
        qr.put(QuranDatabase.FIELD_AYAT, ayat);
        qr.put(QuranDatabase.FIELD_TAFSIR, tafsir);
        db.insert("tafsir_adzikro",null,qr);
    }
    public void addIbnu(SQLiteDatabase db,String tafsir, int ayat,int surat) {
        ContentValues qr = new ContentValues();
        qr.put(QuranDatabase.FIELD_SURAT, surat);
        qr.put(QuranDatabase.FIELD_AYAT, ayat);
        qr.put(QuranDatabase.FIELD_TAFSIR, tafsir);
        db.insert("tafsir_ibnu_katsir",null,qr);
    }
    public void addUlum(SQLiteDatabase db,String isi, int _id,String judul) {
        ContentValues qr = new ContentValues();
        qr.put("_id", _id);
        qr.put("judul", judul);
        qr.put("isi", isi);
        db.insert("ulum",null,qr);
    }

    private void hapusTable(SQLiteDatabase db, int index){
            switch (index){
                case 0:
                    db.execSQL("drop table lafdzi");
                    db.execSQL("CREATE TABLE IF NOT EXISTS lafdzi (_id int, SURAT int, AYAT int, KATAARAB text, ARTI text)");
                    break;
                case 1:
                    db.execSQL("drop table tafsir_adzikro");
                    db.execSQL("CREATE TABLE IF NOT EXISTS tafsir_adzikro (_id int, SURAT int, AYAT int, TAFSIR text)");
                    break;
                case 2:
                    db.execSQL("drop table tafsir_ibnu_katsir");
                    db.execSQL("CREATE TABLE IF NOT EXISTS tafsir_ibnu_katsir (_id int, SURAT int, AYAT int, TAFSIR text)");
                    break;
                case 3:
                    db.execSQL("drop table ulum");
                    db.execSQL("CREATE TABLE IF NOT EXISTS ulum (_id int, judul text, isi text)");
                    break;
            }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    copyData(0);
                    info.setText("Update data terjemah perkata");
                    break;
                case 1:
                    copyData(1);
                    info.setText("Update data Tafsir Adzikro");
                    break;
                case 2:
                    copyData(2);
                    info.setText("Update data Tafsir Ibnu Katsir");
                    break;
                case 3:
                    copyData(3);
                    info.setText("Update data Teori Ulum Qur'an");
                    break;

                case 4:
                    info.setText("Update Selesai..");
                    cek.setText("Selesai");
                    settings.setUpdate(true,versi_update);
                    File file = new File(Fungsi.PATH_DATABASE()+"update");
                    if(file.exists())file.delete();
                    cek.setVisibility(View.VISIBLE);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void copyData(final int index) {
        loading.setVisibility(View.VISIBLE);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = QuranDatabase.open(ActivityUpdate.this);
                android.database.sqlite.SQLiteDatabase dataasal = new ExtractDatabase(ActivityUpdate.this, Fungsi.PATH_DATABASE() + "update").getReadableDatabase();
                hapusTable(database,index);
                Cursor cursor = dataasal.rawQuery(getQuery(index), null);
                database.beginTransaction();
                while (cursor.moveToNext()) {
                    final int value = cursor.getPosition();
                    switch (index) {
                        case 0:
                            addLafdzi(database, cursor.getString(cursor.getColumnIndex("arti")), cursor.getInt(cursor.getColumnIndex("ayat")), cursor.getInt(cursor.getColumnIndex("surat")), value,cursor.getString(cursor.getColumnIndex("arab")));
                            break;
                        case 1:
                            addAdzikro(database, cursor.getString(cursor.getColumnIndex("tafsir")), cursor.getInt(cursor.getColumnIndex("ayat")), cursor.getInt(cursor.getColumnIndex("surat")));
                            break;
                        case 2:
                            addIbnu(database, cursor.getString(cursor.getColumnIndex("tafsir")), cursor.getInt(cursor.getColumnIndex("ayat")), cursor.getInt(cursor.getColumnIndex("surat")));
                            break;
                        case 3:
                            addUlum(database, cursor.getString(cursor.getColumnIndex("isi")), cursor.getInt(cursor.getColumnIndex("_id")), cursor.getString(cursor.getColumnIndex("judul")));
                            break;
                    }
                    //aksi
                    loading.post(new Runnable() {
                        @Override
                        public void run() {
                            loading.setProgress(value);
                        }
                    });
                }
                cursor.close();
                database.setTransactionSuccessful();
                database.endTransaction();
                database.close();
                dataasal.close();
                handler.sendMessage(Message.obtain(handler, index+1));
            }
        };
        new Thread(runnable).start();
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
            File file = new File(path+zipname);
            if(file.exists())file.delete();

            handler.sendMessage(Message.obtain(handler, 0));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
