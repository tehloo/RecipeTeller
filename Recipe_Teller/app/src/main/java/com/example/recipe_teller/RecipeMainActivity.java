package com.example.recipe_teller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RecipeMainActivity extends AppCompatActivity {

    String documentName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_main);
        Intent intent = getIntent();
        documentName = intent.getExtras().getString("recipeName");
        recipeDataInit();
        buttonInit();
    }

    private void buttonInit() {
        Button backButton = (Button)findViewById(R.id.backButton);
        Button startCookButton = (Button)findViewById(R.id.startCookButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        startCookButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainCookActivity.class);
                intent.putExtra("recipeName", documentName);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void recipeDataInit() {
        Log.e("recipeDataInit",documentName);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("RecipeData").document(documentName); // 임시 경로, 데이터베이스 내용 구축 후 변경될 예정
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String myTime  = (String) documentSnapshot.get("COOK_TOTAL_TIME");
                String myName  = (String) documentSnapshot.get("RECIPE_TITLE");
                String myWriter  = (String) documentSnapshot.get("RECIPE_WRITER");
                String myLevel = "";
                //Long level  = (Long) documentSnapshot.get("LEVEL");
                String level = (String)documentSnapshot.get("LEVEL");
                String imgURL = (String)documentSnapshot.get("RECIPE_IMG");
                if(level.equals("1")){
                    myLevel = "하";
                }
                else if(level.equals("2")){
                    myLevel = "중";
                }
                else if(level.equals("3")){
                    myLevel = "상";
                }

                String nesIngred = "";
                ArrayList<String> nesIngredList = (ArrayList<String>)documentSnapshot.get("NES_INGREDIENT");
                if(nesIngredList!=null) {
                    for (String ingred : nesIngredList) {
                        nesIngred = nesIngred + " " + ingred + ",";
                    }
                    if(nesIngred.length()>0)
                        nesIngred = nesIngred.substring(0, nesIngred.length()-1);
                }

                String chsIngred = "";
                ArrayList<String> chsIngredList = (ArrayList<String>)documentSnapshot.get("CHS_INGREDIENT");
                if(chsIngredList!=null) {
                    for (String ingred : chsIngredList) {
                        chsIngred = chsIngred + " " + ingred + ",";
                    }
                    if(chsIngred.length()>0)
                        chsIngred = chsIngred.substring(0, chsIngred.length()-1);
                 }

                String sauceIngred = "";
                ArrayList<String> sauceIngredList = (ArrayList<String>)documentSnapshot.get("SAUCE_INGREDIENT");

                if(sauceIngredList!=null) {
                    for (String ingred : sauceIngredList) {
                        sauceIngred = sauceIngred + " " + ingred + ",";
                    }
                    if(sauceIngred.length()>0)
                        sauceIngred = sauceIngred.substring(0, sauceIngred.length() - 1);
                }

                TextView rLevel = (TextView)findViewById(R.id.rLevelTextView);
                TextView rTime = (TextView)findViewById(R.id.rTimeTextView);
                TextView rName = (TextView)findViewById(R.id.rNameTextView);
                Button rWriter = (Button)findViewById(R.id.writerBtn);
                TextView rNesIngred = (TextView)findViewById(R.id.nesIngredTextView);
                TextView rChsIngred = (TextView)findViewById(R.id.chsIngredTextView);
                TextView rSauceIngred = (TextView)findViewById(R.id.sauceIngredTextView);

                rLevel.setText(myLevel);
                rTime.setText(myTime);
                rName.setText(myName);
                rWriter.setText(myWriter);
                rNesIngred.setText(nesIngred);
                rChsIngred.setText(chsIngred);
                rSauceIngred.setText(sauceIngred);
                //이미지 넣기
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference gsReference = firebaseStorage.getReferenceFromUrl(imgURL);
                Log.e("error:", gsReference.toString());
                ImageView imageView = (ImageView) findViewById(R.id.recipeImageView);
                GlideApp
                        .with(getApplicationContext())
                        .load(gsReference)
                        .into(imageView);
            }
        });
    }
}
