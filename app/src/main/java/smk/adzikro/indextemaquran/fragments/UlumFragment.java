package smk.adzikro.indextemaquran.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.ActivityUlum;


/**
 * Created by server on 3/20/16.
 */
public class UlumFragment extends Fragment {
    ListView listView;
    ArrayList<HashMap<String, String>> alist;
    CustomListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_bookmark, container, false);
        listView = (ListView)view.findViewById(R.id.list_bookmark);
        loadData();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              //  ((MainActivity)getContext()).jumpToUlum(position);
                Intent intent = new Intent(getContext(),ActivityUlum.class);
                intent.putExtra("page",position);
                startActivity(intent);
            }
        });
        return view;
    }
    private void loadData(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                alist = new ArrayList<HashMap<String, String>>();
                String[] data =getContext().getResources().getStringArray(R.array.ulum);
                String[] info =getContext().getResources().getStringArray(R.array.detail_ulum);
                for (int i = 1; i < 25; i++) {
                    HashMap<String, String> hmap = new HashMap<String, String>();
                    hmap.put("no", "" + i );
                    hmap.put("judul", data[i-1]);
                    hmap.put("detail", info[i-1]);
                    alist.add(hmap);
                    adapter = new CustomListAdapter(getContext(),
                            R.layout.bookmark, alist);
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(adapter);
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
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
            convertView = inflater.inflate(R.layout.bookmark,
                    parent, false);
            holder = new Holder();
            holder.no = (TextView) convertView
                    .findViewById(R.id.suraNumber);
            holder.judul = (TextView) convertView
                    .findViewById(R.id.title);
            holder.detail = (TextView) convertView
                    .findViewById(R.id.metadata);
            holder.lin_background = (LinearLayout) convertView
                    .findViewById(R.id.linear);
            convertView.setTag(holder);


            holder = (Holder) convertView.getTag();

            holder.no.setText(alist.get(pos).get("no"));
            holder.judul.setText(alist.get(pos).get("judul"));
            holder.detail.setText(alist.get(pos).get("detail"));
            return convertView;

        }

        class Holder {
            TextView no, judul, detail;
            LinearLayout lin_background;
        }
    }
}
