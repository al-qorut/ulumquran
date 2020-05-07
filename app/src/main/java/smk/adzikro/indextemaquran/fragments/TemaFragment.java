package smk.adzikro.indextemaquran.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.interfaces.TemaList;
import smk.adzikro.indextemaquran.interfaces.TemaPresenter;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.Tema;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.widgets.IconTreeItemHolder;

/**
 * Created by server on 11/26/16.
 */

public class TemaFragment extends Fragment
implements TemaPresenter.View{

    private static final String TAG = "TemaFragment";
    private TextView statusBar;
    private AndroidTreeView tView;
    ViewGroup containerView;
    List<Tema> temas, ayat;
    TreeNode root, tema_root, click_node;
    TemaList presenter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_tema, container, false);
        containerView = (ViewGroup) view.findViewById(R.id.container);
        statusBar = (TextView) view.findViewById(R.id.status_bar);
        presenter = new TemaList();
        presenter.subscribe(this,0);
        root = TreeNode.root();
        tema_root = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_laptop, getString(R.string.thema_quran),null));
        root.addChild(tema_root);
        click_node = tema_root;
        tView = new AndroidTreeView(getActivity(), root);
        tView.setDefaultAnimation(true);
        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(IconTreeItemHolder.class);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);
        containerView.addView(tView.getView());
        if(savedInstanceState!=null){
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }
        return view;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("tState", tView.getSaveState());
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onResume(){
        super.onResume();
       // isiData();
    }
    private void isiData(TreeNode ortu, List<Tema> temas){
        if(temas.size()>0){
            List<TreeNode> list = new ArrayList<>();
            for (int i=0; i<temas.size();i++){
                Tema tema = temas.get(i);
                if(tema.id!=-1){
                    TreeNode anax = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_folder, tema.title,null));
                    list.add(anax);
                }else{
                    String data[] =tema.title.split(":");
                    Ayah ayah = new Ayah(Integer.valueOf(data[0]),Integer.valueOf(data[1]));
                    TreeNode ayaT = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_drive_file, tema.title,ayah));
                    list.add(ayaT);
                }
            }
            ortu.addChildren(list);
        }
    }

    //recursif create tree
    private void createChild(TreeNode ortut, int ortu){
        List<Tema> anak = getAnak(ortu);
        List<TreeNode> anakTree = new ArrayList<>();
        for(int i=0; i<anak.size(); i++){
            TreeNode anax = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_folder, anak.get(i).title,null));
            anakTree.add(anax);
            List<Tema> anakNext = getAnak(anak.get(i).id);
            if(anakNext.size()>0) {
              createChild(anax, anak.get(i).id);
            }else{
                List<Tema> ayatTema = getAyat(anak.get(i).id);
                if(ayatTema.size()>0){
                    List<TreeNode> ayatTree = new ArrayList<>();
                   for(int jAyat=0 ; jAyat<ayatTema.size();jAyat++){
                       TreeNode ayaT = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_drive_file, ayatTema.get(jAyat).title,null));
                       ayatTree.add(ayaT);
                   }
                  anax.addChildren(ayatTree);
                }

            }
        }
        ortut.addChildren(anakTree);

    }


    private List<Tema> getAnak(int ortu){
        List<Tema> anak = new ArrayList<>();
        for(int i=0; i<temas.size();i++){
            if(ortu==temas.get(i).parent){
                anak.add(temas.get(i));
            }
        }
        return  anak;
    }
    private List<Tema> getAyat(int ortu){
        List<Tema> anak = new ArrayList<>();
        for(int i=0; i<ayat.size();i++){
            if(ortu==ayat.get(i).parent){
                anak.add(ayat.get(i));
            }
        }
        return  anak;
    }



    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
            statusBar.setText(item.text);
            Ayah ayah = ((IconTreeItemHolder.IconTreeItem) value).ayah;
            if (node==tema_root)return;
            if(ayah==null){
                click_node = node;
                presenter.OnClickTema(item.text);
            }else {
                presenter.OnClickAyat(ayah);
            }
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
            if(item.text.contains(":")){
                return false;
            }
            presenter.OnLongClickAyat(item.text);
            return true;
        }
    };

    @Override
    public Context getAppContext() {
        return getContext();
    }

    @Override
    public void OnLoadTema(List<Tema> bundleTema) {
        int anakna = click_node.getChildren().size();
        if(anakna>0)return;
        isiData(click_node, bundleTema);
    }

    @Override
    public void OnLoadAyat(Ayah ayah) {
        List<Ayah> ayahs = new ArrayList<>();
        ayahs.add(ayah);
        Fungsi.ShowMessage(getContext(),ayahs, statusBar.getText().toString());
    }

    @Override
    public void OnLongLoadAyat(List<Ayah> ayah) {
      if(ayah.size()>0)
          Fungsi.ShowMessage(getContext(),ayah,statusBar.getText().toString());
    }
}
