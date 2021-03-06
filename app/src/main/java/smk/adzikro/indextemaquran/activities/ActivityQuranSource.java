package smk.adzikro.indextemaquran.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.adapter.QuranSourceAdapter;
import smk.adzikro.indextemaquran.db.QuranDataLocal;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.services.QuranDownloadService;
import smk.adzikro.indextemaquran.services.utils.DefaultDownloadReceiver;
import smk.adzikro.indextemaquran.services.utils.QuranDownloadNotifier;
import smk.adzikro.indextemaquran.services.utils.ServiceIntentHelper;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.util.Fungsi;
import timber.log.Timber;

import static smk.adzikro.indextemaquran.constans.Constants.TRANSLATION_DOWNLOAD_KEY;

/**
 * Created by server on 1/2/18.
 */

public class ActivityQuranSource extends AppCompatActivity
implements View.OnClickListener,
        DefaultDownloadReceiver.SimpleDownloadListener,
        QuranSourceAdapter.OnItemCheckListener{
    private static final String TAG = ActivityQuranSource.class.getSimpleName();
    public static final String KODE = "kode";
    private RecyclerView recyclerView;
    private QuranSourceAdapter adapter;
    private QuranSettings settings;
    private QuranSource downloadingItem;
    List<QuranSource> data=new ArrayList<>();
    private int kode=-1;
    private DefaultDownloadReceiver mDownloadReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_recycleview);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_source_quran);
        setSupportActionBar(toolbar);
        settings = QuranSettings.getInstance(this);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        if(savedInstanceState!=null) {
            kode = savedInstanceState.getInt(KODE);
            downloadingItem = savedInstanceState.getParcelable(TRANSLATION_DOWNLOAD_KEY);
        }else {
            if(getIntent().getExtras()!=null)
            kode = getIntent().getExtras().getInt(KODE);
            downloadingItem = null;
            mDownloadReceiver = null;
        }
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuranSourceAdapter(this, null, this,this);
        recyclerView.setAdapter(adapter);
        loadData();
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        if(downloadingItem!=null)
        bundle.putParcelable(TRANSLATION_DOWNLOAD_KEY, downloadingItem);
        bundle.putInt(KODE, kode);
        super.onSaveInstanceState(bundle);
    }

    private void loadData(){
        new Thread(() -> {
            getData();
            recyclerView.post(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private void getData(){
        SQLiteDatabase db = new QuranDataLocal(this).getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from quran order by ada desc";
        if(kode!=-1)
            sql = "select * from quran where type="+kode+" order by ada desc";


        cursor = db.rawQuery(sql,null);
        Log.e(TAG, "data quran "+cursor.getCount());
        data.clear();
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    QuranSource q= new QuranSource(cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(5),
                            cursor.getString(4));
                    q.setTranslator_asing(cursor.getString(3));
                    File file = new File(Fungsi.PATH_DATABASES(this)+File.separator+cursor.getString(5));
                    if(file.exists()){
                        db.execSQL("update quran set ada=1 where _id="+cursor.getInt(0));
                        Log.e(TAG, "file aya "+cursor.getString(5));
                    }
                  //  Log.e(TAG, "aya teu "+cursor.getString(5));
                    q.setAda(cursor.getInt(6));
                    q.setType(cursor.getInt(7));
                    q.setActive(cursor.getInt(8));
                    q.setId(cursor.getInt(0));
                    data.add(q);
                }
            }
            adapter.setData(data);
        }finally {
            cursor.close();
            db.close();
        }
    }

    @Override
    public void onClick(View view) {
        int pos = recyclerView.getChildAdapterPosition(view);
        File selectedItem = new File(Fungsi.PATH_DATABASES(this)+File.separator+data.get(pos).getFile_name());
        if(selectedItem.exists()){
            CheckBox checkBox = view.findViewById(R.id.aktif);
            if(checkBox.isChecked()){
                onItemUncheck(data.get(pos));
            }else{
                onItemCheck(data.get(pos));
            }
            checkBox.setChecked(!checkBox.isChecked());

        }else {
            downloadItem(data.get(pos));
        }
    }
    private void removeItem(final QuranSource translationRowData) {
        if (adapter == null) {
            return;
        }

        String msg = String.format(getString(R.string.remove_dlg_msg), translationRowData.getDisplayName());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove_dlg_title)
                .setMessage(msg)
                .setPositiveButton(R.string.remove_button,
                        (dialog, id) -> {
                           // Toast.makeText(this, translationRowData.getDisplayName()+" "+getString(R.string.download_successful),Toast.LENGTH_SHORT).show();
                            File selectedItem = new File(Fungsi.PATH_DATABASES(this)+translationRowData.getFile_name());
                            selectedItem.delete();
                            adapter.notifyDataSetChanged();
                        })
                .setNegativeButton(R.string.cancel,
                        (dialog, i) -> dialog.dismiss());
        builder.show();
    }
    @Override
    public void handleDownloadSuccess() {
        if(downloadingItem!=null)
        Toast.makeText(this, downloadingItem.getDisplayName()+" "+getString(R.string.download_successful),Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
        downloadingItem = null;
    }

    @Override
    public void handleDownloadFailure(int errId) {
        File selectedItem = new File(Fungsi.PATH_DATABASES(this)+downloadingItem.getFile_name());
        if (downloadingItem != null && selectedItem.exists()) {
            try {
                File f = new File(Fungsi.PATH_DATABASE(),
                        downloadingItem.getFile_name() + ".old");
                File destFile = new File(Fungsi.PATH_DATABASES(this), downloadingItem.getFile_name());
                if (f.exists() && !destFile.exists()) {
                    f.renameTo(destFile);
                } else {
                    f.delete();
                }
            } catch (Exception e) {
                Timber.d(e, "error restoring translation after failed download");
            }
        }
        downloadingItem = null;
    }

    private void downloadItem(QuranSource quranSource) {
        File selectedItem = new File(Fungsi.PATH_DATABASES(this)+quranSource.getFile_name());
        if (selectedItem.exists()) {
            return;
        }
        downloadingItem = quranSource;
        if (mDownloadReceiver == null) {
            mDownloadReceiver = new DefaultDownloadReceiver(this,
                    QuranDownloadService.DOWNLOAD_TYPE_TRANSLATION);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mDownloadReceiver, new IntentFilter(
                            QuranDownloadNotifier.ProgressIntent.INTENT_NAME));
        }
        mDownloadReceiver.setListener(this);

        // actually start the download
        String url = quranSource.getFile_url();
        if (quranSource.getFile_url() == null) {
            return;
        }
        String destination = Fungsi.PATH_DATABASES(this);
        Timber.d("downloading %s to %s", url, destination);

        if (selectedItem.exists()) {
            try {
                File f = new File(destination, quranSource.getFile_name());
                if (f.exists()) {
                    File newPath = new File(destination,
                            quranSource.getFile_name() + ".old");
                    if (newPath.exists()) {
                        newPath.delete();
                    }
                    f.renameTo(newPath);
                }
            } catch (Exception e) {
                Timber.d(e, "error backing database file up");
            }
        }

        // start the download
        String notificationTitle = quranSource.getDisplayName();
        Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
                destination, notificationTitle, TRANSLATION_DOWNLOAD_KEY,
                QuranDownloadService.DOWNLOAD_TYPE_TRANSLATION);
        String filename = quranSource.getFile_name();
        if (url.endsWith("zip")) {
            filename += ".zip";
        }
        intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME, filename);
        startService(intent);
    }

    @Override
    public void onItemCheck(QuranSource item) {
        SQLiteDatabase db = new QuranDataLocal(this).getWritableDatabase();
        db.execSQL("update quran set active=1 where _id="+item.getId());
        db.close();
        settings.setTafsir(true);
    }

    @Override
    public void onItemUncheck(QuranSource item) {
      //  Toast.makeText(ActivityQuranSource.this,"di unChecked",Toast.LENGTH_SHORT).show();
        SQLiteDatabase db = new QuranDataLocal(this).getWritableDatabase();
        db.execSQL("update quran set active=0 where _id="+item.getId());
        db.close();
        settings.setTafsir(true);
    }

    @Override
    public void onItemClick(QuranSource item) {
        File selectedItem = new File(Fungsi.PATH_DATABASE()+item.getFile_name());
        if(selectedItem.exists()){
            removeItem(item);
        }else{
            downloadItem(item);
        }
    }
}
