package com.left.shothappy.views;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.left.shothappy.R;
import com.left.shothappy.adapters.ThesaurusAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 词库的页面
 */
public class ThesaurusFragment extends Fragment {

    private TabLayout tab_FindFragment_title;                            //定义TabLayout
    private ViewPager vp_FindFragment_pager;                             //定义viewPager
    private FragmentPagerAdapter fAdapter;                               //定义adapter

    private List<Fragment> list_fragment;                                //定义要装fragment的列表
    private List<String> list_title;                                     //tab名称列表

    private AnimalDictionaryFragment animalFragment;             //动物fragment
    private FruitDictionaryFragment fruitFragment;              //水果fragment
    private VegetableDictionaryFragment vegetableFragment;       //蔬菜fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thesaurus, container, false);
        initControls(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 初始化各控件
     *
     * @param view
     */
    private void initControls(View view) {

        tab_FindFragment_title = (TabLayout) view.findViewById(R.id.tab_fragment_title);
        vp_FindFragment_pager = (ViewPager) view.findViewById(R.id.vp_fragment_pager);

        //初始化各fragment
        animalFragment = new AnimalDictionaryFragment();
        fruitFragment = new FruitDictionaryFragment();
        vegetableFragment = new VegetableDictionaryFragment();

        //将fragment装进列表中
        list_fragment = new ArrayList<>();
        list_fragment.add(animalFragment);
        list_fragment.add(fruitFragment);
        list_fragment.add(vegetableFragment);

        //将名称加载tab名字列表，正常情况下，我们应该在values/arrays.xml中进行定义然后调用
        list_title = new ArrayList<>();
        list_title.add(getString(R.string.animal_dictionary));
        list_title.add(getString(R.string.fruits_dictionary));
        list_title.add(getString(R.string.vegetable_dictionary));

        //设置TabLayout的模式
        tab_FindFragment_title.setTabMode(TabLayout.MODE_FIXED);
        //为TabLayout添加tab名称
        tab_FindFragment_title.addTab(tab_FindFragment_title.newTab().setText(list_title.get(0)));
        tab_FindFragment_title.addTab(tab_FindFragment_title.newTab().setText(list_title.get(1)));
        tab_FindFragment_title.addTab(tab_FindFragment_title.newTab().setText(list_title.get(2)));

        fAdapter = new ThesaurusAdapter(getActivity().getSupportFragmentManager(), list_fragment, list_title);

        //viewpager加载adapter
        vp_FindFragment_pager.setAdapter(fAdapter);
        //TabLayout加载viewpager
        tab_FindFragment_title.setupWithViewPager(vp_FindFragment_pager);
    }

}