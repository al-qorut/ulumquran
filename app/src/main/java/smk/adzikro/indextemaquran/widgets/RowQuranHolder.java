package smk.adzikro.indextemaquran.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.johnkil.print.PrintView;
import com.unnamed.b.atv.model.TreeNode;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.object.Ayah;

/**
 * Created by server on 1/20/18.
 */

public class RowQuranHolder extends
        TreeNode.BaseNodeViewHolder<RowQuranHolder.RowQuran>{

    public RowQuranHolder(Context context) {
        super(context);
    }
    TextView suraNumber, namaSurat, noPage;
    PrintView arrowView;
    @Override
    public View createNodeView(TreeNode node, RowQuran value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.index_sura_row, null, false);
        suraNumber = view.findViewById(R.id.suraNumber);
        namaSurat = view.findViewById(R.id.suraName);
        noPage =view.findViewById(R.id.nopage);
        suraNumber.setText(value.nomor);
        namaSurat.setText(value.text);
        noPage.setText(""+value.page);
        final PrintView iconView = (PrintView) view.findViewById(R.id.icon);
        iconView.setIconText(context.getResources().getString(value.icon));
        arrowView = (PrintView) view.findViewById(R.id.arrow_icon);
        return view;
    }
    @Override
    public void toggle(boolean active) {
        arrowView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
    }
    public static class RowQuran {
        public String nomor;
        public int icon;
        public String text;
        public int page;
        public RowQuran(String nomor, int icon, String text, int page){
            this.nomor = nomor;
            this.icon = icon;
            this.text = text;
            this.page = page;
        }
    }
}
