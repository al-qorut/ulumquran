package smk.adzikro.indextemaquran.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.UlumQuranActivity;
import smk.adzikro.indextemaquran.constans.Constants;

/**
 * Created by server on 12/6/16.
 */

public class ImageQuranFragment extends Fragment {
    private static final String PAGE_NUMBER_EXTRA = "page";

    private int mPageNumber;
    private DisplayImageOptions options;
    private String[] IMG_URL;
    private  static final String path = Environment.getExternalStorageDirectory().getPath()+"/.adzikro/indexQuran/image/";
    private static final String TAG="ImageQuranFragment";

    public static ImageQuranFragment newInstance(int page) {
        final ImageQuranFragment f = new ImageQuranFragment();
        Log.e(TAG,"newInstance");
        final Bundle args = new Bundle();
        args.putInt(PAGE_NUMBER_EXTRA, page);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments() != null ?
                getArguments().getInt(PAGE_NUMBER_EXTRA) : 1;
        Log.e(TAG,"onCreate page "+mPageNumber);
        setHasOptionsMenu(true);
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.folder)
                .showImageOnFail(R.drawable.rowfolder)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)

                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        IMG_URL = new String[Constants.IMAGE_URL.length];
        for(int i=0;i<Constants.IMAGE_URL.length;i++){
            IMG_URL[i]="file://"+path+Constants.IMAGE_URL[i];
            Log.e(TAG,IMG_URL[i]);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View imageLayout = inflater.inflate(R.layout.item_pager_image, container);
        assert imageLayout != null;
        final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UlumQuranActivity)getContext()).toggleActionBar();
            }
        });
        final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        //tiga
        ImageLoader.getInstance().displayImage(IMG_URL[mPageNumber], imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "IO error";
                        break;
                    case DECODING_ERROR:
                        message = "Image can't be decoded";
                        break;
                    case NETWORK_DENIED:
                        message = "Downloads are denied";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case UNKNOWN:
                        message = "Unknown error";
                        break;
                }
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
			}
        });

        return imageLayout;
    }

    private boolean mJustCreated;
    @Override
    public void onResume() {
        super.onResume();
    }

    public void updateView() {

    }

    public void cleanup() {
      //  if (ImageLoader != null)
    }
    public void refresh() {
        Activity activity = getActivity();
        if (activity != null) {

        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }



}
