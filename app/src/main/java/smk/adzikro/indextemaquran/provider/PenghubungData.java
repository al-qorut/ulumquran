package smk.adzikro.indextemaquran.provider;

/**
 * Created by server on 11/7/15.
 */
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import smk.adzikro.indextemaquran.db.QuranDatabase;

/**
 * utk akses ke kamus database.
 * class 'PenghubungData' ini bertindak sebagai
 * perantara antara database dan aplikasi
 */
public class PenghubungData extends ContentProvider {
    String TAG = "PenyediaData";
    // tunjukan alamat database
    public static String AUTHORITY = "smk.adzikro.indextemaquran.provider.PenghubungData";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/indexquran");

    // MIME types (multi-media/multi-purpose internet mail extension) utk
    // mencari KATA dan Arti_NYA
    public static final String MIME_TYPE_KATA = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/smk.adzikro.indextemaquran";
    public static final String MIME_TYPE_ARTI_NYA = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/smk.adzikro.indextemaquran";
    public static final String MIME_TYPE_ALAMAT_NYA = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/smk.adzikro.indextemaquran";

    private QuranDatabase QuranKu;

    // untuk mencocokan lokasi resource-nya
    // melalui URI(universal resource identifier)
    private static final int KLIK_PD_KATA = 0;
    private static final int DAPAT_ARTI_KATA = 1;
    private static final int KATA_YG_KELUAR = 2;
    private static final int REFRESH_SHORTCUT = 3;
    private static final int ALAMAT_KATA = 4;
    private static final UriMatcher cocokan_URI_nya = buatkan_UriMatcher_nya();


    private static UriMatcher buatkan_UriMatcher_nya() {
        UriMatcher mencocokanURI =  new UriMatcher(UriMatcher.NO_MATCH);
        //  klik pd kata agar artinya muncul
        mencocokanURI.addURI(AUTHORITY, "indexquran", KLIK_PD_KATA);
        mencocokanURI.addURI(AUTHORITY, "indexquran/#", DAPAT_ARTI_KATA);
        // kata-kata yang keluar saat ketik di kotak search
        mencocokanURI.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, KATA_YG_KELUAR);
        mencocokanURI.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", KATA_YG_KELUAR);

