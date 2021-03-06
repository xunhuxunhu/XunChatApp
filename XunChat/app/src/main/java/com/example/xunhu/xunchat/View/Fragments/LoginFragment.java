package com.example.xunhu.xunchat.View.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.xunhu.xunchat.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by xunhu on 6/10/2017.
 */
@EFragment(R.layout.login_fragment_layout)
public class LoginFragment extends android.app.Fragment {
    @ViewById(R.id.et_login_username) EditText etLoginUsername;
    @ViewById(R.id.et_login_password) EditText etLoginPassword;
    @ViewById(R.id.btn_login) Button btnLogin;
    @ViewById Button btnSignUp;
    @SystemService InputMethodManager mgr;
    LoginInterface comm;
    @Click({R.id.btn_login,R.id.btnSignUp})
    void onSignUpClick(View view){
        switch (view.getId()){
            case R.id.btn_login:
                String username = etLoginUsername.getText().toString();
                String password = etLoginPassword.getText().toString();
                if (username.isEmpty()){
                    etLoginUsername.setError("Username is empty!");
                    return;
                }else if (password.isEmpty()){
                    etLoginPassword.setError("Password is empty!");
                    return;
                }
                //soft keyboard manager
                mgr.hideSoftInputFromWindow(etLoginPassword.getWindowToken(), 0);
                mgr.hideSoftInputFromWindow(etLoginUsername.getWindowToken(), 1);
                comm.operateLogin(username,password);
                break;
            case R.id.btnSignUp:
                comm.switchToRegister();
                break;
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        comm = (LoginInterface) activity;
    }
    public interface LoginInterface{
         void switchToRegister();
         void operateLogin(String username, String password);
    }
}
