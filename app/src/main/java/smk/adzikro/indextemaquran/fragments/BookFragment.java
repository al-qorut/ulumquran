package smk.adzikro.indextemaquran.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import smk.adzikro.indextemaquran.activities.MainActivity;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.interfaces.BookList;
import smk.adzikro.indextemaquran.interfaces.BookPresenter;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.Books;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.widgets.IconTreeItemHolder;


public class BookFragment extends Fragment
implements BookPresenter.View{

    private ViewGroup containerView;
    private TreeNode root;
    private AndroidTreeView tView;
    private BookList presenter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        final Context context = getActivity();
        containerView = (ViewGroup) view.findViewById(R.id.container);
        presenter = new BookList();
        presenter.subscribe(this,0);
        root = TreeNode.root();
        tView = new AndroidTreeView(getActivity(), root);
        tView.setDefaultAnimation(true);
        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(IconTreeItemHolder.class);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);
    //    containerView.addView(tView.getView());
        if(savedInstanceState!=null){
            String state = savedInstanceState.getString("tFragment");
            if (!TextUtils.isEmpty(state)) {
              //  tView.restoreState(state);
            }
        }
        return view;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("tFragment", tView.getSaveState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public Context getAppContext() {
        return getContext();
    }
    private List<String> folder(List<Books>books){
        List<String> list = new ArrayList<>();
        for (int i=0; i<books.size();i++){
            list.add(books.get(i).folder);
        }
        Set<String> hs = new HashSet<>();
        hs.addAll(list);
        list.clear();
        list.addAll(hs);
        return list;
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    private void createListBook(List<Books> books){
            List<String> folders = folder(books);
            List<TreeNode> listForder = new ArrayList<>();
            for(int f=0; f<folders.size(); f++) {
                TreeNode folder = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_folder, folders.get(f),null));
                for (int i = 0; i < books.size(); i++) {
                    Books books1 = books.get(i);
                    if(folders.get(f).equals(books1.folder)) {
                        Ayah ayah = new Ayah(books1.surat, books1.ayat);
                        TreeNode isi = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_bookmark, BaseQuranInfo.getSuraAyahString(getContext(), books1.surat, books1.ayat),ayah));
                        folder.addChild(isi);
                    }
                }
                listForder.add(folder);
            }
            root.addChildren(listForder);
            containerView.addView(tView.getView());
    }

    @Override
    public void OnLoadBook(List<Books> books) {
        createListBook(books);
    }
    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
            if (((IconTreeItemHolder.IconTreeItem) value).ayah!=null) {
                Ayah ayah = ((IconTreeItemHolder.IconTreeItem) value).ayah;
                int page = BaseQuranInfo.getPageFromSuraAyah(ayah.sura, ayah.ayat);
                ((MainActivity)getAppContext()).jumpTo(page,0);
            }
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            return false;
        }
    };
}
