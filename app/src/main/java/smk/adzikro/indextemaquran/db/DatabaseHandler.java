package smk.adzikro.indextemaquran.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DefaultDatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;


import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.QuranLafdzi;
import smk.adzikro.indextemaquran.object.QuranText;
import smk.adzikro.indextemaquran.object.Tema;
import smk.adzikro.indextemaquran.object.VerseRange;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.util.QuranFileUtils;
import timber.log.Timber;


public class DatabaseHandler {
  private static final String COL_SURA = "sura";
  private static final String COL_AYAH = "ayah";
  private static final String COL_TEXT = "text";
  public static final String VERSE_TABLE = "verses";
  public static final String ARABIC_TEXT_TABLE = "arabic_text";
  public static final String LAFDZI_IN = "id";
  public static final String LAFDZI_EN = "en";
  private static final String PROPERTIES_TABLE = "properties";
  private static final String COL_PROPERTY = "property";
  private static final String COL_VALUE = "value";

  private static final String MATCH_END = "</font>";
  private static final String ELLIPSES = "<b>...</b>";
  private static final String TAG = "DatabaseHandler";

  private static Map<String, DatabaseHandler> databaseMap = new HashMap<>();

  private int schemaVersion = 1;
  private String matchString;
  private SQLiteDatabase database = null;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef( { TextType.ARABIC, TextType.TRANSLATION } )
  public @interface TextType {
    int ARABIC = 0;
    int TRANSLATION = 1;
  }

  public static synchronized DatabaseHandler getDatabaseHandler(
      Context context, String databaseName) {
   // Log.e(TAG, "onCreate");
    DatabaseHandler handler = databaseMap.get(databaseName);
    if (handler == null) {
      handler = new DatabaseHandler(context.getApplicationContext(), databaseName);
      databaseMap.put(databaseName, handler);
    }
    return handler;
  }

  private DatabaseHandler(Context context, String databaseName) throws SQLException {
    String base = QuranFileUtils.getQuranDatabaseDirectory(context);
   if(base == null) return;
    String path = Fungsi.PATH_DATABASE()+databaseName;//base + File.separator + databaseName;
    Log.e(TAG,"opening database file: " + databaseName);
    try {
      database = SQLiteDatabase.openDatabase(path, null,
        SQLiteDatabase.NO_LOCALIZED_COLLATORS, new DefaultDatabaseErrorHandler());
      Log.e(TAG,"sukses... opening database file: " + path);
    } catch (SQLiteDatabaseCorruptException sce) {
      Log.e(TAG,"corrupt database: " + databaseName);
      throw sce;
    } catch (SQLException se){
      Log.e(TAG,"database file " + path +
          (new File(path).exists()? " exists" : " doesn't exist"));
      throw se;
    }

    //schemaVersion = getSchemaVersion();
    matchString = "<font color=\"" +
        ContextCompat.getColor(context, R.color.translation_highlight) +
        "\">";
  }

  public boolean validDatabase() {
    return database != null && database.isOpen();
  }

  //private Cursor getVerses(int sura, int minAyah, int maxAyah) {
    //return getVerses(sura, minAyah, maxAyah, VERSE_TABLE);
  //}

  private int getProperty(@NonNull String column) {
    int value = 1;
    if (!validDatabase()) {
      return value;
    }

    Cursor cursor = null;
    try {
      cursor = database.query(PROPERTIES_TABLE, new String[]{ COL_VALUE },
          COL_PROPERTY + "= ?", new String[]{ column }, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        value = cursor.getInt(0);
      }
      return value;
    } catch (SQLException se) {
      return value;
    } finally {
      closeCursor(cursor);
    }
  }

  private int getSchemaVersion() {
    return getProperty("schema_version");
  }

  public int getTextVersion() {
    return getProperty("text_version");
  }

  //private Cursor getVerses(int sura, int minAyah, int maxAyah, String table) {
    //return getVerses(sura, minAyah, sura, maxAyah, table);
  //}


