package smk.adzikro.indextemaquran.setting;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.Calendar;
import java.util.Date;

import smk.adzikro.indextemaquran.BuildConfig;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.services.QuranDownloadService;
import smk.adzikro.indextemaquran.util.AudioUtils;
import smk.adzikro.indextemaquran.util.Encryption;


public class QuranSettings {
  private static final String PREFS_FILE = "smk.adzikro.indextemaquran";

  private static QuranSettings sInstance;
  private SharedPreferences mPrefs;
  private SharedPreferences mPerInstallationPrefs;

  public static synchronized QuranSettings getInstance(@NonNull Context context) {
    if (sInstance == null) {
      sInstance = new QuranSettings(context.getApplicationContext());
    }
    return sInstance;
  }
  public String base64EncodedPublicKey ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4YNeVKJk/+D1wPfdilAhPkkSAuxo9vmIWS6I/wrYtuI29LeBrJqxiaE8lxgOeFX4XEUAK+ddOFqqYMa73Gw/pH32N0pfDKwKzzV7FyUee2G9cKJY2xwUL9/dNQPMZidbcsElSaR2UJyIjj22sYAWcJBU9DmMGMgt8/AE/N8CmWwD0SmOKjzsPrr9YlGkSZQ7WOeC1N8rhEyzHxB2oWy75I9vsu5tRUL4v30DGzCzWYS/x5J0D2hSZ5Itjeh7oWbVxddwVAFopygScNR5q0/7N8IWVeadHmXDSpgvTtUTACiSNoI6te/qP3kdcAjRTWF/P3vJa43Q6umY3qgiKyeGywIDAQAB";

  public String getKunciPayload() {
    Encryption sokAcak = Encryption.getDefault("!!Al-Qorut", "Salto", new byte[16]);
    String hasilAcak = sokAcak.encryptOrNull("InfakPengembangan");
    return hasilAcak;
  }
  @VisibleForTesting
  public static void setInstance(QuranSettings settings) {
    sInstance = settings;
  }

