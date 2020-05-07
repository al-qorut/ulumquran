package smk.adzikro.indextemaquran.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.Html;
import android.util.Log;


import androidx.annotation.NonNull;

import java.util.List;
import smk.adzikro.indextemaquran.BuildConfig;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.db.DatabaseHandler;
import smk.adzikro.indextemaquran.db.QuranDataLocal;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.ui.QuranUtils;
import smk.adzikro.indextemaquran.util.QuranFileUtils;
import timber.log.Timber;

public class QuranDataProvider extends ContentProvider {

  private static final String TAG =QuranDataProvider.class.getSimpleName() ;
  public static String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.QuranDataProvider";
  public static final Uri SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/quran/search");

  public static final String VERSES_MIME_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.smk.adzikro.indextemaquran";
  public static final String QURAN_ARABIC_DATABASE = QuranFileConstants.ARABIC_DATABASE;

  // UriMatcher stuff
  private static final int SEARCH_VERSES = 0;
  private static final int SEARCH_SUGGEST = 1;
  private static final UriMatcher uriMatcher = buildUriMatcher();
  private QuranDataLocal translationsDBAdapter;

  private static UriMatcher buildUriMatcher() {
    UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    matcher.addURI(AUTHORITY, "quran/search", SEARCH_VERSES);
    matcher.addURI(AUTHORITY, "quran/search/*", SEARCH_VERSES);
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    return matcher;
  }

  @Override
  public boolean onCreate() {
    translationsDBAdapter = new QuranDataLocal(getContext());
    return true;
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    Context context = getContext();


  //  Crashlytics.log("uri: " + uri.toString());
    switch (uriMatcher.match(uri)) {
      case SEARCH_SUGGEST: {
        if (selectionArgs == null) {
          throw new IllegalArgumentException(
              "selectionArgs must be provided for the Uri: " + uri);
        }

        return getSuggestions(selectionArgs[0]);
      }
      case SEARCH_VERSES: {
        if (selectionArgs == null) {
          throw new IllegalArgumentException(
              "selectionArgs must be provided for the Uri: " + uri);
        }

        return search(selectionArgs[0]);
      }
      default: {
        throw new IllegalArgumentException("Unknown Uri: " + uri);
      }
    }
  }

  private Cursor search(String query) {
    return search(query, getAvailableTranslations());
  }

  private List<QuranSource> getAvailableTranslations() {
    return translationsDBAdapter.getTranslations();
  }

  private Cursor getSuggestions(String query) {
    if (query.length() < 3) {
      return null;
    }

    final boolean queryIsArabic = QuranUtils.doesStringContainArabic(query);
    final boolean haveArabic = queryIsArabic &&
        QuranFileUtils.hasTranslation(getContext(), QURAN_ARABIC_DATABASE);

    List<QuranSource> translations = getAvailableTranslations();
    if (translations.size() == 0 && (queryIsArabic && !haveArabic)) {
      return null;
    }
    int total = translations.size();
    int start = haveArabic ? -1 : 0;

    String[] cols = new String[] { BaseColumns._ID,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_TEXT_2,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
    MatrixCursor mc = new MatrixCursor(cols);

    Context context = getContext();
    boolean gotResults = false;
    for (int i = start; i < total; i++) {
      if (gotResults) {
        continue;
      }

      String database;
      if (i < 0) {
        database = QURAN_ARABIC_DATABASE;
      } else {
        QuranSource quran = translations.get(i);
        // skip non-arabic databases if the query is in arabic
        if (queryIsArabic &&
            quran.languageCode != null &&
            !"ar".equals(quran.languageCode)) {
          continue;
        } else if (!queryIsArabic && "ar".equals(quran.languageCode)) {
          // skip arabic databases when the query isn't arabic
          continue;
        }
        database = quran.file_name;
      }

      Cursor suggestions = null;
      try {
        suggestions = search(query, database, false);
        if (suggestions != null && suggestions.moveToFirst()) {
          do {
            int sura = suggestions.getInt(1);
            int ayah = suggestions.getInt(2);
            String text = suggestions.getString(3);
            String foundText = context.getString(
                R.string.found_in_sura, BaseQuranInfo.getSuraName(context, sura, false), ayah);

            gotResults = true;
            MatrixCursor.RowBuilder row = mc.newRow();
            int id = suggestions.getInt(0);

            row.add(id);
            row.add(Html.fromHtml(text));
            row.add(foundText);
            row.add(id);
          } while (suggestions.moveToNext());
        }
      } finally {
        suggestions.close();
      }
    }

    return mc;
  }

  private Cursor search(String query, List<QuranSource> translations) {
    Timber.d("query: %s", query);

    final Context context = getContext();
    final boolean queryIsArabic = QuranUtils.doesStringContainArabic(query);
    final boolean haveArabic = queryIsArabic &&
        QuranFileUtils.hasTranslation(context, QURAN_ARABIC_DATABASE);
    if (translations.size() == 0 && (queryIsArabic && !haveArabic)) {
      return null;
    }

    int start = haveArabic ? -1 : 0;
    int total = translations.size();

    for (int i = start; i < total; i++) {
      String databaseName;
      if (i < 0) {
        databaseName = QURAN_ARABIC_DATABASE;
      } else {
        QuranSource translation = translations.get(i);
        // skip non-arabic databases if the query is in arabic
        if (queryIsArabic &&
            translation.languageCode != null &&
            !"ar".equals(translation.languageCode)) {
          continue;
        } else if (!queryIsArabic && "ar".equals(translation.languageCode)) {
          // skip arabic databases when the query isn't arabic
          continue;
        }
        databaseName = translation.getFile_name();
        Log.e(TAG,"Databasenya looping "+databaseName);
      }

      Cursor cursor = search(query, databaseName, true);
      if (cursor != null && cursor.getCount() > 0) {
        return cursor;
      }
    }
    return null;
  }

  private Cursor search(String query, String databaseName, boolean wantSnippets) {
    Log.e(TAG,"Databasenya "+databaseName);
    final DatabaseHandler handler = DatabaseHandler.getDatabaseHandler(getContext(), databaseName);
    return handler.search(query, wantSnippets);
  }

  @Override
  public String getType(@NonNull Uri uri) {
    switch (uriMatcher.match(uri)) {
      case SEARCH_VERSES: {
        return VERSES_MIME_TYPE;
      }
      case SEARCH_SUGGEST: {
        return SearchManager.SUGGEST_MIME_TYPE;
      }
      default: {
        throw new IllegalArgumentException("Unknown URL " + uri);
      }
    }
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }
}
