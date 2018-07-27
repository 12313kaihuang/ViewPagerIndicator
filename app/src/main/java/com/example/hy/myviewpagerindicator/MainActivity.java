package com.example.hy.myviewpagerindicator;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hy.myviewpagerindicator.view.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ViewPagerIndicator indicator;

    private List<String> titleLists = Arrays.asList("短信", "收藏", "推荐", "4", "5", "6", "7", "8", "9");
    private List<VPSimpleFragment> fragmentList = new ArrayList<>();
    private FragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initView();
        initData();

        indicator.setTabVisibleCount(3);   //设置一页可见的Tab数
        indicator.setTabItemTitles(titleLists);  //添加标题
        //indicator.setNormalTextColor(0xFFFF0000); //设置未选中时颜色
        //indicator.setHighLightTextColor(0xFFFF0000); //设置选中时颜色
        //indicator.getPaint().setColor(Color.parseColor("#00FF00"));   //设置画笔属性
//        indicator.setGraphicsHeight(10);
        indicator.setType(ViewPagerIndicator.RECTANGULAR);
        indicator.setViewPager(viewPager, 1);
        viewPager.setAdapter(adapter);

    }

    private void initData() {
        for (String title :
                titleLists) {

            VPSimpleFragment fragment = VPSimpleFragment.newInstance(title);
            fragmentList.add(fragment);
        }

        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        };
    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.id_indicator);
    }
}
