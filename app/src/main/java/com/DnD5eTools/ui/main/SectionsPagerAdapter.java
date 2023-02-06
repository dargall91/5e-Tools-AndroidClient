package com.DnD5eTools.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.DnD5eTools.ui.CombatTracker;
import com.DnD5eTools.ui.EncounterBuilder;
import com.DnD5eTools.ui.MonsterBuilder;
import com.DnD5eTools.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;
    private CombatTracker combatTracker;
    private MonsterBuilder monsterBuilder;
    private EncounterBuilder encounterBuilder;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        //return PlaceholderFragment.newInstance(position + 1);

        Fragment fragment = null;

        //TODO: add 4th page for magic items? (also requires update in strings.xml)
        switch (position) {
            case 0:
                if (combatTracker == null) {
                    combatTracker = new CombatTracker();
                }

                fragment = combatTracker;
                break;
            case 1:
                if (monsterBuilder == null) {
                    monsterBuilder = new MonsterBuilder();
                }

                fragment = monsterBuilder;
                break;
            case 2:
                if (encounterBuilder == null) {
                    encounterBuilder = new EncounterBuilder();
                }

                fragment = encounterBuilder;
                break;
        }

        return fragment;
    }

    public CombatTracker getCombatTracker() {
        return combatTracker;
    }

    public MonsterBuilder getMonsterBuilder() {
        return monsterBuilder;
    }

    public EncounterBuilder getEncounterBuilder() {
        return encounterBuilder;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}