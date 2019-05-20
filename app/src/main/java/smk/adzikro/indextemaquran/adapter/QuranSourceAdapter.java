package smk.adzikro.indextemaquran.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.object.QuranSource;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 1/2/18.
 */

public class QuranSourceAdapter extends RecyclerView.Adapter<QuranSourceAdapter.RowHolder> {
    private static final String TAG = QuranSourceAdapter.class.getSimpleName();
    private Context context;
    private List<QuranSource> data = new ArrayList<>();
    private View.OnClickListener onClickListener;

    @NonNull
    public OnItemCheckListener onItemCheckListener;

    public QuranSourceAdapter(Context context,List<QuranSource> data,
                              View.OnClickListener clickListener,
                              OnItemCheckListener onItemCheckListener){
        this.context = context;
        this.data = data;
        this.onClickListener = clickListener;
        this.onItemCheckListener = onItemCheckListener;
    }

    public void setData(List<QuranSource> data){
        this.data = data;
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_download,parent,false);
        return new RowHolder(view);
    }

    @Override
    public void onBindViewHolder(RowHolder holder, int position) {
        QuranSource quranSource = data.get(position);
        if(data!=null) {
            holder.textName.setText(quranSource.getDisplayName());
            String asing = " ";;
            if(!quranSource.getTranslator_asing().equals("null")){
                asing=quranSource.getTranslator_asing();
            }

            String pengarang = "";;
            if(!quranSource.getTranslator().equals("null"))pengarang=quranSource.getTranslator();

          //  holder.textTranslator.setText(pengarang);
          //  holder.textTranslator_asing.setText(asing);
            File file = new File(Fungsi.PATH_DATABASE()+quranSource.getFile_name());
            holder.imageView.setImageResource(R.drawable.ic_download);
            holder.checkBox.setChecked(false);
            holder.checkBox.setEnabled(false);
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.checkBox.isChecked())
                        onItemCheckListener.onItemCheck(quranSource);
                    else
                        onItemCheckListener.onItemUncheck(quranSource);
                }
            });
            if(file.exists()) {
                holder.imageView.setImageResource(R.drawable.ic_cancel);
                holder.checkBox.setEnabled(true);
                if(quranSource.getActive()==1)
                    holder.checkBox.setChecked(true);
            }
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemCheckListener.onItemClick(quranSource);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    public class RowHolder extends RecyclerView.ViewHolder{
        TextView textName;
       // TextView textTranslator,textTranslator_asing;
        ImageView imageView;
        CheckBox checkBox;

        public RowHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.displayName);
          //  textTranslator = itemView.findViewById(R.id.translator);
          //  textTranslator_asing = itemView.findViewById(R.id.translator_asing);
            imageView = itemView.findViewById(R.id.img_donlot);
            checkBox = itemView.findViewById(R.id.aktif);
            itemView.setOnClickListener(defaultClickListener);

        }
    }
    private View.OnClickListener defaultClickListener = view -> {
        //final int position = recyclerView.getChildAdapterPosition(v);
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    };

    public interface OnItemCheckListener {
        void onItemCheck(QuranSource item);
        void onItemUncheck(QuranSource item);
        void onItemClick(QuranSource item);
    }

}
