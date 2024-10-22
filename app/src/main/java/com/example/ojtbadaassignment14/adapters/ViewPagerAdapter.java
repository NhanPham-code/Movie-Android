package com.example.ojtbadaassignment14.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ojtbadaassignment14.fragments.AboutFragment;
import com.example.ojtbadaassignment14.fragments.FavoriteListFragment;
import com.example.ojtbadaassignment14.fragments.MovieListFragment;
import com.example.ojtbadaassignment14.fragments.SettingFragment;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

        private List<Fragment> fragmentList;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragmentList) {
            super(fragmentActivity);
            this.fragmentList = fragmentList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }

}
