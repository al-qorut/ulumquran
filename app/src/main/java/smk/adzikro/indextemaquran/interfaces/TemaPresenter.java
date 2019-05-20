package smk.adzikro.indextemaquran.interfaces;



import java.util.List;

import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.BundleTema;
import smk.adzikro.indextemaquran.object.Tema;

/**
 * Created by server on 1/18/18.
 */

public interface TemaPresenter {
    interface View extends BaseApp.View{
        void OnLoadTema(List<Tema> temaList);
        void OnLoadAyat(Ayah ayah);
        void OnLongLoadAyat(List<Ayah> ayah);
    }
    interface Presenter extends BaseApp.Presenter<TemaPresenter.View>{
        void OnClickAyat(Ayah ayah);
        void OnLongClickAyat(String title);
        void OnClickTema(String tema);
    }
}
