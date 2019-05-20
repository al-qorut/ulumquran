package smk.adzikro.indextemaquran.object;

import java.util.List;

/**
 * Created by server on 1/18/18.
 */

public class BundleTema {
    public List<Tema> parent;
    public List<Tema> child;
    public BundleTema(List<Tema> ortu, List<Tema> anak){
        this.parent=ortu;
        this.child = anak;
    }
}
