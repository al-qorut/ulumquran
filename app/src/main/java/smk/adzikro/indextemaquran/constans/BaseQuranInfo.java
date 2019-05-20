package smk.adzikro.indextemaquran.constans;


import android.content.Context;
import android.text.TextUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import smk.adzikro.indextemaquran.object.SuraAyahIterator;
import smk.adzikro.indextemaquran.ui.QuranUtils;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.object.SuraAyah;

import static smk.adzikro.indextemaquran.constans.Constants.PAGES_FIRST;
import static smk.adzikro.indextemaquran.constans.Constants.PAGES_LAST;

public class BaseQuranInfo {
  public static int[] SURA_PAGE_START = BaseQuranData.SURA_PAGE_START;
  public static int[] PAGE_SURA_START = BaseQuranData.PAGE_SURA_START;
  public static int[] PAGE_AYAH_START = BaseQuranData.PAGE_AYAH_START;
  public static int[] JUZ_PAGE_START = BaseQuranData.JUZ_PAGE_START;
  public static int[] PAGE_RUB3_START = BaseQuranData.PAGE_RUB3_START;
  public static int[] SURA_NUM_AYAHS = BaseQuranData.SURA_NUM_AYAHS;
  public static boolean[] SURA_IS_MAKKI = BaseQuranData.SURA_IS_MAKKI;
  public static int[][] QUARTERS = BaseQuranData.QUARTERS;
  /**
   * Get localized sura name from resources
   *
   * @param context    Application context
   * @param sura       Sura number (1~114)
   * @param wantPrefix Whether or not to show prefix "Sura"
   * @return Compiled sura name without translations
   */
  public static String getSuraName(Context context, int sura, boolean wantPrefix) {
    return getSuraName(context, sura, wantPrefix, false);
  }

  /**
   * Get localized sura name from resources
   *
   * @param context         Application context
   * @param sura            Sura number (1~114)
   * @param wantPrefix      Whether or not to show prefix "Sura"
   * @param wantTranslation Whether or not to show sura name translations
   * @return Compiled sura name based on provided arguments
   */
  public static String getSuraName(Context context, int sura,
                                   boolean wantPrefix, boolean wantTranslation) {
    if (sura < Constants.SURA_FIRST ||
        sura > Constants.SURA_LAST) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    if (wantPrefix) {
      builder.append(context.getString(R.string.quran_sura_title,
          context.getResources().getStringArray(R.array.nama_surat_ar)[sura - 1]));
    } else {
      builder.append(context.getResources().getStringArray(R.array.nama_surat_ar)[sura - 1]);
    }
    if (wantTranslation) {
      String translation = context.getResources().getStringArray(R.array.nama_surat_id)[sura - 1];
      if (!TextUtils.isEmpty(translation)) {
        // Some sura names may not have translation
        builder.append(" (");
        builder.append(translation);
        builder.append(")");
      }
    }

    return builder.toString();
  }
  public static int safelyGetSuraOnPage(int page) {
    if (page < PAGES_FIRST || page > PAGES_LAST) {
      //Crashlytics.logException(new IllegalArgumentException("got page: " + page));
      page = 1;
    }
    return PAGE_SURA_START[page - 1];
  }
  public static String getArtiSuraName(Context context, int sura) {
    if (sura < Constants.SURA_FIRST ||
            sura > Constants.SURA_LAST) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
      builder.append(context.getString(R.string.quran_sura_title_with_ayah,
              context.getResources().getStringArray(R.array.nama_surat)[sura - 1],
              BaseQuranData.SURA_NUM_AYAHS[sura-1]));

        return builder.toString();
  }
  public static int getSuraNumberFromPage(int page) {
    int sura = -1;
    for (int i = 0; i < Constants.SURAS_COUNT; i++) {
      if (SURA_PAGE_START[i] == page) {
        sura = i + 1;
        break;
      } else if (SURA_PAGE_START[i] > page) {
        sura = i;
        break;
      }
    }

    return sura;
  }

  public static String getSuraNameFromPage(Context context, int page,
                                           boolean wantTitle) {
    int sura = getSuraNumberFromPage(page);
    return (sura > 0) ? getSuraName(context, sura, wantTitle, false) : "";
  }

