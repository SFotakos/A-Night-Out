package sfotakos.anightout.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import sfotakos.anightout.R;
import sfotakos.anightout.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    public static final String HOME_ACTIVITY_PARENT = "home-activity";
    private ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        setSupportActionBar(mBinding.homeToolbar);

        mBinding.homeViewPager.setAdapter(
                new HomeNavigationFragmentPagerAdapter(this, getSupportFragmentManager()));

        mBinding.homeTabLayout.setupWithViewPager(mBinding.homeViewPager);
    }
}
