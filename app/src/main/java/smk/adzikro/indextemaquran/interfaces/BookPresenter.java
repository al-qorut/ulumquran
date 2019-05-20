package smk.adzikro.indextemaquran.interfaces;


import java.util.List;

import smk.adzikro.indextemaquran.object.Books;

/**
 * Created by server on 1/19/18.
 */

public interface BookPresenter {
    interface View extends BaseApp.View{
        void OnLoadBook(List<Books> books);
    }
    interface Presenter extends BaseApp.Presenter<BookPresenter.View>{
        void OnClickAyat(Books books);
        void OnLongClickAyat(Books books);
    }
}
