package com.example.xunhu.xunchat.View;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.xunhu.xunchat.Model.Entities.Me;
import com.example.xunhu.xunchat.Model.SQLite.XunChatDatabaseHelper;
import com.example.xunhu.xunchat.Presenter.LogoutPresenter;
import com.example.xunhu.xunchat.Presenter.MyCookieValidationPresenter;
import com.example.xunhu.xunchat.Presenter.MyLoginPresenter;
import com.example.xunhu.xunchat.Presenter.MyProfileUpdatePresenter;
import com.example.xunhu.xunchat.Presenter.MyRegisterPresenter;
import com.example.xunhu.xunchat.R;
import com.example.xunhu.xunchat.View.Activities.ProfileThemeActivity_;
import com.example.xunhu.xunchat.View.Activities.SubActivity_;
import com.example.xunhu.xunchat.View.AllViewClasses.MyDialog;
import com.example.xunhu.xunchat.View.Fragments.ChatsFragment;
import com.example.xunhu.xunchat.View.Fragments.ChatsFragment_;
import com.example.xunhu.xunchat.View.Fragments.ContactsFragment;
import com.example.xunhu.xunchat.View.Fragments.ContactsFragment_;
import com.example.xunhu.xunchat.View.Fragments.DatePickerDialogFragment;
import com.example.xunhu.xunchat.View.Fragments.DiscoverFragment;
import com.example.xunhu.xunchat.View.Fragments.DiscoverFragment_;
import com.example.xunhu.xunchat.View.Fragments.GenderSelectionFragment;
import com.example.xunhu.xunchat.View.Fragments.LocationListDialog;
import com.example.xunhu.xunchat.View.Fragments.LoginFragment;
import com.example.xunhu.xunchat.View.Fragments.LoginFragment_;
import com.example.xunhu.xunchat.View.Fragments.MeFragment;
import com.example.xunhu.xunchat.View.Fragments.MeFragment_;
import com.example.xunhu.xunchat.View.Fragments.SignUpFragment;
import com.example.xunhu.xunchat.View.Fragments.SignUpFragment_;
import com.example.xunhu.xunchat.View.Fragments.UpdateProfileDialogFragment;
import com.example.xunhu.xunchat.View.Interfaces.LoginView;
import com.example.xunhu.xunchat.View.Interfaces.LogoutView;
import com.example.xunhu.xunchat.View.Interfaces.RegisterView;
import com.example.xunhu.xunchat.View.Interfaces.UpdateProfileView;
import com.example.xunhu.xunchat.View.Interfaces.ValidateCookiesView;
import com.google.firebase.iid.FirebaseInstanceId;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

