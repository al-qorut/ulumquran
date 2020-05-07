package smk.adzikro.indextemaquran.activities;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.lang.ref.WeakReference;


import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.SearchActivity;
import smk.adzikro.indextemaquran.adapter.QuranPageAdapter;
import smk.adzikro.indextemaquran.adapter.QuranViewPager;
import smk.adzikro.indextemaquran.constans.BaseQuranData;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.db.BookmarkHelper;
import smk.adzikro.indextemaquran.fragments.TranslationFragment;
import smk.adzikro.indextemaquran.object.QariItem;
import smk.adzikro.indextemaquran.object.SuraAyah;
import smk.adzikro.indextemaquran.services.AudioService;
import smk.adzikro.indextemaquran.services.QuranDownloadService;
import smk.adzikro.indextemaquran.services.utils.AudioRequest;
import smk.adzikro.indextemaquran.services.utils.DefaultDownloadReceiver;
import smk.adzikro.indextemaquran.services.utils.DownloadAudioRequest;
import smk.adzikro.indextemaquran.services.utils.QuranDownloadNotifier;
import smk.adzikro.indextemaquran.services.utils.ServiceIntentHelper;
import smk.adzikro.indextemaquran.services.utils.StreamingAudioRequest;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.QuranDisplayHelper;
import smk.adzikro.indextemaquran.ui.QuranUtils;
import smk.adzikro.indextemaquran.util.AudioUtils;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.widgets.AudioStatusBar;
import smk.adzikro.indextemaquran.widgets.AyahToolBar;
import smk.adzikro.indextemaquran.widgets.HighlightType;
import timber.log.Timber;

import static smk.adzikro.indextemaquran.constans.Constants.PAGES_LAST;

/*
 git init
 git remote add origin https://github.com/al-qorut/ulumquran.git
 git pull origin master --allow-unrelated-histories
 git add .
 git commit -m “pesan”
 git push origin master
*/

