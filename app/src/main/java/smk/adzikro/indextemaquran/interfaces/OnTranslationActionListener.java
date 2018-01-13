package smk.adzikro.indextemaquran.interfaces;


import smk.adzikro.indextemaquran.object.QuranInfo;

public interface OnTranslationActionListener {
  void onTranslationAction(QuranInfo ayah, String[] translationNames, int actionId);
}
