package smk.adzikro.indextemaquran.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.MainActivity;
import smk.adzikro.indextemaquran.constans.BaseQuranData;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.QuranRow;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.QuranUtils;
import smk.adzikro.indextemaquran.widgets.IconTreeItemHolder;
import smk.adzikro.indextemaquran.widgets.RowQuranHolder;

import static smk.adzikro.indextemaquran.constans.Constants.JUZ2_COUNT;
import static smk.adzikro.indextemaquran.constans.Constants.PAGES_LAST;
import static smk.adzikro.indextemaquran.constans.Constants.SURAS_COUNT;

public class SuraListFragment extends Fragment {
  private ViewGroup containerView;
  private TreeNode root;
  private AndroidTreeView tView;
  Spinner surat, ayat;
  QuranSettings settings;
  TextView last_open;
  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.quran_list, container, false);
    final Context context = getActivity();
    settings = QuranSettings.getInstance(getContext());
    containerView = (ViewGroup) view.findViewById(R.id.container);
    surat = view.findViewById(R.id.list_nama_surat);
    ayat = view.findViewById(R.id.list_ayat);
    last_open = view.findViewById(R.id.last_opened);
    last_open.setVisibility(View.GONE);

    root = TreeNode.root();
    createList();
    tView = new AndroidTreeView(getActivity(), root);
    tView.setDefaultAnimation(true);
    tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
    tView.setDefaultViewHolder(RowQuranHolder.class);
    tView.setDefaultNodeClickListener(nodeClickListener);
    tView.setDefaultNodeLongClickListener(nodeLongClickListener);
    containerView.addView(tView.getView());
    if(savedInstanceState!=null){
      String state = savedInstanceState.getString("tFragment");
      if (!TextUtils.isEmpty(state)) {
        tView.restoreState(state);
      }
    }
    last_open.setOnClickListener(view1 -> ((MainActivity)getContext()).jumpTo(settings.getLastPage(),0));

    surat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int jAyat;
        String[] listAyat;
        jAyat = BaseQuranData.SURA_NUM_AYAHS[i];
        listAyat = new String[jAyat];
        for (i = 0; i < jAyat; i++) {
          listAyat[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> list_ayat = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, listAyat);
        list_ayat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ayat.setAdapter(list_ayat);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    ImageView gotoq =view.findViewById(R.id.goto_quran);
    gotoq.setOnClickListener(view12 -> {
      int sura = surat.getSelectedItemPosition()+1;
      int aya = ayat.getSelectedItemPosition()+1;
      int page = BaseQuranInfo.getPageFromSuraAyah(sura, aya);
      ((MainActivity)getContext()).jumpTo(page,0);
    });
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
      createList();
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB &&
        settings.isArabicNames()) {
      updateScrollBarPositionHoneycomb();
    }

    super.onResume();
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void updateScrollBarPositionHoneycomb() {
   // mRecyclerView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
  }
  private void createList(){
    if(settings.getLastPage()!=Constants.NO_PAGE_SAVED){
      int page = settings.getLastPage();
      String nama = BaseQuranInfo.getSuraNameFromPage(getContext(),page,true)+" "+BaseQuranInfo.getPageSubtitle(getContext(),page);
      last_open.setText(String.format(getContext().getString(R.string.terakhir_dibuka),nama));
      last_open.setVisibility(View.VISIBLE);
    }
    QuranRow[] list = getSuraList();
    List<TreeNode> hulu = new ArrayList<>();
    TreeNode header=null;
    for (int i=0; i<list.length;i++){
      QuranRow row = list[i];
      if(list[i].isHeader()){
        header = new TreeNode(new RowQuranHolder.RowQuran("",R.string.ic_folder,row.text ,row.page));
        hulu.add(header);
      }else{
        TreeNode surat = new TreeNode(new RowQuranHolder.RowQuran(""+row.sura, R.string.ic_drive_document,row.text,row.page));
        header.addChild(surat);
      }
    }
    root.addChildren(hulu);
  }
  private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
    @Override
    public void onClick(TreeNode node, Object value) {
      RowQuranHolder.RowQuran item = (RowQuranHolder.RowQuran) value;
      if(node.getChildren().size()>0){
        return;
      }else{
        ((MainActivity)getContext()).jumpTo(item.page,0);
      }
    }
  };
  private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
    @Override
    public boolean onLongClick(TreeNode node, Object value) {
      RowQuranHolder.RowQuran item = (RowQuranHolder.RowQuran) value;
      if(node.getChildren().size()>0){
        ((MainActivity)getContext()).jumpTo(item.page,0);
        return false;
      }
      return true;
    }
  };
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