public class UlumQuranActivity extends AppCompatActivity
 implements AudioStatusBar.AudioBarListener,
        DefaultDownloadReceiver.SimpleDownloadListener{
    QuranPageAdapter adapter;
    //QuranViewPager pager;
    public int aksi=-1, page;
    private final PagerHandler mHandler = new PagerHandler(this);
    public static final int MSG_HIDE_ACTIONBAR = 1;
    public static final int MSG_REMOVE_WINDOW_BACKGROUND = 2;
    private long mLastPopupTime = 0;
    private ViewPager mViewPager = null;
    private AyahToolBar mAyahToolBar;
    private AyahToolBar.AyahToolBarPosition mAyahToolBarPos;
    private SuraAyah mStart;
    private SuraAyah mEnd;
    private QuranSettings mSettings = null;
    private View mToolBarArea;
    private TextView title, subTitle;
    private static final String LAST_READ_PAGE = "LAST_READ_PAGE";
    private static final String LAST_READING_MODE_IS_TRANSLATION =
            "LAST_READING_MODE_IS_TRANSLATION";
    private static final String LAST_ACTIONBAR_STATE = "LAST_ACTIONBAR_STATE";
    private static final String LAST_START_POINT = "LAST_START_POINT";
    private static final String LAST_ENDING_POINT = "LAST_ENDING_POINT";
    private AudioStatusBar audioStatusBar;
    private Menu menu;
    private static final long DEFAULT_HIDE_AFTER_TIME = 2000;
    private AyahToolBar ayahToolBar;
    BookmarkHelper bookmarkHelper=null;
    private InterstitialAd mInterstitialAd;
    private DefaultDownloadReceiver downloadReceiver;


    private static class PagerHandler extends Handler {
        private final WeakReference<UlumQuranActivity> mActivity;

        public PagerHandler(UlumQuranActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            UlumQuranActivity activity = mActivity.get();
            if (activity != null) {
                super.handleMessage(msg);
                if (msg.what == MSG_HIDE_ACTIONBAR) {
                    activity.toggleActionBarVisibility(false);
                } else if (msg.what == MSG_REMOVE_WINDOW_BACKGROUND) {
                    activity.getWindow().setBackgroundDrawable(null);
                } else {
                    super.handleMessage(msg);
                }
            }
        }
    }
  //  Toolbar toolbar=null;
  private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quran_page_activity);
        mViewPager = (QuranViewPager) findViewById(R.id.quran_pager);
        mToolBarArea = findViewById(R.id.toolbar_area);
        //   ViewCompat.setLayoutDirection(pager, ViewCompat.LAYOUT_DIRECTION_RTL);
      //  mToolBarArea.setVisibility(View.GONE);
        final View statusBarBackground = findViewById(R.id.status_bg);
        statusBarBackground.getLayoutParams().height = getStatusBarHeight();
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        audioStatusBar = findViewById(R.id.audio_area);
        audioStatusBar.setAudioBarListener(this);
        mSettings = QuranSettings.getInstance(this);

        if(!mSettings.isIklas()){
            iklan();
        }
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        ab.setTitle("");
        mAyahToolBar = (AyahToolBar) findViewById(R.id.ayah_toolbar);
        bookmarkHelper = new BookmarkHelper(this);
        title = (TextView)findViewById(R.id.title);
        subTitle = (TextView)findViewById(R.id.subtitle);
        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            page = extras.getInt("page");
        }else{
            page = mSettings.getLastPage();
        }
        mShowingTranslation = !mSettings.isModeImage();
        adapter = new QuranPageAdapter(getSupportFragmentManager(), mShowingTranslation);
        mViewPager.setAdapter(adapter);


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
              //  Log.e(TAG,"inPageScroll");
                if (mAyahToolBar.isShowing() && mAyahToolBarPos != null) {
                    int barPos = BaseQuranInfo.getPosFromPage(mStart.getPage());
                    if (position == barPos) {
                        // Swiping to next ViewPager page (i.e. prev quran page)
                        mAyahToolBarPos.xScroll = 0 - positionOffsetPixels;
                    } else if (position == barPos - 1) {
                        // Swiping to prev ViewPager page (i.e. next quran page)
                        mAyahToolBarPos.xScroll = mViewPager.getWidth() - positionOffsetPixels;
                    } else {
                        // Totally off screen, should hide toolbar
                        mAyahToolBar.setVisibility(View.GONE);
                        return;
                    }
                    mAyahToolBar.updatePosition(mAyahToolBarPos);
                    // If the toolbar is not showing, show it
                    if (mAyahToolBar.getVisibility() != View.VISIBLE) {
                        mAyahToolBar.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                page = BaseQuranInfo.getPageFromPos(position);
                Log.e(TAG,"Page onPageSelected "+page+" aksi "+aksi);
              //  final int page = QuranInfo.getPageFromPos(position);
                mSettings.setLastPage(page);
                if (mSettings.shouldDisplayMarkerPopup()) {
                    mLastPopupTime = QuranDisplayHelper.displayMarkerPopup(
                            UlumQuranActivity.this, page, mLastPopupTime);
                }
                    updateActionBarTitle();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // mTranslationItems = getResources().getStringArray(R.array.aksi);
         mViewPager.setCurrentItem(BaseQuranInfo.getPageFromPos(page));

     //   mStart = savedInstanceState.getParcelable(LAST_START_POINT);
     //   mEnd = savedInstanceState.getParcelable(LAST_ENDING_POINT);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                audioReceiver,
                new IntentFilter(AudioService.AudioUpdateIntent.INTENT_NAME));

        downloadReceiver = new DefaultDownloadReceiver(this,
                QuranDownloadService.DOWNLOAD_TYPE_AUDIO);
        String action = QuranDownloadNotifier.ProgressIntent.INTENT_NAME;
        LocalBroadcastManager.getInstance(this).registerReceiver(
                downloadReceiver,
                new IntentFilter(action));
        downloadReceiver.setListener(this);

    }

    private void iklan() {
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        startGame();
    }
    private void startGame() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }
    private void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
    public void showDialogIklan(String title, String tanya){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(tanya)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        showInterstitial();
                        //resumeGame(APP_LENGTH_MILLISECONDS);
                        startGame();
                    }})
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startGame();
                    }
                }).show();
    }

    private int getStatusBarHeight() {
        // thanks to https://github.com/jgilfelt/SystemBarTint for this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Resources resources = getResources();
            final int resId = resources.getIdentifier(
                    "status_bar_height", "dimen", "android");
            if (resId > 0) {
                return resources.getDimensionPixelSize(resId);
            }
        }
        return 0;
    }


    @Override
    public void onDestroy(){
        mSettings.setLastAksi(aksi);
        if(bookmarkHelper!=null){
            bookmarkHelper = null;
        }
        if(mInterstitialAd!=null){
            mInterstitialAd = null;
        }
        // remove broadcast receivers
        LocalBroadcastManager.getInstance(this).unregisterReceiver(audioReceiver);
        if (downloadReceiver != null) {
            downloadReceiver.setListener(null);
            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(downloadReceiver);
            downloadReceiver = null;
        }
        super.onDestroy();
    }
    @Override
    public void onResume(){
      //  Log.e(TAG, "onResume");
        if(mShowingTranslation)
        adapter.notifyDataSetChanged();
       // requestTranslationsList();
        if(bookmarkHelper==null){
            bookmarkHelper = new BookmarkHelper(this);
        }
        super.onResume();
    }



    private void updateToolbarPosition(final SuraAyah start) {
        mAyahToolBar.updatePosition(mAyahToolBarPos);
        if (mAyahToolBar.getVisibility() != View.VISIBLE) {
            mAyahToolBar.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setUiVisibility(boolean isVisible){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            setUiVisibilityKitKat(isVisible);
            return;
        }

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (!isVisible) {
            flags |= View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        mViewPager.setSystemUiVisibility(flags);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setUiVisibilityKitKat(boolean isVisible) {
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (!isVisible) {
            flags |= View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        mViewPager.setSystemUiVisibility(flags);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setUiVisibilityListener(){
        mViewPager.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int flags) {
                        boolean visible =
                                (flags & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
                        mIsActionBarHidden = !visible;

                        // animate toolbar
                        mToolBarArea.animate()
                                .translationY(visible ? 0 : -mToolBarArea.getHeight())
                                .setDuration(250)
                                .start();

            /* the bottom margin on the audio bar is not part of its height, and so we have to
             * take it into account when animating the audio bar off the screen. */
                    }
                });
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void clearUiVisibilityListener(){
        mViewPager.setOnSystemUiVisibilityChangeListener(null);
    }
    @Override
    public void onBackPressed(){
        if(!mSettings.isIklas()){
            if(QuranUtils.haveInternet(this)) {
                showInterstitial();
            }else{
                startActivity(new Intent(UlumQuranActivity.this, AdsActivity.class));
            }
        }
        super.onBackPressed();
    }
    @Override
    public void onSaveInstanceState(Bundle state) {
        int lastPage = BaseQuranInfo.getPageFromPos(mViewPager.getCurrentItem());
        state.putInt(LAST_READ_PAGE, lastPage);
        state.putBoolean(LAST_READING_MODE_IS_TRANSLATION, mShowingTranslation);
        state.putBoolean(LAST_ACTIONBAR_STATE, mIsActionBarHidden);
        if (mStart != null && mEnd != null) {
            state.putParcelable(LAST_START_POINT, mStart);
            state.putParcelable(LAST_ENDING_POINT, mEnd);
        }
        super.onSaveInstanceState(state);
    }

    public void updateActionBarTitle() {
        String sura = BaseQuranInfo.getSuraNameFromPage(this, page, true);
        String desc = BaseQuranInfo.getPageSubtitle(this, page);
        title.setText(sura);
        subTitle.setText(desc);
    }
    private int getCurrentPage() {
        return BaseQuranInfo.getPageFromPos(mViewPager.getCurrentItem());
    }
    private void log(String info){
        Log.e(TAG,info);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quran_menu, menu);
        final MenuItem item = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) item.getActionView();
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, SearchActivity.class)));
        return true;
    }
    boolean mShowingTranslation = false;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem quran = menu.findItem(R.id.goto_quran);
        MenuItem translation = menu.findItem(R.id.goto_translation);
        if (quran != null && translation != null) {
            if (!mShowingTranslation) {
                quran.setVisible(false);
                translation.setVisible(true);
            } else {
                quran.setVisible(true);
                translation.setVisible(false);
            }
        }


        return true;
    }

    public void switchToQuran() {
        if(!Fungsi.isFileImageExist()){
            Fungsi.setModeView(this, true);
            finish();
        }else {
            adapter.setTranslationMode(false);
         //   adapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(BaseQuranInfo.getPageFromPos(page));
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("");
            mShowingTranslation = false;
            updateActionBarTitle();
        }
    }
    public void switchToTranslation(){
        adapter.setTranslationMode(true);
     //   adapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(BaseQuranInfo.getPageFromPos(page));
        mShowingTranslation = true;
        updateActionBarTitle();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mHandler.sendEmptyMessageDelayed(
                    MSG_HIDE_ACTIONBAR, DEFAULT_HIDE_AFTER_TIME);
        } else {
            mHandler.removeMessages(MSG_HIDE_ACTIONBAR);
        }
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
                mViewPager.setCurrentItem(BaseQuranInfo.getPageFromPos(page));
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
                ArrayAdapter<String> list_ayat = new ArrayAdapter<String>(UlumQuranActivity.this, R.layout.sherlock_spinner_dropdown_item, listAyat);
                list_ayat.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
                ayat.setAdapter(list_ayat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        dialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.goto_quran) {
            switchToQuran();
            return true;
        }else if (itemId == R.id.goto_translation) {
            switchToTranslation();
            return true;
        }else if (itemId == R.id.cab_play_from_here) {
            //switchToTranslation();
            onPlayPressed();
            return true;
        } else if (itemId == R.id.settings) {
            startActivity(new Intent(UlumQuranActivity.this, settings.class));
            return true;
        } else if (itemId == R.id.qori) {
         //   audioStatusBar.spinner.performClick();
            return true;
        } else if (itemId == android.R.id.home) {
           // startActivity(new Intent(UlumQuranActivity.this, AdsActivity.class));
            if(!mSettings.isIklas()){
                if(QuranUtils.haveInternet(this)) {
                    showInterstitial();
                }else{
                    startActivity(new Intent(UlumQuranActivity.this, AdsActivity.class));
                }
            }
            onBackPressed();
            return true;
        } else if (itemId == R.id.jump) {
            gotoAyat();
            return true;
        }else if (itemId == R.id.play) {
            startActivity(new Intent(UlumQuranActivity.this, ActivityQuranSource.class));
            return true;
        }else if (itemId == R.id.search) {
            onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    boolean mIsActionBarHidden = false;

    public void toggleActionBar(){
        if (mIsActionBarHidden) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setUiVisibility(true);
                mToolBarArea.setVisibility(View.VISIBLE);
            } else {
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mToolBarArea.setVisibility(View.VISIBLE);
                audioStatusBar.updateSelectedItem();
                audioStatusBar.setVisibility(View.VISIBLE);
            }

            mIsActionBarHidden = false;
        } else {
            mHandler.removeMessages(MSG_HIDE_ACTIONBAR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setUiVisibility(false);
                mToolBarArea.setVisibility(View.GONE);
            } else {
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                mToolBarArea.setVisibility(View.GONE);
                audioStatusBar.setVisibility(View.GONE);
            }

            mIsActionBarHidden = true;
        }
    }

    public void toggleActionBarVisibility(boolean visible) {
        if (!(visible ^ mIsActionBarHidden)) {
            toggleActionBar();
        }
    }


    String TAG="UlumQuranActivity";

    public boolean setLoading(boolean loading){
        return loading;
    }



    private AlertDialog promptDialog = null;



    public void showMessage(String title, String message){
        if (promptDialog != null) {
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                    onAcceptPressed();
                    promptDialog=null;
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> promptDialog=null);
        promptDialog = alert.create();
        promptDialog.show();
    }

