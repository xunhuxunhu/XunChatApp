package com.example.xunhu.xunchat.View.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.xunhu.xunchat.Model.Entities.Message;
import com.example.xunhu.xunchat.Model.Entities.User;
import com.example.xunhu.xunchat.Model.Services.XunChatReceiveMessageService;
import com.example.xunhu.xunchat.Presenter.SendMessagePresenter;
import com.example.xunhu.xunchat.R;
import com.example.xunhu.xunchat.View.AllAdapters.ChatMessageAdapter;
import com.example.xunhu.xunchat.View.AllViewClasses.MyDialog;
import com.example.xunhu.xunchat.View.Interfaces.SendChatView;
import com.example.xunhu.xunchat.View.MainActivity;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by xunhu on 7/18/2017.
 */
@EActivity(R.layout.chat_activity_layout)
public class ChatBoardActivity extends Activity implements SendChatView {
    @ViewById(R.id.iv_chat_activity_back) ImageView ivBack;
    @ViewById(R.id.tv_remark) TextView tvRemark;
    @ViewById(R.id.lv_message) ListView lvMessage;
    @ViewById(R.id.ib_camera) ImageButton ibCamera;
    @ViewById(R.id.et_message) EditText etMessage;
    @ViewById(R.id.ib_voice) ImageButton ibVoice;
    @ViewById(R.id.ib_sending) ImageButton ibSending;
    MediaRecorder mediaRecorder;
    public static User user;
    boolean isRecordingStart=false;
    List<Message> messages = new ArrayList<>();
    ChatMessageAdapter adapter;
    SendMessagePresenter presenter;
    IntentFilter intentFilter = new IntentFilter();
    private String audioOutput= null;
    private MyDialog myDialog;
    private static final int ACCESS_RECORDER = 10;
    public static final int UPDATE_DB = 11;
    public static final int SENDING_IMAGE= 9;
    private MyDialog myLoadingDialog=null;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadMessage();
        }
    };
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case UPDATE_DB:
                    myDialog.setDBResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };
    @AfterViews void setChatBoardActivityViews(){
        user = (User) getIntent().getSerializableExtra("user");
        adapter = new ChatMessageAdapter(this,R.layout.message_unit_layout,messages,user);
        lvMessage.setAdapter(adapter);
        lvMessage.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lvMessage.setStackFromBottom(false);
        tvRemark.setText(user.getRemark());
        myDialog = new MyDialog(this);
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                    ){

            }else {
                requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, ACCESS_RECORDER);
            }
        }
    }
    @Touch({R.id.ib_voice}) boolean onVoiceTouch(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                establishRecorder();
                startRecording();
                break;
            case MotionEvent.ACTION_CANCEL:
                stopAndPlay();
                break;
            case MotionEvent.ACTION_UP:
                stopAndPlay();
                sendRecordedAudio();
                break;
            default:
                break;
        }
        return false;
    }

    public void establishRecorder(){
        String dir = Environment.getExternalStorageDirectory()+
                File.separator+MainActivity.me.getUsername()+
                File.separator+user.getUsername();
        File file = new File(dir);
        if (!file.exists()){
            file.mkdirs();
        }
        audioOutput = dir+"initial_audio.3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioEncodingBitRate(320000);
        mediaRecorder.setOutputFile(audioOutput);
    }
    public void startRecording(){
        myDialog.createVoiceLevelDialog();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecordingStart=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                   while (isRecordingStart){
                        if (mediaRecorder!=null){
                            try {
                                int db = mediaRecorder.getMaxAmplitude();
                                android.os.Message message = new android.os.Message();
                                message.what=UPDATE_DB;
                                message.arg1=db;
                                handler.sendMessage(message);
                            }catch (RuntimeException e){
                                e.printStackTrace();
                            }
                        }
                       try {
                           Thread.sleep(100);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stopAndPlay(){
        myDialog.cancelVoiceLevelDialog();
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            isRecordingStart=false;
            mediaRecorder=null;
        } catch (RuntimeException e){
            e.printStackTrace();
        }
    }
    public void sendRecordedAudio(){
        byte[] bytes = getAudioBytes(audioOutput);
        String encoded = Base64.encodeToString(bytes,0);
        Long timestamp = System.currentTimeMillis();
        storeLatestMessage(user.getUserID(),user.getUsername(),user.getRemark(),
                user.getUrl(),encoded,String.valueOf(timestamp),2);
        Message message = new Message(MainActivity.domain_url+MainActivity.me.getUrl(),2,0,
                encoded,String.valueOf(timestamp));
        presenter = new SendMessagePresenter(this);
        presenter.sendingMessage(MainActivity.me,user.getUsername(),user.getUserID(),2,encoded,timestamp);
        messages.add(message);
        adapter.notifyDataSetChanged();
        scrollMyListViewToBottom();
    }
    public byte[] getAudioBytes(String filename){
        File file = new File(filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
            }
            byte[] bytes = bos.toByteArray();
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
    @Click({R.id.iv_chat_activity_back,R.id.ib_sending,R.id.et_message,R.id.ib_camera})
    public void onClickView(View view){
        switch (view.getId()){
            case R.id.iv_chat_activity_back:
                onBackPressed();
                break;
            case R.id.ib_sending:
                sendMessageAndStoreMessage();
                break;
            case R.id.ib_camera:
                Intent intent = new Intent(this,PhotoTakenActivity_.class);
                startActivityForResult(intent,SENDING_IMAGE);
                break;
            default:
                break;
        }
    }
    public void sendMessageAndStoreMessage(){
        if (etMessage.getText().toString().isEmpty()){
            etMessage.setError("empty text");
        }else {
            long timestamp = System.currentTimeMillis();
            presenter = new SendMessagePresenter(this);
            presenter.sendingMessage(MainActivity.me,user.getUsername(),user.getUserID(),
                    0,etMessage.getText().toString(),timestamp);
            Message message = new Message(MainActivity.domain_url+MainActivity.me.getUrl(),
                    0,0,etMessage.getText().toString(),String.valueOf(timestamp));
            messages.add(message);
            adapter.notifyDataSetChanged();
            scrollMyListViewToBottom();
            storeLatestMessage(user.getUserID(),user.getUsername(),user.getRemark(),user.getUrl(),
                    etMessage.getText().toString(),String.valueOf(timestamp),0);
            etMessage.setText("");
        }
    }
    public void clearUnreadMessage(){
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        Long timestamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put("unread",0);
        database.update("latest_message",contentValues,
                "username=? and friend_username=?",new String[]{MainActivity.me.getUsername(),user.getUsername()});
        database.close();
    }

    public void storeLatestMessage(int friendID, String friendUsername,String friendNickname,
                                   String friendURL,String message,String timestamp,int messageType ){
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        String queryFriendName = "";
        Cursor cursor = database.rawQuery("select friend_username from latest_message where username=? " +
                "and friend_username=?",new String[]{MainActivity.me.getUsername(),friendUsername});
            if (cursor.moveToFirst()){
                do {
                    queryFriendName = cursor.getString(cursor.getColumnIndex("friend_username"));
                }while (cursor.moveToNext());
                cursor.close();
            }
        System.out.println("@ query "+queryFriendName);
        ContentValues contentValues = new ContentValues();
        contentValues.put("friend_id",friendID);
        contentValues.put("friend_username",friendUsername);
        contentValues.put("friend_nickname",friendNickname);
        contentValues.put("friend_url",friendURL);
        contentValues.put("friend_latest_message",message);
        contentValues.put("friend_time",timestamp);
        contentValues.put("type",messageType);
        contentValues.put("unread",0);
        contentValues.put("username",MainActivity.me.getUsername());
        if (queryFriendName.isEmpty()){
            database.insert("latest_message",null,contentValues);
        }else {
            database.update("latest_message",
                    contentValues,
                    "username=? and friend_username=?",
                    new String[]{MainActivity.me.getUsername(),friendUsername});
        }
        contentValues.clear();
        contentValues.put("friend_username",friendUsername);
        contentValues.put("username",MainActivity.me.getUsername());
        contentValues.put("message_type",messageType);
        contentValues.put("me_or_friend",0);
        contentValues.put("message_content",message);
        contentValues.put("time",timestamp);
        contentValues.put("is_sent",1);
        database.insert("message",null,contentValues);
        database.close();
    }
    public void loadMessage(){
        messages.clear();
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from message where username=? and friend_username=? order by time",
                new String[]{MainActivity.me.getUsername(),user.getUsername()});
        if (cursor.moveToFirst()){
            do {
                Message message;
                int messageType = cursor.getInt(cursor.getColumnIndex("message_type"));
                int meOrFriend = cursor.getInt(cursor.getColumnIndex("me_or_friend"));
                String messageContent = cursor.getString(cursor.getColumnIndex("message_content"));
                String timestamp = cursor.getString(cursor.getColumnIndex("time"));
                int isSent = cursor.getInt(cursor.getColumnIndex("is_sent"));
                message = (meOrFriend==0) ?
                        new Message(MainActivity.domain_url+MainActivity.me.getUrl(),messageType,meOrFriend,messageContent,timestamp)
                        :new Message(MainActivity.domain_url+user.getUrl(),messageType,meOrFriend,messageContent,timestamp);
                message.setIsSentSuccess(isSent);
                messages.add(message);
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        adapter.notifyDataSetChanged();
        scrollMyListViewToBottom();
    }
    @Override
    public void sendingMessageFail(long timestamp,String msg) {
        for (int i=0;i<messages.size();i++){
            if (messages.get(i).getTime().equals(String.valueOf(timestamp))){
                messages.get(i).setIsSentSuccess(0);
                setMessageFalse(MainActivity.me.getUsername(),user.getUsername(),String.valueOf(timestamp));
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }
    public void setMessageFalse(String myUsername,String friendUsername,String time){
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("is_sent",0);
        database.update("message",contentValues,"(username=?) and (friend_username=?) and (time=?)",
                new String[]{myUsername,friendUsername,time});
        database.close();
    }

    @Override
    public void sendingMessageSuccessful(long timestamp) {

    }
    @Override
    public void sendingImageSuccessful(long timestamp, String msg) {
        int index = msg.indexOf("}{");
        String subString = msg.substring(index+1);
        SQLiteDatabase database = MainActivity.xunChatDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message_type",1);
        contentValues.put("me_or_friend",0);
        contentValues.put("message_content",subString);
        contentValues.put("username",MainActivity.me.getUsername());
        contentValues.put("is_sent",1);
        contentValues.put("friend_username",ChatBoardActivity.user.getUsername());
        database.update("message",
                contentValues,
                "(username=?) and (friend_username=?) and (time=?)",
                new String[]{MainActivity.me.getUsername(),user.getUsername(),String.valueOf(timestamp)});
        for (int i=0;i<messages.size();i++){
            if (messages.get(i).getTime().equals(String.valueOf(timestamp))){
                messages.get(i).setMessageContent(subString);
                adapter.notifyDataSetChanged();
                break;
            }
        }
        database.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        intentFilter.addAction(XunChatReceiveMessageService.REFRESH_CHAT_BOARD);
        registerReceiver(broadcastReceiver,intentFilter);
        loadMessage();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearUnreadMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
    private void scrollMyListViewToBottom() {
        lvMessage.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                lvMessage.setSelection(adapter.getCount() - 1);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==ACCESS_RECORDER){
            if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext().getApplicationContext(),"we need access to your location",Toast.LENGTH_SHORT).show();
            }else {
                establishRecorder();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==SENDING_IMAGE && resultCode==RESULT_OK){
            if (data!=null){
                String bitmap = data.getStringExtra("bitmap");
                String caption = data.getStringExtra("caption");
                long timestamp = data.getLongExtra("timestamp",0);
                presenter = new SendMessagePresenter(this);
                JSONObject object = new JSONObject();
                try {
                    object.put("image_code",bitmap);
                    object.put("image_caption",caption);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                presenter.sendingMessage(MainActivity.me,user.getUsername(),user.getUserID(),1,object.toString(),timestamp);
            }
        }
    }
}
