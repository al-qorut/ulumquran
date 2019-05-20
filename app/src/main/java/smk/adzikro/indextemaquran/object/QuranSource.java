package smk.adzikro.indextemaquran.object;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by server on 1/2/18.
 */

public class QuranSource implements Parcelable {
    public String displayName;
    public String translator=" ";
    public String translator_asing=" ";
    public String file_url;
    public String file_name;
    public int ada=0;
    public int type=0;
    public int id=0;
    public String languageCode;
    public int getId() {
        return id;
    }



    public void setId(int id) {
        this.id = id;
    }

    int active=0;
    public QuranSource(String nama, String translator, String file_name, String file_url){
        this.displayName = nama;
        this.translator = translator;
        this.file_name = file_name;
        this.file_url = file_url;
    }
    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTranslator() {
        return translator;
    }

    public void setTranslator(String translator) {
        this.translator = translator;
    }

    public String getTranslator_asing() {
        return translator_asing;
    }

    public void setTranslator_asing(String translator_asing) {
        this.translator_asing = translator_asing;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public int getAda() {
        return ada;
    }

    public void setAda(int ada) {
        this.ada = ada;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeString(this.translator);
        dest.writeString(this.translator_asing);
        dest.writeString(this.file_url);
        dest.writeString(this.file_name);
        dest.writeInt(this.ada);
        dest.writeInt(this.type);
        dest.writeInt(this.id);
        dest.writeInt(this.active);
    }

    protected QuranSource(Parcel in) {
        this.displayName = in.readString();
        this.translator = in.readString();
        this.translator_asing = in.readString();
        this.file_url = in.readString();
        this.file_name = in.readString();
        this.ada = in.readInt();
        this.type = in.readInt();
        this.id = in.readInt();
        this.active = in.readInt();
    }

    public static final Parcelable.Creator<QuranSource> CREATOR = new Parcelable.Creator<QuranSource>() {
        @Override
        public QuranSource createFromParcel(Parcel source) {
            return new QuranSource(source);
        }

        @Override
        public QuranSource[] newArray(int size) {
            return new QuranSource[size];
        }
    };
}
