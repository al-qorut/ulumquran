package smk.adzikro.indextemaquran.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.object.QuranInfo;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.setting.UthmaniSpan;
import smk.adzikro.indextemaquran.ui.TranslationViewRow;
import smk.adzikro.indextemaquran.widgets.AyahNumberView;
import smk.adzikro.indextemaquran.widgets.DividerView;

import static android.support.v4.view.ViewCompat.LAYOUT_DIRECTION_RTL;
import static smk.adzikro.indextemaquran.util.Fungsi.AR_BASMALLAH;
import static smk.adzikro.indextemaquran.util.Fungsi.getAyahWithoutBasmallah;

public class  TranslationAdapter extends RecyclerView.Adapter<TranslationAdapter.RowViewHolder> {
  private static final boolean USE_UTHMANI_SPAN =
      Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
  private static final float ARABIC_MULTIPLIER = 1.4f;

  private static final int HIGHLIGHT_CHANGE = 1;
  private static final String TAG= TranslationAdapter.class.getSimpleName();
  private final Context context;
  private final LayoutInflater inflater;
  private final RecyclerView recyclerView;
  private final List<TranslationViewRow> data;
  private View.OnClickListener onClickListener;
  private OnVerseSelectedListener onVerseSelectedListener;

  private int fontSize;
  private int textColor;
  private int dividerColor;
  private int arabicTextColor;
  private int suraHeaderColor;
  private int ayahSelectionColor;

  private int highlightedAyah;
  private int highlightedRowCount;
  private int highlightedStartPosition;

  private View.OnClickListener defaultClickListener = v -> {
    if (onClickListener != null) {
      onClickListener.onClick(v);
    }
  };

  private View.OnLongClickListener defaultLongClickListener = this::selectVerseRows;

  public TranslationAdapter(Context context,
                     RecyclerView recyclerView,
                     View.OnClickListener onClickListener,
                     OnVerseSelectedListener verseSelectedListener) {
    Log.e(TAG,"onCreate");
    this.context = context;
    this.data = new ArrayList<>();
    this.recyclerView = recyclerView;
    this.inflater = LayoutInflater.from(context);
    this.onClickListener = onClickListener;
    this.onVerseSelectedListener = verseSelectedListener;
  }

  public void setData(List<TranslationViewRow> data) {
    this.data.clear();
    this.data.addAll(data);
    if (highlightedAyah > 0) {
      highlightAyah(highlightedAyah, false);
    }
  }

  public void setHighlightedAyah(int ayahId) {
    highlightAyah(ayahId, true);
  }

  private void highlightAyah(int ayahId, boolean notify) {
    if (ayahId != highlightedAyah) {
      int count = 0;
      int startPosition = -1;
      for (int i = 0, size = this.data.size(); i < size; i++) {
        QuranInfo item = this.data.get(i).ayahInfo;
        if (item.ayahId == ayahId) {
          if (count == 0) {
            startPosition = i;
          }
          count++;
        } else if (count > 0) {
          break;
        }
      }

      // highlight the newly highlighted ayah
      if (count > 0 && notify) {
        int startChangeCount = count;
        int startChangeRange = startPosition;
        if (highlightedRowCount > 0) {
          // merge the requests for notifyItemRangeChanged when we're either the next ayah
          if (highlightedStartPosition + highlightedRowCount + 1 == startPosition) {
            startChangeRange = highlightedStartPosition;
            startChangeCount = startChangeCount + highlightedRowCount;
          } else if (highlightedStartPosition - 1 == startPosition + count) {
            // ... or when we're the previous ayah
            startChangeCount = startChangeCount + highlightedRowCount;
          } else {
            // otherwise, unhighlight
            notifyItemRangeChanged(highlightedStartPosition, highlightedRowCount, HIGHLIGHT_CHANGE);
          }
        }

        // and update rows to be highlighted
        notifyItemRangeChanged(startChangeRange, startChangeCount, HIGHLIGHT_CHANGE);
        recyclerView.smoothScrollToPosition(startPosition + count);
      }

      highlightedAyah = ayahId;
      highlightedStartPosition = startPosition;
      highlightedRowCount = count;
    }
  }

  public void unhighlight() {
    if (highlightedAyah > 0 && highlightedRowCount > 0) {
      notifyItemRangeChanged(highlightedStartPosition, highlightedRowCount);
    }

    highlightedAyah = 0;
    highlightedRowCount = 0;
    highlightedStartPosition = -1;
  }

