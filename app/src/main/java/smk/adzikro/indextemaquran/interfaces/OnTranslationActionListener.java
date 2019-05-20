package smk.adzikro.indextemaquran.interfaces;


import java.util.List;

import smk.adzikro.indextemaquran.object.QuranInfo;

public interface OnTranslationActionListener {
  void onTranslationAction(QuranInfo ayah, List<String> translationNames, int actionId);
}
