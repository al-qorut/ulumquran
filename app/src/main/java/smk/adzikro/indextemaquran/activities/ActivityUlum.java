package smk.adzikro.indextemaquran.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

//import net.sqlcipher.database.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashMap;

import smk.adzikro.indextemaquran.db.QuranDatabase;
import smk.adzikro.indextemaquran.R;

/**
 * Created by server on 3/21/16.
 */
public class ActivityUlum extends AppCompatActivity {


    ViewPager viewPager;
    ScrollView scrollView;
    UlumPagerAdapter adapter;
    ArrayList<HashMap<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fr_image_pager);
        viewPager = (ViewPager) findViewById(R.id.pager);
        Bundle extra = getIntent().getExtras();
        int page = extra.getInt("page");
        list = getMateriUlum();
        adapter = new UlumPagerAdapter(this,list);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(page);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public ArrayList<HashMap<String, String>> getMateriUlum() {
        ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = QuranDatabase.open(getBaseContext());
        Cursor cursor = db.rawQuery("select * from ulum", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("no", String.valueOf(cursor.getPosition() + 1));
                hashMap.put("id", cursor.getString(cursor.getColumnIndex("_id")));
                hashMap.put("judul", cursor.getString(cursor.getColumnIndex("judul")));
                if(cursor.getString(cursor.getColumnIndex("isi"))==null) {
                    hashMap.put("isi", getBaseContext().getString(R.string.infak));
                }else{
                    hashMap.put("isi", cursor.getString(cursor.getColumnIndex("isi")));
                }
                alist.add(hashMap);
            }
        }
        cursor.close();
        db.close();
        return alist;
    }

    private class UlumPagerAdapter extends PagerAdapter {
        Context context;
        ArrayList<HashMap<String, String>> alist;

        public UlumPagerAdapter(Context context,
                                ArrayList<HashMap<String, String>> alist) {
            this.alist = alist;
            this.context = context;
        }


        @Override
        public int getCount() {
            return alist.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = ((Activity) context)
                    .getLayoutInflater();
            View tafsirLayout = inflater.inflate(R.layout.hasil_terjemah, container, false);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivityUlum.this);
            final int sizeHuruf = sharedPreferences.getInt("size", 15);
            final int sizeHurufArab = sharedPreferences.getInt("sizeArab", 20);
            scrollView = (ScrollView) tafsirLayout.findViewById(R.id.content);

            TextView ay = (TextView) tafsirLayout.findViewById(R.id.arti);
            TextView ak = (TextView) tafsirLayout.findViewById(R.id.aksi);

            ay.setTextSize(sizeHurufArab);

            ay.setText(alist.get(position).get("judul"));
            ak.setTextSize(sizeHuruf);
            SpannableString translation = new SpannableString(alist.get(position).get("isi"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ak.setText(Html.fromHtml(String.valueOf(translation),0));
            }else{
                ak.setText(Html.fromHtml(String.valueOf(translation)));
            }
            container.addView(tafsirLayout);
            return tafsirLayout;
        }


        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Remove viewpager_item.xml from ViewPager
            ((ViewPager) container).removeView((View) object);

        }

    }
}
