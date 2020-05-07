package smk.adzikro.indextemaquran.widgets;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.View;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.fragments.TranslationView;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.ui.QuranPageLayout;


public class QuranTranslationPageLayout extends QuranPageLayout {
  private TranslationView translationView;

  public QuranTranslationPageLayout(Context context) {
    super(context);
  }

  @Override
  protected View generateContentView(Context context) {
    translationView = new TranslationView(context);
    return translationView;
  }

  @Override
  protected void setContentNightMode(boolean nightMode, int textBrightness) {

  }

  @Override
  protected boolean shouldWrapWithScrollView() {
    return false;
  }

  public TranslationView getTranslationView() {
    return translationView;
  }
}
