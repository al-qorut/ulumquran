package smk.adzikro.indextemaquran.adapter;


import android.util.Log;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import smk.adzikro.indextemaquran.fragments.TranslationFragment;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.fragments.ImagePagerQuran;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;

public class QuranPageAdapter extends FragmentStatePagerAdapter {

    private boolean mIsShowingTranslation = true;

   public QuranPageAdapter(FragmentManager fm,
                           boolean isShowingTranslation){
      super(fm);
      mIsShowingTranslation = isShowingTranslation;
   }

   public void setTranslationMode(boolean mIsShowing){
       this.mIsShowingTranslation = mIsShowing;
         notifyDataSetChanged();
   }

   @Override
   public int getItemPosition(Object object){
      /* when the ViewPager gets a notifyDataSetChanged (or invalidated),
       * it goes through its set of saved views and runs this method on
       * each one to figure out whether or not it should remove the view
       * or not.  the default implementation returns POSITION_UNCHANGED,
       * which means that "this page is as is."
       *
       * as noted in http://stackoverflow.com/questions/7263291 in one
       * of the answers, if you're just updating your view (changing a
       * field's value, etc), this is highly inefficient (because you
       * recreate the view for nothing).
       *
       * in our case, however, this is the right thing to do since we
       * change the fragment completely when we notifyDataSetChanged.
       */
      return POSITION_NONE;
   }

	@Override
	public int getCount() {
    return Constants.PAGES_LAST;
  }
    String TAG="QuranPageAdapter";


	@Override
	public Fragment getItem(int position){
    int mPage = position;//QuranInfo.getPageFromPos(position);
	   Log.e(TAG,"getting page: " + mPage);
      if (mIsShowingTranslation){
         return TranslationFragment.newInstance(mPage);
      } else {
         return ImagePagerQuran.newInstance(mPage);
      }
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object){
	   super.destroyItem(container, position, object);
	}



}
