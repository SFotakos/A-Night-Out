package sfotakos.anightout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

public class HomeNavigationFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private String titles[] = new String[]{"Events", "Map"};
    private ArrayList<Fragment> fragments = new ArrayList<>();

    public HomeNavigationFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                fragments.add(EventFragment.newInstance());
                break;

            case 1:
                fragments.add(SupportMapFragment.newInstance());
                break;

            default:
                break;
        }
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
