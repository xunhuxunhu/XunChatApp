package com.example.xunhu.xunchat.Model.Services;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.xunhu.xunchat.Model.Entities.LatestMessage;
import com.example.xunhu.xunchat.Model.SQLite.XunChatDatabaseHelper;
import com.example.xunhu.xunchat.View.MainActivity;
import com.example.xunhu.xunchat.View.Notifications.MyNotification;
import com.example.xunhu.xunchat.View.XunApplication;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xunhu on 6/23/2017.
 */

public class XunChatReceiveMessageService extends FirebaseMessagingService {
    private int FRIEND_REQUST_NOTIFICATION_ID = 45612;
    private int REQUEST_RESPOND_NOTIFICATION_ID  = 45613;
    private int CHAT_MESSAGE_NOTIFICATION_ID = 45614;
    private static final String FRIEND_REQUEST = "friend_request";
    private static final String REQUEST_ACCEPTED = "accepted_respond";
    private static final String MESSAGE = "message";
    public static final String REFRESH_CHAT_FRAGMENT = "refresh.chat.fragment";
    public static final String REFRESH_CHAT_BOARD = "refresh.chat.board";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("@ message"+remoteMessage.getData().get("message"));
        try {
            operateMessage(remoteMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void operateMessage(RemoteMessage remoteMessage) throws JSONException {
        JSONObject object = new JSONObject(remoteMessage.getData().get("message"));
        String type = object.getString("message_type");
        switch (type){
            case FRIEND_REQUEST:
                int senderID = object.getInt("sender_id");
                String senderName = object.getString("sender_username");
                String senderNickname = object.getString("sender_nickname");
                String senderURL = object.getString("sender_url");
                String senderExtras = object.getString("sender_extras");
                String requestTime = String.valueOf(System.currentTimeMillis());
                storeFriendRequest(senderID,senderName,senderNickname,senderURL,senderExtras,requestTime);
                sendFriendRequestBroadcast(senderName);
                MyNotification friendRequestNotification = new MyNotification(FRIEND_REQUST_NOTIFICATION_ID);
                String requestTicker = senderName+" has sent you a friend request.";
                String requestContent = " has sent you a friend request.";
                friendRequestNotification.createRequestRespondNotification(senderName,senderURL,requestTicker,requestContent);
                break;
            case REQUEST_ACCEPTED:
                int responderID = object.getInt("responder_id");
                String responderUsername = object.getString("responder_username");
                String responderNickname = object.getString("responder_nickname");
                String responderURL = object.getString("responder_url");
                String respondTime = String.valueOf(System.currentTimeMillis());
                MyNotification respondNotification = new MyNotification(REQUEST_RESPOND_NOTIFICATION_ID);
                String respondTicker = responderUsername+" has accepted your friend request.";
                String respondContent = " has accepted your friend request.";
                storeFriend(responderID,responderUsername,responderNickname,responderURL);
                respondNotification.createRequestRespondNotification(responderUsername,responderURL,respondTicker,respondContent);
                break;
            case MESSAGE:
                int friendID = object.getInt("sender_id");
                String friendUsername = object.getString("sender_username");
                String friendURL = object.getString("sender_url");
                String friendNickname = object.getString("sender_nickname");
                int messageType = object.getInt("sending_message_type");
                String message = object.getString("message");
                String time = object.getString("timestamp");
                MyNotification chatMessageNotification = new MyNotification(CHAT_MESSAGE_NOTIFICATION_ID);
                String chatMessageTicker = friendNickname+" has sent you a message.";
                switch (messageType){
                    case 0:
                        chatMessageNotification.createRequestRespondNotification(friendNickname+":",friendURL,chatMessageTicker,message);
                        break;
                    case 1:
                        chatMessageNotification.createRequestRespondNotification(friendNickname+":",friendURL,chatMessageTicker,"[photo]");
                        break;
                    default:
                        chatMessageNotification.createRequestRespondNotification(friendNickname+":",friendURL,chatMessageTicker,"[audio]");
                        break;
                }
                storeLatestChatMessage(friendID,friendUsername,friendNickname,friendURL,message,time,messageType);
                break;
            default:
                break;
        }
    }
    public void storeLatestChatMessage(int friendID, String friendUsername,String friendNickname,
                                       String friendURL,String message,String timestamp,int messageType){
        XunChatDatabaseHelper xunChatDatabaseHelper = new XunChatDatabaseHelper(
                XunApplication.getContext(),"XunChat.db",null);
        SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
        String currentUser = returnCurrentUser();
        if (!currentUser.isEmpty()){
            String queryFriendName = "";
            int unread = 0;
            Cursor cursor = database.rawQuery("select unread,friend_username from latest_message " +
                    "where username=? AND friend_username=?",new String[]{currentUser,friendUsername});
            if (cursor.moveToFirst()){
                do {
                    queryFriendName = cursor.getString(cursor.getColumnIndex("friend_username"));
                    unread = cursor.getInt(cursor.getColumnIndex("unread"));
                    break;
                }while (cursor.moveToNext());
                cursor.close();
            }
            unread++;
            ContentValues contentValues = new ContentValues();
            contentValues.put("friend_id",friendID);
            contentValues.put("friend_username",friendUsername);
            contentValues.put("friend_nickname",friendNickname);
            contentValues.put("friend_url",friendURL);
            contentValues.put("friend_latest_message",message);
            contentValues.put("friend_time",timestamp);
            contentValues.put("type",messageType);
            contentValues.put("unread",unread);
            contentValues.put("username",currentUser);
            if (queryFriendName.isEmpty()){
                database.insert("latest_message",null,contentValues);
            }else {
                database.update("latest_message",
                        contentValues,
                        "username=? and friend_username=?",
                        new String[]{currentUser,friendUsername});
            }
            contentValues.clear();
            contentValues.put("friend_username",friendUsername);
            contentValues.put("username",currentUser);
            contentValues.put("message_type",messageType);
            contentValues.put("me_or_friend",1);
            contentValues.put("message_content",message);
            contentValues.put("time",timestamp);
            contentValues.put("is_sent",1);
            database.insert("message",null,contentValues);
            database.close();
        }
        Intent intentLatestChat = new Intent(REFRESH_CHAT_FRAGMENT);
        getApplicationContext().sendBroadcast(intentLatestChat);
        Intent chatMessage = new Intent(REFRESH_CHAT_BOARD);
        getApplicationContext().sendBroadcast(chatMessage);
    }
    public void storeFriend(int friendID,String friendUsername,String friendNickname,String friendURL){
        XunChatDatabaseHelper xunChatDatabaseHelper = new XunChatDatabaseHelper(
                XunApplication.getContext(),"XunChat.db",null);
        SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
        String currentUser = returnCurrentUser();
        if (!currentUser.isEmpty()){
            ContentValues values = new ContentValues();
            values.put("friend_id",friendID);
            values.put("friend_username",friendUsername);
            values.put("friend_nickname",friendNickname);
            values.put("friend_url",MainActivity.domain_url+friendURL);
            values.put("username",currentUser);
            database.insert("friend",null,values);
        }
        xunChatDatabaseHelper.close();
        database.close();
    }
    public void sendFriendRequestBroadcast(String username){
        Intent intent = new Intent(FRIEND_REQUEST);
        intent.putExtra("username",username);
        sendBroadcast(intent);
    }
    public String returnCurrentUser(){
        String me="";
        XunChatDatabaseHelper xunChatDatabaseHelper = new XunChatDatabaseHelper(
                XunApplication.getContext(),"XunChat.db",null);
        SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT username FROM user WHERE isActive=?",new String[]{"1"} );
        if (cursor.moveToFirst()){
            do{
                me  = cursor.getString(cursor.getColumnIndex("username"));
                break;
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        xunChatDatabaseHelper.close();
        return me;
    }
    public void storeFriendRequest(int senderID,String senderName,String senderNickname, String url,String extras,String time){
        XunChatDatabaseHelper xunChatDatabaseHelper = new XunChatDatabaseHelper(
                XunApplication.getContext(),"XunChat.db",null);
        SQLiteDatabase database = xunChatDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        ArrayList<String> list = new ArrayList<>();
        String me="";
        String querySenderName = "";
        Cursor cursor = database.rawQuery("SELECT username FROM user WHERE isActive=?",new String[]{"1"} );
        if (cursor.moveToFirst()){
            do{
                me  = cursor.getString(cursor.getColumnIndex("user.username"));
                list.add(querySenderName);
                break;
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (!me.equals("")){
            String sender_name = "";
            contentValues.put("sender_id",String.valueOf(senderID));
            contentValues.put("sender",senderName);
            contentValues.put("sender_nickname",senderNickname);
            contentValues.put("extras",extras);
            contentValues.put("isRead","0");
            contentValues.put("isAgreed","0");
            contentValues.put("time",time);
            contentValues.put("url",MainActivity.domain_url+url);
            contentValues.put("username",me);
            Cursor cursorRequest = database.rawQuery("SELECT sender FROM request WHERE username=? AND sender=?",new String[]{me,senderName});
            if (cursorRequest.moveToFirst()){
                do {
                    sender_name = cursorRequest.getString(cursorRequest.getColumnIndex("sender"));
                }while (cursorRequest.moveToNext());
            }
            cursorRequest.close();
            if (sender_name.isEmpty()){
                database.insert("request",null,contentValues);
            }else {
                database.update("request",contentValues,"username=? AND sender=?",new String[]{me,senderName});
            }
        }
        xunChatDatabaseHelper.close();
        database.close();
    }
}
