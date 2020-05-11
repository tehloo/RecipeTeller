package com.example.recipe_teller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private AlertDialog dialog;//

    private static final String TAG = "LoginActivity";//태그 생성함
    private FirebaseAuth mAuth;//파이어베이스 인증기능

    private String userEmail;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        Button loginButton = (Button) findViewById((R.id.loginButton));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });//버튼을 클릭하면 레지스터 실행된다.

        TextView registerButton = (TextView) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signupintent = new Intent(LoginActivity.this, SignupActivity.class);
                LoginActivity.this.startActivity(signupintent);
            }
        });//회원 가입 버튼을 눌렀을때 회원가입창으로 이동한다.

    };


    @Override//로그인 되어있는 상태일때의 동작을 지정한다.
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    protected  void onStop() {
        super.onStop();
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }//다이얼로그 켠상태에서는 끌수 없다.

    private void login(){//회원가입 로직을구현
        final EditText emailText = (EditText) findViewById((R.id.idText));
        final EditText passwordText = (EditText) findViewById(R.id.passwordText);

        String userEmail = emailText.getText().toString();
        String userPassword = passwordText.getText().toString();

        if(userEmail.equals("")||userPassword.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            dialog = builder.setMessage("빈 칸 없이 입력해주세요.")
                    .setNegativeButton("확인",null)
                    .create();
            dialog.show();
            return;
        }
        if(userPassword.length()<6){
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            dialog = builder.setMessage("비밀번호는 6자 이상 입력.")
                    .setNegativeButton("확인",null)
                    .create();
            dialog.show();
            return;
        }

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            dialog = builder.setMessage("Welcome to TILO")
                                    .setPositiveButton("확인", null)
                                    .create();
                            dialog.show();
                            Intent mainintent = new Intent(LoginActivity.this, MainActivity.class);
                            mainintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//뒤로가기 버튼 눌렀을때 메인에서 로그인으로 못가게?
                            LoginActivity.this.startActivity(mainintent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            dialog = builder.setMessage("Login failed")
                                    .setNegativeButton("확인", null)
                                    .create();
                            dialog.show();
                        }

                        // ...
                    }
                });
    }
}
