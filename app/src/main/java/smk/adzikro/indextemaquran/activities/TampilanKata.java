package smk.adzikro.indextemaquran.activities;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.db.QuranDatabase;

public class TampilanKata extends AppCompatActivity{

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kata);

        Uri uri = getIntent().getData();
        Cursor cursor = managedQuery(uri, null, null, null, null);
        setTitle("Hasil pencarian..");
        if (cursor == null) {
            finish();
        } else {
            cursor.moveToFirst();

            TextView kataKata = (TextView) findViewById(R.id.kolomKata);
            TextView artinya = (TextView) findViewById(R.id.kolomArtinya);
            TextView suratayat =(TextView)findViewById(R.id.kolomAlamatnya);

            int indexKata = cursor.getColumnIndexOrThrow(QuranDatabase.FIELD_ARAB);
            int indexArtinya = cursor.getColumnIndexOrThrow(QuranDatabase.FIELD_IND);
            int indexSurat = cursor.getColumnIndexOrThrow(QuranDatabase.FIELD_SURAT);
            int indexAyat = cursor.getColumnIndexOrThrow(QuranDatabase.FIELD_AYAT);
            String nama = BaseQuranInfo.getSuraName(this,cursor.getInt(indexSurat),false,true);
            kataKata.setText(cursor.getString(indexKata));
            artinya.setText(cursor.getString(indexArtinya));
            suratayat.setText("QS "+nama+"("+cursor.getString(indexSurat)+":"+cursor.getString(indexAyat)+")");
        }
    }


}