package smk.adzikro.indextemaquran.ui;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import smk.adzikro.indextemaquran.object.QuranInfo;

/**
 * Created by server on 12/31/17.
 */

public class TranslationViewRow {
    @IntDef({ Type.BASMALLAH, Type.SURA_HEADER, Type.QURAN_TEXT, Type.TRANSLATOR,
            Type.TAFSIR_ARABIC, Type.VERSE_NUMBER, Type.LAFDZI , Type.TAFSIR_LATIN, Type.SPACER })
    public @interface Type {
        int BASMALLAH = 0;
        int SURA_HEADER = 1;
        int QURAN_TEXT = 2;
        int TRANSLATOR = 3;
        int TAFSIR_ARABIC = 4;
        int VERSE_NUMBER = 5;
        int LAFDZI = 6;
        int TAFSIR_LATIN = 7;
        int SPACER = 8;
    }
    @Type
    public final int type;
    @NonNull
    public final QuranInfo ayahInfo;
    @Nullable
    public final String data;

    public TranslationViewRow(int type, @NonNull QuranInfo ayahInfo) {
        this(type, ayahInfo, null);
    }

    public TranslationViewRow(int type, @NonNull QuranInfo ayahInfo, @Nullable String data) {
        this.type = type;
        this.ayahInfo = ayahInfo;
        this.data = data;
    }
}

