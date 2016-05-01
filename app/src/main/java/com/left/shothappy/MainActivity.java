package com.left.shothappy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.left.shothappy.bean.RewardVideo;
import com.left.shothappy.bean.Schedule;
import com.left.shothappy.bean.User;
import com.left.shothappy.utils.PicUtils;
import com.left.shothappy.utils.ScheduleUtils;
import com.left.shothappy.views.ARFragment;
import com.left.shothappy.views.RateoflearningFragment;
import com.left.shothappy.views.SettingFragment;
import com.left.shothappy.views.ThesaurusFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private FragmentManager fm;
    private ARFragment arFragment;
    private RateoflearningFragment rateoflearningFragment;
    private ThesaurusFragment thesaurusFragment;
    private SettingFragment settingFragment;
    private CircleImageView head_imageView;
    private TextView username;
    private TextView email;
    private User user;
    private Bitmap head;//头像Bitmap
    private ImageView today_card;//今日卡片
    private Menu top_right_menu;//右上角那一块的menu，主要是用来查找刷新按钮和搜索按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fm = getSupportFragmentManager();
        // 设置默认的Fragment
        setDefaultFragment();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        head_imageView = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);
        today_card = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.today_card);
        username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email);

        user = BmobUser.getCurrentUser(this, User.class);
        if (user != null) {
            //设置界面用户信息
            username.setText(user.getUsername());
            email.setText(user.getEmail());

            BmobQuery bmobQuery = new BmobQuery();
            bmobQuery.getObject(getApplicationContext(), user.getObjectId(), new GetListener<User>() {
                @Override
                public void onSuccess(User o) {

                    //显示图片的配置
                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .bitmapConfig(Bitmap.Config.RGB_565)
                            .build();
                    //载入图片
                    ImageLoader.getInstance().displayImage(o.getHead().getFileUrl(getApplicationContext()), head_imageView, options);
                }

                @Override
                public void onFailure(int i, String s) {

                }
            });
        } else {
            //缓存用户对象为空时， 打开登录界面
            Snackbar.make(navigationView, getString(R.string.userinfo_overdue), Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        head_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从相册里面取照片
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 1);
            }
        });

        today_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BmobQuery<Schedule> query = new BmobQuery<>();
                List<BmobQuery<Schedule>> and = new ArrayList<>();
                //大于00：00：00
                BmobQuery<Schedule> q1 = new BmobQuery<>();
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = sdf.parse(sdf.format(ScheduleUtils.getTodayZero()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                q1.addWhereGreaterThanOrEqualTo("createdAt", new BmobDate(date));
                and.add(q1);

                //小于23：59：59
                BmobQuery<Schedule> q2 = new BmobQuery<>();
                Date date1 = null;
                try {
                    date1 = sdf.parse(sdf.format(ScheduleUtils.getTodayEnd()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                q2.addWhereLessThanOrEqualTo("createdAt", new BmobDate(date1));
                and.add(q2);
                //添加复合与查询
                query.and(and);

                query.addWhereEqualTo("user", user);    // 查询当前用户当日Schedule
                query.order("createdAt");
                query.findObjects(getApplicationContext(), new FindListener<Schedule>() {
                    @Override
                    public void onSuccess(List<Schedule> list) {
                        if (list == null || list.size() == 0 || list.get(0).getWords() == null || list.get(0).getWords().size() < 10) {
                            Snackbar.make(navigationView, getString(R.string.today_tip), Snackbar.LENGTH_SHORT).show();
                        } else {
                            //查找到当日的奖励视频
                            BmobQuery<RewardVideo> videoBmobQuery = new BmobQuery<>();
                            List<BmobQuery<RewardVideo>> videoand = new ArrayList<>();
                            //大于00：00：00
                            BmobQuery<RewardVideo> q3 = new BmobQuery<>();
                            Date date = null;
                            try {
                                date = sdf.parse(sdf.format(ScheduleUtils.getTodayZero()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            q3.addWhereGreaterThanOrEqualTo("createdAt", new BmobDate(date));
                            videoand.add(q3);

                            //小于23：59：59
                            BmobQuery<RewardVideo> q4 = new BmobQuery<>();
                            Date date1 = null;
                            try {
                                date1 = sdf.parse(sdf.format(ScheduleUtils.getTodayEnd()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            q4.addWhereLessThanOrEqualTo("createdAt", new BmobDate(date1));
                            videoand.add(q4);
                            //添加复合与查询
                            videoBmobQuery.and(videoand);
                            videoBmobQuery.order("createdAt");
                            videoBmobQuery.findObjects(getApplicationContext(), new FindListener<RewardVideo>() {
                                @Override
                                public void onSuccess(List<RewardVideo> list) {
                                    if (list == null || list.size() == 0 || list.get(0).getVideo() == null) {
                                        Snackbar.make(navigationView, getString(R.string.reward_tip), Snackbar.LENGTH_SHORT).show();
                                    } else {
                                         /* 开始播放视频 */
                                        Intent intent = new Intent();
                                        //设置传递方向
                                        intent.setClass(getApplicationContext(), RewardActivity.class);
                                        //绑定数据
                                        intent.putExtra("path", list.get(0).getVideo().getFileUrl(getApplicationContext()));
                                        //启动activity
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onError(int i, String s) {
                                    Snackbar.make(navigationView, getString(R.string.error_network), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        Snackbar.make(navigationView, getString(R.string.error_network), Snackbar.LENGTH_SHORT).show();
                    }
                });
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        top_right_menu = menu;

        return true;
    }

    //利用反射机制使每一个Action按钮对应的图标显示出来
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 各社交平台分享功能
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.share_wechatmoments) {
            share(SHARE_MEDIA.WEIXIN_CIRCLE);
            return true;
        }
        if (id == R.id.share_wechat) {
            share(SHARE_MEDIA.WEIXIN);
            return true;
        }
        if (id == R.id.share_qzone) {
            share(SHARE_MEDIA.QZONE);
            return true;
        }
        if (id == R.id.share_qq) {
            share(SHARE_MEDIA.QQ);
            return true;
        }
        if (id == R.id.share_weibo) {
            share(SHARE_MEDIA.SINA);
            return true;
        }
        //刷新，重新绘制
        if (id == R.id.icon_refresh_search) {
            //绘制BarChart
            ScheduleUtils.getDailyData(this, RateoflearningFragment.mBarChart, RateoflearningFragment.mBarColors);
            //绘制LineChart
            ScheduleUtils.getImprovementData(this, RateoflearningFragment.mLineChart, RateoflearningFragment.mLineColors);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            if (arFragment == null) {
                arFragment = new ARFragment();
            }
            showFragment(arFragment, R.string.title_ar);
            top_right_menu.findItem(R.id.icon_refresh_search).setVisible(false);
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_gallery) {
            if (thesaurusFragment == null) {
                thesaurusFragment = new ThesaurusFragment();
            }
            showFragment(thesaurusFragment, R.string.title_thesaurus);
//            top_right_menu.findItem(R.id.icon_refresh_search).setVisible(true);
//            top_right_menu.findItem(R.id.icon_refresh_search).setIcon(R.drawable.search);
            top_right_menu.findItem(R.id.icon_refresh_search).setVisible(false);
            fab.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_slideshow) {
            if (rateoflearningFragment == null) {
                rateoflearningFragment = new RateoflearningFragment();
            }
            showFragment(rateoflearningFragment, R.string.title_rateoflearning);
            top_right_menu.findItem(R.id.icon_refresh_search).setVisible(true);
            top_right_menu.findItem(R.id.icon_refresh_search).setIcon(R.drawable.refresh);
            fab.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_manage) {
            if (settingFragment == null) {
                settingFragment = new SettingFragment();
            }
            showFragment(settingFragment, R.string.title_setting);
            top_right_menu.findItem(R.id.icon_refresh_search).setVisible(false);
            fab.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_share) {
            //调用分享
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
            intent.putExtra(Intent.EXTRA_TEXT, getText(R.string.share_app_text));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, getString(R.string.app_name)));
        } else if (id == R.id.nav_send) {
            //去评分，打开应用商场
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            //退出登录
            User.logOut(getApplicationContext());   //清除缓存用户对象
            //跳转至登录页
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    PicUtils.cropPhoto(data.getData(), this);//裁剪图片
                }
                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        /**
                         * 上传服务器代码
                         */
                        final BmobFile bmobFile = new BmobFile(PicUtils.saveBitmap2file(head, user));
                        bmobFile.uploadblock(getApplicationContext(), new UploadFileListener() {

                            @Override
                            public void onSuccess() {
                                //记得更新对应user的头像
                                User newUser = new User();
                                newUser.setHead(bmobFile);
                                newUser.update(getApplicationContext(), user.getObjectId(), new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        //用ImageView显示出来
                                        head_imageView.setImageBitmap(head);
                                    }

                                    @Override
                                    public void onFailure(int code, String msg) {
                                        Snackbar.make(navigationView, getString(R.string.error_head_replace), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            @Override
                            public void onProgress(Integer value) {
                            }
                            @Override
                            public void onFailure(int code, String msg) {
                                Snackbar.make(navigationView, getString(R.string.error_head_replace), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 设置activity一启动显示的默认content
     */
    private void setDefaultFragment() {
        FragmentTransaction transaction = fm.beginTransaction();
        arFragment = new ARFragment();
        transaction.add(R.id.content, arFragment);
        transaction.commit();
        //设置左上角标题
        setTitle(R.string.title_ar);
    }

    /**
     * 起切换fragment的作用
     *
     * @param fragment
     */
    private void showFragment(Fragment fragment, int titleId) {
        setTitle(titleId);

        FragmentTransaction transaction = fm.beginTransaction();
        //先判断当前的有没有，有的话直接做显示，没的话先添加
        if (!fm.getFragments().contains(fragment)) {
            transaction.add(R.id.content, fragment);
        }
        //先全部hide
        if (fm.getFragments().contains(arFragment)) {
            transaction.hide(arFragment);
        }
        if (fm.getFragments().contains(rateoflearningFragment)) {
            transaction.hide(rateoflearningFragment);
        }
        if (fm.getFragments().contains(thesaurusFragment)) {
            transaction.hide(thesaurusFragment);
        }
        if (fm.getFragments().contains(settingFragment)) {
            transaction.hide(settingFragment);
        }

        //显示要显示的
        transaction.show(fragment);
        transaction.commit();
    }

    /**
     * 分享
     */
    private void share(SHARE_MEDIA num) {
        Bitmap shot = PicUtils.takeShot(this);

        PicUtils.share(num, this,shot);
    }
}
