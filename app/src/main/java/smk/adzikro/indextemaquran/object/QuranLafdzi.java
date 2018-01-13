package smk.adzikro.indextemaquran.object;

import android.support.annotation.NonNull;

/**
 * Created by server on 1/6/18.
 */

public class QuranLafdzi {
    public final int sura;
    public final int ayah;
    @NonNull
    public final String arab;
    public final String arti;

    public QuranLafdzi(int sura, int ayah, @NonNull String text, String arti) {
        this.sura = sura;
        this.ayah = ayah;
        this.arab = text;
        this.arti = arti;
    }
}
