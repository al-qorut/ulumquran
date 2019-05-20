package smk.adzikro.indextemaquran.object;

/**
 * Created by server on 1/18/18.
 */

public class Tema {
    public int id, parent;
    public String title;
    public Tema(int id, int parent, String title){
        this.id=id;
        this.parent = parent;
        this.title = title;
    }
}