        /* berikut tak di pakai, tapi kalau kita pakai
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} sebagai
         * kolom di table, maka kita akan menerima
         * refresh queries ketika a shortcutted suggestion di
         * tayangkan di Quick Search Box, yg dalam hal ini,
         * URI berikut di sediakan dan kita akan
         * mengembalikan kursor dengan single item yg
         * merepresentasikan sugesti dat yang telah di refresh.
         */
        mencocokanURI.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        mencocokanURI.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        return mencocokanURI;
    }

    @Override
    public boolean onCreate() {
        QuranKu = new QuranDatabase(getContext());
        return true;
    }

    /**
     * utk menghandle semua pencarian dan kata2 apa yang di perkirakan akan
     * muncul dari Search Manager.
     * ketika mencari kata yang telah kita ketahui, hanya URI akan di perlukan.
     * ketika mencari semua kata2 atau tak jelas kata apa
     * yang mau di cari, maka 'pilihDariSemuaKataYgAda' harus sebagai
     * elemen pertama untuk search query.
     * semua argumen lain-nya akan di kesampingkan/tak dipedulikan.
     */
    @Override
    public void shutdown(){
        if (QuranKu!=null){
            QuranKu.close();
        }
        super.shutdown();
    }
    @Override
    public Cursor query(Uri uri, String[] perkiraanKata, String pilihan, String[] pilihDariSemuaKataYgAda,
                        String menurutAbjad) {

        // gunakan UriMatcher utk melihat query macam
        // apa saja yang kita butuhkan sehingga
        // DB query dapat di format sesuai kebutuhan
        switch (cocokan_URI_nya.match(uri)) {
            case KATA_YG_KELUAR:
                if (pilihDariSemuaKataYgAda == null) {
                    throw new IllegalArgumentException(
                            "tidak ada kata yg mau di pilih di: " + uri);
                }
                return getSuggestions(pilihDariSemuaKataYgAda[0]);
            case KLIK_PD_KATA:
                if (pilihDariSemuaKataYgAda == null) {
                    throw new IllegalArgumentException(
                            "tidak ada data yg mau di pilih di: " + uri);
                }
                return search(pilihDariSemuaKataYgAda[0]);
            case DAPAT_ARTI_KATA:
                return getWord(uri);
            case REFRESH_SHORTCUT:
                return refreshShortcut(uri);
            default:
                throw new IllegalArgumentException("tak mengenal alamat Uri: " + uri);
        }
    }

    private Cursor getSuggestions(String deretanKataYgMuncul) {
        deretanKataYgMuncul = deretanKataYgMuncul.toLowerCase();
        String[] kolom_nya = new String[] {
                BaseColumns._ID,
                QuranDatabase.FIELD_GUNDUL,
                QuranDatabase.FIELD_ARAB,
                QuranDatabase.FIELD_IND,
                QuranDatabase.FIELD_AYAT,
                QuranDatabase.FIELD_SURAT,
  //              KamusDatabase.ALAMAT,
       /* SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                        (hanya jikalau short cut-nya butuh di refresh) */
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

        return QuranKu.getWordMatches(deretanKataYgMuncul, kolom_nya);
    }

    private Cursor search(String kataYgDiCari) {
        kataYgDiCari = kataYgDiCari.toLowerCase();
        String[] kolom_nya = new String[] {
                BaseColumns._ID,
                QuranDatabase.FIELD_GUNDUL,
                QuranDatabase.FIELD_ARAB,
                QuranDatabase.FIELD_IND,
                QuranDatabase.FIELD_SURAT,
                QuranDatabase.FIELD_AYAT
               };

        return QuranKu.getWordMatches(kataYgDiCari, kolom_nya);
    }

    private Cursor getWord(Uri uri) {
        String rowId = uri.getLastPathSegment();
        String[] columns = new String[] {
                QuranDatabase.FIELD_GUNDUL,
                QuranDatabase.FIELD_ARAB,
                QuranDatabase.FIELD_IND,
                QuranDatabase.FIELD_SURAT,
                QuranDatabase.FIELD_AYAT};

        return QuranKu.getWord(rowId, columns);
    }

    private Cursor refreshShortcut(Uri uri) {
      /* berikut takan di gunakan pada
       * implementasi ini, tapi kalau kitan mengikut sertakan
       * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} sebagai sebuah
       * kolom suggestions table, kita akan menerima refresh queries
       * ketika sebuah shortcutted suggestion di tunjukan di kotak Quick Search
       * dalam arti, methode ini  akan melihat kedalam table utk kata tertentu
       * menggunakan item dalam URI dan menyediakan
       * semua kolom yang tadinya di sediakan untuk
       * suggestion query.
       */
        String barisId = uri.getLastPathSegment();
        String[] semuaKolom = new String[] {
                BaseColumns._ID,
                QuranDatabase.FIELD_GUNDUL,
                QuranDatabase.FIELD_ARAB,
                QuranDatabase.FIELD_IND,
                QuranDatabase.FIELD_SURAT,
                QuranDatabase.FIELD_AYAT,
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2};

        return QuranKu.getWord(barisId, semuaKolom);
    }

    /**
     * metode berikut di perlukan agar
     * dapat men-query tipe query yang di dukung.
     * dan juga berguna dalam metode query() kita sendiri agar
     * mengetahui type URI yang di terima.
     */
    @Override
    public String getType(Uri uri) {
        switch (cocokan_URI_nya.match(uri)) {
            case KLIK_PD_KATA:
                return MIME_TYPE_KATA;
            case DAPAT_ARTI_KATA:
                return MIME_TYPE_ARTI_NYA;
            case ALAMAT_KATA:
                return MIME_TYPE_ALAMAT_NYA;
            case KATA_YG_KELUAR:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    // implementasi lain yang di butuhkan

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}