@EActivity
public class MainActivity extends FragmentActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener, LoginFragment.LoginInterface, RegisterView, SignUpFragment.SignUpInterface,
        LoginView, ValidateCookiesView,GenderSelectionFragment.GenderInterface,MeFragment.MeFragmentInterface,
        UpdateProfileView,UpdateProfileDialogFragment.UpdateProfileDialogFragmentInterface,
        DatePickerDialogFragment.DatePickerInterface, LocationListDialog.LocationDialogInterface,
        LogoutView,ContactsFragment.ContactFragmentInterface{
    @ViewById(R.id.iv_addFriends) ImageView ivAddFriends;
    @ViewById(R.id.mypager) ViewPager pager;
    @ViewById(R.id.bottomMenu) android.support.design.widget.BottomNavigationView BottomMenu;
    @ViewById(R.id.tv_network_error) TextView tvNetworkError;
    @ViewById(R.id.topBar) RelativeLayout rlTopBar;
    @ViewById(R.id.top_logout_layout) RelativeLayout rlLogout;
    @org.androidannotations.annotations.res.StringRes public static String FRIEND_REQUEST_RESPOND;
    public static final String SEARCH_FRIEND = "http://xunsavior.com/xunchat/search.php";
    public static final String SEND_FRIEND_REQUEST = "http://xunsavior.com/xunchat/FriendRequest.php";
    public static final String EDIT_PROFILE = "http://xunsavior.com/xunchat/edit_profile.php";
    public static final String DECLINE_REQUEST = "http://xunsavior.com/xunchat/request_decline.php";
    public static final String RETRIEVE_FRIEND_LIST = "http://xunsavior.com/xunchat/retrieve_friend_list.php";
    public static final String SET_REMARK = "http://xunsavior.com/xunchat/set_remark.php";
    public static final String DELETE_FRIEND = "http://xunsavior.com/xunchat/delete_friend.php";
    public static final String SEND_MESSAGE = "http://xunsavior.com/xunchat/send_message.php";
    public static final String POST = "http://xunsavior.com/xunchat/post.php";
    public static final String FETCH_POSTS = "http://xunsavior.com/xunchat/fetch_posts.php";
    public static final String LIKE_POST = "http://xunsavior.com/xunchat/like_post.php";
    public static final String DELETE_POST = "http://xunsavior.com/xunchat/delete_post.php";
    public static final String LOAD_WHO_LIKE_POST = "http://xunsavior.com/xunchat/load_who_like_post.php";
    private static final String FRIEND_REQUEST = "friend_request";
    private static final String NETWORK_STATE_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    MyPagerAdapter adapter;
    LoginFragment loginFragment;
    SignUpFragment signUpFragment;
    @Bean MyRegisterPresenter registerPresenter;
    @Bean MyLoginPresenter loginPresenter;
    @Bean MyCookieValidationPresenter validationPresenter;
    MyProfileUpdatePresenter profileUpdatePresenter;
    @Bean LogoutPresenter logoutPresenter;
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    public static String domain_url ="http://xunsavior.com/xunchat/";
    static String password = "";
    static String username = "";
    public static Me me=null;
    static int screenWidth = 0;
    ChatsFragment chatsFragment;
    ContactsFragment contactsFragment;
    DiscoverFragment discoverFragment;
    MeFragment meFragment;
    FragmentManager fm = getSupportFragmentManager();
    boolean isYourProfileLoading = false;
    public static XunChatDatabaseHelper xunChatDatabaseHelper;
    float Y = 0;
    private int numberOfRequests = 0;
    XunChatBroadcastReceiver xunChatBroadcastReceiver=null;
    IntentFilter intentFilter;
    MyDialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDialog = new MyDialog(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.background_layout);
        xunChatDatabaseHelper = new XunChatDatabaseHelper(this,"XunChat.db",null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        validateCookies();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }
    public void setBroadcastReceivers(){
        xunChatBroadcastReceiver = new XunChatBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(FRIEND_REQUEST);
        intentFilter.addAction(NETWORK_STATE_CHANGE);
        registerReceiver(xunChatBroadcastReceiver,intentFilter);
    }
    @Override
    public void performUpdate(String title, String content) {
        profileUpdatePresenter = new MyProfileUpdatePresenter(this,this.getApplicationContext());
        profileUpdatePresenter.attemptUpdateProfile(me.getUsername(),title,content);
    }
    @Override
    public void updateFail(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void setEditLocation(String title, String content) {
        profileUpdatePresenter = new MyProfileUpdatePresenter(this,this.getApplicationContext());
        profileUpdatePresenter.attemptUpdateProfile(me.getUsername(),title,content);
    }
    @Override
    public void changeMyProfile(String msg) {
        try {
            JSONObject object = new JSONObject(msg);
            String title = object.getString("title");
            String content = object.getString("content");
                if (title.contains("nickname")){
                    me.setNickname(content);
                    meFragment.getEditProfileLayout().getTvEditUsername().setText(content);
                    meFragment.setIvMyProfile(me);
                }else if (title.contains("email")){
                    me.setEmail(content);
                    meFragment.getEditProfileLayout().getTvEditEmail().setText(content);
                }else if (title.contains("gender")){
                    me.setGender(content);
                    meFragment.getEditProfileLayout().getTvChangeGender().setText(content);
                }else if (title.contains("region")){
                    me.setRegion(content);
                    meFragment.getEditProfileLayout().getTvEditRegion().setText(content);
                }else if (title.contains("whatsup")){
                    me.setWhatsup(content);
                    meFragment.getEditProfileLayout().getTvWhatsUp().setText(content);
                }else if (title.contains("birthday")){
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    int age = currentYear-Integer.parseInt(content.split("/")[2]);
                    me.setBirthday(content);
                    me.setAge(age);
                    meFragment.getEditProfileLayout().getTvBirthday().setText(content);
                    meFragment.setIvMyProfile(me);
                }else if (title.contains("edit profile image")){
                    me.setUrl(content);
                    meFragment.setIvMyProfile(me);
                    meFragment.getEditProfileLayout().setInformation(me);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadLoginRegisterInterface(){
        setContentView(R.layout.log_regiser_layout);
        registerPresenter = new MyRegisterPresenter(getApplicationContext());
        registerPresenter.setRegisterView(this);
        loginPresenter = new MyLoginPresenter(getApplicationContext());
        loginPresenter.setLoginView(this);
        addLoginFragment();
    }
    public void loadUserPageInterface(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setContentView(R.layout.activity_main);
        pager.addOnPageChangeListener(this);
        rlTopBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Y = event.getY();
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return  true;
                    case MotionEvent.ACTION_MOVE:
                        float up_y = event.getY();
                        float a = up_y-Y;
                        if (a>100){
                            rlLogout.setVisibility(View.VISIBLE);
                        }else if (a<-100){
                            rlLogout.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        BottomMenu.setOnNavigationItemSelectedListener(this);
        adapter = new MyPagerAdapter(fm);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
        setBroadcastReceivers();
        checkRequestStatus();
    }
    public void validateCookies(){
        validationPresenter = new MyCookieValidationPresenter(getApplicationContext());
        validationPresenter.setValidateCookiesView(this);
        String cookies = readCookie();
        if (!cookies.isEmpty()){
            try {
                JSONObject jsonObject = new JSONObject(cookies);
                String username = jsonObject.getString("username");
                password= jsonObject.getString("password");
                validationPresenter.validateAttempt(username,password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            loadLoginRegisterInterface();
        }
    }
    @Override
    public void operateLogin(String username, String password) {
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        this.password = password;
        myDialog.createLoadingGifDialog();
        loginPresenter.attemptLogin(username,password,refreshedToken);
    }
    @Override
    public void loginFail(String msg) {
        myDialog.cancelLoadingGifDialog();
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }
    public void retrieveUserInformation(String msg){
        try {
            JSONObject object = new JSONObject(msg);
            me = new Me(
                    object.getInt("user_id"),
                    object.getString("username"),
                    object.getString("nickname"),
                    object.getString("url"),
                    object.getString("email"),
                    object.getString("gender"),
                    object.getString("region"),
                    object.getString("QRCode"),
                    object.getString("what_is_up"),
                    object.getInt("age"),
                    object.getString("birthday"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void refreshActivity(String msg) {
        myDialog.cancelLoadingGifDialog();
        retrieveUserInformation(msg);
        this.username = me.getUsername();
        storeCookies(me.getUsername(),password);
        loadUserPageInterface();
        Toast.makeText(MainActivity.this,"login successfully",Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void operateSignUp(String username, String nickname,String password, String email, String url, String birthday, String token, String gender, String region,String QRCode) {
        myDialog.createLoadingGifDialog();
        registerPresenter.attemptRegister(username,nickname,password,email,url,birthday,token,gender,region,QRCode);
    }

    @Override
    public void launchGenderSelectionFragment() {
        GenderSelectionFragment genderDialog = new GenderSelectionFragment();
        genderDialog.show(getFragmentManager(),"gender dialog");
    }

    @Override
    public void launchBirthdayPickerFragment() {
        DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
        datePickerDialogFragment.show(getFragmentManager(),"Select Your Date of Birth");
    }

    @Override
    public void launchLocationListsFragment() {
        LocationListDialog dialog = new LocationListDialog("register");
        dialog.show(getFragmentManager(),"Location Dialog");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (isYourProfileLoading==false && me!=null){
            meFragment.setIvMyProfile(me);
            isYourProfileLoading=true;
        }
        switch (item.getItemId()){
            case R.id.item_chats:
                pager.setCurrentItem(0);
                break;
            case R.id.item_contacts:
                pager.setCurrentItem(1);
                break;
            case R.id.item_discover:
                pager.setCurrentItem(2);
                break;
            case R.id.item_me:
                pager.setCurrentItem(3);
                break;
        }
        return true;
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
            if (isYourProfileLoading==false && me!=null){
                meFragment.setIvMyProfile(me);
                isYourProfileLoading=true;
             }
        switch (position){
            case 0:
                BottomMenu.getMenu().getItem(0).setChecked(true);
                break;
            case 1:
                BottomMenu.getMenu().getItem(1).setChecked(true);
                break;
            case 2:
                BottomMenu.getMenu().getItem(2).setChecked(true);
                break;
            case 3:
                BottomMenu.getMenu().getItem(3).setChecked(true);
                break;
            default:
                break;
        }
        for (int i = 0;i<4;i++){
            if (i!=position){
                BottomMenu.getMenu().getItem(i).setChecked(false);
            }
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }
    @Override
    public void registerFail(String msg) {
        myDialog.cancelLoadingGifDialog();
        if (msg.equals("This username has already existed!")){
            signUpFragment.existError(msg);
        }else {
            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void switchToLoginInterface() {
        myDialog.cancelLoadingGifDialog();
        switchToLogin();
        Toast.makeText(MainActivity.this,"Register Successfully!",Toast.LENGTH_SHORT).show();
    }
    public void addLoginFragment(){
        loginFragment= new LoginFragment_();
        getFragmentManager().beginTransaction().add(R.id.panel,loginFragment).commit();
    }
    public void switchToLogin(){
        getFragmentManager().beginTransaction().replace(R.id.panel,loginFragment).commit();
    }
    public void storeCookies(String username,String password){
        String name = "";
        ContentValues values = new ContentValues();
        SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
        Cursor cursor = database.query("user",new String[]{"username"},
                "username"+"=?",new String[]{username},
                null,null,null);
        if (cursor.moveToFirst()){
            do{
                name = cursor.getString(cursor.getColumnIndex("username"));
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (name.equals("")){
            values.put("username",username);
            values.put("password",password);
            values.put("isActive","1");
            database.insert("user",null,values);
        }else {
            values.put("password",password);
            values.put("isActive","1");
            database.update("user",values,"username=?",new String[]{username});
        }
        database.close();
    }
    public String readCookie(){
        String content = "";
        String name = "";
        String pw = "";
        JSONObject object = new JSONObject();
        SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
        Cursor cursor = database.query("user",new String[]{"username","password"},"password"+"!=?",new String[]{""},null,null,null);
        if (cursor.moveToFirst()){
            do{
                name = cursor.getString(cursor.getColumnIndex("username"));
                pw = cursor.getString(cursor.getColumnIndex("password"));
            }while (cursor.moveToNext());
            try {
                object.put("username",name);
                object.put("password",pw);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        content = object.toString();
        System.out.println("@ content "+content);
        database.close();
        if (content.equals("{}")){
            return "";
        }else {
            return content;
        }
    }
    @SuppressLint("ResourceType")
    @Override
    public void switchToRegister() {
        signUpFragment= new SignUpFragment_();
        getFragmentManager().beginTransaction().
                replace(R.id.panel,signUpFragment).
                setCustomAnimations(R.animator.card_flip_right_in,R.animator.card_flip_right_out).addToBackStack(null).
                commit();
    }
    @Override
    public void validateFail() {
        loadLoginRegisterInterface();
    }

    @Override
    public void validateTimeout() {
        loadUserPageInterface();
        Toast.makeText(getApplicationContext(),"no response from server",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void switchToAccountLayout(String msg) {
        retrieveUserInformation(msg);
        loadUserPageInterface();

    }
    public void unregisterReceivers(){
        if (xunChatBroadcastReceiver!=null){
            unregisterReceiver(xunChatBroadcastReceiver);
        }
    }
    @Override
    public void respondGender(String selection) {
        signUpFragment.setGender(selection);
    }

    @Override
    public void enlargeProfileImage(String url) {
        ProfileThemeActivity_.intent(this).extra("url",url).start();
    }
    @Override
    public void launchUpdateDialog(String title, String content) {
        UpdateProfileDialogFragment updateProfileDialogFragment = new UpdateProfileDialogFragment(title,content);
        updateProfileDialogFragment.show(getFragmentManager(),title);
    }

    @Override
    public void launchLocationListDialog() {
        LocationListDialog dialog = new LocationListDialog("edit");
        dialog.show(getFragmentManager(),"location");
    }

    @Override
    public void setBottomMenuInvisible() {
       BottomMenu.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed() {
        if (meFragment!=null){
            if (meFragment.getEditProfileLayout()!=null){
                meFragment.removeEditProfileLayout();
                BottomMenu.setVisibility(View.VISIBLE);
            }else {
                moveTaskToBack(true);
            }
        }else {
                super.onBackPressed();
        }

    }
    @Click({R.id.iv_addFriends,R.id.top_logout_layout})
    public void respond(View view){
        switch (view.getId()){
            case R.id.iv_addFriends:
                SubActivity_.intent(this).extra("type","new friends").start();
                break;
            case R.id.top_logout_layout:
                myDialog.createGifLogoutDialog();
                logoutPresenter = new LogoutPresenter(getApplicationContext());
                logoutPresenter.setLogoutView(this);
                logoutPresenter.implementLogout(me.getUsername());
                break;
            default:
                break;
        }
    }
    public void setPasswordToEmptyAndReloadActivity(){
            SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("password","");
            contentValues.put("isActive","0");
            database.update("user",contentValues,"username=?",new String[]{me.getUsername()});
            database.close();
            finish();
            startActivity(getIntent());
    }
    public static int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public void birthdaySelected(String birthday) {
        signUpFragment.setEtBirthday(birthday);
    }

    @Override
    public void setSelectedLocation(String location) {
        signUpFragment.setLocation(location);
    }

    @Override
    public void logoutSuccessful(String msg) {
        myDialog.cancelLogoutDialog();
        setPasswordToEmptyAndReloadActivity();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void logoutFail(String msg) {
        myDialog.cancelLogoutDialog();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clearRequests() {
        numberOfRequests=0;
        contactsFragment.getTvNumOfRequests().setText(String.valueOf(numberOfRequests));
        contactsFragment.getTvNumOfRequests().setVisibility(View.INVISIBLE);
    }
    class XunChatBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()){
                    case FRIEND_REQUEST:
                        checkRequestStatus();
                        break;
                    case NETWORK_STATE_CHANGE:
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if (networkInfo!=null && networkInfo.isAvailable()){
                            tvNetworkError.setVisibility(View.GONE);
                        }else {
                            tvNetworkError.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }
        }
    }
    public void checkRequestStatus(){
            try {
                SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
                Cursor cursor = database.rawQuery("SELECT DISTINCT sender, " +
                                "extras FROM request WHERE isRead=? AND username=?",
                        new String[]{"0",me.getUsername()});
                numberOfRequests=0;
                if (cursor.moveToFirst()){
                    do{
                        System.out.println("@ "+cursor.getString(cursor.getColumnIndex("sender"))+
                                " "+cursor.getString(cursor.getColumnIndex("extras")));
                        numberOfRequests++;
                    }while (cursor.moveToNext());
                }
                if (numberOfRequests>0){
                    contactsFragment.getTvNumOfRequests().setText(String.valueOf(numberOfRequests));
                    contactsFragment.getTvNumOfRequests().setVisibility(View.VISIBLE);
                }
                database.close();
                cursor.close();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
    }

    public static String timeGap(long timestamp){
        long currentTime = System.currentTimeMillis();
        long timeGap = currentTime-timestamp;
        if (timeGap/1000/60<1){
            return String.valueOf(timeGap/1000)+"s ago";
        }else if (timeGap/1000/60<60 && timeGap/1000/60>0){
            return String.valueOf(timeGap/1000/60)+"min ago";
        }else if (timeGap/1000/60/60>0 && timeGap/1000/60/60<24){
            return String.valueOf(timeGap/1000/60/60)+"hrs ago";
        }else{
            return String.valueOf(timeGap/1000/60/60/24)+"day(s) ago";
        }
    }

    class MyPagerAdapter extends FragmentStatePagerAdapter{
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            chatsFragment = new ChatsFragment_();
            contactsFragment = new ContactsFragment_();
            discoverFragment = new DiscoverFragment_();
            meFragment = new MeFragment_();
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return chatsFragment;
                case 1:
                    return contactsFragment;
                case 2:
                    return discoverFragment;
                case 3:
                    return meFragment;
                default:
                    return null;
            }
        }
        @Override
        public int getCount() {
            return 4;
        }
    }
}
