package smk.adzikro.indextemaquran.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import smk.adzikro.indextemaquran.ui.QuranUtils;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.util.IabBroadcastReceiver;
import smk.adzikro.indextemaquran.util.IabHelper;
import smk.adzikro.indextemaquran.util.IabResult;
import smk.adzikro.indextemaquran.util.Inventory;
import smk.adzikro.indextemaquran.util.Purchase;

/**
 * Created by server on 12/20/16.
 */

public class ActivityIAP extends AppCompatActivity implements
        IabBroadcastReceiver.IabBroadcastListener,
        View.OnClickListener {
    IabHelper mHelper;
    QuranSettings settings;
    private static String TAG="ActivityIAP";
    IabBroadcastReceiver mBroadcastReceiver;
    static final String SKU_INFAK = "infak";
    static final int RC_REQUEST = 10001;
    TextView versi;
    Button upgrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        settings = QuranSettings.getInstance(this);
        versi = (TextView)findViewById(R.id.versi);
        upgrade = (Button)findViewById(R.id.upgrade);
        versi.setText(getVersi());
        upgrade.setOnClickListener(this);
        versi.setOnClickListener(this);
        mHelper = new IabHelper(this, settings.base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(ActivityIAP.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }
    private void complain(String s){
        Log.e(TAG,s);
    }

    @Override
    public void onClick(View view) {
        if(!QuranUtils.haveInternet(this)) return;
        if(view.getId()==R.id.upgrade){
            bayarInfak();
        }else if(view.getId()==R.id.versi){
            cekRegistrasi();
        }
    }
    public String getVersi(){
        String infox="";
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            String versi = info.versionName;
            //ambil data cek free atau tidak
            String iklhas = "Free";//settings.isIklas()?"Ikhlas":"Free";
            if(settings.isIklas()){
                iklhas = "Ikhlas";
                upgrade.setText("Infak Pengembangan");
            }else{
                iklhas = "Free";
            }
            infox=String.format(getString(R.string.versi),versi,iklhas);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return infox;
    }
    @Override
    public void receivedBroadcast() {
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            Purchase infakPurchase = inventory.getPurchase(SKU_INFAK);
            if (infakPurchase!= null && verifyDeveloperPayload(infakPurchase)) {
                Log.d(TAG, "We have infak. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_INFAK), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming infak. Another async operation in progress.");
                }
                return;
            }

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                if (purchase.getSku().equals(SKU_INFAK)) {
                    settings.setIklas();
                    versi.setText(getVersi());
                    //update disini waktu untuk free iklan
                    //koin = koin == KOIN_MAX ? KOIN_MAX : koin + 100;
                    //Fungsi.setKoin(MainActivity.this, playerId, koin, 1);
                  //  complain("Kamu telah membeli Koin. Koin Kamu sekarang " + String.valueOf(koin));
                }
            } else {
                complain("Error while consuming: " + result);
            }
            //ambilData();
            //updateUI();
            //    setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                //       setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                //     setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_INFAK)) {
                // bought 1/4 tank of gas. So consume it.
                Log.d(TAG, "Purchase is koin. Starting koin consumption.");
                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming koin. Another async operation in progress.");
                   // updateUI();
                    return;
                }
            }
        }
    };
    private void bayarInfak(){
        String payload = settings.getKunciPayload();
        if(mHelper==null)return;
        try {
            mHelper.launchPurchaseFlow(this, SKU_INFAK, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");

        }
    }
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mHelper == null) return;
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void cekRegistrasi(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.cekregistrasi);
        Button ok = (Button)dialog.findViewById(R.id.ok);
        Button batal = (Button)dialog.findViewById(R.id.batal);
        final EditText email = (EditText)dialog.findViewById(R.id.email);
        final ProgressBar loading = (ProgressBar)dialog.findViewById(R.id.loading);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().equals(""))return;
                loading.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean cek = Fungsi.getVersiInet(email.getText().toString());
                        loading.post(new Runnable() {
                            @Override
                            public void run() {
                                if(cek){
                                    settings.setIklas();
                                    versi.setText(getVersi());
                                    Toast.makeText(ActivityIAP.this,"Selamat anda telah teregistrasi",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ActivityIAP.this,"Tidak teregistrasi",Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });
        batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