  public Cursor getVerses(int minSura, int minAyah, int maxSura,
                          int maxAyah, String table) {
    return getVersesInternal(new VerseRange(minSura, minAyah, maxSura, maxAyah), table);
  }



  public List<QuranText> getQuran(VerseRange verses, @TextType int textType) {
    Cursor cursor = null;
    List<QuranText> results = new ArrayList<>();
    try {
      String table = textType == TextType.ARABIC ? ARABIC_TEXT_TABLE : VERSE_TABLE;
      cursor = getVersesInternal(verses, table);
      while (cursor != null && cursor.moveToNext()) {
        int sura = cursor.getInt(1);
        int ayah = cursor.getInt(2);
        String text = cursor.getString(3);
     //   Log.e(TAG,text);
        QuranText quranAyah = new QuranText(sura, ayah, text);
        results.add(quranAyah);
      }
    } finally {
      closeCursor(cursor);
    }
    return results;
  }

  public List<String> getLafadzArabic(VerseRange verses, @TextType int textType){
    List<String> arab = new ArrayList<>();
    Cursor cursor = null;
    try {
      String table = textType == TextType.ARABIC ? ARABIC_TEXT_TABLE : VERSE_TABLE;
      cursor = getVersesInternal(verses, table);
      while (cursor != null && cursor.moveToNext()) {
        String text = cursor.getString(3);
         String pecah[] = TextUtils.split(text, " ");
         for(int i=0; i< pecah.length;i++) {
           Log.e(TAG, pecah[i]);
           arab.add(pecah[i]);
         }
      }
    } finally {
      closeCursor(cursor);
    }
    return arab;
  }

