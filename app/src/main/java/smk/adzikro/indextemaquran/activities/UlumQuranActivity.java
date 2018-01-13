package smk.adzikro.indextemaquran.activities;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import android.widget.Toast;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.adapter.QuranPageAdapter;
import smk.adzikro.indextemaquran.adapter.QuranSourceAdapter;
import smk.adzikro.indextemaquran.adapter.QuranViewPager;
import smk.adzikro.indextemaquran.constans.BaseQuranData;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.db.BookmarkHelper;
import smk.adzikro.indextemaquran.fragments.TranslationFragment;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.object.SuraAyah;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.QuranDisplayHelper;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.widgets.AyahToolBar;

import static smk.adzikro.indextemaquran.constans.Constants.PAGES_LAST;

/**
 * Created by server on 11/30/16.
 */

public class UlumQuranActivity extends AppCompatActivity {
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
    private ArrayAdapter<String> mSpinnerAdapter = null;
    private String[] mTranslationItems;
    private View mToolBarArea;
    private Spinner translationsSpinner;
    private TextView title, subTitle;
    private static final String LAST_READ_PAGE = "LAST_READ_PAGE";
    private static final String LAST_READING_MODE_IS_TRANSLATION =
            "LAST_READING_MODE_IS_TRANSLATION";
    private static final String LAST_ACTIONBAR_STATE = "LAST_ACTIONBAR_STATE";
    private static final String LAST_START_POINT = "LAST_START_POINT";
    private static final String LAST_ENDING_POINT = "LAST_ENDING_POINT";


    private static final long DEFAULT_HIDE_AFTER_TIME = 2000;
    private AyahToolBar ayahToolBar;
    BookmarkHelper bookmarkHelper=null;
    private boolean isBookmarked = false;
    private Menu menu;
    private InterstitialAd mInterstitialAd;

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

        mSettings = QuranSettings.getInstance(this);
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        ab.setTitle("");
        mAyahToolBar = (AyahToolBar) findViewById(R.id.ayah_toolbar);
        bookmarkHelper = new BookmarkHelper(this);

