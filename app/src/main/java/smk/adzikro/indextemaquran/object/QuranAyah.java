package smk.adzikro.indextemaquran.object;

import android.os.Parcel;
import android.os.Parcelable;

public class QuranAyah implements Parcelable {

  private int mSura = 0;
  private int mAyah = 0;
  private String textArab, textLatin, textTafsir, textTarzim;

  public QuranAyah(int sura, int ayah){
    this.mSura = sura;
    this.mAyah = ayah;
  }

  public String getTextArab() {
    return textArab;
  }

  public int getSura() {
    return mSura;
  }

  public void setSura(int mSura) {
    this.mSura = mSura;
  }

  public int getAyah() {
    return mAyah;
  }

  public void setAyah(int mAyah) {
    this.mAyah = mAyah;
  }

  public void setTextArab(String textArab) {
    this.textArab = textArab;
  }

  public String getTextLatin() {
    return textLatin;
  }

  public void setTextLatin(String textLatin) {
    this.textLatin = textLatin;
  }

  public String getTextTafsir() {
    return textTafsir;
  }

  public void setTextTafsir(String textTafsir) {
    this.textTafsir = textTafsir;
  }

  public String getTextTarzim() {
    return textTarzim;
  }

  public void setTextTarzim(String textTarzim) {
    this.textTarzim = textTarzim;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.mSura);
    dest.writeInt(this.mAyah);
    dest.writeString(this.textArab);
    dest.writeString(this.textLatin);
    dest.writeString(this.textTafsir);
    dest.writeString(this.textTarzim);
  }

  protected QuranAyah(Parcel in) {
    this.mSura = in.readInt();
    this.mAyah = in.readInt();
    this.textArab = in.readString();
    this.textLatin = in.readString();
    this.textTafsir = in.readString();
    this.textTarzim = in.readString();
  }

  public static final Parcelable.Creator<QuranAyah> CREATOR = new Parcelable.Creator<QuranAyah>() {
    @Override
    public QuranAyah createFromParcel(Parcel source) {
      return new QuranAyah(source);
    }

    @Override
    public QuranAyah[] newArray(int size) {
      return new QuranAyah[size];
    }
  };
}
