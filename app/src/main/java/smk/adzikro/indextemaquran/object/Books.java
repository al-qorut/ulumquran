package smk.adzikro.indextemaquran.object;

/**
 * Created by server on 1/19/18.
 */

public class Books {
    public int surat;
    public int ayat;
    public String isi;
    public String folder;

    public Books(int surat, int ayat, String folder){
        this.surat=surat;
        this.ayat=ayat;
        this.folder=folder;
    }
    public void setIsi(String isi){
        this.isi=isi;
    }

}