        translationsSpinner = (Spinner) findViewById(R.id.spinner);
        title = (TextView)findViewById(R.id.title);
        subTitle = (TextView)findViewById(R.id.subtitle);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UlumQuranActivity.this, ActivityQuranSource.class));
              //if(mShowingTranslation)
               // translationsSpinner.performClick();
              //    showSourceTranslation();
            }
        });
        subTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(mShowingTranslation)
                 //   translationsSpinner.performClick();
                startActivity(new Intent(UlumQuranActivity.this, ActivityQuranSource.class));
            }
        });

      //  iklan();
        mAyahToolBar.setOnItemSelectedListener(new AyahMenuItemSelectionHandler());
        Bundle extras = getIntent().getExtras();
        page = extras.getInt("page");
        aksi = extras.getInt("aksi");
        if (aksi == -1){
            mShowingTranslation = false;// Fungsi.getModeView(this);
        }else{
            mShowingTranslation = true;
          }

            if(mShowingTranslation) {
                adapter = new QuranPageAdapter(getSupportFragmentManager(), mShowingTranslation, aksi);
                mViewPager.setAdapter(adapter);
                adapter.setAksi(aksi);
            //    spinnerAksi.setVisibility(View.VISIBLE);
            }else {
                //type image
                int tampil= Fungsi.getModeView(this)?1:2;
                adapter = new QuranPageAdapter(
                        getSupportFragmentManager(), mShowingTranslation, tampil);
                mViewPager.setAdapter(adapter);
            //    spinnerAksi.setVisibility(View.GONE);
            }


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
                if (!mShowingTranslation) {
                    updateActionBarTitle();
                } else {
                    refreshActionBarSpinner();
                }
                isBookmarked=bookmarkHelper.isPageBookmarked(page);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // mTranslationItems = getResources().getStringArray(R.array.aksi);
         requestTranslationsList();
         mSpinnerAdapter= new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item, mTranslationItems);
         translationsSpinner.setAdapter(mSpinnerAdapter);
         translationsSpinner.setOnItemSelectedListener(translationItemSelectedListener);
         mViewPager.setCurrentItem(BaseQuranInfo.getPageFromPos(page));

     //   mStart = savedInstanceState.getParcelable(LAST_START_POINT);
     //   mEnd = savedInstanceState.getParcelable(LAST_ENDING_POINT);
    }
    private void iklan() {
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                //  onResume();
                startGame();
            }
            @Override
            public void onAdLeftApplication ()
            {
                mSettings.setIklanKlik(true);
            }
        });
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
    public void setPage(int page1){
        page = page1;
        MenuItem bookmenu = menu.findItem(R.id.favorite_item);
        isBookmarked=bookmarkHelper.isPageBookmarked(page);
        bookmenu.setIcon(isBookmarked ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);
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
        super.onDestroy();
    }
    @Override
    public void onResume(){
        Log.e(TAG, "onResume");
        adapter.notifyDataSetChanged();
       // requestTranslationsList();
        if(bookmarkHelper==null){
            bookmarkHelper = new BookmarkHelper(this);
        }
        super.onResume();
    }

    private void requestTranslationsList() {
        mTranslationItems = null;
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
        translationsSpinner.setAdapter(null);
        mSpinnerAdapter= new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item, mTranslationItems);
        translationsSpinner.setAdapter(mSpinnerAdapter);
        if(mShowingTranslation)adapter.notifyDataSetChanged();
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
        String sura="";
        if(mShowingTranslation){
            log("Mode Translate ");
            page = BaseQuranInfo.getPageFromPos(mViewPager.getCurrentItem());
            if(aksiPertama) {
                sura = getAksiName(mSettings.getLastAksi());// getActiveTranslation();
                aksiPertama = false;
                aksi = mSettings.getLastAksi();
                log("Aksi pertama");
            }else{
                sura = mTranslationItems[translationsSpinner.getSelectedItemPosition()];
                aksi = translationsSpinner.getSelectedItemPosition();
                log("Aksi pertama false");
            }

        }else {
            log("Mode Tadarrus ");
            aksi = -1;
            sura = BaseQuranInfo.getSuraNameFromPage(this, page, true);
            if(!Fungsi.getModeView(this)){
                log("Mode Tadarrus Text");
                page = BaseQuranInfo.getPageFromPos(mViewPager.getCurrentItem());
                int pos = mViewPager.getCurrentItem() - 1;
                for (int count = 0; count < 3; count++) {
                    if (pos + count < 0) {
                        continue;
                    }
                    Fragment f = adapter
                            .getFragmentIfExists(pos + count);
                    if (f instanceof TranslationFragment) {
                        ((TranslationFragment) f).refresh();
                    }
                }
            }
        }
        String desc = BaseQuranInfo.getPageSubtitle(this, page);
        title.setText(sura);
        subTitle.setText(desc);
       // mSpinnerAdapter = null;

    }
    private int getCurrentPage() {
        return BaseQuranInfo.getPageFromPos(mViewPager.getCurrentItem());
    }
    private void log(String info){
        Log.e(TAG,info);
    }

    private void refreshActionBarSpinner() {
        log("refreshActionBarSpinner");
        if (mSpinnerAdapter != null) {
            log("mSpinnerAdapter != null");
           // mSpinnerAdapter = null;
             mSpinnerAdapter.notifyDataSetChanged();
           // updateActionBarSpinner();
            //int position = translationsSpinner.getSelectedItemPosition();
            aksi = getIndexAksi(translationsSpinner.getSelectedItem().toString());
            int pos = mViewPager.getCurrentItem() - 1;
            for (int count = 0; count < 3; count++) {
                if (pos + count < 0) {
                    continue;
                }
                Fragment f = adapter
                        .getFragmentIfExists(pos + count);
                if (f instanceof TranslationFragment) {
                    ((TranslationFragment) f).refresh();
                }
            }
        } else {
            log("mSpinnerAdapter == null");
            updateActionBarSpinner();
        }
        updateActionBarTitle();
    }
    private void updateActionBarSpinner() {
        if (mTranslationItems== null || mTranslationItems.length == 0) {
            log("mTranslationItems== null || mTranslationItems.length == 0");
            updateActionBarTitle();
            return;
        }

        if (mSpinnerAdapter == null) {
            log("mSpinnerAdapter == null");
            mSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item, mTranslationItems);
            translationsSpinner.setAdapter(mSpinnerAdapter);
            translationsSpinner.setOnItemSelectedListener(translationItemSelectedListener);
        }
    }
    private int getIndexAksi(String aksi){
        String[] lAksi = getResources().getStringArray(R.array.aksi);
        int hasil=0;
        for(int i=0;i<lAksi.length;i++){
            if(aksi.equals(lAksi[i])){
                hasil=i;
                break;
            }
        }
        return hasil;
    }
    private String getAksiName(int aksi){
        String[] lAksi = getResources().getStringArray(R.array.aksi);
        String hasil="";
        for(int i=0;i<lAksi.length;i++){
            if(aksi==i){
                hasil=lAksi[i];
                break;
            }
        }
        return hasil;
    }

    boolean aksiPertama = true;
    private AdapterView.OnItemSelectedListener translationItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.e(TAG,"item chosen: "+position);
                    log("AdapterView.OnItemSelectedListener");
                    if (mTranslationItems != null && mTranslationItems.length > position) {
                     //   aksi = position ;
                        if(aksiPertama){
                            aksi = mSettings.getLastAksi();
                         //   aksiPertama = false;
                        }else{
                            Log.e(TAG,translationsSpinner.getSelectedItem().toString());
                            aksi = getIndexAksi(translationsSpinner.getSelectedItem().toString());
                            mSettings.setActiveTranslation(translationsSpinner.getSelectedItem().toString());
                        }
                        int pos = mViewPager.getCurrentItem() - 1;
                        for (int count = 0; count < 3; count++) {
                            if (pos + count < 0) {
                                continue;
                            }
                            Fragment f = adapter
                                    .getFragmentIfExists(pos + count);
                            if (f instanceof TranslationFragment) {
                                ((TranslationFragment) f).refresh();
                            }
                        }
                        mSettings.setLastAksi(aksi);
                    }
                    updateActionBarTitle();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quran_menu, menu);
        final MenuItem item = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, CariAyatQuran.class)));
        return true;
    }
    boolean mShowingTranslation = false;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.favorite_item);
        if (item != null) {
            //int page = BaseQuranInfo.getPageFromPos(mViewPager.getCurrentItem());

           // boolean bookmarked = false;
           // if (bookmarksCache.indexOfKey(page) >= 0) {
           //     bookmarked = bookmarksCache.get(page);
           // }
            item.setIcon(isBookmarked ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);
        }

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
        mShowingTranslation = false;
     //   mSettings.setLastAksi(aksi);
        translationsSpinner.setVisibility(View.GONE);
        int mode = Fungsi.getModeView(this)?1:2;
        adapter.setQuranMode(mode);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        updateActionBarTitle();
    }
    public void switchToTranslation(){
        adapter.setTranslationMode(mSettings.getLastAksi());
        Log.e(TAG,"aksi terakshir "+mSettings.getLastAksi());
        //adapter.setAksi();
        adapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(BaseQuranInfo.getPageFromPos(page));
        mShowingTranslation = true;
        updateActionBarTitle();
        translationsSpinner.setVisibility(View.VISIBLE);
        //invalidateOptionsMenu();
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
        if (itemId == R.id.favorite_item) {
           // int page = getCurrentPage();
            Integer[] data=BaseQuranInfo.getPageBounds(page);
           // toggleBookmark(null, null, page, aksi);
            if(isBookmarked){
                isBookmarked = false;
                bookmarkHelper.hapusBookPage(page);
                item.setIcon(R.drawable.ic_not_favorite);
            }else {
                isBookmarked = true;
                if (aksi < 0) {
                    bookmarkHelper.addBookmarkTadarrus(data[0], data[1], page, -1, "");
                } else {
                    bookmarkHelper.addBookmarkIfNotExists(data[0], data[1], page, aksi, "");
                }
                item.setIcon(R.drawable.ic_favorite);
            }


            return true;
        }else if (itemId == R.id.goto_quran) {
            switchToQuran();
            return true;
        }else if (itemId == R.id.goto_translation) {
            switchToTranslation();
            return true;
        } else if (itemId == R.id.settings) {
            startActivity(new Intent(UlumQuranActivity.this, settings.class));
            return true;
        } else if (itemId == R.id.help) {
           // showHelp();
            //showSourceTranslation();
            startActivity(new Intent(this, ActivityQuranSource.class));
            return true;
        } else if (itemId == R.id.tampil) {
            showDialogIklan("Tampilkan",getString(R.string.tampilkan_tafsir));
//            ((MainActivity)getBaseContext()).showDialogIklan("test","iklan");
        return true;
        }else if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.jump) {
            gotoAyat();
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
            }

            mIsActionBarHidden = true;
        }
    }

    public void toggleActionBarVisibility(boolean visible) {
        if (!(visible ^ mIsActionBarHidden)) {
            toggleActionBar();
        }
    }
    public void setLoadingIfPage(int page) {
        int position = mViewPager.getCurrentItem();
        int currentPage = PAGES_LAST - position;
        if (currentPage == page) {
            setLoading(true);
        }
    }

    String TAG="UlumQuranActivity";

    public boolean setLoading(boolean loading){
        return loading;
    }

    private class AyahMenuItemSelectionHandler implements MenuItem.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int sliderPage = -1;
            switch (item.getItemId()) {
                case R.id.cab_bookmark_ayah:
                 //   toggleBookmark(mStart.sura, mStart.ayah, mStart.getPage());
                    break;
                case R.id.cab_tag_ayah:
                 //   sliderPage = mSlidingPagerAdapter.getPagePosition(TAG_PAGE);
                    break;
                case R.id.cab_translate_ayah:
                  //  sliderPage = mSlidingPagerAdapter.getPagePosition(TRANSLATION_PAGE);
                    break;
                case R.id.cab_play_from_here:
                  //  sliderPage = mSlidingPagerAdapter.getPagePosition(AUDIO_PAGE);
                    break;
                case R.id.cab_share_ayah_link:
                  //  new ShareQuranAppTask(PagerActivity.this, mStart, mEnd).execute();
                    break;
                case R.id.cab_share_ayah_text:
                  //  new ShareAyahTask(PagerActivity.this, mStart, mEnd, false).execute();
                    break;
                case R.id.cab_copy_ayah:
                  //  new ShareAyahTask(PagerActivity.this, mStart, mEnd, true).execute();
                    break;
                default:
                    return false;
            }
            if (sliderPage < 0) {
           //     endAyahMode();
            } else {
           //     showSlider(sliderPage);
            }
            return true;
        }
    }
    private Integer[] getAyahFromId(int id){
        Integer[] data = new Integer[2];
        int count=0;
        for(int i=0;i<BaseQuranInfo.SURA_NUM_AYAHS.length-1;i++){
            for(int x=0; x<BaseQuranInfo.SURA_NUM_AYAHS[i];x++){
                count++;
                if(count==id){
                    data[0]=i+1;
                    data[1]=x+1;
                }
            }
        }
        return data;
    }
    private AlertDialog promptDialog = null;

    public void showHelp(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Help");
        alert.setMessage(R.string.help)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alert.show();
    }
    public void showSourceTranslation(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.quran_list);
        RecyclerView recyclerView = dialog.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<QuranSource> data = Fungsi.getDataSourceQuran(this);
        QuranSourceAdapter sourceAdapter = new QuranSourceAdapter(this, data,null,null);
        recyclerView.setAdapter(sourceAdapter);
        dialog.show();
    }
    public void showGetBookmark(final int surat, final int ayat, final String text) {
        if (promptDialog != null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Ayat mau di Bookmark ?")
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int option) {
                                //toggleBookmark(surat,ayat,BaseQuranInfo.getPageFromSuraAyah(surat,ayat),aksi);
                                bookmarkHelper.addBookmarkIfNotExists(surat, ayat, BaseQuranInfo.getPageFromSuraAyah(surat,ayat),aksi,text );
                                Toast.makeText(UlumQuranActivity.this,"Tambahkan bookmark",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                promptDialog = null;
                            }
                        })
                .setNegativeButton("Tidak",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int option) {
                                dialog.dismiss();
                                promptDialog = null;
                            }
                        });
        promptDialog = builder.create();
        promptDialog.show();
    }

    public void onLongClick(final int idAyat, final String text) {
        Integer[] datax = new Integer[2];
        datax = getAyahFromId(idAyat);
        int surat = datax[0];
        int ayat = datax[1];
        showGetBookmark(surat, ayat, text);
    }


}
