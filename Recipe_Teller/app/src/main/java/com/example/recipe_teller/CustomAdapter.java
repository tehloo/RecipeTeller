package com.example.recipe_teller;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/*import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;*/

public class CustomAdapter extends PagerAdapter {

  //  private static final String TAG = "MainCookActivity";//태그 생성함
    LayoutInflater inflater;
    ArrayList<String> ImgList = new ArrayList<>();
    ArrayList<String> CookContextList = new ArrayList<>();
    Long page_num;
    String ImgUrl;

    // List<Recipe> data = new ArrayList<>();



    public CustomAdapter(LayoutInflater inflater, ArrayList<String> ImgList, ArrayList<String> CookContextList,
                         Long page_num) {

        // TODO Auto-generated constructor stub

        //전달 받은 LayoutInflater를 멤버변수로 전달
        this.inflater = inflater;
        this.CookContextList = CookContextList;
        this.ImgList = ImgList;
        this.page_num = page_num;
    }


    //PagerAdapter가 가지고 잇는 View의 개수를 리턴
    //보통 보여줘야하는 이미지 배열 데이터의 길이를 리턴

    @Override
    public int getCount() {
        String tmp = page_num.toString();
        return Integer.parseInt(tmp);
    }


    //ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
    //쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
    //첫번째 파라미터 : ViewPager
    //두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)

    @Override

    public Object instantiateItem(ViewGroup container, int position) {

        // TODO Auto-generated method stub

        View view=null;


 //       myTimer = new MyTimer(MAX_Timer * 1000,1000);

        //새로운 View 객체를 Layoutinflater를 이용해서 생성
        //만들어질 View의 설계는 res폴더>>layout폴더>>viewpater_childview.xml 레이아웃 파일 사용

        view= inflater.inflate(R.layout.viewpager_cooking_childview, null);
      //  View view2 = ;

        //만들어진 View안에 있는 ImageView 객체 참조
        //위에서 inflated 되어 만들어진 view로부터 findViewById()를 해야 하는 것에 주의.


        TextView textView = (TextView)view.findViewById(R.id.HowtoCook);


        Button button_start = (Button) view.findViewById(R.id.btnStart);
        Button button_reset = (Button) view.findViewById(R.id.btnReset);




        //ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
        //현재 position에 해당하는 이미지를 setting
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference gsReference = firebaseStorage.getReferenceFromUrl(ImgList.get(position));
        ImageView img= (ImageView)view.findViewById(R.id.img_viewpager_childimage);
        GlideApp
                .with(view.getContext())
                .load(gsReference)
                .into(img);
        //img.setImageResource(R.drawable.step1+position);

        CharSequence charSequence = CookContextList.get(position).toString();
                //"요리방법" + Integer.toString(position+1); // 각 페이지 요리방법 DB 적용
        textView.setText(charSequence);

        //ViewPager에 만들어 낸 View 추가
        container.addView(view);

        //Image가 세팅된 View를 리턴
        return view;

    }
    //화면에 보이지 않은 View는파쾨를 해서 메모리를 관리함.
    //첫번째 파라미터 : ViewPager
    //두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
    //세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        // TODO Auto-generated method stub

        //ViewPager에서 보이지 않는 View는 제거
        //세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
        container.removeView((View) object);
    }

    //instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드

    @Override
    public boolean isViewFromObject(View v, Object obj) {

        // TODO Auto-generated method stub

        return v == obj;

    }




}
