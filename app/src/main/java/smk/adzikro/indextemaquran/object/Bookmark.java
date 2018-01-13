package smk.adzikro.indextemaquran.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bookmark {

  public final long id;
  public final Integer sura;
  public final Integer ayah;
  public final int page;
  public final int aksi;
  public final long timestamp;
  public final List<Long> tags;

  public Bookmark(long id, Integer sura, Integer ayah, int page, int aksi) {
    this(id, sura, ayah, page, aksi, System.currentTimeMillis());
  }

  public Bookmark(long id, Integer sura, Integer ayah, int page, int aksi,long timestamp) {
    this(id, sura, ayah, page, aksi, timestamp, Collections.<Long>emptyList());
  }

  public Bookmark(long id, Integer sura, Integer ayah, int page, int aksi,long timestamp, List<Long> tags) {
    this.id = id;
    this.sura = sura;
    this.ayah = ayah;
    this.page = page;
    this.aksi = aksi;
    this.timestamp = timestamp;
    this.tags = Collections.unmodifiableList(tags);
  }

  public boolean isPageBookmark() {
    return sura == null && ayah == null;
  }

  public Bookmark withTags(List<Long> tagIds) {
    return new Bookmark(id, sura, ayah, page, aksi, timestamp, new ArrayList<>(tagIds));
  }

  public String getAyahText() {
    return null;
  }


}
