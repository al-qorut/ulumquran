package smk.adzikro.indextemaquran.object;

/**
 * Created by server on 1/18/18.
 */

public class Ayah {
    public int sura;
    public int ayat;
    public String arab;
    public String arti;
    public Ayah(int sura, int ayat){
        this.sura = sura;
        this.ayat = ayat;
    }

    public String getArab() {
        return arab;
    }

    public void setArab(String arab) {
        this.arab = arab;
    }

    public String getArti() {
        return arti;
    }

    public void setArti(String arti) {
        this.arti = arti;
    }
}
