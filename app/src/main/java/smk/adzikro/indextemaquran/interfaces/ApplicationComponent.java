package smk.adzikro.indextemaquran.interfaces;


import smk.adzikro.indextemaquran.activities.MainActivity;
import smk.adzikro.indextemaquran.activities.UlumQuranActivity;
import smk.adzikro.indextemaquran.services.QuranDownloadService;

public interface ApplicationComponent {
  // subcomponents

  // content provider

  // services
  void inject(QuranDownloadService quranDownloadService);

  // activities
  void inject(MainActivity quranActivity);
  void inject(UlumQuranActivity quranImportActivity);


}
