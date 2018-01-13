package smk.adzikro.indextemaquran.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranData;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.fragments.SuraListFragment;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.setting.RateMeMaybe;
import smk.adzikro.indextemaquran.util.Fungsi;


public class MainActivity extends AppCompatActivity implements
        RateMeMaybe.OnRMMUserChoiceListener{
    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBar actionBar;
    QuranSettings mSettings;

    //===untuk free ada iklan
    private static final long APP_LENGTH_MILLISECONDS = 50000;
    private InterstitialAd mInterstitialAd;
    private CountDownTimer mCountDownTimer;
    private long mTimerMilliseconds;
    private boolean pertama=true;

    //===untuk free ada iklan
    DownloadImage downloadImage=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        action();
    }

    public void init(){
       // iklan();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView =findViewById(R.id.navigation_view);
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
                    startActivity(new Intent(MainActivity.this, ActivityUpdate.class));
                }
                return true;
            }
        });
        SuraListFragment suraListFragment = new SuraListFragment();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.pager,suraListFragment)
                .addToBackStack(null).commit();
    }

    String TAG="Main ACtivity";
    public void jumpTo(int page, int ak) {
        final Intent intent = new Intent(MainActivity.this, UlumQuranActivity.class);
        intent.putExtra("page", page);
        intent.putExtra("aksi", ak);
        if(ak<0){
            if(mSettings.isModeImage()){
                if(!Fungsi.isFileImageExist()){
                    //image tidak ada tanya mau donlot tidak?
                    if(mSettings.isDownloadImage()){
                        // cek apakah donwload sedang proses
                        if(downloadImage==null){
                            downloadImage = new DownloadImage();
                            downloadImage.execute("https://www.dropbox.com/s/5xgul9c98bcgzv3/quran.zip?dl=1");
                        }else {
                            Toast.makeText(this, "Image Quran sedang proses download", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    new AlertDialog.Builder(this)
                            .setTitle("Image Quran Tidak ada")
                            .setMessage("Mau Download atau tampil mode Text?")
                            .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    downloadImage = new DownloadImage();
                                    downloadImage.execute("https://www.dropbox.com/s/5xgul9c98bcgzv3/quran.zip?dl=1");
                                }
                            })
                            .setNegativeButton("Mode Text", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mSettings.setModeImage(false);
                                    startActivity(intent);
                                }
                            }).show();
                }else {
                    startActivity(intent);
                }
            }else{
                startActivity(intent);
            }
        }else {
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
    public String[] getListAksi(){
        String[] mTranslationItems;
        List<String> temp = new ArrayList<>();
        temp.add("Terjemah Indonesia");
        temp.add("Terjemah English");
        temp.add("Terjemah Lafdziyah");
        temp.add("Tafsir Adz-Dzikro");
        temp.add("Tafsir Jalalain");
        if(mSettings.isTafsirIbnuKatsir())
            temp.add("Tafsir Ibnu Katsir");
        if(mSettings.isTafsirIrab())
            temp.add("I'rab Al Qur'an");
        if(mSettings.isTafsirSharf())
            temp.add("Sharf Al Qur'an");
        if(mSettings.isTafsirBalagha())
            temp.add("Balaghah Al Qur'an");
        mTranslationItems = new String[temp.size()];
        for(int i=0;i<temp.size();i++) {
            mTranslationItems[i] = temp.get(i);
        }
        return mTranslationItems;
    }
    public void gotoAyat(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_goto);
        dialog.setCancelable(true);
        final Spinner surat = (Spinner) dialog.findViewById(R.id.surat);
        final Spinner ayat = (Spinner) dialog.findViewById(R.id.ayat);
        final Spinner aksi = (Spinner) dialog.findViewById(R.id.aksi);
        aksi.setVisibility(View.VISIBLE);
        String[] data =getListAksi();//getResources().getStringArray(R.array.aksi);
        String[] dataAksi = new String[data.length+1];
        dataAksi[0]= "Tadarrus";
        for (int i=0;i<data.length;i++){
            dataAksi[i+1]= data[i];
        }
        aksi.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, dataAksi));
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
                int aksix = aksi.getSelectedItemPosition()-1;
                //pager.setCurrentItem(BaseQuranInfo.getPageFromPos(page));
                if(aksix!=0){
                    mSettings.setLastAksi(aksix);
                }
                jumpTo(page,aksix);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    private void iklan() {
         mInterstitialAd = new InterstitialAd(MainActivity.this);
         mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
         mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        startGame();
                    }
                    @Override
                    public void onAdLeftApplication ()
                    {
                        mSettings.setIklanKlik(true);
                        tampilIklan++;
                    }
                });
         startGame();

    }

    int tampilIklan=0;
    public void showDialogIklan(String title, String tanya){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(tanya)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        showInterstitial();
                        startGame();
                    }})
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startGame();
                    }
                }).show();
    }
    //====== iklan
    private void startGame() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
        resumeGame(APP_LENGTH_MILLISECONDS);
    }

    private void resumeGame(long milliseconds) {
        // Create a new timer for the correct length and start it.
        //mAppIsInProgress = true;
        mTimerMilliseconds = milliseconds;
        createTimer(milliseconds);
        mCountDownTimer.start();
    }
    public void showIklan(View view){
        showInterstitial();
    }
    private void createTimer(final long milliseconds) {
        // Create the game timer, which counts down to the end of the level
        // and shows the "retry" button.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }


        mCountDownTimer = new CountDownTimer(milliseconds, 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                mTimerMilliseconds = millisUnitFinished;
            }

            @Override
            public void onFinish() {
               // showInterstitial();
                if(tampilIklan<1) {
                    if(mInterstitialAd !=null) {
                        if(!mSettings.isIklas()) {
                            if(!mSettings.isDownloadImage())
                            showDialogIklan("Iklan", getString(R.string.infak_iklan));
                        }
                    }
                }
            }
        };
    }

    private void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
    //===== iklan
    @Override
    public void onDestroy(){
        mSettings.setIklanKlik(false);
        if(mInterstitialAd!=null){
            mInterstitialAd = null;
        }
        super.onDestroy();
    }
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    showInfo(infoError);
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
    class DownloadImage extends AsyncTask<String, String, String> {
        ProgressDialog prgDialog=null;
        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Shows Progress Bar Dialog and then call doInBackground method
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            mSettings.setDowloadImage(true);
            prgDialog = new ProgressDialog(MainActivity.this);
            prgDialog.setMessage("Downloading Image file. Please wait...");
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

                mSettings.setDowloadImage(false);
               // Toast.makeText(MainActivity.this,"ada Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                infoError = "Terjadi Error "+e.getMessage();
                handler.sendMessage(Message.obtain(handler, 0));
                Log.e("Error: ", e.getMessage());
                prgDialog.dismiss();
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
            // Dismiss the dialog after the Music file was downloaded
            mSettings.setDowloadImage(false);
            prgDialog.dismiss();
          // 	Toast.makeText(MainActivity.this, "Download complete... ", Toast.LENGTH_LONG).show();

        }
    }
    String infoError;
    private void showInfo(String message){
        Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
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
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
