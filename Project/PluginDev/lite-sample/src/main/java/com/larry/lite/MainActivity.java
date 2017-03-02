package com.larry.lite;

import com.larry.light.LightFragmentActivity;

public class MainActivity extends LightFragmentActivity {

    @Override
    protected void initializeStartingFragment() {
        try {
            loadFragment(new MainFragment(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