  public List<List<QuranLafdzi>> getLafadz(VerseRange verses, List<QuranText> quranText, String bahasa) {
    Cursor cursor = null;
    List<List<QuranLafdzi>> results = new ArrayList<>();
    cursor = getVersesLafdzi(verses, bahasa);
    for(int i=0; i< quranText.size();i++) {
        QuranText quran = quranText.get(i);
        List<QuranLafdzi> lafdzis = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
          int sura = cursor.getInt(1);
          int ayah = cursor.getInt(2);
          String arab = cursor.getString(3);
          String arti = cursor.getString(4);
          if(sura==quran.sura && ayah==quran.ayah) {
            QuranLafdzi lafdzi = new QuranLafdzi(sura, ayah, arab, arti);
            lafdzis.add(lafdzi);
          }
        }
        results.add(lafdzis);
        cursor.moveToFirst();
      }
      closeCursor(cursor);
    return results;
  }

 public List<Ayah> getListAyahTema(String tema){
   List<Ayah> ayahs = new ArrayList<>();
   Cursor cursor=database.rawQuery("select _id from TEMA where TEMA=\""+tema+"\"",null);
   try {
     if (cursor != null) {
       cursor.moveToNext();
       int id = cursor.getInt(0);
       cursor=database.rawQuery("select SURAT, AYAT from AYAT_TEMA where id="+id,null);
       if(cursor.getCount()>0){
         while (cursor.moveToNext()){
            Ayah ayah = new Ayah(cursor.getInt(0),cursor.getInt(1));
            ayahs.add(ayah);
         }
       }
     }
   }finally {
     closeCursor(cursor);
   }
   return ayahs;
 }

  private void closeCursor(Cursor cursor){
    if(cursor!=null){
      try {
        cursor.close();
      } catch (Exception e) {
        // no op
      }
    }
  }

  public List<Tema> getListTema(String title) {
    String query;
    List<Tema> alist = new ArrayList<>();
    Cursor cursor=null;
    try {
      if (title.equals("0")) {
        query = "select * from TEMA where PARENT=0";
        cursor = database.rawQuery(query, null);
        if (cursor != null) {
          while (cursor.moveToNext()) {
            Tema tema = new Tema(cursor.getInt(cursor.getColumnIndex("_id")),
                    cursor.getInt(cursor.getColumnIndex("PARENT")),
                    cursor.getString(cursor.getColumnIndex("TEMA")));
            alist.add(tema);
          }
        }
      } else {
        cursor = database.rawQuery("select _id from TEMA where TEMA=\"" + title + "\"", null);
        if (cursor != null && cursor.getCount() > 0) {
          cursor.moveToNext();
          int id = cursor.getInt(0);
          query = "select * from TEMA where PARENT=" + id;
          cursor = database.rawQuery(query, null);
          if (cursor != null && cursor.getCount()>0) {
            while (cursor.moveToNext()) {
              Tema tema = new Tema(cursor.getInt(cursor.getColumnIndex("_id")),
                      cursor.getInt(cursor.getColumnIndex("PARENT")),
                      cursor.getString(cursor.getColumnIndex("TEMA")));
              alist.add(tema);
            }
          }
          cursor=null;
          cursor = database.rawQuery("select * from AYAT_TEMA where id=" + id, null);
          if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
              Tema tema = new Tema(-1,
                      cursor.getInt(cursor.getColumnIndex("id")),
                      cursor.getInt(cursor.getColumnIndex("SURAT")) + ":" + cursor.getInt(cursor.getColumnIndex("AYAT")));
              alist.add(tema);
            }
          }
        }
      }
    }finally {
      closeCursor(cursor);
    }
    return alist;
  }

  public List<Tema> getListChild() {
    List<Tema> alist = new ArrayList<>();
    Cursor cursor = database.rawQuery("select * from AYAT_TEMA", null);
    if (cursor != null) {
      while (cursor.moveToNext()) {
        Tema tema = new Tema(-1,
                cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getString(cursor.getColumnIndex("SURAT"))+":"+cursor.getString(cursor.getColumnIndex("AYAT")));
        alist.add(tema);
      }
    }
    closeCursor(cursor);
    return alist;
  }

  private Cursor getVersesInternal(VerseRange verses, String table) {
    if (!validDatabase()) {
     //   Log.e(TAG,"DataBAse te valid");
        return null;
    }

    StringBuilder whereQuery = new StringBuilder();
    whereQuery.append("(");

    if (verses.startSura == verses.endingSura) {
      whereQuery.append(COL_SURA)
          .append("=").append(verses.startSura)
          .append(" and ").append(COL_AYAH)
          .append(">=").append(verses.startAyah)
          .append(" and ").append(COL_AYAH)
          .append("<=").append(verses.endingAyah);
    } else {
      // (sura = minSura and ayah >= minAyah)
      whereQuery.append("(").append(COL_SURA).append("=")
          .append(verses.startSura).append(" and ")
          .append(COL_AYAH).append(">=").append(verses.startAyah).append(")");

      whereQuery.append(" or ");

      // (sura = maxSura and ayah <= maxAyah)
      whereQuery.append("(").append(COL_SURA).append("=")
          .append(verses.endingSura).append(" and ")
          .append(COL_AYAH).append("<=").append(verses.endingAyah).append(")");

      whereQuery.append(" or ");

      // (sura > minSura and sura < maxSura)
      whereQuery.append("(").append(COL_SURA).append(">")
          .append(verses.startSura).append(" and ")
          .append(COL_SURA).append("<")
          .append(verses.endingSura).append(")");
    }

    whereQuery.append(")");
  //  Log.e(TAG," "+table+whereQuery.toString());
    return database.query(table,
        new String[] { "rowid as _id", COL_SURA, COL_AYAH, COL_TEXT },
        whereQuery.toString(), null, null, null,
        COL_SURA + "," + COL_AYAH);
  }

  private Cursor getVersesLafdzi(VerseRange verses, String bahasa) {
    final String COL_AYAH ="aya";
    final String COL_ARAB ="hi";
    final String COL_INDEX ="word";
    if (!validDatabase()) {
      return null;
    }
    StringBuilder whereQuery = new StringBuilder();
    whereQuery.append("(");

    if (verses.startSura == verses.endingSura) {
      whereQuery.append(COL_SURA)
              .append("=").append(verses.startSura)
              .append(" and ").append(COL_AYAH)
              .append(">=").append(verses.startAyah)
              .append(" and ").append(COL_AYAH)
              .append("<=").append(verses.endingAyah);
    } else {
      // (sura = minSura and ayah >= minAyah)
      whereQuery.append("(").append(COL_SURA).append("=")
              .append(verses.startSura).append(" and ")
              .append(COL_AYAH).append(">=").append(verses.startAyah).append(")");

      whereQuery.append(" or ");

      // (sura = maxSura and ayah <= maxAyah)
      whereQuery.append("(").append(COL_SURA).append("=")
              .append(verses.endingSura).append(" and ")
              .append(COL_AYAH).append("<=").append(verses.endingAyah).append(")");

      whereQuery.append(" or ");

      // (sura > minSura and sura < maxSura)
      whereQuery.append("(").append(COL_SURA).append(">")
              .append(verses.startSura).append(" and ")
              .append(COL_SURA).append("<")
              .append(verses.endingSura).append(")");
    }

    whereQuery.append(")");

    return database.query("quran",
            new String[] { "rowid as _id ", COL_SURA, COL_AYAH, COL_ARAB, bahasa },
            whereQuery.toString(), null, null, null,
            COL_SURA+","+COL_AYAH);
  }
  public String getAyahText(int sura, int ayah, @TextType int textType) {
    String table = textType == TextType.ARABIC ? ARABIC_TEXT_TABLE : VERSE_TABLE;
    Cursor cursor = database.query(table,
            new String[]{"rowid as _id", COL_SURA, COL_AYAH, COL_TEXT},
            COL_SURA + "=" + sura + " and " + COL_AYAH + "=" + ayah, null, null, null,null);
    String text="";
    try {
      while (cursor != null && cursor.moveToNext()) {
        text = cursor.getString(3);
      }
    } finally {
      closeCursor(cursor);
    }
    return text;
  }

  public Cursor getVersesByIds(List<Integer> ids) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0, idsSize = ids.size(); i < idsSize; i++) {
      if (i > 0) {
        builder.append(",");
      }
      builder.append(ids.get(i));
    }

    Timber.d("querying verses by ids for tags...");
    final String sql = "SELECT rowid as _id, " + COL_SURA + ", " + COL_AYAH + ", " + COL_TEXT +
        " FROM " + ARABIC_TEXT_TABLE + " WHERE rowid in(" + builder.toString() + ")";
    return database.rawQuery(sql, null);
  }

  public Cursor search(String query, boolean withSnippets) {
    return search(query, VERSE_TABLE, withSnippets);
  }

  public Cursor search(String q, String table, boolean withSnippets) {
    if (!validDatabase()) {
        return null;
    }

    final String limit = withSnippets ? "" : "LIMIT 25";

    String query = q;
    String operator = " like ";
    String whatTextToSelect = COL_TEXT;

    boolean useFullTextIndex = (schemaVersion > 1);
    if (useFullTextIndex) {
      operator = " MATCH ";
      query = query + "*";
    } else {
      query = "%" + query + "%";
    }

    int pos = 0;
    int found = 0;
    boolean done = false;
    while (!done) {
      int quote = query.indexOf("\"", pos);
      if (quote > -1) {
        found++;
        pos = quote + 1;
      } else {
        done = true;
      }
    }

    if (found % 2 != 0) {
      query = query.replaceAll("\"", "");
    }

    if (useFullTextIndex && withSnippets) {
      whatTextToSelect = "snippet(" + table + ", '" +
          matchString + "', '" + MATCH_END +
          "', '" + ELLIPSES + "', -1, 64)";
    }

    String qtext = "select rowid as " + BaseColumns._ID + ", " + COL_SURA + ", " + COL_AYAH +
        ", " + whatTextToSelect + " from " + table + " where " + COL_TEXT +
        operator + " ? " + " " + limit;
    Log.e(TAG,"search query: " + qtext + ", query: " + query);

    try {
      return database.rawQuery(qtext, new String[]{ query });
    } catch (Exception e){
      Log.e(TAG,e.getMessage());
      return null;
    }
  }
}
