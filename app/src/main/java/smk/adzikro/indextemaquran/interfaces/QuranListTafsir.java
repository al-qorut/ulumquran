package smk.adzikro.indextemaquran.interfaces;

import android.util.Log;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import smk.adzikro.indextemaquran.db.QuranApi;
import smk.adzikro.indextemaquran.object.QuranAyah;
import smk.adzikro.indextemaquran.object.QuranInfo;

/**
 * Created by server on 12/17/17.
 */

public class QuranListTafsir implements QuranListContrack.Presenter {
    private static final String TAG ="QuranListTafsir" ;
    private QuranListContrack.View mView;
    private CompositeDisposable compositeDisposable;
    private int page;

    @Override
    public void subscribe(QuranListContrack.View view, int page) {
        Log.e(TAG, "subscribe");
        this.mView =view;
        this.page = page;
        compositeDisposable = new CompositeDisposable();
        getListQuran();
    }

    @Override
    public void unSubscribe() {
        mView = null;
        if(compositeDisposable!=null)
            compositeDisposable.dispose();
    }


    private void getListQuran(){
        Log.e(TAG, "getListQuran");
       Disposable disposable = QuranApi.getsInstance(mView.getAppContext())
               .getQuran(page)
               .subscribe(new Consumer<QuranApi.BundleQuranInfo>() {
                   @Override
                   public void accept(QuranApi.BundleQuranInfo qurans) throws Exception {
                        mView.displayQuran(qurans);
                   }
               });
       compositeDisposable.add(disposable);
    }

    @Override
    public void disPlayTafsir(List<QuranAyah> qurans) {

    }
}
