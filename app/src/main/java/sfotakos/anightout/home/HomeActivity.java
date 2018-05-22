package sfotakos.anightout.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import sfotakos.anightout.R;
import sfotakos.anightout.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        setSupportActionBar(mBinding.homeToolbar);

        mBinding.homeViewPager.setAdapter(
                new HomeNavigationFragmentPagerAdapter(getSupportFragmentManager()));

        mBinding.homeTabLayout.setupWithViewPager(mBinding.homeViewPager);
        setupTabIcons();
    }

    private void setupTabIcons () {
        for (int i = 0; i < mBinding.homeTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mBinding.homeTabLayout.getTabAt(i);
            if (tab != null) {
                if (i == 0){
                    tab.setIcon(R.drawable.ic_events);
                } else if (i == 1){
                    tab.setIcon(R.drawable.ic_map);
                } else {
                    throw new RuntimeException();
                }
            }
        }
    }
}
