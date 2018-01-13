package smk.adzikro.indextemaquran.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.db.QuranDatabase;

/**
 * Created by server on 11/26/16.
 */

public class TemaFragment extends Fragment  {

    Context context;
    ListView listView;
    ArrayList<HashMap<String, String>> list;
    CustomListAdapter adapter;
    ListAyatAdapter listAyatAdapter;
    String[][] parent_ortu;
    int i, ortu;
    TextView info;
    ImageButton up;
    String[] data = new String[3];
    boolean tampilAyat=false;
    SearchView caritema;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_tema, container, false);
        context = getContext();
        listView = (ListView)view.findViewById(R.id.list_tema);
        info = (TextView)view.findViewById(R.id.infoTema);
        up = (ImageButton) view.findViewById(R.id.textParentTema);
        caritema = (SearchView)view.findViewById(R.id.cari_tema);

        parent_ortu = new String[12][12];
        parent_ortu[i][0]="0"; //parent
        parent_ortu[i][1]=""; //tema

        loadData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int ix, long l) {
//                Toast.makeText(getContext(), list.get(ix).get("id"),Toast.LENGTH_SHORT).show();

                if(tampilAyat)return;
                if(loading)return;
                ortu = Integer.valueOf(list.get(ix).get("id"));
                if(isEmpty(Integer.valueOf(list.get(ix).get("id")))) {
                    i++;
                    parent_ortu[i][0] = list.get(ix).get("id");
                    parent_ortu[i][1] = list.get(ix).get("tema");
                    tampilTema();
                 //   list_dir();
                }else {
                    i++;
                    parent_ortu[i][0] = list.get(ix).get("id");
                    parent_ortu[i][1] = list.get(ix).get("tema");
                    tampilAyatTema();
                    tampilAyat = true;
                  //  Toast.makeText(getContext(), list.get(i).get("tema") +"Terakhir ",Toast.LENGTH_SHORT).show();
                }
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loading)return;
                i = i - 1;
                if (i < 0) {
                    i = 0;
                    return;
                }
                if (i == 0) {
                    ortu = 0;
                } else {
                    ortu = Integer.valueOf(parent_ortu[i][0]);
                }
                data = getData(ortu);
                tampilTema();
                parent_ortu[i][0] = data[1];
                parent_ortu[i][1] = data[2];
                tampilAyat = false;
            }
        });
        caritema.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(loading)return false;
                tampilCariTema(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        return view;
    }

    private void loadData(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                list = getListParent(0);
                adapter = new CustomListAdapter(context, R.layout.list_tema,list);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    private String[] getData(int parent){
        String[] data = new String[3];
        SQLiteDatabase db = QuranDatabase.open(context);
        Cursor cursor = db.rawQuery("select * from TEMA where _id="+parent, null);
        cursor.moveToFirst();
        try {
            if (cursor.getCount() != 0) {
                data[0] = cursor.getString(cursor.getColumnIndex("_id"));
                data[1] = cursor.getString(cursor.getColumnIndex("PARENT"));
                data[2] = cursor.getString(cursor.getColumnIndex("TEMA"));
            }
            return data;

        } finally {
            cursor.close();
            db.close();
        }
    }
    boolean loading = false;

    private void tampilTema(){
        loading = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                list = getListParent(ortu);
                adapter = new CustomListAdapter(context, R.layout.list_tema,list);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                        list_dir();
                        loading = false;
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    private void tampilCariTema(final String tema){
        loading = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                list = getSearchTema(tema);
                adapter = new CustomListAdapter(context, R.layout.list_tema,list);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                        list_dir();
                        loading = false;
                    }
                });
            }
        };
        new Thread(runnable).start();
    }
    private void tampilAyatTema(){
        loading = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                list = getListAyat(ortu);
                listAyatAdapter = new ListAyatAdapter(context, R.layout.hasil,list);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(listAyatAdapter);
                        list_dir();
                        loading = false;
                    }
                });
            }
        };

        new Thread(runnable).start();
    }
    public void list_dir() {
        String judul = "";
        for (int xx = 1; xx < i; xx++) {
            if (judul.equals("")) {
                judul = parent_ortu[xx][1];
            } else {
                judul = judul + " ~ " + parent_ortu[xx][1];
            }
        }
        if (judul != "") {
            info.setText(judul + " ~ " + parent_ortu[i][1]);
        } else {
            info.setText(parent_ortu[i][1]);
        }
    }

    private boolean isEmpty(int id){
        boolean jadiOrtu=false;
        SQLiteDatabase db = QuranDatabase.open(context);
        Cursor cursor = db.rawQuery("select * from TEMA where parent="+id, null);
        cursor.moveToLast();
        if(cursor.getCount()==0){
            jadiOrtu = false;
        }else{
            jadiOrtu = true;
        }
        cursor.close();
        db.close();
        return  jadiOrtu;
    }

    public ArrayList<HashMap<String, String>> getListParent(int parent) {
        ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = QuranDatabase.open(context);
        Cursor cursor = db.rawQuery("select * from TEMA where parent="+parent, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("no", String.valueOf(cursor.getPosition()+1));
                hashMap.put("id", cursor.getString(cursor.getColumnIndex("_id")));
                hashMap.put("parent", cursor.getString(cursor.getColumnIndex("PARENT")));
                hashMap.put("tema", cursor.getString(cursor.getColumnIndex("TEMA")));
                alist.add(hashMap);
            }
        }
        cursor.close();
        db.close();
        return alist;
    }
    public ArrayList<HashMap<String, String>> getSearchTema(String tema) {
        ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = QuranDatabase.open(context);
        Cursor cursor = db.rawQuery("select * from TEMA where TEMA like'%"+tema+"%'", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("no", String.valueOf(cursor.getPosition()+1));
                hashMap.put("id", cursor.getString(cursor.getColumnIndex("_id")));
                hashMap.put("parent", cursor.getString(cursor.getColumnIndex("PARENT")));
                hashMap.put("tema", cursor.getString(cursor.getColumnIndex("TEMA")));
                alist.add(hashMap);
            }
        }
        cursor.close();
        db.close();
        return alist;
    }
    String TAG="TemaFragment";

    public ArrayList<HashMap<String, String>> getListAyat(int parent) {
        String query ="select AYAT_TEMA.*, ALQURAN.arab, ALQURAN.ind "+
                " from ALQURAN, AYAT_TEMA " +
                " where AYAT_TEMA.AYAT=ALQURAN.ayat "+
                " and AYAT_TEMA.SURAT=ALQURAN.surat "+
                " and AYAT_TEMA.id="+parent;

        Log.e(TAG,query);
        ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = QuranDatabase.open(context);
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor!=null) {
                while (cursor.moveToNext()) {
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("no", String.valueOf(cursor.getPosition() + 1));
                    hashMap.put("id", cursor.getString(cursor.getColumnIndex("id")));
                    hashMap.put("surat", cursor.getString(cursor.getColumnIndex("SURAT")));
                    hashMap.put("ayat", cursor.getString(cursor.getColumnIndex("AYAT")));
                    hashMap.put("arab", cursor.getString(cursor.getColumnIndex("arab")));
                    hashMap.put("arti", cursor.getString(cursor.getColumnIndex("ind")));
                 //   Log.e(TAG, cursor.getString(cursor.getColumnIndex("arab")) + "\n" + cursor.getString(cursor.getColumnIndex("ind")));
                    alist.add(hashMap);
                }
            }
        }finally {
            cursor.close();
            db.close();
        }
        return alist;
    }

    class CustomListAdapter extends ArrayAdapter<HashMap<String, String>> {
        Context context;
        int textViewResourceId;
        ArrayList<HashMap<String, String>> alist;

        public CustomListAdapter(Context context, int textViewResourceId,
                                 ArrayList<HashMap<String, String>> alist) {
            super(context, textViewResourceId);
            this.context = context;
            this.alist = alist;
            this.textViewResourceId = textViewResourceId;

        }

        public int getCount() {

            return alist.size();
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context)
                    .getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_tema,
                    parent, false);
            holder = new Holder();
            holder.no = (TextView) convertView
                    .findViewById(R.id.nomor);
            holder.judul = (TextView) convertView
                    .findViewById(R.id.txtTema);
            holder.lin_background = (LinearLayout) convertView
                    .findViewById(R.id.linear);
            convertView.setTag(holder);


            holder = (Holder) convertView.getTag();

            holder.no.setText(alist.get(pos).get("no"));
            holder.judul.setText(alist.get(pos).get("tema"));

            return convertView;

        }

        class Holder {
            TextView no, judul;
            LinearLayout lin_background;
        }
    }

    class ListAyatAdapter extends ArrayAdapter<HashMap<String, String>> {
        Context context;
        int textViewResourceId;
        ArrayList<HashMap<String, String>> alist;

        public ListAyatAdapter(Context context, int textViewResourceId,
                                 ArrayList<HashMap<String, String>> alist) {
            super(context, textViewResourceId);
            this.context = context;
            this.alist = alist;
            this.textViewResourceId = textViewResourceId;

        }

        public int getCount() {

            return alist.size();
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            final Typeface face;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String value = sharedPreferences.getString("huruf", null);
            final int sizeHuruf = sharedPreferences.getInt("size", 18);

            if (value == null) {
                face = Typeface.createFromAsset(context.getAssets(), "font/quran.ttf");
            } else {
                face = Typeface.createFromAsset(context.getAssets(), "font/" + value);
            }

            Holder holder = null;

            LayoutInflater inflater = ((Activity) context)
                    .getLayoutInflater();
            convertView = inflater.inflate(R.layout.hasil,
                    parent, false);
            holder = new Holder();
            holder.arab = (TextView) convertView
                    .findViewById(R.id.kolomKata);
            holder.arti = (TextView) convertView
                    .findViewById(R.id.kolomArtinya);
            holder.suratayat = (TextView) convertView
                    .findViewById(R.id.kolomAlamatnya);
            holder.lin_background = (LinearLayout) convertView
                    .findViewById(R.id.linear);
            convertView.setTag(holder);
            holder.arab.setTypeface(face);
            holder.arab.setTextSize(sizeHuruf);
            holder = (Holder) convertView.getTag();

            holder.arab.setText(alist.get(pos).get("arab"));
            holder.arti.setText(alist.get(pos).get("arti"));
            int surat = Integer.valueOf(alist.get(pos).get("surat"));
            int ayat = Integer.valueOf(alist.get(pos).get("ayat"));
            holder.suratayat.setText(BaseQuranInfo.getSuraAyahString(context,surat,ayat));
            return convertView;

        }

        class Holder {
            TextView arab, arti, suratayat;
            LinearLayout lin_background;
        }
    }
}
