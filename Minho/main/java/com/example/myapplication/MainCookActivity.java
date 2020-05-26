package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class MainCookActivity extends AppCompatActivity {

    ViewPager pager;
    View view = null;
    TextView textView;
    private Button button1;
    private Button button2;
    int page_num; // 총 페이지 수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_main);
        int position; // 현재 보여지는 아이템의 위치를 리턴
        pager= (ViewPager)findViewById(R.id.pager);

        button1 = (Button)findViewById(R.id.btn_previous);
        button2 = (Button)findViewById(R.id.btn_next);
        textView = (TextView)findViewById(R.id.pageCount) ;

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

                //charSequence = Integer.toString(position) + "/9 페이지";
                if(position-1 == 0) // 버튼 안보이게
                    button1.setVisibility(View.INVISIBLE);
                if(position-1 != 9)
                    button2.setVisibility(View.VISIBLE);
                textView.setText(String.valueOf(position)+"/9 페이지");
                break;
            case R.id.btn_next://다음버튼 클릭
                position=pager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴


                //현재 위치(position)에서 +1 을 해서 다음 position으로 변경
                //다음 Item으로 현재의 아이템 변경 설정(가장 마지막이면 더이상 이동하지 않음)
                //첫번째 파라미터: 설정할 현재 위치
                //두번째 파라미터: 변경할 때 부드럽게 이동하는가? false면 팍팍 바뀜

                pager.setCurrentItem(position+1,true);
                if(position+2 == 9) // 버튼 안보이게
                    button2.setVisibility(View.INVISIBLE);
                if(position+2 != 9){ // 버튼 보이게
                    button1.setVisibility(View.VISIBLE);
                }
                /// charSequence = Integer.toString(position) + "/9 페이지";
                textView.setText(String.valueOf(position+2)+"/9 페이지");
                break;
        }

    }
}
