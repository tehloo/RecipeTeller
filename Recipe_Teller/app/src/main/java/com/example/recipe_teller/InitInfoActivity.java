package com.example.recipe_teller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class InitInfoActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";//태그 생성함
    private FirebaseAuth mAuth;//파이어베이스 인증기능

    private String userName;
    private String userAge;
    private String userGender;
    private AlertDialog dialog;// 선언부 끝

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_info);

        mAuth = FirebaseAuth.getInstance();

        //등록하기 버튼 기능 구현
        Button registerButton = (Button) findViewById((R.id.registerButton));
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileUpdate();
            }
        });//버튼을 클릭하면 레지스터 실행된다.

    }//온크리에이트 끝


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

    public void onBackPressed(){
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private void profileUpdate(){//회원가입 로직을구현

        final EditText nameText = (EditText) findViewById(R.id.nameText);
        final EditText ageText = (EditText) findViewById(R.id.ageText);

        String userName= nameText.getText().toString();
        String userAge= ageText.getText().toString();

        //성별 라디오 버튼
        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.genderGroup);
        int genderGroupID = genderGroup.getCheckedRadioButtonId();
        userGender = ((RadioButton)findViewById(genderGroupID)).getText().toString();

        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int i) {
                RadioButton genderButton = (RadioButton) findViewById(i);
                userGender = genderButton.getText().toString();
            }
        });//젠더는 라디오 버튼으로 입력을 받는다.
        //입력값 받아오기

        if(userName.length()>0 && userAge.length()>0 && userGender.length()>0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            // Access a Cloud Firestore instance from your Activity
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            UserInfo info= new UserInfo(userName, userAge, userGender);

            if(user != null){
                db.collection("user").document(user.getUid())
                        .set(info)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                AlertDialog.Builder builder = new AlertDialog.Builder(InitInfoActivity.this);
                                dialog = builder.setMessage("회원 정보 등록 성공.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                                AlertDialog.Builder builder = new AlertDialog.Builder(InitInfoActivity.this);
                                dialog = builder.setMessage("회원 정보 등록 실패.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();
                            }
                        });
            }

        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(InitInfoActivity.this);
            dialog = builder.setMessage("회원 정보를 입력해주세요.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
        }
    }
}
