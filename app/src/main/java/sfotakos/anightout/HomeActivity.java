package sfotakos.anightout;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import sfotakos.anightout.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        setSupportActionBar(mBinding.toolbar);

        mBinding.homeViewPager.setAdapter(
                new HomeNavigationFragmentPagerAdapter(getSupportFragmentManager()));

        mBinding.homeTabLayout.setupWithViewPager(mBinding.homeViewPager);
    }
}
