package smk.adzikro.indextemaquran.interfaces;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import smk.adzikro.indextemaquran.db.BookmarkHelper;
import smk.adzikro.indextemaquran.object.Books;

/**
 * Created by server on 1/19/18.
 */

public class BookList implements BookPresenter.Presenter {
    private CompositeDisposable compositeDisposable;
    private BookPresenter.View mView;

    @Override
    public void subscribe(BookPresenter.View view, int page) {
        this.mView =view;
        compositeDisposable = new CompositeDisposable();
        getData();
    }

    @Override
    public void unSubscribe() {
        mView = null;
        if(compositeDisposable!=null)
            compositeDisposable.dispose();
    }

    private void getData(){
        BookmarkHelper bookmarkHelper = new BookmarkHelper(mView.getAppContext());
        Disposable disposable = bookmarkHelper.getListBooks()
                .subscribe(books -> mView.OnLoadBook(books));
        compositeDisposable.add(disposable);
    }

    @Override
    public void OnClickAyat(Books books) {

    }

    @Override
    public void OnLongClickAyat(Books books) {

    }
}
