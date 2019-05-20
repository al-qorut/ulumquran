package smk.adzikro.indextemaquran.interfaces;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import smk.adzikro.indextemaquran.db.QuranApi;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.Tema;

/**
 * Created by server on 1/18/18.
 */

public class TemaList implements TemaPresenter.Presenter {

    private CompositeDisposable compositeDisposable;
    TemaPresenter.View mView;

    @Override
    public void subscribe(TemaPresenter.View view, int page) {
        this.mView =view;
        compositeDisposable = new CompositeDisposable();
        triggerData(""+page);
    }

    private void triggerData(String s){
        Disposable disposable = QuranApi.getsInstance(mView.getAppContext())
                .getTema(s)
                .subscribe(temaList -> mView.OnLoadTema(temaList));
        compositeDisposable.add(disposable);
    }
    @Override
    public void unSubscribe() {
        mView = null;
        if(compositeDisposable!=null)
            compositeDisposable.dispose();
    }

    @Override
    public void OnClickAyat(Ayah ayah) {
        compositeDisposable = new CompositeDisposable();
        ambilAyat(ayah);
    }

    @Override
    public void OnLongClickAyat(String title) {
        compositeDisposable = new CompositeDisposable();
        ambilListAyat(title);
    }

    @Override
    public void OnClickTema(String tema) {
        compositeDisposable = new CompositeDisposable();
        triggerData(tema);
    }

    private void ambilAyat(Ayah ayah){
        Disposable disposable = QuranApi.getsInstance(mView.getAppContext())
                .getAyat(ayah)
                .subscribe(ayah1 -> mView.OnLoadAyat(ayah1));
        compositeDisposable.add(disposable);
    }

    private void ambilListAyat(String title){
        Disposable disposable = QuranApi.getsInstance(mView.getAppContext())
                .getListTema(title)
                .subscribe(ayah1 -> mView.OnLongLoadAyat(ayah1));
        compositeDisposable.add(disposable);
    }
}
