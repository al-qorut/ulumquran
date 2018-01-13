package smk.adzikro.indextemaquran.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.UlumQuranActivity;
import smk.adzikro.indextemaquran.adapter.QuranListAyahAdapter;
import smk.adzikro.indextemaquran.adapter.TranslationAdapter;
import smk.adzikro.indextemaquran.db.QuranApi;
import smk.adzikro.indextemaquran.interfaces.OnTranslationActionListener;
import smk.adzikro.indextemaquran.interfaces.QuranListContrack;
import smk.adzikro.indextemaquran.interfaces.QuranListTafsir;
import smk.adzikro.indextemaquran.object.QuranInfo;
import smk.adzikro.indextemaquran.object.QuranLafdzi;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.TranslationViewRow;
import smk.adzikro.indextemaquran.widgets.QuranTranslationPageLayout;


public class TranslationFragment extends Fragment
    implements QuranListContrack.View,
        View.OnClickListener,
        TranslationAdapter.OnVerseSelectedListener,
        OnTranslationActionListener{
    private static final String PAGE_NUMBER_EXTRA = "page";
    private int mPageNumber;
    private QuranListTafsir mPresenter;

    public static TranslationFragment newInstance(int page) {
       final TranslationFragment sInstance = new TranslationFragment();
       final Bundle args = new Bundle();
       args.putInt(PAGE_NUMBER_EXTRA, page);
       sInstance.setArguments(args);
       return sInstance;
    }
    String TAG="TranslationFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(PAGE_NUMBER_EXTRA);
        setHasOptionsMenu(true);
    }
    private QuranSettings mQuranSettings;

    TranslationView mMainView;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mMainView = new TranslationView(getContext());//LayoutInflater.from(getContext()).inflate(R.layout.quran_display,container,false);
        mPresenter = new QuranListTafsir();
        mPresenter.subscribe(this, mPageNumber);
        mQuranSettings = QuranSettings.getInstance(getContext());
        mMainView.setTranslationClickedListener(this);
        mMainView.setOnTranslationActionListener(this);
        return mMainView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
      //  outState.putInt(PAGE_NUMBER_EXTRA, mPageNumber);
        super.onSaveInstanceState(outState);
    }


    public void refresh() {
        Activity activity = getActivity();
        if (activity != null) {
            mPresenter.subscribe(this, mPageNumber);
        }
        newInstance(mPageNumber);
    }

    @Override
    public Context getAppContext() {
        return getContext();
    }

    private void setQuran(QuranApi.BundleQuranInfo quraninfo){

        List<String> translations= quraninfo.info;
        List<QuranInfo> ayahs = quraninfo.quranAyahList;
        List<TranslationViewRow> data = new ArrayList<>();

        int currenSura=-1;
        boolean wantTranslationHeaders = translations.size()> 1;

        for(int i=0 ;i<ayahs.size();i++){

            QuranInfo quran = ayahs.get(i);
           // Log.e(TAG, quran.ayah+" "+quran.arabicText);
            int sura = quran.sura;

            if(sura != currenSura){
                data.add(new TranslationViewRow(TranslationViewRow.Type.SURA_HEADER, quran));
                currenSura = sura;
            }
            if(quran.ayah == 1 && sura != 1 && sura != 9){
                data.add(new TranslationViewRow(TranslationViewRow.Type.BASMALLAH, quran));
            }
            data.add(new TranslationViewRow(TranslationViewRow.Type.VERSE_NUMBER, quran));

            if(quran.arabicText != null){
                data.add(new TranslationViewRow(TranslationViewRow.Type.QURAN_TEXT, quran));
            }
            //Ambil translate
            int verseText = quran.texts.size();
         //   Log.e(TAG,"Banyak tran "+verseText);
            for (int j = 0; j < translations.size(); j++) {
                String text = verseText > j ? quran.texts.get(j) : "";

                if (!TextUtils.isEmpty(text)) {
                    if (wantTranslationHeaders) {
                        data.add(new TranslationViewRow(TranslationViewRow.Type.TRANSLATOR, quran, translations.get(j).toString()));
                    }
                    if(translations.get(j).contains("Arabic"))
                        data.add(new TranslationViewRow(TranslationViewRow.Type.TAFSIR_ARABIC, quran, text));
                    else
                        data.add(new TranslationViewRow(TranslationViewRow.Type.TAFSIR_LATIN, quran, text));
                      //  Log.e(TAG,"Isinya "+text);
                }
            }
           // Log.e(TAG, "Lafdzi "+quran.lafdzi.size());
            if(quran.lafdzi!=null){
                data.add(new TranslationViewRow(TranslationViewRow.Type.LAFDZI, quran));
            }
            
            //boleh tambah garis di sini
            data.add(new TranslationViewRow(TranslationViewRow.Type.SPACER,quran));
        }

        mMainView.setData(data);
    }

    @Override
    public void displayQuran(QuranApi.BundleQuranInfo quranlist) {
        setQuran(quranlist);
    }

    @Override
    public void onClick(View view) {
       // Toast.makeText(getContext(), "Click", Toast.LENGTH_SHORT).show();
        //mMainView.
        ((UlumQuranActivity) getActivity()).toggleActionBar();
    }

    @Override
    public void onVerseSelected(QuranInfo ayahInfo) {

    }

    @Override
    public void onTranslationAction(QuranInfo ayah, String[] translationNames, int actionId) {

        Activity activity = getActivity();
        if (activity instanceof UlumQuranActivity) {

            switch (actionId) {
                case R.id.cab_favorite:
                    Toast.makeText(activity, "Pavorite", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.cab_share_ayah_text:
                    Toast.makeText(activity, "Share ayah", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.cab_copy_ayah:
                    Toast.makeText(activity, "Copy ayah", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.cab_play:
                    Toast.makeText(activity, "Play", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.cab_tema:
                    Toast.makeText(activity, "Tema", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }
}

