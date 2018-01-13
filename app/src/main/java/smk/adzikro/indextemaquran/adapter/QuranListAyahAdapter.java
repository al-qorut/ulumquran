package smk.adzikro.indextemaquran.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import smk.adzikro.indextemaquran.BuildConfig;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.UlumQuranActivity;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.object.QuranAyah;
import smk.adzikro.indextemaquran.object.QuranInfo;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.setting.UthmaniSpan;
import smk.adzikro.indextemaquran.ui.Holder;
import smk.adzikro.indextemaquran.ui.TranslationViewRow;
import smk.adzikro.indextemaquran.widgets.AyahNumberView;

import static smk.adzikro.indextemaquran.util.Fungsi.AR_BASMALLAH;
import static smk.adzikro.indextemaquran.util.Fungsi.getAyahWithoutBasmallah;

/**
 * Created by server on 12/30/17.
 */

public class QuranListAyahAdapter extends
        RecyclerView.Adapter<QuranListAyahAdapter.RowViewHolder> {
    private static final String TAG = QuranListAyahAdapter.class.getSimpleName() ;
    private Context context;
    private static final boolean USE_UTHMANI_SPAN =
            Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
    private static final float ARABIC_MULTIPLIER = 1.4f;
    private QuranSettings settings;
    private View.OnClickListener onClickListener;
    private List<TranslationViewRow> data = new ArrayList<>();
    private int fontSize, arabicTextColor;
    private int textColor, suraHeaderColor;

    public QuranListAyahAdapter(Context context, List<TranslationViewRow> data){
        this.context = context;
        settings = QuranSettings.getInstance(context);
        this.fontSize = settings.getTextSize();
        this.data = data;
        arabicTextColor = Color.BLACK;
        textColor = Color.GRAY;
        suraHeaderColor = Color.DKGRAY;

        Log.e(TAG,"onCreate ");
    }
    public void refresh(QuranSettings quranSettings) {
        this.fontSize = quranSettings.getTranslationTextSize();
        this.textColor = ContextCompat.getColor(context, R.color.translation_text_color);
      //  this.dividerColor = ContextCompat.getColor(context, R.color.translation_divider_color);
        this.arabicTextColor = Color.BLACK;
        this.suraHeaderColor = ContextCompat.getColor(context, R.color.translation_sura_header);
       // this.ayahSelectionColor =
        //        ContextCompat.getColor(context, R.color.translation_ayah_selected_color);

        if (!this.data.isEmpty()) {
            notifyDataSetChanged();
        }
    }
    public void setData(List<TranslationViewRow> data){
        Log.e(TAG,"Masukan Data ");
        if(this.data==null) {
            this.data = new ArrayList<>();
            this.data.addAll(data);
        }
    }
    @Override
    public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG,"onCreateViewHolder ");
        @LayoutRes int layout;
        if(viewType == TranslationViewRow.Type.SURA_HEADER) {
            layout = R.layout.quran_translation_header_row;
        }else if (viewType == TranslationViewRow.Type.BASMALLAH ||
                viewType == TranslationViewRow.Type.QURAN_TEXT) {
            layout = R.layout.quran_translation_arabic_row;
        }  else if (viewType == TranslationViewRow.Type.VERSE_NUMBER) {
            layout = R.layout.header_nomor_ayat;;
        } else if (viewType == TranslationViewRow.Type.TRANSLATOR) {
            layout = R.layout.quran_translation_translator_row;
        } else if (viewType == TranslationViewRow.Type.TAFSIR_LATIN) {
            layout = R.layout.quran_translation_text_row;
        }else {
            layout = R.layout.quran_translation_arabic_row;
        }
        View view = LayoutInflater.from(context).inflate(layout,parent,false);
        return new RowViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RowViewHolder holder, int position) {
        TranslationViewRow row = data.get(position);
            Resources res = context.getResources();
         final CharSequence text;
        if (row.type == TranslationViewRow.Type.SURA_HEADER) {
               text = BaseQuranInfo.getSuraName(context, row.ayahInfo.sura, true);
               holder.textView.setBackgroundColor(res.getColor(R.color.panel_background_color));
               holder.textView.setText(text);
               holder.textView.setGravity(Gravity.CENTER);
           } else if (row.type == TranslationViewRow.Type.BASMALLAH ||
                   row.type == TranslationViewRow.Type.QURAN_TEXT) {
               SpannableString str = new SpannableString(row.type == TranslationViewRow.Type.BASMALLAH ?
                       AR_BASMALLAH : getAyahWithoutBasmallah(
                       row.ayahInfo.sura, row.ayahInfo.ayah, row.ayahInfo.arabicText));
               if (USE_UTHMANI_SPAN) {
                   str.setSpan(new UthmaniSpan(context), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
               }
               text = str;
               holder.textView.setTextColor(arabicTextColor);
               holder.textView.setTextSize(ARABIC_MULTIPLIER * fontSize);
               holder.textView.setText(text);
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                   holder.textView.setTextDirection(View.TEXT_DIRECTION_RTL);
               holder.textView.setBackgroundColor(Color.TRANSPARENT);
           }  else if (row.type == TranslationViewRow.Type.TRANSLATOR) {
               text = row.data;
               holder.textView.setText(text);
               holder.textView.setTextSize(fontSize * 0.7f);
               holder.textView.setBackgroundColor(Color.TRANSPARENT);
               holder.textView.setGravity(Gravity.CENTER);
               holder.textView.setTextColor(res.getColor(R.color.accent_color_dark));
           } else if (row.type == TranslationViewRow.Type.TAFSIR_ARABIC) {
               text = row.data;
               holder.textView.setTextSize(fontSize * 1.1f);
               holder.textView.setTextColor(res.getColor(R.color.primary_text));
               holder.textView.setBackgroundColor(Color.TRANSPARENT);
               if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
                   holder.textView.setText(Html.fromHtml(text.toString(), 0));
               else
                   holder.textView.setText(Html.fromHtml(text.toString()));
           } else if (row.type == TranslationViewRow.Type.TAFSIR_LATIN) {
               text = row.data;
               holder.textView.setTextColor(res.getColor(R.color.primary_text));
               holder.textView.setBackgroundColor(Color.TRANSPARENT);
               holder.textView.setTextSize(fontSize);
               if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
                   holder.textView.setText(Html.fromHtml(text.toString(), 0));
               else
                   holder.textView.setText(Html.fromHtml(text.toString()));
           } else if (row.type == TranslationViewRow.Type.VERSE_NUMBER) {
               holder.textView.setTypeface(settings.getFontQuran(context));
               holder.textView.setTextSize(fontSize*ARABIC_MULTIPLIER);
               holder.textView.setTextColor(Color.BLACK);
               holder.textView.setText(BaseQuranInfo.setHurufArab(context, ""+row.ayahInfo.ayah));
           }
           holder.textView.setTag(row);

    }


   @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    public class RowViewHolder extends RecyclerView.ViewHolder {
            @Nullable
            TextView textView;
            @Nullable
            AyahNumberView nomorAyat;
            @Nullable
            ImageView imageView;
        public RowViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            nomorAyat = itemView.findViewById(R.id.ayah_number);
            imageView = itemView.findViewById(R.id.img_favorite);
            itemView.setOnClickListener(view -> {
                TranslationViewRow row = (TranslationViewRow)view.getTag();
                ((UlumQuranActivity)context).toggleActionBar();
            });
        }
    }


}
