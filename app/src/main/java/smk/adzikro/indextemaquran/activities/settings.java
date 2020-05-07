package smk.adzikro.indextemaquran.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import smk.adzikro.indextemaquran.R;

/**
 * Created by server on 12/21/15.
 */
public class settings extends AppCompatActivity {
    public Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        context = getBaseContext();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_settings);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getFragmentManager();
        Fragment fragment1 = fm.findFragmentById(R.id.content);
        if(fragment1==null) {
            fm.beginTransaction()
                    .replace(R.id.content, new PrefsFragment())
                    .commit();
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    public static  class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            Preference myPref = (Preference) findPreference("list_translate");
            myPref.setOnPreferenceClickListener(preference -> {
                showList(0);
                return true;
            });
            Preference myPreft = (Preference) findPreference("list_tafsir");
            myPreft.setOnPreferenceClickListener(preference -> {
                showList(1);
                return true;
            });

        }

        private void showList(int kode){
            Intent intent = new Intent(getActivity(), ActivityQuranSource.class);
            intent.putExtra(ActivityQuranSource.KODE,kode);
            startActivity(intent);
        }



    }


}
