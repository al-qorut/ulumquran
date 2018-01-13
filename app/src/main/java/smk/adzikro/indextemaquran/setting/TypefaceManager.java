package smk.adzikro.indextemaquran.setting;


import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import smk.adzikro.indextemaquran.constans.QuranFileConstants;

public class TypefaceManager {
  public static final int TYPE_UTHMANI_HAFS = 1;
  public static final int TYPE_NOOR_HAYAH = 2;

  private static Typeface sTypeface;

  public static Typeface getUthmaniTypeface(@NonNull Context context) {
    if (sTypeface == null) {
      final String fontName;
      switch (QuranFileConstants.FONT_TYPE) {
        case TYPE_NOOR_HAYAH: {
          fontName = "quran.ttf";
          break;
        }
        case TYPE_UTHMANI_HAFS:
        default: {
          fontName = "uthmanic_hafs_ver09.otf";
        }
      }
      sTypeface = Typeface.createFromAsset(context.getAssets(),"font/"+fontName);
    }
    return sTypeface;
  }
}