//=========Audio region---------------
    private boolean shouldOverridePlaying = false;
    private DownloadAudioRequest lastAudioDownloadRequest = null;
    private boolean needsPermissionToDownloadOver3g = true;
    private static final String AUDIO_DOWNLOAD_KEY = "AUDIO_DOWNLOAD_KEY";
    private boolean isActionBarHidden = true;
    private AudioRequest lastAudioRequest;

    @Override
    public void onPlayPressed() {
        if (audioStatusBar.getCurrentMode() == AudioStatusBar.PAUSED_MODE) {
            // if we are "paused," just un-pause.
            play(null);
            return;
        }
        int position = mViewPager.getCurrentItem();
        int page = PAGES_LAST - position;
        int startSura = BaseQuranInfo.safelyGetSuraOnPage(page);
        int startAyah = BaseQuranInfo.PAGE_AYAH_START[page - 1];
        playFromAyah(page, startSura, startAyah, false);
    }

    public void playFromAyah(int page, int startSura,
                              int startAyah, boolean force) {
        final SuraAyah start = new SuraAyah(startSura, startAyah);
        playFromAyah(start, null, page, 0, 0, false, force);
    }

    public void playFromAyah(SuraAyah start, SuraAyah end,
                             int page, int verseRepeat, int rangeRepeat,
                             boolean enforceRange, boolean force) {
        if (force) {
            shouldOverridePlaying = true;
        }

        QariItem item = audioStatusBar.getAudioInfo();

        lastAudioDownloadRequest = getAudioDownloadRequest(start, end, page, item,
                verseRepeat, rangeRepeat, enforceRange);
        if (mSettings.shouldStream() && lastAudioDownloadRequest != null &&
                !AudioUtils.haveAllFiles(lastAudioDownloadRequest)) {
            playStreaming(start, end, page, item, verseRepeat, rangeRepeat, enforceRange);
        } else {
            playAudioRequest(lastAudioDownloadRequest);
        }
    }

    @Nullable
    private DownloadAudioRequest getAudioDownloadRequest(SuraAyah ayah, SuraAyah ending,
                                                         int page, @NonNull QariItem item, int verseRepeat,
                                                         int rangeRepeat, boolean enforceBounds) {
        final SuraAyah endAyah;
        if (ending != null) {
            endAyah = ending;
        } else {
            endAyah = AudioUtils.getLastAyahToPlay(ayah, page,
                    mSettings.getPreferredDownloadAmount(), false);
        }

        String baseUri = AudioUtils.getLocalQariUrl(this, item);
        if (endAyah == null || baseUri == null) {
            return null;
        }
        String dbFile = AudioUtils.getQariDatabasePathIfGapless(this, item);

        String fileUrl;
        if (TextUtils.isEmpty(dbFile)) {
            fileUrl = baseUri + File.separator + "%d" + File.separator +
                    "%d" + AudioUtils.AUDIO_EXTENSION;
        } else {
            fileUrl = baseUri + File.separator + "%03d" +
                    AudioUtils.AUDIO_EXTENSION;
        }
        DownloadAudioRequest request =  new DownloadAudioRequest(fileUrl, ayah, item, baseUri);
        request.setGaplessDatabaseFilePath(dbFile);
        request.setPlayBounds(ayah, endAyah);
        request.setEnforceBounds(enforceBounds);
        request.setRangeRepeatCount(rangeRepeat);
        request.setVerseRepeatCount(verseRepeat);
        return request;
    }

    private void playStreaming(SuraAyah ayah, SuraAyah end,
                               int page, QariItem item, int verseRepeat,
                               int rangeRepeat, boolean enforceRange) {
        String qariUrl = AudioUtils.getQariUrl(item);
        String dbFile = AudioUtils.getQariDatabasePathIfGapless(this, item);
        if (!TextUtils.isEmpty(dbFile)) {
            // gapless audio is "download only"
            lastAudioDownloadRequest = getAudioDownloadRequest(ayah, end, page, item,
                    verseRepeat, rangeRepeat, enforceRange);
            playAudioRequest(lastAudioDownloadRequest);
            return;
        }

        final SuraAyah ending;
        if (end != null) {
            ending = end;
        } else {
            // this won't be enforced unless the user sets a range
            // repeat, but we set it to a sane default anyway.
            ending = AudioUtils.getLastAyahToPlay(ayah, page,
                    mSettings.getPreferredDownloadAmount(), false);
        }
        AudioRequest request = new StreamingAudioRequest(qariUrl, ayah);
        request.setPlayBounds(ayah, ending);
        request.setEnforceBounds(enforceRange);
        request.setRangeRepeatCount(rangeRepeat);
        request.setVerseRepeatCount(verseRepeat);
        play(request);

        audioStatusBar.switchMode(AudioStatusBar.PLAYING_MODE);
        audioStatusBar.setRepeatCount(verseRepeat);
    }

    private void playAudioRequest(@Nullable DownloadAudioRequest request) {
        if (request == null) {
            audioStatusBar.switchMode(AudioStatusBar.STOPPED_MODE);
            return;
        }

        boolean needsPermission = needsPermissionToDownloadOver3g;
        if (needsPermission) {
            if (QuranUtils.isOnWifiNetwork(this)) {
                Timber.d("on wifi, don't need permission for download...");
                needsPermission = false;
            }
        }

        Timber.d("seeing if we can play audio request...");
       if (AudioUtils.shouldDownloadGaplessDatabase(request)) {
            Timber.d("need to download gapless database...");
            if (needsPermission) {
                showMessage(getString(R.string.confirm),getString(R.string.download_non_wifi_prompt));
             //   audioStatusBar.switchMode(AudioStatusBar.PROMPT_DOWNLOAD_MODE);
                return;
            }

            if (isActionBarHidden) {
                toggleActionBar();
            }
            audioStatusBar.switchMode(AudioStatusBar.DOWNLOADING_MODE);
            String url = AudioUtils.getGaplessDatabaseUrl(request);
            if(url.contains("null")){
                Log.e(TAG,"aya null");
                return;
            }
            Log.e(TAG,"Naon "+url);
            String destination = request.getLocalPath();
            // start the download
            String notificationTitle = getString(R.string.timing_database);
            Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
                    destination, notificationTitle, AUDIO_DOWNLOAD_KEY,
                    QuranDownloadService.DOWNLOAD_TYPE_AUDIO);
            startService(intent);
        } else if (AudioUtils.haveAllFiles(request)) {
            if (!AudioUtils.shouldDownloadBasmallah(request)) {
                Timber.d("have all files, playing!");
                play(request);
                lastAudioDownloadRequest = null;
            } else {
                Timber.d("should download basmalla...");
                if (needsPermission) {
                  //  audioStatusBar.switchMode(AudioStatusBar.PROMPT_DOWNLOAD_MODE);
                    showMessage(getString(R.string.confirm),getString(R.string.download_non_wifi_prompt));
                    return;
                }

                SuraAyah firstAyah = new SuraAyah(1, 1);
                String qariUrl = AudioUtils.getQariUrl(request.getQariItem());
                Log.e(TAG,"Naon berikut "+qariUrl);
                audioStatusBar.switchMode(AudioStatusBar.DOWNLOADING_MODE);

                if (isActionBarHidden) {
                    toggleActionBar();
                }
                String notificationTitle = BaseQuranInfo.getNotificationTitle(
                        this, firstAyah, firstAyah, request.isGapless());
                Intent intent = ServiceIntentHelper.getDownloadIntent(this, qariUrl,
                        request.getLocalPath(), notificationTitle,
                        AUDIO_DOWNLOAD_KEY,
                        QuranDownloadService.DOWNLOAD_TYPE_AUDIO);
                intent.putExtra(QuranDownloadService.EXTRA_START_VERSE, firstAyah);
                intent.putExtra(QuranDownloadService.EXTRA_END_VERSE, firstAyah);
                startService(intent);
            }
        } else {
            if (needsPermission) {
               // audioStatusBar.switchMode(AudioStatusBar.PROMPT_DOWNLOAD_MODE);
                showMessage(getString(R.string.confirm),getString(R.string.download_non_wifi_prompt));
                return;
            }

            if (isActionBarHidden) {
                toggleActionBar();
            }
            audioStatusBar.switchMode(AudioStatusBar.DOWNLOADING_MODE);
            String notificationTitle = BaseQuranInfo.getNotificationTitle(this,
                    request.getMinAyah(), request.getMaxAyah(), request.isGapless());
            String qariUrl = AudioUtils.getQariUrl(request.getQariItem());
            Timber.d("need to start download: %s", qariUrl);
            Log.e(TAG,"Naon next  "+qariUrl);
            // start service
            Intent intent = ServiceIntentHelper.getDownloadIntent(this, qariUrl,
                    request.getLocalPath(), notificationTitle, AUDIO_DOWNLOAD_KEY,
                    QuranDownloadService.DOWNLOAD_TYPE_AUDIO);
            intent.putExtra(QuranDownloadService.EXTRA_START_VERSE,
                    request.getMinAyah());
            intent.putExtra(QuranDownloadService.EXTRA_END_VERSE,
                    request.getMaxAyah());
            intent.putExtra(QuranDownloadService.EXTRA_IS_GAPLESS,
                    request.isGapless());
            startService(intent);
        }
    }
    private void play(AudioRequest request) {
        needsPermissionToDownloadOver3g = true;
        Intent i = new Intent(this, AudioService.class);
        i.setAction(AudioService.ACTION_PLAYBACK);
        if (request != null) {
            i.putExtra(AudioService.EXTRA_PLAY_INFO, request);
            lastAudioRequest = request;
            audioStatusBar.setRepeatCount(request.getVerseRepeatCount());
        }

        if (shouldOverridePlaying) {
            // force the current audio to stop and start playing new request
            i.putExtra(AudioService.EXTRA_STOP_IF_PLAYING, true);
            shouldOverridePlaying = false;
        }
        // just a playback request, so tell audio service to just continue
        // playing (and don't store new audio data) if it was already playing
        else {
            i.putExtra(AudioService.EXTRA_IGNORE_IF_PLAYING, true);
        }
        startService(i);
    }
    @Override
    public void onPausePressed() {
        startService(AudioUtils.getAudioIntent(
                this, AudioService.ACTION_PAUSE));
        audioStatusBar.switchMode(AudioStatusBar.PAUSED_MODE);
    }

    @Override
    public void onNextPressed() {
        startService(AudioUtils.getAudioIntent(this,
                AudioService.ACTION_SKIP));
    }

    @Override
    public void onPreviousPressed() {
        startService(AudioUtils.getAudioIntent(this,
                AudioService.ACTION_REWIND));
    }

    @Override
    public void onStopPressed() {

    }

    @Override
    public void onCancelPressed(boolean stopDownload) {
        if (stopDownload) {
            needsPermissionToDownloadOver3g = true;

            int resId = R.string.canceling;
            audioStatusBar.setProgressText(getString(resId), true);
            Intent i = new Intent(this, QuranDownloadService.class);
            i.setAction(QuranDownloadService.ACTION_CANCEL_DOWNLOADS);
            startService(i);
        } else {
            audioStatusBar.switchMode(AudioStatusBar.STOPPED_MODE);
        }
    }

    @Override
    public void setRepeatCount(int repeatCount) {
        if (lastAudioRequest != null) {
            Intent i = new Intent(this, AudioService.class);
            i.setAction(AudioService.ACTION_UPDATE_REPEAT);
            i.putExtra(AudioService.EXTRA_VERSE_REPEAT_COUNT, repeatCount);
            startService(i);
            lastAudioRequest.setVerseRepeatCount(repeatCount);
        }
    }

    @Override
    public void onAcceptPressed() {
        if (lastAudioDownloadRequest != null) {
            needsPermissionToDownloadOver3g = false;
            playAudioRequest(lastAudioDownloadRequest);
        }
    }

    @Override
    public void onAudioSettingsPressed() {
        if (lastPlayingSura != null) {
            mStart = new SuraAyah(lastPlayingSura, lastPlayingAyah);
            mEnd = mStart;
        }

        if (mStart == null) {
            final Integer bounds[] = BaseQuranInfo.getPageBounds(getCurrentPage());
            mStart = new SuraAyah(bounds[0], bounds[1]);
            mEnd = mStart;
        }
       // showSlider(AUDIO_PAGE);
    }
    public boolean updatePlayOptions(int rangeRepeat,
                                     int verseRepeat, boolean enforceRange) {
        if (lastAudioRequest != null) {
            Intent i = new Intent(this, AudioService.class);
            i.setAction(AudioService.ACTION_UPDATE_REPEAT);
            i.putExtra(AudioService.EXTRA_VERSE_REPEAT_COUNT, verseRepeat);
            i.putExtra(AudioService.EXTRA_RANGE_REPEAT_COUNT, rangeRepeat);
            i.putExtra(AudioService.EXTRA_RANGE_RESTRICT, enforceRange);
            startService(i);

            lastAudioRequest.setVerseRepeatCount(verseRepeat);
            lastAudioRequest.setRangeRepeatCount(rangeRepeat);
            lastAudioRequest.setEnforceBounds(enforceRange);
            audioStatusBar.setRepeatCount(verseRepeat);
            return true;
        } else {
            return false;
        }
    }

