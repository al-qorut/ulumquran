package smk.adzikro.indextemaquran.interfaces;


public interface AyahSelectedListener {

  public enum EventType { SINGLE_TAP, LONG_PRESS, DOUBLE_TAP }

  /** Return true to receive the ayah info along with the
   * click event, false to receive just the event type */
  public boolean isListeningForAyahSelection(EventType eventType);


  /** General click event without ayah info */
  public boolean onClick(EventType eventType);

}
