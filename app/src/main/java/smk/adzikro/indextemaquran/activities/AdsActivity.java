package smk.adzikro.indextemaquran.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import java.util.List;

import smk.adzikro.indextemaquran.R;

/**
 * Created by server on 1/24/18.
 */

public class AdsActivity extends AppCompatActivity {
    private static final String ADMOB_AD_UNIT_ID ="ca-app-pub-3624492980147085/8112553650";//"ca-app-pub-3940256099942544/2247696110";//ca-app-pub-3624492980147085/8112553650";

    private Button mRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ads);
        mRefresh = (Button) findViewById(R.id.remove);
     //   refreshAd(false, true);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdsActivity.this, ActivityIAP.class));
            }
        });
        ImageView close = (ImageView) findViewById(R.id.cancel);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     * @param requestAppInstallAds indicates whether app install ads should be requested
     * @param requestContentAds    indicates whether content ads should be requested
     */
  /*  private void refreshAd(boolean requestAppInstallAds, boolean requestContentAds) {
        if (!requestAppInstallAds && !requestContentAds) {
            Toast.makeText(this, "At least one ad format must be checked to request an ad.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AdLoader.Builder builder = new AdLoader.Builder(this, ADMOB_AD_UNIT_ID);

        if (requestAppInstallAds) {
            builder.forAppInstallAd(ad -> {
                FrameLayout frameLayout =
                        (FrameLayout) findViewById(R.id.fl_adplaceholder);
                NativeAppInstallAdView adView = (NativeAppInstallAdView) getLayoutInflater()
                        .inflate(R.layout.ad_app_install, null);
                populateAppInstallAdView(ad, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            });
        }

        if (requestContentAds) {
            builder.forContentAd(ad -> {
                FrameLayout frameLayout =
                        (FrameLayout) findViewById(R.id.fl_adplaceholder);
                NativeContentAdView adView = (NativeContentAdView) getLayoutInflater()
                        .inflate(R.layout.ad_content, null);
                populateContentAdView(ad, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            });
        }

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(AdsActivity.this, "Failed to load native ad: "
                        + errorCode, Toast.LENGTH_SHORT).show();
                refreshAd(false, true);
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    } */
}