  public void refresh(QuranSettings quranSettings) {
    this.fontSize = quranSettings.getTranslationTextSize();
    this.textColor = ContextCompat.getColor(context, R.color.translation_text_color);
    this.dividerColor = ContextCompat.getColor(context, R.color.translation_divider_color);
    this.arabicTextColor = Color.BLACK;
    this.suraHeaderColor = ContextCompat.getColor(context, R.color.translation_sura_header);
    this.ayahSelectionColor =
    ContextCompat.getColor(context, R.color.translation_ayah_selected_color);

    if (!this.data.isEmpty()) {
      notifyDataSetChanged();
    }
  }

  private boolean selectVerseRows(View view) {
    int position = recyclerView.getChildAdapterPosition(view);
    if (position != RecyclerView.NO_POSITION && onVerseSelectedListener != null) {
      QuranInfo ayahInfo = data.get(position).ayahInfo;
      highlightAyah(ayahInfo.ayahId, true);
      onVerseSelectedListener.onVerseSelected(ayahInfo);
      return true;
    }
    return false;
  }

  public int[] getSelectedVersePopupPosition() {
    int[] result = null;
    if (highlightedStartPosition > -1) {
      int versePosition = -1;
      int highlightedEndPosition = highlightedStartPosition + highlightedRowCount;
      for (int i = highlightedStartPosition; i < highlightedEndPosition; i++) {
        if (data.get(i).type == TranslationViewRow.Type.VERSE_NUMBER) {
          versePosition = i;
          break;
        }
      }

      if (versePosition > -1) {
        RowViewHolder viewHolder =
            (RowViewHolder) recyclerView.findViewHolderForAdapterPosition(versePosition);
        if (viewHolder != null && viewHolder.ayahNumber != null) {
          result = new int[2];
          result[0] += viewHolder.ayahNumber.getLeft() + viewHolder.ayahNumber.getBoxCenterX();
          result[1] += viewHolder.ayahNumber.getTop() + viewHolder.ayahNumber.getBoxBottomY();
        }
      }
    }
    return result;
  }

  @Override
  public int getItemViewType(int position) {
    return data.get(position).type;
  }

