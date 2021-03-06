package com.example.xunhu.xunchat.View.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.xunhu.xunchat.Model.AsyTasks.MySingleton;
import com.example.xunhu.xunchat.Model.AsyTasks.PicassoClient;
import com.example.xunhu.xunchat.Model.Entities.User;
import com.example.xunhu.xunchat.Presenter.DeleteFriendActionPresenter;
import com.example.xunhu.xunchat.Presenter.RequestRespondPresenter;
import com.example.xunhu.xunchat.Presenter.SetRemarkPresenter;
import com.example.xunhu.xunchat.R;
import com.example.xunhu.xunchat.View.AllViewClasses.MyDialog;
import com.example.xunhu.xunchat.View.Fragments.ContactsFragment;
import com.example.xunhu.xunchat.View.Fragments.RemarkDialogFragment;
import com.example.xunhu.xunchat.View.Interfaces.DeleteFriendView;
import com.example.xunhu.xunchat.View.Interfaces.RequestRespondView;
import com.example.xunhu.xunchat.View.Interfaces.SetRemarkView;
import com.example.xunhu.xunchat.View.MainActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by xunhu on 6/21/2017.
 */
@EActivity(R.layout.profile_activity_layout)
public class ProfileActivity extends Activity implements RequestRespondView,
        RemarkDialogFragment.RemarkDialogFragmentInterface,SetRemarkView, DeleteFriendView {
    @ViewById(R.id.iv_profile_activity_back) ImageView btnBack;
    @ViewById(R.id.iv_profile_activity_image) CircleImageView ivProfileImage;
    @ViewById(R.id.tv_profile_activity_nickname) TextView tvNickname;
    @ViewById(R.id.iv_profile_activity_gender) ImageView ivGender;
    @ViewById(R.id.tv_profile_activity_age) TextView tvAge;
    @ViewById(R.id.tv_profile_activity_username) TextView tvUsername;
    @ViewById(R.id.tv_profile_activity_what) TextView tvWhatsup;
    @ViewById(R.id.tv_profile_activity_region) TextView tvRegion;
    @ViewById(R.id.llAlbum) LinearLayout llAlbum;
    @ViewById(R.id.btn_send_or_add) Button btnSendOrAdd;
    @ViewById(R.id.ib_profile_menu) ImageButton ivMenu;
    @ViewById ImageView ivOne,ivTwo,ivThree;
    RemarkDialogFragment remarkDialogFragment;
    SetRemarkPresenter setRemarkPresenter;
    DeleteFriendActionPresenter deleteFriendActionPresenter;
    User user;
    String profile_url="";
    RequestRespondPresenter presenter;
    private static final int STRANGER = -1;
    private static final int PENDING = 0;
    private static final int NEED_TO_ACCEPT = 1;
    private static final int FRIEND = 2;
    MyDialog myDialog;
    IntentFilter intentFilter = new IntentFilter();
    public static final String BUTTON_PENDING = "pending";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case BUTTON_PENDING:
                    btnSendOrAdd.setText("Pending...");
                    btnSendOrAdd.setClickable(false);
                    btnSendOrAdd.setBackgroundColor(Color.RED);
                    break;
                default:
                    break;
            }
        }
    };
    @AfterViews void setProfileActivity(){
        intentFilter.addAction("pending");
        registerReceiver(broadcastReceiver,intentFilter);
        myDialog = new MyDialog(this);
        ButterKnife.bind(this);
        user = (User) getIntent().getSerializableExtra("user");
        setViews(user.getRelationship_type());
    }
    private void setViews(int relationshipType){
        switch (relationshipType){
            case STRANGER:
                btnSendOrAdd.setText("Add");
                profile_url = MainActivity.domain_url + user.getUrl();
                btnSendOrAdd.setClickable(true);
                btnSendOrAdd.setBackgroundColor(Color.parseColor("#00BFFF"));
                break;
            case PENDING:
                btnSendOrAdd.setText("Pending...");
                btnSendOrAdd.setClickable(false);
                btnSendOrAdd.setBackgroundColor(Color.RED);
                profile_url = MainActivity.domain_url+user.getUrl();
                break;
            case NEED_TO_ACCEPT:
                btnSendOrAdd.setText("Accept");
                profile_url = MainActivity.domain_url+user.getUrl();
                btnSendOrAdd.setClickable(true);
                btnSendOrAdd.setBackgroundColor(Color.parseColor("#00BFFF"));
                break;
            case FRIEND:
                btnSendOrAdd.setText("Message");
                profile_url = MainActivity.domain_url+user.getUrl();
                btnSendOrAdd.setClickable(true);
                btnSendOrAdd.setBackgroundColor(Color.parseColor("#00BFFF"));
                break;
            default:
                break;
        }
        tvUsername.setText("username: "+user.getUsername());
        tvNickname.setText(user.getNickname());
        tvAge.setText(String.valueOf(user.getAge()));
        tvRegion.setText(user.getRegion());
        tvWhatsup.setText(user.getWhatsup());
        if (user.getGender().equals("female")){
            ivGender.setImageResource(R.drawable.female_icon);
        }else {
            ivGender.setImageResource(R.drawable.male_icon);
        }
        if (!user.getImages().isEmpty()){
            try {
                JSONObject imageObjects = new JSONObject(user.getImages());
                String images = imageObjects.getString("images");
                JSONArray jsonArray = new JSONArray(images);
                switch (jsonArray.length()){
                    case 1:
                        PicassoClient.downloadImage(this,MainActivity.domain_url+jsonArray.getString(0),ivOne);
                        break;
                    case 2:
                        PicassoClient.downloadImage(this,MainActivity.domain_url+jsonArray.getString(0),ivOne);
                        PicassoClient.downloadImage(this,MainActivity.domain_url+jsonArray.getString(1),ivTwo);
                        break;
                    case 3:
                        PicassoClient.downloadImage(this,MainActivity.domain_url+jsonArray.getString(0),ivOne);
                        PicassoClient.downloadImage(this,MainActivity.domain_url+jsonArray.getString(1),ivTwo);
                        PicassoClient.downloadImage(this,MainActivity.domain_url+jsonArray.getString(2),ivThree);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ImageRequest imageRequest = new ImageRequest(profile_url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                ivProfileImage.setImageBitmap(response);
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Loading image fail",Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getmInstance(getApplicationContext()).addImageRequestToRequestQueue(imageRequest);
    }
    @Click({R.id.btn_send_or_add,R.id.iv_profile_activity_back,R.id.iv_profile_activity_image,
    R.id.ib_profile_menu,R.id.llAlbum})
    public void onRespond(View view){
        switch (view.getId()){
            case R.id.iv_profile_activity_back:
                onBackPressed();
                break;
            case R.id.btn_send_or_add:
                if(btnSendOrAdd.getText().toString().equals("Add")){
                    FriendRequestActivity_.intent(this).extra("user",user).start();
                }else if (btnSendOrAdd.getText().toString().equals("Accept")){
                    remarkDialogFragment = new RemarkDialogFragment(user.getNickname());
                    remarkDialogFragment.show(getFragmentManager(),"remarkFragmentDialog");
                }else if (btnSendOrAdd.getText().toString().equals("Message")){
                    ChatBoardActivity_.intent(this).extra("user",user).start();
                    finish();
                }
                break;
            case R.id.iv_profile_activity_image:
                ProfileThemeActivity_.intent(this).
                        extra("url",MainActivity.domain_url+user.getUrl()).start();
                break;
            case R.id.ib_profile_menu:
                if (btnSendOrAdd.getText().toString().equals("Message")){
                    createPopupMenu(view);
                }
                break;
            case R.id.llAlbum:
                SubActivity_.intent(this).extra("type","moments").
                        extra("id",user.getUserID()).extra("nickname",user.getNickname()).start();
                break;
            default:
                break;
        }
    }
    @SuppressLint("RestrictedApi")
    public void createPopupMenu(View view){
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        myDialog.createLoadingGifDialog();
                        deleteFriend();
                        return true;
                    case R.id.mute:
                        return true;
                    case R.id.set_remark:
                        remarkDialogFragment = new RemarkDialogFragment(user.getNickname());
                        remarkDialogFragment.show(getFragmentManager(),"");
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.friend_action_menu);
        popup.show();

    }
    public void deleteFriend(){
        deleteFriendActionPresenter = new DeleteFriendActionPresenter(this);
        deleteFriendActionPresenter.deleteFriend(MainActivity.me.getId(),user.getUserID());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
    @Override
    public void respondSuccess(String msg) {
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("friend_id",user.getUserID());
        values.put("friend_username",user.getUsername());
        values.put("friend_nickname",remarkDialogFragment.getRemark());
        values.put("friend_url",MainActivity.domain_url+user.getUrl());
        values.put("username",MainActivity.me.getUsername());
        database.insert("friend",null,values);
        values.clear();
        values.put("isAgreed","1");
        database.update("request",values,"username=? AND sender=?",
                new String[]{MainActivity.me.getUsername(),user.getUsername()});
        myDialog.cancelBottomGifDialog();
        btnSendOrAdd.setText("Message");
        Intent intent = new Intent(ContactsFragment.NEW_FRIEND_ADDED);
        sendBroadcast(intent);
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void respondFail(String msg) {
        myDialog.cancelBottomGifDialog();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setRemark(String remark) {
        myDialog.createBottomGifDialog();
        switch (btnSendOrAdd.getText().toString()){
            case "Message":
                setRemarkPresenter = new SetRemarkPresenter(this);
                setRemarkPresenter.setRemark(MainActivity.me.getId(),user.getUserID(),remark);
                break;
            case "Accept":
                presenter = new RequestRespondPresenter(this);
                presenter.sendRespond(user.getUserID(),MainActivity.me,remark);
                break;
        }
    }
    @Override
    public void setRemarkSuccessful(String msg) {
        myDialog.cancelBottomGifDialog();
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("friend_nickname",remarkDialogFragment.getRemark());
        database.update("friend",values,"username=? and friend_username=?",
                new String[]{MainActivity.me.getUsername(),user.getUsername()});
        database.update("latest_message",values,"username=? and friend_username=?",
                new String[]{MainActivity.me.getUsername(),user.getUsername()});
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setRemarkFail(String msg) {
        myDialog.cancelBottomGifDialog();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deleteSuccessful(String msg) {
        myDialog.cancelLoadingGifDialog();
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        database.delete("friend","username=? and friend_username=?",
                new String[]{MainActivity.me.getUsername(),user.getUsername()});
        database.delete("request","username=? and sender=?",
                new String[]{MainActivity.me.getUsername(),user.getUsername()});
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void deleteFail(String msg) {
        myDialog.cancelLoadingGifDialog();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
}
