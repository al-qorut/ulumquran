package smk.adzikro.indextemaquran.interfaces;


import java.util.List;

import smk.adzikro.indextemaquran.db.QuranApi;
import smk.adzikro.indextemaquran.object.QuranAyah;
import smk.adzikro.indextemaquran.object.QuranInfo;

/**
 * Created by server on 12/17/17.
 */

public interface QuranListContrack {
    interface View extends BaseApp.View{
        void displayQuran(QuranApi.BundleQuranInfo quranlist);
    }
    interface Presenter extends BaseApp.Presenter<View>{
        void disPlayTafsir(List<QuranAyah> qurans);
    }
}