  @Override
  public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    @LayoutRes int layout;
    if (viewType == TranslationViewRow.Type.SURA_HEADER) {
      layout = R.layout.quran_translation_header_row;
    } else if (viewType == TranslationViewRow.Type.BASMALLAH ||
        viewType == TranslationViewRow.Type.QURAN_TEXT) {
      layout = R.layout.quran_translation_arabic_row;
    } else if (viewType == TranslationViewRow.Type.SPACER) {
      layout = R.layout.quran_translation_spacer_row;
    } else if (viewType == TranslationViewRow.Type.VERSE_NUMBER) {
      layout = R.layout.quran_translation_verse_number_row;
    } else if (viewType == TranslationViewRow.Type.TRANSLATOR) {
      layout = R.layout.quran_translation_translator_row;
    }else if (viewType == TranslationViewRow.Type.LAFDZI) {
      layout = R.layout.list_lafdzi;
    } else {
      layout = R.layout.quran_translation_text_row;
    }
    View view = inflater.inflate(layout, parent, false);
    return new RowViewHolder(view);
  }

  @SuppressLint("WrongConstant")
  @Override
  public void onBindViewHolder(RowViewHolder holder, int position) {
    TranslationViewRow row = data.get(position);
    if (holder.text != null) {
      final CharSequence text;
      if (row.type == TranslationViewRow.Type.SURA_HEADER) {
        text = BaseQuranInfo.getSuraName(context, row.ayahInfo.sura, true);
        holder.text.setBackgroundColor(suraHeaderColor);
      } else if (row.type == TranslationViewRow.Type.BASMALLAH ||
          row.type == TranslationViewRow.Type.QURAN_TEXT) {
        SpannableString str = new SpannableString(row.type == TranslationViewRow.Type.BASMALLAH ?
            AR_BASMALLAH : getAyahWithoutBasmallah(
            row.ayahInfo.sura, row.ayahInfo.ayah, row.ayahInfo.arabicText));
        if (USE_UTHMANI_SPAN) {
          str.setSpan(new UthmaniSpan(context), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        text = str;
        holder.text.setTextColor(arabicTextColor);
        holder.text.setTextSize(ARABIC_MULTIPLIER * fontSize);
      } else {
        if (row.type == TranslationViewRow.Type.TRANSLATOR) {
          text = row.data;
        } else {
          // translation
          if(row.type == TranslationViewRow.Type.TAFSIR_LATIN) {
            text = row.data;
            holder.text.setTextColor(textColor);
            holder.text.setTextSize(fontSize);
          }else{
            text = row.data;
            holder.text.setTextColor(textColor);
            holder.text.setTextSize(fontSize);
          }
        }
      }

      if(row.type == TranslationViewRow.Type.TAFSIR_LATIN ||
              row.type == TranslationViewRow.Type.TAFSIR_ARABIC){
          if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
          holder.text.setText(Html.fromHtml(text.toString(),0));
          else
            holder.text.setText(Html.fromHtml(text.toString()));
      }else{
          holder.text.setText(text);
      }

    } else if (holder.divider != null) {
      boolean showLine = true;
      if (position + 1 < data.size()) {
        TranslationViewRow nextRow = data.get(position + 1);
        if (nextRow.ayahInfo.sura != row.ayahInfo.sura) {
          showLine = false;
        }
      } else {
        showLine = false;
      }
      holder.divider.toggleLine(showLine);
      holder.divider.setDividerColor(dividerColor);
    } else if (holder.ayahNumber != null) {
      String text = BaseQuranInfo.setHurufArab(context,""+row.ayahInfo.ayah);//context.getString(R.string.sura_ayah, row.ayahInfo.sura, row.ayahInfo.ayah);
      //if(row.ayahInfo.ayah==200)
       // holder.ayahNumber.setAyahFavorite(true);
        holder.ayahNumber.setAyahString(text);
        holder.ayahNumber.setTextColor(textColor);
    } else if(holder.recyclerView!=null){
      if(Build.VERSION.SDK_INT>17)
        holder.recyclerView.setLayoutDirection(LAYOUT_DIRECTION_RTL);
      GridLayoutManager gridLayoutManager =new GridLayoutManager(context,5, LinearLayoutManager.VERTICAL, false);
      holder.recyclerView.setHasFixedSize(true);
      holder.recyclerView.setLayoutManager(gridLayoutManager);
      ListLafdziAdapter adapter = new ListLafdziAdapter(context, row.ayahInfo.lafdzi);
      holder.recyclerView.setAdapter(adapter);
     // adapter.notifyDataSetChanged();
    }
    updateHighlight(row, holder);
  }

  public int calculateNoOfColumns(Context context, int length) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
    int noOfColumns = (int) (dpWidth / length);
    return noOfColumns;
  }

  @Override
  public void onBindViewHolder(RowViewHolder holder, int position, List<Object> payloads) {
    if (payloads.contains(HIGHLIGHT_CHANGE)) {
      updateHighlight(data.get(position), holder);
    } else {
      super.onBindViewHolder(holder, position, payloads);
    }
  }

  private void updateHighlight(TranslationViewRow row, RowViewHolder holder) {
    // toggle highlighting of the ayah, but not for sura headers and basmallah
    boolean isHighlighted = row.ayahInfo.ayahId == highlightedAyah;
    if (row.type != TranslationViewRow.Type.SURA_HEADER &&
        row.type != TranslationViewRow.Type.BASMALLAH &&
        row.type != TranslationViewRow.Type.SPACER) {
      if (isHighlighted) {
        holder.wrapperView.setBackgroundColor(ayahSelectionColor);
      } else {
        holder.wrapperView.setBackgroundColor(0);
      }
    } else if (holder.divider != null) { // SPACER type
      if (isHighlighted) {
        holder.divider.highlight(ayahSelectionColor);
      } else {
        holder.divider.unhighlight();
      }
    }
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  class RowViewHolder extends RecyclerView.ViewHolder {
    View wrapperView;
    TextView text;
    AyahNumberView ayahNumber;
    DividerView divider;
    RecyclerView recyclerView;
    RowViewHolder(@NonNull View itemView) {
      super(itemView);
    //  this.wrapperView = itemView;
     // ButterKnife.bind(this, itemView);
      wrapperView = itemView;
      text = itemView.findViewById(R.id.text);
      divider = itemView.findViewById(R.id.divider);
      ayahNumber = itemView.findViewById(R.id.ayah_number);
      divider = itemView.findViewById(R.id.divider);
      recyclerView = itemView.findViewById(R.id.recycler_view);
      itemView.setOnClickListener(defaultClickListener);
      itemView.setOnLongClickListener(defaultLongClickListener);
    }
  }

  public interface OnVerseSelectedListener {
    void onVerseSelected(QuranInfo ayahInfo);
  }
}
