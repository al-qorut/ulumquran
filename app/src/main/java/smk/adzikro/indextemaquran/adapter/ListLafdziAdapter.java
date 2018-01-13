package smk.adzikro.indextemaquran.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.interfaces.QuranListContrack;
import smk.adzikro.indextemaquran.object.QuranLafdzi;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 1/8/18.
 */

public class ListLafdziAdapter extends RecyclerView.Adapter<ListLafdziAdapter.Lafdzi> {
    private static final String TAG = ListLafdziAdapter.class.getSimpleName();
    Context context;
    List<QuranLafdzi> data = new ArrayList<>();
    QuranSettings settings;
    int fontSize;

    public ListLafdziAdapter(Context context, List<QuranLafdzi> data){
        this.data = data;
        this.context = context;
        settings = QuranSettings.getInstance(context);
        fontSize = settings.getTextSize();
    }

    public void setData(List<QuranLafdzi> data){
        this.data.addAll(data);
    }
    @Override
    public Lafdzi onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lafdzi,parent,false);
        return new Lafdzi(view);
    }

    @Override
    public void onBindViewHolder(Lafdzi holder, int position) {
        QuranLafdzi quran = data.get(position);
        holder.arab.setText(quran.arab);
        holder.arab.setTextSize(fontSize*1.4f);
        holder.arab.setTypeface(Fungsi.getHurufArab(context));
        holder.arti.setText(quran.arti);
        holder.arti.setTextSize(fontSize*0.7f);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Lafdzi extends RecyclerView.ViewHolder {
        TextView arab, arti;
        public Lafdzi(View itemView) {
            super(itemView);
            arab = itemView.findViewById(R.id.arab);
            arti = itemView.findViewById(R.id.arti);

        }
    }
}
