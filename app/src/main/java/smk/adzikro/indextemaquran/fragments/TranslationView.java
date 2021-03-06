package smk.adzikro.indextemaquran.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.adapter.TranslationAdapter;
import smk.adzikro.indextemaquran.interfaces.OnTranslationActionListener;
import smk.adzikro.indextemaquran.object.QuranInfo;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.TranslationViewRow;
import smk.adzikro.indextemaquran.widgets.AyahToolBar;

public class TranslationView extends FrameLayout implements View.OnClickListener,
    TranslationAdapter.OnVerseSelectedListener,
    MenuItem.OnMenuItemClickListener {
  private static final String TAG = TranslationView.class.getSimpleName();
  private final TranslationAdapter translationAdapter;
  private final AyahToolBar ayahToolBar;

  private List<String> translations;
  private QuranInfo selectedAyah;
  private OnClickListener onClickListener;
  private OnTranslationActionListener onTranslationActionListener;
  private LinearLayoutManager layoutManager;

  public TranslationView(Context context) {
    this(context, null);
  }

  public TranslationView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TranslationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    Log.e(TAG,"oncreate");
    RecyclerView translationRecycler = new RecyclerView(context);
    layoutManager = new LinearLayoutManager(context);
    translationRecycler.setLayoutManager(layoutManager);
    translationRecycler.setItemAnimator(new DefaultItemAnimator());
    translationAdapter = new TranslationAdapter(context, translationRecycler, this, this);
    translationRecycler.setAdapter(translationAdapter);
    addView(translationRecycler, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    translationRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        // do not modify the RecyclerView from this method or any method called from
        // the onScrolled listener, since most modification methods cannot be called
        // while the RecyclerView is computing layout or scrolling
        if (selectedAyah != null) {
          updateAyahToolBarPosition();
        }
      }
    });

    ayahToolBar = new AyahToolBar(context, R.menu.share_menu);
    ayahToolBar.setOnItemSelectedListener(this);
    ayahToolBar.setVisibility(View.GONE);
    addView(ayahToolBar, LayoutParams.WRAP_CONTENT,
        context.getResources().getDimensionPixelSize(R.dimen.toolbar_total_height));
  }
  public void setData(List<TranslationViewRow> rows ){
    translationAdapter.setData(rows);
   // this.translations = rows.
    translationAdapter.notifyDataSetChanged();
    translationAdapter.refresh(QuranSettings.getInstance(getContext()));
  }


  public void refresh(@NonNull QuranSettings quranSettings) {
    translationAdapter.refresh(quranSettings);
  }

  public void setTranslationClickedListener(OnClickListener listener) {
    onClickListener = listener;
  }

  public void setOnTranslationActionListener(OnTranslationActionListener listener) {
    onTranslationActionListener = listener;
  }

  public void highlightAyah(int ayahId) {
    translationAdapter.setHighlightedAyah(ayahId);
  }

  public void unhighlightAyat() {
    if (selectedAyah != null) {
      selectedAyah = null;
      ayahToolBar.hideMenu();
    }
    translationAdapter.unhighlight();
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    if (onTranslationActionListener != null && selectedAyah != null) {
      onTranslationActionListener.onTranslationAction(selectedAyah, translations, item.getItemId());
      return true;
    }
    return false;
  }

  @Override
  public void onClick(View v) {
    if (selectedAyah != null) {
      ayahToolBar.hideMenu();
      unhighlightAyat();
      selectedAyah = null;
    }

    if (onClickListener != null) {
      onClickListener.onClick(v);
    }
  }

  /**
   * This method updates the toolbar position when an ayah is selected
   * This method is called from the onScroll listener, and as thus must make sure not to ask
   * the RecyclerView to change anything (otherwise, it will result in a crash, as methods to
   * update the RecyclerView cannot be called amidst scrolling or computing of a layout).
   */
  private void updateAyahToolBarPosition() {
    int[] versePopupPosition = translationAdapter.getSelectedVersePopupPosition();
    if (versePopupPosition != null) {
      AyahToolBar.AyahToolBarPosition position = new AyahToolBar.AyahToolBarPosition();
      if (versePopupPosition[1] > getHeight() || versePopupPosition[1] < 0) {
        ayahToolBar.hideMenu();
      } else {
        position.x = versePopupPosition[0];
        position.y = versePopupPosition[1];
        position.pipPosition = AyahToolBar.PipPosition.UP;
        if (!ayahToolBar.isShowing()) {
          ayahToolBar.showMenu();
        }
        ayahToolBar.updatePositionRelative(position);
      }
    }
  }

  @Override
  public void onVerseSelected(QuranInfo ayahInfo) {
    selectedAyah = ayahInfo;
    updateAyahToolBarPosition();
  }

  public int findFirstCompletelyVisibleItemPosition() {
    return layoutManager.findFirstCompletelyVisibleItemPosition();
  }

  public void setScrollPosition(int position) {
    layoutManager.scrollToPosition(position);
  }


}