  public static String getPageSubtitle(Context context, int page) {
    String description = context.getString(R.string.page_description);
    return String.format(description,
        QuranUtils.getLocalizedNumber(context, page),
        QuranUtils.getLocalizedNumber(context,
                BaseQuranInfo.getJuzFromPage(page)));
  }

  public static String getJuzString(Context context, int page) {
    String description = context.getString(R.string.juz2_description);
    return String.format(description, QuranUtils.getLocalizedNumber(
        context, BaseQuranInfo.getJuzFromPage(page)));
  }

  public static String getSuraAyahString(Context context, int sura, int ayah) {
    String suraName = getSuraName(context, sura, false, false);
    return context.getString(R.string.sura_ayah_notification_str, suraName, ayah);
  }

  public static String getNotificationTitle(Context context,
                                            SuraAyah minVerse,
                                            SuraAyah maxVerse,
                                            boolean isGapless) {
    int minSura = minVerse.sura;
    int maxSura = maxVerse.sura;

    String notificationTitle =
            BaseQuranInfo.getSuraName(context, minSura, true, false);
    if (isGapless) {
      // for gapless, don't show the ayah numbers since we're
      // downloading the entire sura(s).
      if (minSura == maxSura) {
        return notificationTitle;
      } else {
        return notificationTitle + " - " +
                BaseQuranInfo.getSuraName(context, maxSura, true, false);
      }
    }

    int maxAyah = maxVerse.ayah;
    if (maxAyah == 0) {
      maxSura--;
      maxAyah = BaseQuranInfo.getNumAyahs(maxSura);
    }

    if (minSura == maxSura) {
      if (minVerse.ayah == maxAyah) {
        notificationTitle += " (" + maxAyah + ")";
      } else {
        notificationTitle += " (" + minVerse.ayah +
                "-" + maxAyah + ")";
      }
    } else {
      notificationTitle += " (" + minVerse.ayah +
              ") - " + BaseQuranInfo.getSuraName(context, maxSura, true, false) +
              " (" + maxAyah + ")";
    }

    return notificationTitle;
  }

  public static String getSuraListMetaString(Context context, int sura) {
    String info = context.getString(BaseQuranInfo.SURA_IS_MAKKI[sura - 1]
        ? R.string.makki : R.string.madani) + " - ";

    int ayahs = BaseQuranInfo.SURA_NUM_AYAHS[sura - 1];
    info += context.getResources().getQuantityString(R.plurals.verses, ayahs,
        QuranUtils.getLocalizedNumber(context, ayahs));
    return info;
  }

  public static Integer[] getPageBounds(int page) {
    if (page > PAGES_LAST)
      page = PAGES_LAST;
    if (page < 1) page = 1;

    Integer[] bounds = new Integer[4];
    bounds[0] = PAGE_SURA_START[page - 1];
    bounds[1] = PAGE_AYAH_START[page - 1];
    if (page == PAGES_LAST) {
      bounds[2] = Constants.SURA_LAST;
      bounds[3] = 6;
    } else {
      int nextPageSura = PAGE_SURA_START[page];
      int nextPageAyah = PAGE_AYAH_START[page];

      if (nextPageSura == bounds[0]) {
        bounds[2] = bounds[0];
        bounds[3] = nextPageAyah - 1;
      } else {
        if (nextPageAyah > 1) {
          bounds[2] = nextPageSura;
          bounds[3] = nextPageAyah - 1;
        } else {
          bounds[2] = nextPageSura - 1;
          bounds[3] = SURA_NUM_AYAHS[bounds[2] - 1];
        }
      }
    }
    return bounds;
  }

  public static String getSuraNameFromPage(Context context, int page) {
    for (int i = 0; i < Constants.SURAS_COUNT; i++) {
      if (SURA_PAGE_START[i] == page) {
        return getSuraName(context, i + 1, false, false);
      } else if (SURA_PAGE_START[i] > page) {
        return getSuraName(context, i, false, false);
      }
    }
    return "";
  }

  public static int getJuzFromPage(int page) {
    int juz = ((page - 2) / 20) + 1;
    return juz > 30 ? 30 : juz < 1 ? 1 : juz;
  }

