/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package smk.adzikro.indextemaquran.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.UlumQuranActivity;
import smk.adzikro.indextemaquran.constans.BaseQuranInfo;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImagePagerQuran extends Fragment {
	private ProgressDialog prgDialog;
	public static int juz, halaman;
	public static String[] IMG_URL;
	public static Context context;
    public static ViewPager viewPager;
	String p;
	public static String TAG="TranslationFragment";
	private static final String PAGE_NUMBER_EXTRA = "page";

	public static ImagePagerQuran newInstance(int page) {
		final ImagePagerQuran f = new ImagePagerQuran();
		final Bundle args = new Bundle();
		args.putInt(PAGE_NUMBER_EXTRA, page);
		f.setArguments(args);
		Log.e(TAG,"new Instance "+page);
		return f;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		juz = getArguments() != null ?
				getArguments().getInt(PAGE_NUMBER_EXTRA) : 1;
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.image_quran, container, false);
		context = getContext();
		p= Fungsi.getPathImage();
		IMG_URL = new String[Constants.IMAGE_URL.length];
		for(int i=0;i<Constants.IMAGE_URL.length;i++){
			IMG_URL[i]="file://"+p+Constants.IMAGE_URL[i];
		}

		juz = getArguments().getInt("page");
			halaman = juz;
		Log.e(TAG,"onView page "+halaman);
		view.setBackgroundColor(Color.WHITE);

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				//	.cacheOnDisc(true).cacheInMemory(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)

				.displayer(new FadeInBitmapDisplayer(300)).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getContext())
				.defaultDisplayImageOptions(defaultOptions)

				.memoryCache(new WeakMemoryCache()).build();
		//.discCacheSize(100 * 1024 * 1024).build();
		if(!ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().init(config);
		}

		viewPager = (ViewPager) view.findViewById(R.id.pager);


		ImageAdapter adapter = new ImageAdapter(getContext());
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(halaman);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				Log.e(TAG,""+position);
				((UlumQuranActivity)context).setPage(BaseQuranInfo.getPosFromPage(position));
				((UlumQuranActivity)context).updateActionBarTitle();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		return view;
	}


	@Override
	public void onResume(){
		super.onResume();
		//setHalaman(((MainFragment)getContext()).getPage());
	}
	private static class ImageAdapter extends PagerAdapter {


		private LayoutInflater inflater;
		private DisplayImageOptions options;

		ImageAdapter(Context context) {
			inflater = LayoutInflater.from(context);

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
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			//dua
			return IMG_URL.length;
		}


		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			assert imageLayout != null;
			final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    ((UlumQuranActivity)context).toggleActionBar();
				}
			});
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setAdjustViewBounds(true);
			//tiga
			ImageLoader.getInstance().displayImage(IMG_URL[position], imageView, options, new SimpleImageLoadingListener() {
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
				//	book= getBook(getHalaman());
					spinner.setVisibility(View.GONE);
			/*		if(book){
						bm.setImageResource(R.drawable.ic_favorite);
					}else{
						bm.setImageResource(R.drawable.ic_not_favorite);
					}
			*/		//loadedImage.

                //    ((MainFragment)context).setInfoQuran(getHalaman());
				}
			});

			view.addView(imageLayout, 0);
			return imageLayout;
		}


			@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	public void cleanup() {

	}

}