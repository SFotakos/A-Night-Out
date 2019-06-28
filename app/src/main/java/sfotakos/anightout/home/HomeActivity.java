package sfotakos.anightout.home;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.databinding.ActivityHomeBinding;
import sfotakos.anightout.events.EventsFragment;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        mBinding.homeViewPager.setAdapter(
                new HomeNavigationFragmentPagerAdapter(this, getSupportFragmentManager()));

        mBinding.homeTabLayout.setupWithViewPager(mBinding.homeViewPager);
    }

    public void navigateToTab(Constants.HomeTabs homeTabs){
        mBinding.homeViewPager.setCurrentItem(homeTabs.getTabPosition());
    }

    public void navigateToEvent(Integer eventId){
        ViewPager viewPager = mBinding.homeViewPager;
        viewPager.setCurrentItem(Constants.HomeTabs.EVENT_TAB.getTabPosition());
        if (viewPager.getAdapter() != null) {
            EventsFragment eventsFragment = (EventsFragment)
                    viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
            eventsFragment.openEventById(eventId);
        }
    }
}
