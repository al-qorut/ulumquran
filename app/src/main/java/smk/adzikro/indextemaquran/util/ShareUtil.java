package smk.adzikro.indextemaquran.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.StringRes;
import android.text.Html;
import android.widget.Toast;


import java.util.List;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.object.Ayah;
import smk.adzikro.indextemaquran.object.QuranInfo;
import smk.adzikro.indextemaquran.object.QuranText;

public class ShareUtil {

  public static void copyVerses(Activity activity, List<QuranText> verses) {
    String text = getShareText(activity, verses);
    copyToClipboard(activity, text);
  }

  public static void copyToClipboard(Activity activity, String text) {
    ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(activity.getString(R.string.app_name), text);
    cm.setPrimaryClip(clip);
    Toast.makeText(activity, activity.getString(R.string.ayah_copied_popup),
        Toast.LENGTH_SHORT).show();
  }

  public static void shareVerses(Activity activity, List<QuranText> verses) {
    String text = getShareText(activity, verses);
    shareViaIntent(activity, text, R.string.share_ayah_text);
  }

  public static void shareViaIntent(Activity activity, String text, @StringRes int titleResId) {
    final Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_TEXT, text);
    activity.startActivity(Intent.createChooser(intent, activity.getString(titleResId)));
  }

  public static String getShareText(Context context,
                                    QuranInfo ayahInfo,
                                    List<String> translationNames) {
    final StringBuilder sb = new StringBuilder();
    if (ayahInfo.arabicText != null) {
      sb.append(ayahInfo.arabicText)
        .append("\n\n");
    }

    for (int i = 0, size = ayahInfo.texts.size(); i < size; i++) {
      if (i < translationNames.size()) {
        sb.append('(')
          .append(translationNames.get(i))
          .append(")\n");
      }
      sb.append(Html.fromHtml(ayahInfo.texts.get(i)))
        .append("\n\n");
    }
    sb.append('-')
      .append(BaseQuranInfo.getSuraAyahString(context, ayahInfo.sura, ayahInfo.ayah));

    return sb.toString();
  }
  public static String getShareTextFromAyah(Context context,
                                    List<Ayah> ayahInfo) {
    final StringBuilder sb = new StringBuilder();
    for (int i=0; i< ayahInfo.size();i++) {
      Ayah ayah = ayahInfo.get(i);
      sb.append(ayah.arab);
      sb.append("\n\n");
      if(ayah.arti!=null) {
        sb.append(ayah.arti);
        sb.append("\n\n");
      }
        sb.append(BaseQuranInfo.getSuraAyahString(context, ayah.sura, ayah.ayat));
      sb.append("\n\n");
    }
    return sb.toString();
  }

  private static String getShareText(Activity activity, List<QuranText> verses) {
    final int size = verses.size();

    StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < size; i++) {
      sb.append(verses.get(i).text);
      if (i + 1 < size) {
        sb.append(" * ");
      }
    }

    // append ) and a new line after last ayah
    sb.append(")\n");
    // append [ before sura label
    sb.append("[");

    final QuranText firstAyah = verses.get(0);
    sb.append(BaseQuranInfo.getSuraName(activity, firstAyah.sura, true));
    sb.append(" ");
    sb.append(firstAyah.ayah);
    if (size > 1) {
      final QuranText lastAyah = verses.get(size - 1);
      sb.append(" - ");
      if (firstAyah.sura != lastAyah.sura) {
        sb.append(BaseQuranInfo.getSuraName(activity, lastAyah.sura, true));
        sb.append(" ");
      }
      sb.append(lastAyah.ayah);
    }
    // close sura label and append two new lines
    sb.append("]\n\n");

    sb.append(activity.getString(R.string.via_string));
    return sb.toString();
  }
}
