package smk.adzikro.indextemaquran.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import smk.adzikro.indextemaquran.R;

/**
 * Created by server on 1/20/18.
 */

public class BannerAdapter extends PagerAdapter {

    private Context context;
    TypedArray banerArray;
    public BannerAdapter(Context context, TypedArray array){
        this.context = context;
        this.banerArray = array;
    }
    @Override
    public int getCount() {
        return banerArray.length();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ImageView bannerLayout = (ImageView) inflater.inflate(R.layout.item_image, container, false);
        bannerLayout.setImageResource(banerArray.getResourceId(position, 0));
        bannerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"swipe clicked"+position, Toast.LENGTH_LONG).show();
            }
        });

        container.addView(bannerLayout);
        return bannerLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
