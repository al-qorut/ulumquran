package smk.adzikro.indextemaquran.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import smk.adzikro.indextemaquran.fragments.SuraListFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        if(position==0) {
            SuraListFragment tab1 = new SuraListFragment();
            return tab1;
        }else if(position==1){
            SuraListFragment tab2 = new SuraListFragment();
            return tab2;
        }else if(position==2){
            //TemaFragment tab3 = new TemaFragment();
            SuraListFragment tab3 = new SuraListFragment();
            return tab3;
        }else{
            //BookFragment tab4 = BookFragment.newInstance();
            SuraListFragment tab4 = new SuraListFragment();
            return tab4;
        }
    }



    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}