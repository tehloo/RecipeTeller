package com.example.recipe_teller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * A simple {@link Fragment} subclass.
 */
public class MypageFragment extends Fragment {

    public MypageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mypage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid="";
            UserInfo profile = user.getProviderData().get(0);
            uid  = profile.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("user").document(uid);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String userName  = (String) documentSnapshot.get("userName");
                    String userAge  = (String) documentSnapshot.get("userAge");
                    String userGender  = (String) documentSnapshot.get("userGender");

                    TextView nameTextView = (TextView)getView().findViewById(R.id.userName);
                    TextView ageTextView = (TextView)getView().findViewById(R.id.userAge);
                    TextView genderTextView = (TextView)getView().findViewById(R.id.userGender);

                    nameTextView.setText(userName);
                    ageTextView.setText(userAge);
                    genderTextView.setText(userGender);
                }
            });
        }
    }
}
