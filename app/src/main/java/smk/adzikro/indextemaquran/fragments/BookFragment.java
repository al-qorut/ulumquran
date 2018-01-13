package smk.adzikro.indextemaquran.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.db.BookmarkHelper;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.activities.MainActivity;
import smk.adzikro.indextemaquran.db.QuranDatabase;
import smk.adzikro.indextemaquran.adapter.QuranListAdapter;
import smk.adzikro.indextemaquran.object.QuranRow;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.R;


public class BookFragment extends Fragment implements
        QuranListAdapter.QuranTouchListener {



    public static BookFragment newInstance(){
        return new BookFragment();
    }


    private RecyclerView mRecyclerView;
    private BookmarkHelper bookmarkHelper=null;
    QuranListAdapter adapter;
    QuranSettings settings;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.quran_list, container, false);

        final Context context = getActivity();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        bookmarkHelper = new BookmarkHelper(getContext());
        settings = QuranSettings.getInstance(getContext());
        loadData();

        return view;
    }
    public void loadData(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                adapter = new QuranListAdapter(getContext(), mRecyclerView, getBookList(), true);
                adapter.setQuranTouchListener(BookFragment.this);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.setAdapter(adapter);
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public void onResume() {
        final Activity activity = getActivity();
        QuranSettings settings = QuranSettings.getInstance(activity);
        if(bookmarkHelper==null){
            bookmarkHelper = new BookmarkHelper(getContext());
        }
        loadData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB &&
                settings.isArabicNames()) {
            updateScrollBarPositionHoneycomb();
        }

        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateScrollBarPositionHoneycomb() {
        mRecyclerView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
    }
    public String getAyahWithoutBasmallah(int sura, int ayah, String ayahText) {
        // note that ayahText.startsWith check is always true for now - but it's explicitly here so
        // that if we update quran.ar.db one day to fix this issue and older clients get a new copy of
        // the database, their code continues to work as before.
        if (ayah == 1 && sura != 9 && sura != 1 && ayahText.startsWith(Fungsi.AR_BASMALLAH)) {
            return ayahText.substring(Fungsi.AR_BASMALLAH.length() + 1);
        }
        return ayahText;
    }
    private String getTextAyat(int surat, int ayat){
        SQLiteDatabase db = QuranDatabase.open(getContext());
        Cursor cursor = null;
        cursor = db.rawQuery("select arab from ALQURAN where ayat="+ayat+" and surat="+surat,null);
        String text ="";
        if(cursor!=null && cursor.moveToFirst()) {
            text = cursor.getString(cursor.getColumnIndex("arab"));
            text = getAyahWithoutBasmallah(surat, ayat, text);
        }
        cursor.close();
        db.close();
        String[] kata = TextUtils.split(text, " ");
        String textAyat="";
        switch (kata.length){
            case 0:
                textAyat="";
                break;
            case 1:
                textAyat= kata[0];
                break;
            case 2:
                textAyat= kata[0]+" "+kata[1];
                break;
            case 3:
                textAyat =kata[0]+" "+kata[1]+" "+kata[2];
                break;
            default:
                textAyat =kata[0]+" "+kata[1]+" "+kata[2]+" "+kata[3];
                break;
        }
        return textAyat;
    }
    private QuranRow[] getBookList() {
        int pos = 0;
        int sura = 1;
        //((MainActivity)getContext()).getListAksi();//
        String[] aks = getResources().getStringArray(R.array.aksi);
        String[] aksi = new String[aks.length+1];
        aksi[0]= "Current Page (Tadarrus)";
        for (int i=0;i<aks.length;i++){
            aksi[i+1]= aks[i];
        }
        int jumlahData = bookmarkHelper.getCount();

        QuranRow[] elements = new QuranRow[aksi.length + jumlahData];

        Activity activity = getActivity();
        boolean wantPrefix = activity.getResources().getBoolean(R.bool.show_surat_prefix);
        boolean wantTranslation = activity.getResources().getBoolean(R.bool.show_sura_names_translation);
        for (int dataAksi = -1; dataAksi < aksi.length-1; dataAksi++) {
            final String headerTitle = aksi[dataAksi + 1];
            final QuranRow.Builder headerBuilder = new QuranRow.Builder()
                    .withType(QuranRow.HEADER)
                    .withText(headerTitle)
                    .withAksi(dataAksi)
                    .withPage(1);
            elements[pos++] = headerBuilder.build();
            Cursor cursor = null;
            cursor = bookmarkHelper.getListBookAksi(dataAksi);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    sura = cursor.getInt(cursor.getColumnIndex("surat"));
                    int ayat = cursor.getInt(cursor.getColumnIndex("ayat"));
                    int page = cursor.getInt(cursor.getColumnIndex("page"));
                    String text =cursor.getString(cursor.getColumnIndex("isi"));
                    if(text.equals("")) text = getTextAyat(sura,ayat);
                    final QuranRow.Builder builder = new QuranRow.Builder()
                            .withType(QuranRow.AYAH_BOOKMARK)
                            .withText(text)
                            .withMetadata(BaseQuranInfo.getAyahMetadata(sura, ayat, page, getContext()))
                            .withSura(sura)
                            .withAksi(dataAksi)
                            .withPage(BaseQuranInfo.getPageFromSuraAyah(sura,ayat));

                    elements[pos++] = builder.build();
                }
            }
        }
        return elements;
    }
    String TAG="BookFragment";

    @Override
    public void onClick(QuranRow row, int position) {
        //Log.e(TAG,"page "+row.page+" aksi "+row.aksi);
        if(!row.isHeader()) {
            adapter.setItemChecked(position, adapter.isItemChecked(position));
            settings.setLastAksi(row.aksi);
            ((MainActivity) getContext()).jumpTo(row.page, row.aksi);
        }
    }

    @Override
    public boolean onLongClick(QuranRow row, int position) {
        return false;
    }
}
