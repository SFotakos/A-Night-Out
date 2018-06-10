package sfotakos.anightout.home;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.events.EventsFragment;
import sfotakos.anightout.map.MapFragment;

public class HomeNavigationFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 2;
    private List<String> titles = new ArrayList<>();
    private ArrayList<Fragment> fragments = new ArrayList<>();

    public HomeNavigationFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        titles.add(context.getString(R.string.home_eventsTab_title));
        titles.add(context.getString(R.string.home_mapTab_title));
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                fragments.add(EventsFragment.newInstance());
                break;

            case 1:
                fragments.add(MapFragment.newInstance());
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
        return titles.get(position);
    }
}
