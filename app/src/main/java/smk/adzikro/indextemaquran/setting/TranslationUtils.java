package smk.adzikro.indextemaquran.setting;


import android.content.Context;

import java.util.List;

import smk.adzikro.indextemaquran.object.TranslationItem;
import smk.adzikro.indextemaquran.setting.QuranSettings;

public class TranslationUtils {

   public static String getDefaultTranslation(Context context,
                                       List<TranslationItem> items) {
     final TranslationItem item = getDefaultTranslationItem(context, items);
     return item == null ? null : item.filename;
   }

  public static TranslationItem getDefaultTranslationItem(Context context,
      List<TranslationItem> items){
      if (items == null || items.size() == 0){ return null; }
      QuranSettings settings = QuranSettings.getInstance(context.getApplicationContext());
      final String db = settings.getActiveTranslation();

      TranslationItem result = null;
      boolean changed = false;
      if (db == null){
         changed = true;
         result = items.get(0);
      }
      else {
         boolean found = false;
         for (TranslationItem item : items){
            if (item.filename.equals(db)){
               found = true;
               result = item;
               break;
            }
         }

         if (!found){
            changed = true;
            result = items.get(0);
         }
      }

      if (changed && result != null){
        settings.setActiveTranslation(result.filename);
      }

      return result;
   }
}
