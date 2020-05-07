package smk.adzikro.indextemaquran.object;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import smk.adzikro.indextemaquran.constans.BaseQuranInfo;

/**
 * Created by server on 1/1/18.
 */

public class QuranInfo {
    public final int sura;
    public final int ayah;
    public final int ayahId;
    private boolean bookmark=false;



    @Nullable
    public final String arabicText;
    @NonNull
    public final List<String> texts;
    @NonNull
    public final List<QuranLafdzi> lafdzi;

    public QuranInfo(int sura,
                         int ayah,
                         @Nullable String arabicText,
                         @NonNull List<String> texts,
                        @NonNull List<QuranLafdzi> lafdzi) {
        this.sura = sura;
        this.ayah = ayah;
        this.arabicText = arabicText;
        this.texts = Collections.unmodifiableList(texts);
        this.ayahId = BaseQuranInfo.getAyahId(sura, ayah);
        this.lafdzi = lafdzi;
    }
    public boolean isBookmark() {
        return bookmark;
    }

    public void setBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }
}
