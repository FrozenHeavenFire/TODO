package com.todo.ui.main;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.todo.R;
import com.todo.ui.add.AddActivity;
import com.todo.ui.adpters.ViewPagerAdapter;
import com.todo.ui.base.BaseActivity;
import com.todo.ui.datepicker.DatePickerActivity;

public class MainActivity extends BaseActivity {
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);
        ImageView imageView = (ImageView) findViewById(R.id.image_view);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        collapsingToolbarLayout.setTitle("星期一");

//        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.head_layout);

        Glide.with(this).load(R.mipmap.bg).into(imageView);

//        linearLayout.setBackgroundResource(R.drawable.bg);;
        tabLayout = (TabLayout) findViewById(R.id.toolbar_tab);
//        tabLayout.addTab(tabLayout.newTab().setText("待办"));
//        tabLayout.addTab(tabLayout.newTab().setText("完成"));
//        tabLayout.addTab(tabLayout.newTab().setText("过期"));

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        tabLayout.setupWithViewPager(viewPager);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rili:
                Intent intent = new Intent(MainActivity.this, DatePickerActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    public void Add(View view) {
        startActivity(new Intent(MainActivity.this, AddActivity.class));
    }
}
