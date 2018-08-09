package com.example.lenovo.mmessenger;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class CustomPagerAdapter extends FragmentPagerAdapter{
    public CustomPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                RequestsFragment requestTab=new RequestsFragment();
                return requestTab;
            case 1:
                ChatsFragment chatsTab= new ChatsFragment();
                return chatsTab;
            case 2:
                FriendsFragment friendsTab= new FriendsFragment();
                return friendsTab;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }
}
