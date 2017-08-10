package net.qiujuer.italker.push.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.example.factory.persistence.Account;

import net.qiujuer.genius.ui.Ui;
import net.qiujuer.genius.ui.widget.FloatActionButton;
import net.qiujuer.italker.common.app.Activity;
import net.qiujuer.italker.common.widget.PortraitView;
import net.qiujuer.italker.push.R;
import net.qiujuer.italker.push.frags.main.ActiveFragment;
import net.qiujuer.italker.push.frags.main.ContactFragment;
import net.qiujuer.italker.push.frags.main.GroupFragment;
import net.qiujuer.italker.push.helper.NavHelper;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends Activity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        NavHelper.OnTabChangeListener<Integer> {

    @BindView(R.id.appbar)
    View mLayAppbar;

    @BindView(R.id.im_portrait)
    PortraitView mPortrait;

    @BindView(R.id.txt_title)
    TextView mTitle;

    @BindView(R.id.lay_container)
    FrameLayout mContainer;

    @BindView(R.id.navigation)
    BottomNavigationView mNavigation;

    @BindView(R.id.btn_action)
    FloatActionButton mAction;
    private NavHelper<Integer> mNavHelper;

    /**
     * MainActivity 显示的入口
     * @param context  上下文
     */
    public static void show(Context context){

        context.startActivity(new Intent(context,MainActivity.class));
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        //初始化底部辅助工具类

        mNavHelper = new NavHelper<Integer>(this, R.id.lay_container,
                getSupportFragmentManager(), this);

        mNavHelper.add(R.id.action_home, new NavHelper.Tab<Integer>(ActiveFragment.class, R.string.title_home))
                .add(R.id.action_group, new NavHelper.Tab<Integer>(GroupFragment.class, R.string.title_group))
                .add(R.id.action_contact, new NavHelper.Tab<Integer>(ContactFragment.class, R.string.title_contact));
        //添加底部导航的监听
        mNavigation.setOnNavigationItemSelectedListener(this);
        Glide.with(this).load(R.drawable.bg_src_morning).centerCrop().into(new ViewTarget<View, GlideDrawable>(mLayAppbar) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                this.view.setBackground(resource.getCurrent());
            }
        });


    }

    @Override
    protected void initData() {
        super.initData();
        //从底部接管我们的Menu，手动触发第一次点击
        Menu menu = mNavigation.getMenu();
        menu.performIdentifierAction(R.id.action_home,0);

        //初始化头像加载
        mPortrait.setup(Glide.with(this),Account.getUser());

    }
    @OnClick(R.id.im_portrait)
    void onPortraitClick(){
        PersonalActivity.show(this,Account.getUserId());
    }

    @OnClick(R.id.im_search)
    void onSearchMenuClick() {
        //在群的界面的时候，点击顶部的搜索就进组群搜索界面
        //其他都为人搜索界面
        int type = Objects.equals(mNavHelper.getCurrentTab().extra,R.string.title_group)?
                SearchActivity.TYPE_GROUP:SearchActivity.TYPE_USER;
       SearchActivity.show(this,type);
    }

    @Override
    protected boolean initArgs(Bundle bundle) {
        if(Account.isComplete()){

            return super.initArgs(bundle);
        }else{
           UserActivity.show(this);
            return false;
        }

    }

    @OnClick(R.id.btn_action)
    void onActionClick() {
        //判断用户信息是否完全，完全则走正常流程
        //如果是群则打开群创建的界面
        //如果是其他，都打开添加用户的界面
        if(Objects.equals(mNavHelper.getCurrentTab().extra,R.string.title_group)){
            //TODO  打开群创建界面
        }else{
           SearchActivity.show(this,SearchActivity.TYPE_USER);
        }

    }

    boolean isFirst = true;

    //当底部导航被点击触发
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //转接事件流到工具类中

        return mNavHelper.performClickMenu(item.getItemId());
    }

    /**
     * NavHelper 处理后回调的方法
     *
     * @param newTab 新的Tab
     * @param oldTab 旧的Tab
     */
    @Override
    public void onTabChanged(NavHelper.Tab<Integer> newTab, NavHelper.Tab<Integer> oldTab) {
        mTitle.setText(newTab.extra);

        //对浮动按钮进行隐藏和显示的动画
        float transY = 0;
        float rotation = 0;
        if(Objects.equals(newTab.extra,R.string.title_home)){
            transY = Ui.dipToPx(getResources(),76);
        }else{
            if(Objects.equals(newTab.extra,R.string.title_group)){
                //群
               mAction.setImageResource(R.drawable.ic_group_add);
                rotation = -360;
            }else{
                //联系人
                mAction.setImageResource(R.drawable.ic_contact_add);
                rotation = 360;
            }
        }

        mAction.animate()
                .rotation(rotation)
                .translationY(transY)
                .setInterpolator(new AnticipateOvershootInterpolator(2))
                .setDuration(480)
                .start();
    }
}