//=======Download Listner
    @Override
    public void handleDownloadSuccess() {
      //  refreshQuranPages();
        playAudioRequest(lastAudioDownloadRequest);
    }

    @Override
    public void handleDownloadFailure(int errId) {
        String s = getString(errId);
        audioStatusBar.setProgressText(s, true);
    }
/*
    @Override
    public void updateDownloadProgress(int progress, long downloadedSize, long totalSize) {
        audioStatusBar.switchMode(
                AudioStatusBar.DOWNLOADING_MODE);
        audioStatusBar.setProgress(progress);
    }

    @Override
    public void updateProcessingProgress(int progress, int processFiles, int totalFiles) {
        audioStatusBar.setProgressText(getString(R.string.extracting_title), false);
        audioStatusBar.setProgress(-1);
    }

    @Override
    public void handleDownloadTemporaryError(int errorId) {
        audioStatusBar.setProgressText(getString(errorId), false);
    }
*/
    private BroadcastReceiver audioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int state = intent.getIntExtra(
                        AudioService.AudioUpdateIntent.STATUS, -1);
                int sura = intent.getIntExtra(
                        AudioService.AudioUpdateIntent.SURA, -1);
                int ayah = intent.getIntExtra(
                        AudioService.AudioUpdateIntent.AYAH, -1);
                int repeatCount = intent.getIntExtra(
                        AudioService.AudioUpdateIntent.REPEAT_COUNT, -200);
                AudioRequest request = intent.getParcelableExtra(AudioService.AudioUpdateIntent.REQUEST);
                if (request != null) {
                    lastAudioRequest = request;
                }
                if (state == AudioService.AudioUpdateIntent.PLAYING) {
                    audioStatusBar.switchMode(AudioStatusBar.PLAYING_MODE);
                    highlightAyah(sura, ayah, HighlightType.AUDIO);
                    if (repeatCount >= -1) {
                        audioStatusBar.setRepeatCount(repeatCount);
                    }
                } else if (state == AudioService.AudioUpdateIntent.PAUSED) {
                    audioStatusBar.switchMode(AudioStatusBar.PAUSED_MODE);
                    highlightAyah(sura, ayah, HighlightType.AUDIO);
                } else if (state == AudioService.AudioUpdateIntent.STOPPED) {
                    audioStatusBar.switchMode(AudioStatusBar.STOPPED_MODE);
                    unHighlightAyahs(HighlightType.AUDIO);
                    lastAudioRequest = null;
                    AudioRequest qi = intent.getParcelableExtra(AudioService.EXTRA_PLAY_INFO);
                    if (qi != null) {
                        // this means we stopped due to missing audio
                    }
                }
            }
        }
    };
    private Integer lastPlayingSura;
    private Integer lastPlayingAyah;

    public void highlightAyah(int sura, int ayah, HighlightType type) {
        if (HighlightType.AUDIO.equals(type)) {
            lastPlayingSura = sura;
            lastPlayingAyah = ayah;
        }
        highlightAyah(sura, ayah, true, type);
    }

    private void highlightAyah(int sura, int ayah,
                               boolean force, HighlightType type) {
        Timber.d("highlightAyah() - %s:%s", sura, ayah);
        int page = BaseQuranInfo.getPageFromSuraAyah(sura, ayah);
        if (page < Constants.PAGES_FIRST ||
                PAGES_LAST < page) {
            return;
        }

        int position = BaseQuranInfo.getPosFromPage(page);
        if (position != mViewPager.getCurrentItem() && force) {
            unHighlightAyahs(type);
            mViewPager.setCurrentItem(position);
        }
        int ayahId = BaseQuranInfo.getAyahId(sura, ayah);
        Fragment f = adapter.getFragmentIfExists(position);
        if (f instanceof TranslationFragment && f.isAdded()) {
            ((TranslationFragment) f).setHighligh(ayahId);
        }
    }

    private void unHighlightAyah(int sura, int ayah, HighlightType type) {
        int position = mViewPager.getCurrentItem();
        Fragment f = adapter.getFragmentIfExists(position);
        if (f instanceof TranslationFragment && f.isVisible()) {
            ((TranslationFragment) f).setHighligh(0);
        }
    }

    private void unHighlightAyahs(HighlightType type) {
        if (HighlightType.AUDIO.equals(type)) {
            lastPlayingSura = null;
            lastPlayingAyah = null;
        }
        int position = mViewPager.getCurrentItem();
        Fragment f = adapter.getFragmentIfExists(position);
        if (f instanceof TranslationFragment && f.isVisible()) {
            ((TranslationFragment) f).setHighligh(0);
        }
    }
}
