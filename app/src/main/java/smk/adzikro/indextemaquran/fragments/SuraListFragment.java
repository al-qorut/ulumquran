package smk.adzikro.indextemaquran.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.adapter.QuranListAdapter;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.object.QuranRow;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.QuranUtils;

import static smk.adzikro.indextemaquran.constans.Constants.JUZ2_COUNT;
import static smk.adzikro.indextemaquran.constans.Constants.PAGES_LAST;
import static smk.adzikro.indextemaquran.constans.Constants.SURAS_COUNT;

public class SuraListFragment extends Fragment {

  private RecyclerView mRecyclerView;
 // private Spinner surat, ayat;

/*  public static SuraListFragment newInstance() {
      SuraListFragment newFrag = new SuraListFragment();
      Bundle args = new Bundle();
      newFrag.setArguments( args );
      return newFrag;
  }
*/
  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.quran_list, container, false);

    final Context context = getActivity();

    mRecyclerView = view.findViewById(R.id.recycler_view);
  //  surat = view.findViewById(R.id.list_surat);
  //  ayat = view.findViewById(R.id.list_ayat);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    final QuranListAdapter adapter =
        new QuranListAdapter(context, mRecyclerView, getSuraList(), true);
    mRecyclerView.setAdapter(adapter);

    return view;
  }

  @Override
  public void onResume() {
    final Activity activity = getActivity();
    QuranSettings settings =QuranSettings.getInstance(activity);
    int lastPage = settings.getLastPage();
    if (lastPage != Constants.NO_PAGE_SAVED &&
        lastPage >= Constants.PAGES_FIRST &&
        lastPage <= Constants.PAGES_LAST) {
      int sura = BaseQuranInfo.PAGE_SURA_START[lastPage - 1];
      int juz = BaseQuranInfo.getJuzFromPage(lastPage);
      int position = sura + juz - 1;
      mRecyclerView.scrollToPosition(position);
    }

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

  private QuranRow[] getSuraList() {
    int next;
    int pos = 0;
    int sura = 1;
    QuranRow[] elements = new QuranRow[SURAS_COUNT + JUZ2_COUNT];

    Activity activity = getActivity();
    boolean wantPrefix = activity.getResources().getBoolean(R.bool.show_surat_prefix);
    boolean wantTranslation = activity.getResources().getBoolean(R.bool.show_sura_names_translation);
    for (int juz = 1; juz <= JUZ2_COUNT; juz++) {
      final String headerTitle = activity.getString(R.string.juz2_description,
          QuranUtils.getLocalizedNumber(activity, juz));
      final QuranRow.Builder headerBuilder = new QuranRow.Builder()
          .withType(QuranRow.HEADER)
          .withText(headerTitle)
          .withPage(BaseQuranInfo.JUZ_PAGE_START[juz - 1]);
      elements[pos++] = headerBuilder.build();
      next = (juz == JUZ2_COUNT) ? PAGES_LAST + 1 :
              BaseQuranInfo.JUZ_PAGE_START[juz];

      while ((sura <= SURAS_COUNT) &&
          (BaseQuranInfo.SURA_PAGE_START[sura - 1] < next)) {
        final QuranRow.Builder builder = new QuranRow.Builder()
            .withText(BaseQuranInfo.getSuraName(activity, sura, wantPrefix, wantTranslation))
            .withMetadata(BaseQuranInfo.getArtiSuraName(activity, sura))
            .withSura(sura)
            .withPage(BaseQuranInfo.SURA_PAGE_START[sura - 1]);
        elements[pos++] = builder.build();
        sura++;
      }
    }

    return elements;
  }

}
