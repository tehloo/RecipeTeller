package com.example.myapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainCookActivity extends AppCompatActivity {

    ViewPager pager; // 뷰 페이저
    View view = null; // 넘길 뷰
    TextView textView;
    private Button button1; // 이전 페이지
    private Button button2; // 다음 페이지
    private Button button3; // 타이머 시작
    private Button button4; // 타이머 종료

    int page_num; // 총 페이지 수
    TextView textView2; // 숫자가 나오는 텍스트뷰
    MyTimer myTimer; // 타이머 객체 생성

    ProgressBar prog = null; // 타이머 바 객체
    int MAX_Timer = 60; // 타이머 할당 시간

    ArrayList<Integer> CookTimeList;
    int Index_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_main);
        int position; // 현재 보여지는 아이템의 위치를 리턴
        pager= (ViewPager)findViewById(R.id.pager);

        button1 = (Button)findViewById(R.id.btn_previous); // 이전 버튼
        button2 = (Button)findViewById(R.id.btn_next); // 다음 버튼
        button3 = (Button)findViewById(R.id.btnStart); // 타이머 시작 버튼
        button4 = (Button)findViewById(R.id.btnReset); // 타이머 리셋 버튼

        textView = (TextView)findViewById(R.id.pageCount) ; // 페이지 세기

        prog = (ProgressBar) findViewById(R.id.TimerProgressBar); // 타이머 바
        textView2 = (TextView) findViewById(R.id.textView2); // 타이머 남은 시간 텍스트

        recipeDataInit();
        MAX_Timer = CookTimeList.get(0);
        initProg();// 타이머 바 초기화

        if (MAX_Timer == 0)
        {
            button3.setVisibility(View.INVISIBLE);
            button4.setVisibility(View.INVISIBLE);
            prog.setVisibility(View.INVISIBLE);
            textView2.setVisibility(View.INVISIBLE);
        }
        else{
            button3.setVisibility(View.VISIBLE);
            button4.setVisibility(View.VISIBLE);
            prog.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
        }


        myTimer = new MyTimer(MAX_Timer * 1000, 1000); // 타이머 주기 설정. - 이게 타이머 시간임.

        // 처음 시작시 총 몇페이지의 1페이지인지 반환
        position=pager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴
        textView.setText(String.valueOf(position+1)+"/9 페이지"); // "/" + %d + "페이지"로 나중에 고쳐야함.

        //ViewPager에 설정할 Adapter 객체 생성
        //ListView에서 사용하는 Adapter와 같은 역할.
        //다만. ViewPager로 스크롤 될 수 있도록 되어 있다는 것이 다름
        //PagerAdapter를 상속받은 CustomAdapter 객체 생성
        //CustomAdapter에게 LayoutInflater 객체 전달

        CustomAdapter adapter= new CustomAdapter(getLayoutInflater());

        //ViewPager에 Adapter 설정
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override  // 스크롤 효과가 나는 동안 계속해서 호출되는 부분.
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("ITPANGPANG","onPageScrolled : "+position);

            }

            @Override   // 현재 선택된 페이지
            public void onPageSelected(int position) {
                Log.d("ITPANGPANG","onPageSelected : "+position);


                // 스크롤로 인한 페이지 버튼 설정
                if(position + 1 == 1) // 처음에는 이전 버튼 안보이게
                    button1.setVisibility(View.INVISIBLE);
                if(position + 1 != 9) // 다음 버튼 보이게
                    button2.setVisibility(View.VISIBLE);
                if(position + 1 == 9) // 마지막에는 다음 버튼 안보이게
                    button2.setVisibility(View.INVISIBLE);
                if(position + 1 != 1){ // 이전 버튼 보이게
                    button1.setVisibility(View.VISIBLE);
                }
                textView.setText(String.valueOf(position + 1)+"/9 페이지");

                // 해당 페이지의 Time 설정
                MAX_Timer = CookTimeList.get(position);

                // 화면이 전환되었으니 기존에 진행되던 타이머가 있으면 멈춤.
                myTimer.cancel();
                initProg();
                button3.setEnabled(true);



                if (MAX_Timer == 0)
                {
                    button3.setVisibility(View.INVISIBLE);
                    button4.setVisibility(View.INVISIBLE);
                    prog.setVisibility(View.INVISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                }
                else{
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    prog.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                }

            }

            @Override // 페이지 상태
            public void onPageScrollStateChanged(int state) {
                Log.d("ITPANGPANG","onPageScrollStateChanged : "+state);

            }
        });

    }

    private void recipeDataInit() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Recipe").document("CuziRaksRamen"); // 임시 경로, 데이터베이스 내용 구축 후 변경될 예정
       // FirebaseStorage storage = FirebaseStorage.getInstance();

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                CookTimeList = (ArrayList<Integer>)documentSnapshot.get("COOK_TIME");
                Index_num = (int) documentSnapshot.get("RECIPE_INDEX_NUM");
            }
        });
    }

    //onClick속성이 지정된 View를 클릭했을때 자동으로 호출되는 메소드
    public void mOnClick(View v){

        int position;

        switch( v.getId() ){
            case R.id.btn_previous://이전버튼 클릭
                position=pager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴
                //현재 위치(position)에서 -1 을 해서 이전 position으로 변경
                //이전 Item으로 현재의 아이템 변경 설정(가장 처음이면 더이상 이동하지 않음)
                //첫번째 파라미터: 설정할 현재 위치
                //두번째 파라미터: 변경할 때 부드럽게 이동하는가? false면 팍팍 바뀜
                pager.setCurrentItem(position-1,true);

                MAX_Timer = CookTimeList.get(position - 1);
                if (MAX_Timer == 0)
                {
                    button3.setVisibility(View.INVISIBLE);
                    button4.setVisibility(View.INVISIBLE);
                    prog.setVisibility(View.INVISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                }
                else{
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    prog.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                }
                //charSequence = Integer.toString(position) + "/9 페이지";
                if(position-1 == 0) // 버튼 안보이게
                    button1.setVisibility(View.INVISIBLE);
                if(position-1 != 9)
                    button2.setVisibility(View.VISIBLE);
                textView.setText(String.valueOf(position)+"/9 페이지");

                button3.setEnabled(true);
                break;
            case R.id.btn_next://다음버튼 클릭
                position=pager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴

                //현재 위치(position)에서 +1 을 해서 다음 position으로 변경
                //다음 Item으로 현재의 아이템 변경 설정(가장 마지막이면 더이상 이동하지 않음)
                //첫번째 파라미터: 설정할 현재 위치
                //두번째 파라미터: 변경할 때 부드럽게 이동하는가? false면 팍팍 바뀜

                pager.setCurrentItem(position+1,true);

                MAX_Timer = CookTimeList.get(position + 1);
                if (MAX_Timer == 0)
                {
                    button3.setVisibility(View.INVISIBLE);
                    button4.setVisibility(View.INVISIBLE);
                    prog.setVisibility(View.INVISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                }
                else{
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    prog.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                }

                if(position+2 == 9) // 버튼 안보이게
                    button2.setVisibility(View.INVISIBLE);
                if(position+2 != 9){ // 버튼 보이게
                    button1.setVisibility(View.VISIBLE);
                }
                /// charSequence = Integer.toString(position) + "/9 페이지";
                textView.setText(String.valueOf(position+2)+"/9 페이지");
                button3.setEnabled(true);
                break;
            case R.id.btnStart: // 타이머 시작 버튼
                myTimer.start();//
                textView2.setText(String.valueOf(MAX_Timer - 1) + " 초");
                button3.setEnabled(false); // 중복 타이머를 막기 위한 비활성화
                break;
            case R.id.btnReset : // 타이머 리셋 버튼
                myTimer.cancel();
                textView2.setText(String.valueOf(MAX_Timer) + " 초"); // 이거 DB 연동 해야함.
                initProg();
                button3.setEnabled(true);
                break;

        }

    }

    class MyTimer extends CountDownTimer
    {
        public MyTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            textView2.setText(millisUntilFinished/1000 + " 초");
            decreaseBar();
        }

        @Override
        public void onFinish() {
            textView2.setText("0 초");
        }
    }
    //프로그래스bar를 초기화하는 함수
    public void initProg(){
        prog.setMax(MAX_Timer);//최대값 10 지정
        prog.setProgress(MAX_Timer); //현재값 10 지정
    }


    public void decreaseBar() { // 타이머 바 줄어드는 함수
        runOnUiThread( //progressBar는 ui에 해당하므로 runOnUiThread로 컨트롤해야한다
                new Runnable() { //thread구동과 마찬가지로 Runnable을 써주고

                    @Override
                    public void run() { //run을 해준다. 그러나 일반 thread처럼 .start()를 해줄 필요는 없다
                        // TODO Auto-generated method stub
                        int currprog = prog.getProgress();

                        if (currprog > 0) {
                            currprog = currprog - 1;
                        } else if (currprog == 0) { // 타이머가 끝났을 때
                            // 여기에 알람 추가해야함.
                            currprog = MAX_Timer;
                        }
                        prog.setProgress(currprog);
                    }
                }
        );
    }

}
