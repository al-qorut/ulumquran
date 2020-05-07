package smk.adzikro.indextemaquran.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.UlumQuranActivity;
import smk.adzikro.indextemaquran.adapter.TranslationAdapter;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.db.BookmarkHelper;
import smk.adzikro.indextemaquran.db.QuranApi;
import smk.adzikro.indextemaquran.interfaces.OnTranslationActionListener;
import smk.adzikro.indextemaquran.interfaces.QuranListContrack;
import smk.adzikro.indextemaquran.interfaces.QuranListTafsir;
import smk.adzikro.indextemaquran.object.QuranInfo;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.TranslationViewRow;
import smk.adzikro.indextemaquran.util.ShareUtil;


public class TranslationFragment extends Fragment
    implements QuranListContrack.View,
        View.OnClickListener,
        TranslationAdapter.OnVerseSelectedListener,
        OnTranslationActionListener{
    private static final String PAGE_NUMBER_EXTRA = "page";
    private int mPageNumber;
    private QuranListTafsir mPresenter;
    private List<String> translat=new ArrayList<>();
    private BookmarkHelper bookmark;
    ArrayList<HashMap<String, Integer>> listBook=new ArrayList<>();

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
        bookmark = new BookmarkHelper(getContext());
        listBook = bookmark.getListBook();
        return mMainView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void refresh() {
        Activity activity = getActivity();
        if (activity != null) {
            mPresenter.subscribe(this, mPageNumber);
        }
        listBook = bookmark.getListBook();
        newInstance(mPageNumber);
    }
    private boolean isBookmark(int sura, int ayah){
        boolean hasil=false;
        for(int i=0;i<listBook.size();i++){
            if(sura==listBook.get(i).get("surat") && ayah==listBook.get(i).get("ayat")){
                hasil=true;
            }
        }
        return hasil;
    }
    @Override
    public Context getAppContext() {
        return getContext();
    }

    private void setQuran(QuranApi.BundleQuranInfo quraninfo){

        List<String> translations= quraninfo.info;
        List<QuranInfo> ayahs = quraninfo.quranAyahList;
        translat.addAll(translations);
        List<TranslationViewRow> data = new ArrayList<>();
        int currenSura=-1;
        boolean wantTranslationHeaders = translations.size()> 1;

        for(int i=0 ;i<ayahs.size();i++){
            QuranInfo quran = ayahs.get(i);
            quran.setBookmark(isBookmark(quran.sura, quran.ayah));
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
            int verseText = quran.texts.size();
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
                }
            }
            if(quran.lafdzi!=null){
                data.add(new TranslationViewRow(TranslationViewRow.Type.LAFDZI, quran));
            }
            
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
        ((UlumQuranActivity) getActivity()).toggleActionBar();
    }

    @Override
    public void onVerseSelected(QuranInfo ayahInfo) {

    }
    public void showDialogNotes(int sura, int aya){
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.input_ayah_favorite);
        dialog.setTitle(getString(R.string.polder_bookmark));
        Button ok = dialog.findViewById(R.id.ok);
        EditText notes = dialog.findViewById(R.id.notes);
        ok.setOnClickListener(view -> {
            String info = notes.getText().toString();
            bookmark.addRemoveBookmark(sura, aya, info);
            refresh();
            dialog.dismiss();
        });
        Button cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
    public void setHighligh(int ayah){
        mMainView.highlightAyah(ayah);
    }
    @Override
    public void onTranslationAction(QuranInfo ayah, List<String> translationNames, int actionId) {

        Activity activity = getActivity();
        if (activity instanceof UlumQuranActivity) {
            String shareText = ShareUtil.getShareText(activity, ayah, translat);
            switch (actionId) {
                case R.id.cab_favorite:
                    bookmark.addRemoveBookmark(ayah.sura, ayah.ayah,"Default");
                    refresh();
                    break;
                case R.id.cab_share_ayah_text:
                     ShareUtil.shareViaIntent(activity, shareText, R.string.share_ayah_text);
                    break;
                case R.id.cab_copy_ayah:
                    ShareUtil.copyToClipboard(activity, shareText);
                    break;
                case R.id.cab_play:
                    int page = BaseQuranInfo.getPageFromSuraAyah(ayah.sura, ayah.ayah);
                    ((UlumQuranActivity)getContext()).playFromAyah(page,ayah.sura, ayah.ayah, false);
                    break;
                case R.id.cab_tema:
                    showDialogNotes(ayah.sura, ayah.ayah);
                    break;

            }
        }
    }
}

