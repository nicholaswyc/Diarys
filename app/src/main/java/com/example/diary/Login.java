package com.example.diary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;
    private EditText accountText;
    private EditText PasswordText;
    private Button login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountText = (EditText) findViewById(R.id.username);
        PasswordText = (EditText) findViewById(R.id.password);

        Button login = (Button) findViewById(R.id.login);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = accountText.getText().toString();
                String pass = PasswordText.getText().toString();
                //如果账号是admin 且密码是123456，就认为登录成功
                if (account.equals("admin")&&pass.equals("123456")){
                    SharedPreferences.Editor editor
                            = getSharedPreferences("LoginData",MODE_PRIVATE).edit();
                    editor.putString("username","admin");
                    editor.putString("password","123456");
                    editor.apply();
                    Intent intent = new Intent(Login.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(Login.this,"登录失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}