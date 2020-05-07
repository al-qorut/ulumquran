package smk.adzikro.indextemaquran.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 1/18/18.
 */

public class TemaListAdapter extends RecyclerView.Adapter<TemaListAdapter.Holder> {
    private List<Ayah> data = new ArrayList<>();
    private Context context;
    private QuranSettings settings;
    private  int fontSize;

    public TemaListAdapter(Context context, List<Ayah> data){
        this.context = context;
        this.data = data;
        settings = QuranSettings.getInstance(context);
        fontSize = settings.getTextSize();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_tema,parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Ayah ayah = data.get(position);
        holder.arab.setText(ayah.arab);
        holder.arti.setText(ayah.arti);
        holder.arab.setTextSize(fontSize*1.4f);
        holder.arab.setTypeface(Fungsi.getHurufArab(context));
        holder.arti.setTextSize(fontSize);
        holder.qs.setText(BaseQuranInfo.getSuraAyahString(context, ayah.sura, ayah.ayat));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView arab, arti, qs;
        public Holder(View itemView) {
            super(itemView);
            arab = itemView.findViewById(R.id.arab);
            arti = itemView.findViewById(R.id.arti);
            qs = itemView.findViewById(R.id.qs);
        }
    }
}