  private QuranSettings(@NonNull Context appContext) {
    mPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);
    mPerInstallationPrefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
  }
  String TAG="QuranSettings";
  public void setIklas(){
    mPrefs.edit().putLong(Constants.PREF_VERSI_IKLHAS, new Date().getTime()).apply();
  }
  public String getQuranName(){
    return mPrefs.getString(Constants.PREF_QURAN_ACTIVE, QuranFileConstants.ARABIC_DATABASE);
  }

  public int getPreferredDownloadAmount() {
    String str = mPrefs.getString(Constants.PREF_DOWNLOAD_AMOUNT,
            "" + AudioUtils.LookAheadAmount.PAGE);
    int val = AudioUtils.LookAheadAmount.PAGE;
    try {
      val = Integer.parseInt(str);
    } catch (Exception e) {
      // no op
    }

    if (val > AudioUtils.LookAheadAmount.MAX ||
            val < AudioUtils.LookAheadAmount.MIN) {
      return AudioUtils.LookAheadAmount.PAGE;
    }
    return val;
  }
  public boolean isIklas(){
    long tglx = mPrefs.getLong(Constants.PREF_VERSI_IKLHAS, 0L);
   // Log.e(TAG,"versi Iklhas cek "+tglx);
    if(tglx==0){
      return false;
    }else {
      Date tglAwal = new Date(tglx);
      Calendar c = Calendar.getInstance();
      c.setTime(tglAwal);
      c.add(Calendar.DATE, 30);
      Date tglAkhir = new Date(c.getTimeInMillis());
   //   Log.e(TAG,"versi Iklhas Awal "+tglAwal);
      Date tglSekarang = new Date();//c.getTime();
   //   Log.e(TAG,"versi Iklhas Akhir "+tglAkhir);
   //   Log.e(TAG,"versi Iklhas Sekarang "+tglSekarang);
      if(tglSekarang.after(tglAwal) && tglSekarang.before(tglAkhir)){
   //     Log.e(TAG,"versi Iklhas masuk ");
        return true;
      }else{
        return false;
      }
    }
  }
  public boolean isArabicNames() {
    return mPrefs.getBoolean(Constants.PREF_USE_ARABIC_NAMES, false);
  }
  public void setTafsir(boolean b) {
    mPrefs.edit().putBoolean(Constants.PREF_TAFSIR_DISPLAY, b).apply();
    mPrefs.edit().putBoolean(Constants.PREF_TRANSLATE_DISPLAY, b).apply();
  }
  public boolean isDisplay() {
    return mPrefs.getBoolean(Constants.PREF_LATIN_TEXT_DISPLAY, false);
  }
  public boolean isLockOrientation() {
    return mPrefs.getBoolean(Constants.PREF_LOCK_ORIENTATION, false);
  }
  public boolean isIklanCliked() {
    return mPrefs.getBoolean(Constants.PREF_IKLAN_CLIKED, false);
  }
  public void setIklanKlik(boolean klik) {
    mPrefs.edit().putBoolean(Constants.PREF_IKLAN_CLIKED, klik).apply();
  }
  public boolean isDownloadImage() {
    return mPrefs.getBoolean("download_image", false);
  }
  public void setDowloadImage(boolean aksi) {
    mPrefs.edit().putBoolean("download_image", aksi).apply();
  }
  public boolean isLatinDisplay() {
    return mPrefs.getBoolean(Constants.PREF_LATIN_TEXT_DISPLAY, true);
  }

  public boolean isLafdziDisplay() {
    return mPrefs.getBoolean(Constants.PREF_LAFDZI_DISPLAY, false);
  }
  public String getLafdziDisplay() {
    return mPrefs.getString(Constants.PREF_LAFDZI_LANGGUAGE, "id");
  }
  public boolean isTafsirDisplay() {
    return mPrefs.getBoolean(Constants.PREF_TAFSIR_DISPLAY, false);
  }
  public boolean isTranslateDisplay() {
    return mPrefs.getBoolean(Constants.PREF_TRANSLATE_DISPLAY, false);
  }
  public boolean isLandscapeOrientation() {
    return mPrefs.getBoolean(Constants.PREF_LANDSCAPE_ORIENTATION, false);
  }
  public boolean isTafsirIbnuKatsir() {
    return mPrefs.getBoolean(Constants.PREF_TAFSIR_IBNU_KATSIR, false);
  }
  public boolean isUpdate(String update) {
    return mPrefs.getBoolean(update, false);
  }
  public void setUpdate(boolean update, String atosupdate) {
    mPrefs.edit().putBoolean(atosupdate, update).apply();
  }
  public boolean isTafsirIrab() {
    return mPrefs.getBoolean(Constants.PREF_TAFSIR_IRAB, false);
  }
  public boolean isTafsirSharf() {
    return mPrefs.getBoolean(Constants.PREF_TAFSIR_SHARF, false);
  }
  public boolean isTafsirBalagha() {
    return mPrefs.getBoolean(Constants.PREF_TAFSIR_BALAGHA, false);
  }

  public boolean shouldStream() {
    return mPrefs.getBoolean(Constants.PREF_PREFER_STREAMING, false);
  }

  public boolean isNightMode() {
    return mPrefs.getBoolean(Constants.PREF_NIGHT_MODE, false);
  }
  public boolean isModeImage() {
    return mPrefs.getBoolean(Constants.PREF_MODE_VIEW, false);
  }
  public void setModeImage(boolean image) {
    mPrefs.edit().putBoolean(Constants.PREF_MODE_VIEW, image).apply();
  }
  public boolean useNewBackground() {
    return mPrefs.getBoolean(Constants.PREF_USE_NEW_BACKGROUND, true);
  }

  public boolean highlightBookmarks() {
    return mPrefs.getBoolean(Constants.PREF_HIGHLIGHT_BOOKMARKS, true);
  }

  public int getNightModeTextBrightness() {
    return mPrefs.getInt(Constants.PREF_NIGHT_MODE_TEXT_BRIGHTNESS,
        Constants.DEFAULT_NIGHT_MODE_TEXT_BRIGHTNESS);
  }

  public boolean shouldOverlayPageInfo() {
    return mPrefs.getBoolean(Constants.PREF_OVERLAY_PAGE_INFO, true);
  }

  public boolean shouldDisplayMarkerPopup() {
    return mPrefs.getBoolean(Constants.PREF_DISPLAY_MARKER_POPUP, true);
  }

  public boolean shouldHighlightBookmarks() {
    return mPrefs.getBoolean(Constants.PREF_HIGHLIGHT_BOOKMARKS, true);
  }

  public boolean wantArabicInTranslationView() {
    return mPrefs.getBoolean(Constants.PREF_AYAH_BEFORE_TRANSLATION, true);
  }


  public int getTranslationTextSize() {
    return mPrefs.getInt(Constants.PREF_TRANSLATION_TEXT_SIZE,
        Constants.DEFAULT_TEXT_SIZE);
  }
  public int getTextSize() {
    return mPrefs.getInt(Constants.PREF_TRANSLATION_TEXT_SIZE,
            Constants.DEFAULT_TEXT_SIZE);
  }

  public int getLastPage() {
    return mPrefs.getInt(Constants.PREF_LAST_PAGE, Constants.NO_PAGE_SAVED);
  }

  public void setLastPage(int page) {
    mPrefs.edit().putInt(Constants.PREF_LAST_PAGE, page).apply();
  }
  public void setLastPageTranslate(int page) {
    mPrefs.edit().putInt(Constants.PREF_LAST_PAGE_TRANSLATE, page).apply();
  }
  public int getLastPageTranslate() {
    return mPrefs.getInt(Constants.PREF_LAST_PAGE_TRANSLATE, Constants.NO_PAGE_SAVED);
  }
  public void setLastAksi(int aksi) {
    mPrefs.edit().putInt(Constants.PREF_LAST_AKSI, aksi).apply();
  }
  public int getLastAksi() {
    return mPrefs.getInt(Constants.PREF_LAST_AKSI, Constants.NO_PAGE_SAVED);
  }
  public int getBookmarksSortOrder() {
    return mPrefs.getInt(Constants.PREF_SORT_BOOKMARKS, 0);
  }

  public void setBookmarksSortOrder(int sortOrder) {
    mPrefs.edit().putInt(Constants.PREF_SORT_BOOKMARKS, sortOrder).apply();
  }

  public boolean getBookmarksGroupedByTags() {
    return mPrefs.getBoolean(Constants.PREF_GROUP_BOOKMARKS_BY_TAG, true);
  }

  public void setBookmarksGroupedByTags(boolean groupedByTags) {
    mPrefs.edit().putBoolean(Constants.PREF_GROUP_BOOKMARKS_BY_TAG, groupedByTags).apply();
  }

  // probably should eventually move this to Application.onCreate..
  public void upgradePreferences() {
    int version = getVersion();
    if (version != BuildConfig.VERSION_CODE) {
      if (version == 0) {
        version = mPrefs.getInt(Constants.PREF_VERSION, 0);
      }

      if (version != 0) {
        if (version < 2672) {
          // migrate preferences
          setAppCustomLocation(mPrefs.getString(Constants.PREF_APP_LOCATION, null));

          if (mPrefs.contains(Constants.PREF_SHOULD_FETCH_PAGES)) {
            setShouldFetchPages(mPrefs.getBoolean(Constants.PREF_SHOULD_FETCH_PAGES, false));
          }


          if (mPrefs.contains(Constants.PREF_ACTIVE_TRANSLATION)) {
            setActiveTranslation(mPrefs.getString(Constants.PREF_ACTIVE_TRANSLATION, null));
          }

          mPrefs.edit()
              .remove(Constants.PREF_VERSION)
              .remove(Constants.PREF_APP_LOCATION)
              .remove(Constants.PREF_SHOULD_FETCH_PAGES)
              .remove(Constants.PREF_ACTIVE_TRANSLATION)
                  // these aren't migrated since they can be derived pretty easily
              .remove("didPresentPermissionsRationale") // was renamed, removing old one
              .remove(Constants.PREF_DEFAULT_IMAGES_DIR)
              .remove(Constants.PREF_HAVE_UPDATED_TRANSLATIONS)
              .remove(Constants.PREF_LAST_UPDATED_TRANSLATIONS)
              .apply();
        } else if (version < 2674) {
          // explicitly an else - if we migrated via the above, we're okay. otherwise, we are in
          // a bad state due to not crashing in 2.6.7-p2 (thus getting its incorrect behavior),
          // and thus crashing on 2.6.7-p3 and above (where the bug was fixed). this works around
          // this issue.
    //      try {
    //        getLastDownloadItemWithError();
     //       getLastDownloadErrorCode();
    //      } catch (Exception e) {
    //        clearLastDownloadError();
          }
        }
      }

      // no matter which version we're upgrading from, make sure the app location is set
      if (!isAppLocationSet()) {
        setAppCustomLocation(getAppCustomLocation());
      }

      // make sure that the version code now says that we're up to date.
      setVersion(BuildConfig.VERSION_CODE);
    }


  public boolean didPresentSdcardPermissionsDialog() {
    return mPerInstallationPrefs.getBoolean(Constants.PREF_DID_PRESENT_PERMISSIONS_DIALOG, false);
  }

  public void setSdcardPermissionsDialogPresented() {
    mPerInstallationPrefs.edit()
        .putBoolean(Constants.PREF_DID_PRESENT_PERMISSIONS_DIALOG, true).apply();
  }

  public String getAppCustomLocation() {
    return mPerInstallationPrefs.getString(Constants.PREF_APP_LOCATION,
        Environment.getExternalStorageDirectory().getAbsolutePath());
  }

  public void setAppCustomLocation(String newLocation) {
    mPerInstallationPrefs.edit().putString(Constants.PREF_APP_LOCATION, newLocation).apply();
  }

  public boolean isAppLocationSet() {
    return mPerInstallationPrefs.getString(Constants.PREF_APP_LOCATION, null) != null;
  }

  public String getActiveTranslation() {
    return mPerInstallationPrefs.getString(Constants.PREF_ACTIVE_TRANSLATION, "quran.id.db");
  }

  public void setActiveTranslation(String translation) {
    mPerInstallationPrefs.edit().putString(Constants.PREF_ACTIVE_TRANSLATION, translation).apply();
  }

  public void removeActiveTranslation() {
    mPerInstallationPrefs.edit().remove(Constants.PREF_ACTIVE_TRANSLATION).apply();
  }

  public int getVersion() {
    return mPerInstallationPrefs.getInt(Constants.PREF_VERSION, 0);
  }

  public void setVersion(int version) {
    mPerInstallationPrefs.edit().putInt(Constants.PREF_VERSION, version).apply();
  }
  public Typeface getFontArab(Context context){
    final Typeface face;
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    final String value = sharedPreferences.getString("huruf", null);
    if (value == null) {
      face = Typeface.createFromAsset(context.getAssets(), "font/quran.ttf");
    } else {
      face = Typeface.createFromAsset(context.getAssets(), "font/" + value);
    }
    return face;
  }
  public Typeface getFontQuran(Context context){
    return   Typeface.createFromAsset(context.getAssets(), "font/quran.ttf");
  }
  public boolean shouldFetchPages() {
    return mPerInstallationPrefs.getBoolean(Constants.PREF_SHOULD_FETCH_PAGES, false);
  }

  public void setShouldFetchPages(boolean shouldFetchPages) {
    mPerInstallationPrefs.edit().putBoolean(Constants.PREF_SHOULD_FETCH_PAGES, shouldFetchPages).apply();
  }

  public void removeShouldFetchPages() {
    mPerInstallationPrefs.edit().remove(Constants.PREF_SHOULD_FETCH_PAGES).apply();
  }

  public boolean haveUpdatedTranslations() {
    return mPerInstallationPrefs.getBoolean(Constants.PREF_HAVE_UPDATED_TRANSLATIONS, false);
  }

  public void setHaveUpdatedTranslations(boolean haveUpdatedTranslations) {
    mPerInstallationPrefs.edit().putBoolean(Constants.PREF_HAVE_UPDATED_TRANSLATIONS,
        haveUpdatedTranslations).apply();
  }

  public long getLastUpdatedTranslationDate() {
    return mPerInstallationPrefs.getLong(Constants.PREF_LAST_UPDATED_TRANSLATIONS,
        System.currentTimeMillis());
  }

  public void setLastUpdatedTranslationDate(long date) {
    mPerInstallationPrefs.edit().putLong(Constants.PREF_LAST_UPDATED_TRANSLATIONS, date).apply();
  }

  public void setLastDownloadError(String lastDownloadItem, int lastDownloadError) {
    mPerInstallationPrefs.edit()
            .putInt(QuranDownloadService.PREF_LAST_DOWNLOAD_ERROR, lastDownloadError)
            .putString(QuranDownloadService.PREF_LAST_DOWNLOAD_ITEM, lastDownloadItem)
            .apply();
  }

  public void clearLastDownloadError() {
    mPerInstallationPrefs.edit()
            .remove(QuranDownloadService.PREF_LAST_DOWNLOAD_ERROR)
            .remove(QuranDownloadService.PREF_LAST_DOWNLOAD_ITEM)
            .apply();
  }



  public boolean haveDefaultImagesDirectory() {
    return mPerInstallationPrefs.contains(Constants.PREF_DEFAULT_IMAGES_DIR);
  }

  public void setDefaultImagesDirectory(String directory) {
    mPerInstallationPrefs.edit().putString(Constants.PREF_DEFAULT_IMAGES_DIR, directory).apply();
  }

  public String getDefaultImagesDirectory() {
    return mPerInstallationPrefs.getString(Constants.PREF_DEFAULT_IMAGES_DIR, "");
  }
}
