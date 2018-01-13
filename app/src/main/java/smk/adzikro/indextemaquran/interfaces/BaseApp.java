package smk.adzikro.indextemaquran.interfaces;

import android.content.Context;

/**
 * Created by server on 12/17/17.
 */

public interface BaseApp {
    interface View{
        Context getAppContext();
    }
    interface Presenter<T extends  View>{
        void subscribe(T view, int page);
        void unSubscribe();
    }
}
