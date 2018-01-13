package smk.adzikro.indextemaquran.interfaces;


import android.view.MotionEvent;

import smk.adzikro.indextemaquran.interfaces.AyahSelectedListener;

public interface PageController {
  boolean handleTouchEvent(MotionEvent event,
                           AyahSelectedListener.EventType eventType, int page);
  void handleRetryClicked();
  void onScrollChanged(int x, int y, int oldx, int oldy);
}
