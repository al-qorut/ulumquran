package smk.adzikro.indextemaquran.activities;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.provider.PenghubungData;
import smk.adzikro.indextemaquran.db.QuranDatabase;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;

/**
 * Created by server on 11/21/15.
 */
public class CariAyatQuran extends AppCompatActivity {

    private TextView munculTulisan;
    private ListView barisanKata;
    Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tampilan);

        munculTulisan = (TextView) findViewById(R.id.tulisanMuncul);
        barisanKata = (ListView) findViewById(R.id.barisDemiBaris);
        handleIntent(getIntent());
        registerForContextMenu(barisanKata);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // oleh karena aktivitas-nya di set dengan launchMode="singleTop", itu berarti
        // halaman(aktivitas) dapat di daur ulang dalam arti kalau halaman telah
        // di buka sebelumnya maka tak perlu di buka lagi tapi hanya menghidupkan
        // intent pada halaman tsb sehingga mengangkatnya ke depan (terlihat lagi)


        handleIntent(intent);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {


        menu.add(Menu.NONE, 0, 0, "Copy");
        menu.add(Menu.NONE, 1, 1, "Share");
        menu.add(Menu.NONE, 2, 2, "Lihat Tafsirnya");



    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String a, ayat, isi,nama;
        int s;
        if(cursor==null){
            return false;
        }
        s =cursor.getInt(cursor.getColumnIndex(QuranDatabase.FIELD_SURAT));
        a =cursor.getString(cursor.getColumnIndex(QuranDatabase.FIELD_AYAT));
        ayat =cursor.getString(cursor.getColumnIndex(QuranDatabase.FIELD_ARAB));
        isi=cursor.getString(cursor.getColumnIndex(QuranDatabase.FIELD_IND));
        nama = BaseQuranInfo.getSuraName(this,s,false,true);
        int page = BaseQuranInfo.getPageFromSuraAyah(s,Integer.valueOf(a));
        CharSequence gabung = ayat+"\n"+isi+"\n\n"+"@indexQuranAdz-Dzikro (QS "+nama+":"+a+")";

        if (item.getTitle() == "Copy") {
            //Toast.makeText(this, "Copy.."+menuInfo.id+", position "+menuInfo.position+cursor.getString(cursor.getColumnIndex("arab")), Toast.LENGTH_SHORT).show();
            ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copy",gabung);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Sukses Copy ayat kedalam memory silahkan paste di tempat lain ", Toast.LENGTH_SHORT).show();

        } else if (item.getTitle() == "Share") {
//            Toast.makeText(this, "Share "+menuInfo.id+", position "+menuInfo.position, Toast.LENGTH_SHORT).show();
            Fungsi.share(this,gabung);
        } else if (item.getTitle() == "Lihat Tafsirnya") {
//            Toast.makeText(this, "Share "+menuInfo.id+", position "+menuInfo.position, Toast.LENGTH_SHORT).show();
        //    Fungsi.jumpTo(this,page,4);
        } else {
            return false;
        }
        return true;
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // mengatur klik utk search suggestion dan menghidupkan
            // activity yg membuat kata2 bisa di klik
           // if(Fungsi.getTerjemahAktif(CariAyatQuran.this)){
             //   Intent agarKataDptDiKlik = new Intent(this, FragmenTerjemah.class);
           //     startActivity(agarKataDptDiKlik);
         //   }else {
                Intent agarKataDptDiKlik = new Intent(this, TampilanKata.class);
                //Intent agarKataDptDiKlik = new Intent(this, htmView.class);
                agarKataDptDiKlik.setData(intent.getData());
                startActivity(agarKataDptDiKlik);
       //     }
            finish();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // mengatur query pencarian
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }
    /**
     * cari di kamus dan tunjukan hasil dari query tertentu
     * @parameter query-nya adalah search query
     */
    private void showResults(String query) {

        cursor = managedQuery(PenghubungData.CONTENT_URI, null, null,
                new String[] {query}, null);

        if (cursor == null) {
            // kalau tak ada hasil di ketemukan
            munculTulisan.setText(getString(R.string.tak_ada_hasil, new Object[]{query}));
        } else {
            // tunjukan beberapa hasil
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.hasil_pencarian,
                    count, new Object[] {count, query});
            munculTulisan.setText(countString);

            // KATA di taruh di kolom 'kolomKata'
            // ARTI_NYA di taruh di 'kolomArtinya'
            String[] dari = new String[] {
                    QuranDatabase.FIELD_ARAB,
                    QuranDatabase.FIELD_IND
                    //QuranDatabase.FIELD_ID
                    //"Qs. ("+QuranDatabase.FIELD_SURAT+"||':'||"+QuranDatabase.FIELD_AYAT+")"
                    };//, KamusDatabase.ALAMAT };

            // buatkan hubungan antara design element, dimana kolom akan muncul
            int[] ke = new int[] { R.id.kolomKata,
                    R.id.kolomArtinya};
                    //R.id.kolomAlamatnya };

            // buatkan cursor adapter sederhana utk semua kata dan
            // artinya dan tayangkan baris demi baris(ListView) pada layar
            SimpleCursorAdapter letakanKataPadaTempatnya = new SimpleCursorAdapter(this,
                    R.layout.hasil, cursor, dari, ke,0)
            {
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    final Typeface face;
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    final String value = sharedPreferences.getString("huruf", null);
                    final int sizeHuruf = sharedPreferences.getInt("size", 18);

                    if (value == null) {
                        face = Typeface.createFromAsset(context.getAssets(), "font/quran.ttf");
                    } else {
                        face = Typeface.createFromAsset(context.getAssets(), "font/" + value);
                    }

                    TextView ar = (TextView) view.findViewById(R.id.kolomKata);
                    TextView at = (TextView) view.findViewById(R.id.kolomArtinya);
                    TextView ay = (TextView) view.findViewById(R.id.kolomAlamatnya);
                    ar.setText(cursor.getString(cursor.getColumnIndex(QuranDatabase.FIELD_ARAB)));
                    at.setText(cursor.getString(cursor.getColumnIndex(QuranDatabase.FIELD_IND)));

                    int surat = cursor.getInt(cursor.getColumnIndex(QuranDatabase.FIELD_SURAT));
                    int ayat = cursor.getInt(cursor.getColumnIndex(QuranDatabase.FIELD_AYAT));
                    String nama = BaseQuranInfo.getSuraName(CariAyatQuran.this, surat, false, true);
                    ay.setText("QS: "+nama+" ("+surat+":"+ayat+")");
                    ar.setTypeface(face);
                    ar.setTextSize(sizeHuruf);

                }
            };
            barisanKata.setAdapter(letakanKataPadaTempatnya);

            // apa yg terjadi saat klik pada kata2 yang telah berjajar baris demi baris di layar
            barisanKata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // ketika sebauh kata dapat di klik
            /*        if(Fungsi.getTerjemahAktif(CariAyatQuran.this)){
                        Intent intent =new Intent();
                        Uri data = Uri.withAppendedPath(PenghubungData.CONTENT_URI,
                        String.valueOf(id));
                        intent.setData(data);
                        setResult(RESULT_OK, intent);
                   //     startActivityForResult(intent,1);
                        finish();
                    }else { */
                        Intent kataDiKlik = new Intent(getApplicationContext(), TampilanKata.class);
                        //Intent kataDiKlik = new Intent(getApplicationContext(), htmView.class);
                        Uri data = Uri.withAppendedPath(PenghubungData.CONTENT_URI,
                                String.valueOf(id));
                        kataDiKlik.setData(data);
                        startActivity(kataDiKlik);
             //       }

                 //   Toast.makeText(getApplicationContext(),String.valueOf(id),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    public void onDestroy(){
        if(cursor!=null){
            cursor.close();
        }
        super.onDestroy();
    }

}
