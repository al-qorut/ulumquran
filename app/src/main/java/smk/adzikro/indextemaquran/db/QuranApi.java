package smk.adzikro.indextemaquran.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.object.QuranAyah;
import smk.adzikro.indextemaquran.object.QuranInfo;
import smk.adzikro.indextemaquran.object.QuranLafdzi;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.object.QuranText;
import smk.adzikro.indextemaquran.object.VerseRange;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.util.Fungsi;


/**
 * Created by server on 12/17/17.
 */

public class QuranApi {
    private static final String TAG = QuranApi.class.getSimpleName();
    private static QuranApi sInstance;
    private DatabaseHandler mQuranHelper;
    private QuranSettings setting;
    private Context context;

    private QuranApi(Context context){
        this.context = context;
        setting = QuranSettings.getInstance(context);

    }

    public static QuranApi getsInstance(Context context){
        Log.e(TAG,"QuranApi OnCreate");
        if(sInstance==null){
            sInstance = new QuranApi(context);
        }
        return sInstance;
    }


    public Observable<BundleQuranInfo> getQuran(final int page){
        Observable<BundleQuranInfo> observable = Observable.create(e -> {
            Integer pages[] = BaseQuranInfo.getPageBounds(604-page);
            VerseRange verseRange = new VerseRange(pages[0],pages[1],pages[2],pages[3]);
            List<String> info = new ArrayList<>();
            List<QuranSource> sources = getListSource();
            List<List<QuranText>> listTextQuran = new ArrayList<>();
            List<List<QuranLafdzi>> listLafdi = new ArrayList<>();

            //Text Quran Arabic
            mQuranHelper = DatabaseHandler.getDatabaseHandler(context, QuranFileConstants.ARABIC_DATABASE);
            List<QuranText> listTextArabic = mQuranHelper.getQuran(verseRange, DatabaseHandler.TextType.ARABIC);
            //ambil text ayat latin
            if(setting.isLatinDisplay()) {
                mQuranHelper = DatabaseHandler.getDatabaseHandler(context, QuranFileConstants.LATIN_DATABASE);
                List<QuranText> listTextLatin = mQuranHelper.getQuran(verseRange, DatabaseHandler.TextType.TRANSLATION);
                info.add("Transliteration");
                listTextQuran.add(listTextLatin);
            }

            if(sources.size()>0){
                for(int i=0; i<sources.size();i++){
                    QuranSource quranSource = sources.get(i);
                    if(!setting.isTafsirDisplay())
                        if(quranSource.getType()==1)continue;
                    if(!setting.isTranslateDisplay())
                        if(quranSource.getType()==0)continue;
                    mQuranHelper = DatabaseHandler.getDatabaseHandler(context, quranSource.getFile_name());
                    List<QuranText> listTranslate = mQuranHelper.getQuran(verseRange, DatabaseHandler.TextType.TRANSLATION);
                    listTextQuran.add(listTranslate);
                    info.add(quranSource.getDisplayName());
                }
            }

            if(setting.isLafdziDisplay()){
                mQuranHelper = DatabaseHandler.getDatabaseHandler(context, QuranFileConstants.LAFDZI_DATABASE );
                listLafdi = mQuranHelper.getLafadz(verseRange, listTextArabic, setting.getLafdziDisplay());
            }

            List<QuranInfo> gabung = combineAyahData(verseRange, listTextArabic, listTextQuran, listLafdi);
            BundleQuranInfo bundleQuran = new BundleQuranInfo(info,gabung);
            e.onNext(bundleQuran);
        });
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    List<QuranInfo> combineAyahData(@NonNull VerseRange verseRange,
                                    @NonNull List<QuranText> arabic,
                                    @NonNull List<List<QuranText>> texts,
                                    List<List<QuranLafdzi>> lafdzi) {
        Log.e(TAG,"combineAyahData "+lafdzi.size());
        final int arabicSize = arabic.size();
        final int translationCount = texts.size();
        List<QuranInfo> result = new ArrayList<>();
        if (translationCount > 0) { //Jumlah translate
            final int verses = arabicSize == 0 ? verseRange.versesInRange : arabicSize;

            for (int i = 0; i < verses; i++) {
                QuranText quran_text = arabicSize == 0 ? null : arabic.get(i);
                final List<String> ayahTranslations = new ArrayList<>();
                for (int j = 0; j < translationCount; j++) {
                    QuranText item = texts.get(j).size() > i ? texts.get(j).get(i) : null;
                    if (item != null) {
                        ayahTranslations.add(texts.get(j).get(i).text);
                        quran_text = item;
                //        Log.e(TAG,"Text Latina "+texts.get(j).get(i).text);
                    } else {
                        // this keeps the translations aligned with their translators
                        // even when a particular translator doesn't load.
                        ayahTranslations.add("");
                    }
                }

                if (quran_text != null) {
                    String arabicText = arabicSize == 0 ? null : arabic.get(i).text;
                    if(lafdzi.size()>0)
                        result.add(new QuranInfo(quran_text.sura, quran_text.ayah, arabicText, ayahTranslations, lafdzi.get(i)));
                    else
                        result.add(new QuranInfo(quran_text.sura, quran_text.ayah, arabicText, ayahTranslations, null));
                }
            }
        } else if (arabicSize > 0) {
            for (int i = 0; i < arabicSize; i++) {
                QuranText arabicItem = arabic.get(i);
                if(lafdzi.size()>0)
                result.add(new QuranInfo(arabicItem.sura, arabicItem.ayah,
                        arabicItem.text, Collections.emptyList(),lafdzi.get(i)));
                else
                    result.add(new QuranInfo(arabicItem.sura, arabicItem.ayah,
                            arabicItem.text, Collections.emptyList(),null));
            }
        }



        return result;
    }
    private void simpan(String s){
        try{
            FileOutputStream fileOutputStream =
                    new FileOutputStream(Fungsi.PATH_DATABASE()+"log");
            fileOutputStream.write(s.getBytes());
            fileOutputStream.flush();
        }catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }
    private List<QuranSource> getListSource(){
        SQLiteDatabase db = new QuranDataLocal(context).getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from quran where active=1",null);
        List<QuranSource> sources = new ArrayList<>();
        try {
            if(cursor.getCount()>0){
                while (cursor.moveToNext()) {
                    QuranSource q = new QuranSource(cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(5),
                            cursor.getString(4));
                    q.setTranslator_asing(cursor.getString(3));
                    q.setAda(cursor.getInt(6));
                    q.setType(cursor.getInt(7));
                    q.setActive(cursor.getInt(8));
                    q.setId(cursor.getInt(0));
                    File file = new File(Fungsi.PATH_DATABASE() + cursor.getString(5));
                    if (file.exists()) {
                        sources.add(q);
                    }
                }
            }
        }finally {
            db.close();
            cursor.close();
        }
        return sources;
    }
    public static class BundleQuranInfo{
        public List<String> info;
        public List<QuranInfo> quranAyahList;

        BundleQuranInfo(List<String> info, List<QuranInfo> quranAyahs){
            this.info = info;
            this.quranAyahList = quranAyahs;
        }

    }
}
