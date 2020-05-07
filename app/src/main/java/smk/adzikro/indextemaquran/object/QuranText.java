package smk.adzikro.indextemaquran.object;


import androidx.annotation.NonNull;

/**
 * Created by server on 1/1/18.
 */

public class QuranText {
    public final int sura;
    public final int ayah;
    @NonNull
    public final String text;

    public QuranText(int sura, int ayah, @NonNull String text) {
        this.sura = sura;
        this.ayah = ayah;
        this.text = text;
    }
}
