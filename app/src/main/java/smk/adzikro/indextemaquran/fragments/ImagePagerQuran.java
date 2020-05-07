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

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.activities.UlumQuranActivity;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * alqorut
 */
public class ImagePagerQuran extends Fragment {
	public static Context context;
    String path;
    private int page;
	public static String TAG="TranslationFragment";
	private static final String PAGE_NUMBER_EXTRA = "page";
	int bitmapWidth, bitmapHeight, screenWidth, screenHeight;

	public static ImagePagerQuran newInstance(int page) {
		final ImagePagerQuran f = new ImagePagerQuran();
		final Bundle args = new Bundle();
		args.putInt(PAGE_NUMBER_EXTRA, page);
		f.setArguments(args);
		return f;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		page = getArguments() != null ?
				getArguments().getInt(PAGE_NUMBER_EXTRA) : 1;
		setHasOptionsMenu(true);
	}
	private ImageView imageView;
	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.image_quran, container, false);
		imageView = view.findViewById(R.id.image);
		imageView.setAdjustViewBounds(true);
		imageView.setClickable(true);
		context = getContext();
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenHeight = size.y;
		screenWidth = size.x;
		//Log.e(TAG, "layar w "+screenWidth+" h "+screenHeight);
		path= Fungsi.getPathImage();
		page = getArguments().getInt("page");
		String gb	="file://"+path+Constants.IMAGE_URL[page];
		Glide.with(context)
				.load(gb)
				.into(imageView);
		//imageView.setOnClickListener(view1 -> ((UlumQuranActivity)getContext()).toggleActionBar());
		imageView.setOnTouchListener(new View.OnTouchListener() {
			float downX, downY;
			int totalX, totalY;
			int scrollByX, scrollByY;
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				float currentX, currentY;
				switch (event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						downX = event.getX();
						downY = event.getY();
						break;
					case MotionEvent.ACTION_UP:
						if(downX== event.getX() && downY==event.getY()){
							((UlumQuranActivity)getContext()).toggleActionBar();
						}
						break;
					case MotionEvent.ACTION_MOVE:
						currentX = event.getX();
						currentY = event.getY();
					//	Log.e(TAG, "Image di scroll "+currentX+" Y "+currentY);
						scrollByX = (int)(downX - currentX);
						scrollByY = (int)(downY - currentY);

								// scrolling to top of image (pic moving to the bottom)
						if (currentY > downY)
						{
							if (totalY == maxTop)
							{
								scrollByY = 0;
							}
							if (totalY > maxTop)
							{
								totalY = totalY + scrollByY;
							}
							if (totalY < maxTop)
							{
								scrollByY = maxTop - (totalY - scrollByY);
								totalY = maxTop;
							}
						}

						// scrolling to bottom of image (pic moving to the top)
						if (currentY < downY)
						{
							if (totalY == maxBottom)
							{
								scrollByY = 0;
							}
							if (totalY < maxBottom)
							{
								totalY = totalY + scrollByY;
							}
							if (totalY > maxBottom)
							{
								scrollByY = maxBottom - (totalY - scrollByY);
								totalY = maxBottom;
							}
						}

						imageView.scrollBy(scrollByX, scrollByY);
						downX = currentX;
						downY = currentY;
						break;

				}

				return true;
			}
		});
		return view;
	}

	int maxX = (int)((bitmapWidth / 2) - (screenWidth / 2));
	int maxY = (int)((bitmapHeight / 2) - (screenHeight / 2));

	// set scroll limits
	final int maxLeft = (maxX * -1);
	final int maxRight = maxX;
	final int maxTop = (maxY * -1);
	final int maxBottom = maxY;

	@Override
	public void onResume(){
		super.onResume();
	}


}