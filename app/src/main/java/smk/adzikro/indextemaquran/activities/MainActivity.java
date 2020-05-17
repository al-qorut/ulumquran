package smk.adzikro.indextemaquran.activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.SearchActivity;
import smk.adzikro.indextemaquran.constans.BaseQuranData;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.fragments.BookFragment;
import smk.adzikro.indextemaquran.fragments.SuraListFragment;
import smk.adzikro.indextemaquran.fragments.TemaFragment;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.services.QuranDownloadService;
import smk.adzikro.indextemaquran.services.utils.DefaultDownloadReceiver;
import smk.adzikro.indextemaquran.services.utils.QuranDownloadNotifier;
import smk.adzikro.indextemaquran.services.utils.ServiceIntentHelper;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.setting.RateMeMaybe;
import smk.adzikro.indextemaquran.ui.QuranUtils;
import smk.adzikro.indextemaquran.util.Fungsi;
import timber.log.Timber;

import static smk.adzikro.indextemaquran.constans.Constants.TRANSLATION_DOWNLOAD_KEY;
/*
 git init
 git remote add origin https://github.com/al-qorut/ulumquran.git
 git pull origin master --allow-unrelated-histories
 git add .
 git commit -m “pesan”
 git push origin master
*/

public class MainActivity extends AppCompatActivity implements
        RateMeMaybe.OnRMMUserChoiceListener,
        DefaultDownloadReceiver.SimpleDownloadListener{
    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    ActionBar actionBar;
    QuranSettings mSettings;

    //===untuk free ada iklan
    private static final long APP_LENGTH_MILLISECONDS = 50000;
//    private InterstitialAd mInterstitialAd;
    private CountDownTimer mCountDownTimer;
    private long mTimerMilliseconds;
    private boolean pertama=true;

    //===untuk free ada iklan
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        action();
        if(savedInstanceState!=null){

        }else{
            SuraListFragment suraListFragment = new SuraListFragment();
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.pager,suraListFragment)
                    .addToBackStack(null).commit();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("f", "ayaan");
    }
    public void init(){

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView =findViewById(R.id.navigation_view);
        bottomNavigationView = findViewById(R.id.bottom);
        mSettings = QuranSettings.getInstance(this);

    }


    public void action(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                //   Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                if (menuItem.getTitle().equals("Index Tema Quran")) {
                //    startActivity(new Intent(MainActivity.this, TemaActivity.class));
                } else if ((menuItem.getTitle().equals("Keluar"))) {
                        MainActivity.this.finish();
                }else if ((menuItem.getTitle().equals("Jump to Ayat.."))) {
                   gotoAyat();
                } else if ((menuItem.getTitle().equals("Registrasi"))) {
                //    startActivity(new Intent(MainActivity.this, RegistrasiActivity.class));
                } else if ((menuItem.getTitle().equals("FAQ"))) {
                //    startActivity(new Intent(MainActivity.this, FAQActivity.class));
                } else if ((menuItem.getTitle().equals("Settings"))) {
                    startActivity(new Intent(MainActivity.this, settings.class));
                } else if ((menuItem.getTitle().equals("Share.."))) {
                    Fungsi.share(MainActivity.this, "Aplikasi Quran, Tafsir, Terjemah Perkata, I'rab, Sharaf  \n \nhttps://play.google.com/store/apps/details?id=smk.adzikro.indextemaquran \n");
                } else if ((menuItem.getTitle().equals("Rate.."))) {
                    rate();
                } else if ((menuItem.getTitle().equals("About"))) {
                    startActivity(new Intent(MainActivity.this, ActivityIAP.class));
                }else if ((menuItem.getTitle().equals("Update Data"))) {
                  //  new cekDataUpdate().execute();
                    startActivity(new Intent(MainActivity.this, AdsActivity.class));
                }
                return true;
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.nav_quran:
                        SuraListFragment suraListFragment = new SuraListFragment();
                        getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .replace(R.id.pager,suraListFragment)
                                .addToBackStack(null).commit();
                        break;
                    case R.id.nav_tema:
                        TemaFragment temaFragment = new TemaFragment();
                        getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .replace(R.id.pager,temaFragment)
                                .addToBackStack(null).commit();
                        break;
                    case R.id.nav_book:
                        BookFragment bookFragment = new BookFragment();
                        getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .replace(R.id.pager,bookFragment)
                                .addToBackStack(null).commit();
                        break;
                }
                return true;
            }
        });
    }
    @Override
    public void onBackPressed(){
       // startActivity(new Intent(MainActivity.this, AdsActivity.class));
        finish();
        //super.onBackPressed();
    }
    String TAG="Main ACtivity";
    private int pagex;
    public void jumpTo(int page, int ak) {
        pagex = page;
        final Intent intent = new Intent(MainActivity.this, UlumQuranActivity.class);
        intent.putExtra("page", page);
        if(!Fungsi.isFileImageExist(this) && Fungsi.getModeView(this)){
            Toast.makeText(this, "Image tidak ada", Toast.LENGTH_SHORT).show();
            downloadImage();
        }else{
            startActivity(intent);
        }
    }

    private void rate() {
        RateMeMaybe.resetData(MainActivity.this);
        RateMeMaybe rmm = new RateMeMaybe(MainActivity.this);
        rmm.setPromptMinimums(0, 0, 0, 0);
        rmm.setRunWithoutPlayStore(true);
        rmm.setAdditionalListener(MainActivity.this);
        rmm.setDialogMessage("Kalau suka dan bermanfaat, "
                //    +"telah menjalankan %totalLaunchCount% kali! "
                + "silahkan rate aplikasinya");
        rmm.setDialogTitle("Rate..");
        rmm.setPositiveBtn("OK!");
        rmm.run();
    }

    public void gotoAyat(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_goto);
        dialog.setCancelable(true);
        final Spinner surat = (Spinner) dialog.findViewById(R.id.surat);
        final Spinner ayat = (Spinner) dialog.findViewById(R.id.ayat);
        Button batal = (Button)dialog.findViewById(R.id.batal);
        batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button ok = (Button)dialog.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int suratna = surat.getSelectedItemPosition()+1;
                int ayatna = ayat.getSelectedItemPosition()+1;
                int page = BaseQuranInfo.getPageFromSuraAyah(suratna,ayatna);
                jumpTo(page,0);
                dialog.dismiss();
            }
        });
        surat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int ix, long l) {
                int jAyat;
                String[] listAyat;
                jAyat = BaseQuranData.SURA_NUM_AYAHS[ix];
                int i;
                listAyat = new String[jAyat];
                for (i = 0; i < jAyat; i++) {
                    listAyat[i] = String.valueOf(i + 1);
                }
                ArrayAdapter<String> list_ayat = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, listAyat);
                list_ayat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ayat.setAdapter(list_ayat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        dialog.show();
    }
    SearchView searchView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_cari);
        searchView = (SearchView) item.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(
                     // new  ComponentName(this, SearchActivity.class)
                        getComponentName()
                )
        );

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_cari:
                onSearchRequested();
               // Toast.makeText(this, "Hallow",Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handlePositive() {

    }

    @Override
    public void handleNeutral() {

    }

    @Override
    public void handleNegative() {

    }




    @Override
    public void onDestroy(){

        super.onDestroy();
    }
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    DefaultDownloadReceiver mDownloadReceiver;

    private void downloadImage() {
        if (mDownloadReceiver == null) {
            mDownloadReceiver = new DefaultDownloadReceiver(this,
                    QuranDownloadService.DOWNLOAD_TYPE_PAGES);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mDownloadReceiver, new IntentFilter(
                            QuranDownloadNotifier.ProgressIntent.INTENT_NAME));
        }
        mDownloadReceiver.setListener(this);

        // actually start the download
        String url = "https://www.dropbox.com/s/5xgul9c98bcgzv3/quran.zip?dl=1"; //
        String destination = Fungsi.PATH_IMAGES(this);
        Timber.d("downloading %s to %s", url, destination);
        String notificationTitle = "Image Quran";
        Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
                destination,
                notificationTitle,
                TRANSLATION_DOWNLOAD_KEY,
                QuranDownloadService.DOWNLOAD_TYPE_PAGES);
        String filename = "quran.zip";
        if (url.endsWith("zip")) {
            filename += ".zip";
        }
        intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME, filename);
        startService(intent);
    }
    //===Download=======//
    @Override
    public void handleDownloadSuccess() {
        Toast.makeText(this, "Sucses", Toast.LENGTH_SHORT).show();
        unpackZip(Fungsi.PATH_IMAGES(this),"quran.zip");
    }

    @Override
    public void handleDownloadFailure(int errId) {
        Toast.makeText(this, "gagal", Toast.LENGTH_SHORT).show();
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
            jumpTo(pagex,0);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
