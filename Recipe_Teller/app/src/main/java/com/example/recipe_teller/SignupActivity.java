package com.example.recipe_teller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";//태그 생성함
    private FirebaseAuth mAuth;//파이어베이스 인증기능
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //등록하기 버튼 기능 구현
        Button registerButton = (Button) findViewById((R.id.registerButton));
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });//버튼을 클릭하면 레지스터 실행된다.


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    protected  void onStop() {
        super.onStop();;
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }//다이얼로그 켠상태에서는 끌수 없다.

    private void register() {//회원가입 로직을구현

        final EditText emailText = (EditText) findViewById(R.id.emailText);
        final EditText passwordText = (EditText) findViewById(R.id.passwordText);
        final EditText passwordText2 = (EditText) findViewById(R.id.passwordText2);

        //입력값 받아오기

        String userEmail = emailText.getText().toString();
        String userPassword = passwordText.getText().toString();
        String userPassword2 = passwordText2.getText().toString();


        if (!userPassword.equals(userPassword2)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
            dialog = builder.setMessage("비밀번호가 일치하지 않습니다.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
            return;

        }
        if (userEmail.equals("") || userPassword.equals("") || userPassword2.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
            dialog = builder.setMessage("빈 칸 없이 입력해주세요.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
            return;
        }

        if (userPassword.length() < 6) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
            dialog = builder.setMessage("비밀번호는 6자 이상 입력.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)//여기다가 이메일이랑 패스워드를 넘겨주면 간단하게 될거다.
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                            dialog = builder.setMessage("회원 등록에 성공했습니다.")
                                    .setPositiveButton("확인", null)
                                    .create();
                            dialog.show();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                            dialog = builder.setMessage("회원 등록에 실패했습니다.")
                                    .setNegativeButton("확인", null)
                                    .create();
                            dialog.show();
                        }

                        // ...
                    }
                });
    }
}
