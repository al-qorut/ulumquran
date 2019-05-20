package smk.adzikro.indextemaquran.services.utils;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.Log;

import smk.adzikro.indextemaquran.object.QariItem;
import smk.adzikro.indextemaquran.object.SuraAyah;
import smk.adzikro.indextemaquran.util.AudioUtils;


public class DownloadAudioRequest extends AudioRequest {

  private static final String TAG = "DownloadAudioRequest";
  @NonNull private final QariItem qariItem;
  private String localDirectoryPath = null;

  public DownloadAudioRequest(String baseUrl, SuraAyah verse,
      @NonNull QariItem qariItem, String localPath) {
    super(baseUrl, verse);
    Log.e(TAG,"DownloadAudioRequest create "+baseUrl+" path "+localPath);
    this.qariItem = qariItem;
    localDirectoryPath = localPath;
  }

  private DownloadAudioRequest(Parcel in) {
    super(in);
    this.qariItem = in.readParcelable(QariItem.class.getClassLoader());
    this.localDirectoryPath = in.readString();
  }
  
  @NonNull
  public QariItem getQariItem() {
    return qariItem;
  }

  public String getLocalPath() {
    return localDirectoryPath;
  }

  @Override
  public boolean haveSuraAyah(int sura, int ayah) {
    return AudioUtils.haveSuraAyahForQari(localDirectoryPath, sura, ayah);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeParcelable(this.qariItem, 0);
    dest.writeString(this.localDirectoryPath);
  }

  public static final Creator<DownloadAudioRequest> CREATOR = new Creator<DownloadAudioRequest>() {
    @Override
    public DownloadAudioRequest createFromParcel(Parcel source) {
      return new DownloadAudioRequest(source);
    }

    @Override
    public DownloadAudioRequest[] newArray(int size) {
      return new DownloadAudioRequest[size];
    }
  };
}