  public static int getRub3FromPage(int page) {
    if ((page > PAGES_LAST) || (page < 1)) return -1;
    return PAGE_RUB3_START[page - 1];
  }

  public static int getPageFromSuraAyah(int sura, int ayah) {
    // basic bounds checking
    if (ayah == 0) ayah = 1;
    if ((sura < 1) || (sura > Constants.SURAS_COUNT)
        || (ayah < Constants.AYA_MIN) ||
        (ayah > Constants.AYA_MAX))
      return -1;

    // what page does the sura start on?
    int index = BaseQuranInfo.SURA_PAGE_START[sura - 1] - 1;
    while (index < PAGES_LAST) {
      // what's the first sura in that page?
      int ss = BaseQuranInfo.PAGE_SURA_START[index];

      // if we've passed the sura, return the previous page
      // or, if we're at the same sura and passed the ayah
      if (ss > sura || ((ss == sura) &&
          (BaseQuranInfo.PAGE_AYAH_START[index] > ayah))) {
        break;
      }

      // otherwise, look at the next page
      index++;
    }

    return index;
  }

  public static String setHurufArab(Context context,String ayat) {
    String hasil = String.format(context.getString(R.string.ayat), ayat);
    hasil = hasil.replaceAll("1", "١").replaceAll("2", "٢").
            replaceAll("3", "٣").replaceAll("4", "٤").replaceAll("5", "٥").replaceAll("6", "٦").
            replaceAll("7", "٧").replaceAll("8", "٨").replaceAll("9", "٩").replaceAll("0", "٠");
    return hasil;
  }
  public static String setHurufArabUthman(Context context,String ayat) {
    String hasil = ayat;
    hasil = hasil.replaceAll("1", "١").replaceAll("2", "٢").
            replaceAll("3", "٣").replaceAll("4", "٤").replaceAll("5", "٥").replaceAll("6", "٦").
            replaceAll("7", "٧").replaceAll("8", "٨").replaceAll("9", "٩").replaceAll("0", "٠");
    return hasil;
  }
  public static int getAyahId(int sura, int ayah) {
    int ayahId = 0;
    for (int i = 0; i < sura - 1; i++) {
      ayahId += SURA_NUM_AYAHS[i];
    }
    ayahId += ayah;
    return ayahId;
  }



  public static int getNumAyahs(int sura) {
    if ((sura < 1) || (sura > Constants.SURAS_COUNT)) return -1;
    return SURA_NUM_AYAHS[sura - 1];
  }

  public static int getPageFromPos(int position) {
    int page = PAGES_LAST - position;
    return page;
  }

  public static int getPosFromPage(int page) {
    int position = PAGES_LAST - page;
    return position;
  }

  public static String getAyahString(int sura, int ayah, Context context) {
    return getSuraName(context, sura, true) + " - " + context.getString(R.string.quran_ayah,
        Integer.valueOf(QuranUtils.getLocalizedNumber(context, ayah)));
  }

  public static String getAyahMetadata(int sura, int ayah, int page, Context context) {
    int juz = getJuzFromPage(page);
    return context.getString(R.string.quran_ayah_details, getSuraName(context, sura, true),
        QuranUtils.getLocalizedNumber(context, ayah), QuranUtils.getLocalizedNumber(context, juz));
  }

  public static String getSuraNameString(Context context, int page) {
    return context.getString(R.string.quran_sura_title, getSuraNameFromPage(context, page));
  }

  public static Set<String> getAyahKeysOnPage(int page, SuraAyah lowerBound, SuraAyah upperBound) {
    Set<String> ayahKeys = new LinkedHashSet<>();
    Integer bounds[] = BaseQuranInfo.getPageBounds(page);
    SuraAyah start = new SuraAyah(bounds[0], bounds[1]);
    SuraAyah end = new SuraAyah(bounds[2], bounds[3]);
    if (lowerBound != null) {
      start = SuraAyah.max(start, lowerBound);
    }
    if (upperBound != null) {
      end = SuraAyah.min(end, upperBound);
    }
    SuraAyahIterator iterator = new SuraAyahIterator(start, end);
    while (iterator.next()) {
      ayahKeys.add(iterator.getSura() + ":" + iterator.getAyah());
    }
    return ayahKeys;
  }

}
