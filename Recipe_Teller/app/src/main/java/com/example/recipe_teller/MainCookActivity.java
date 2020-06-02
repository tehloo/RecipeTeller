package com.example.recipe_teller;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.lge.aip.engine.base.IEngineListener;
import com.lge.aip.engine.servertts.ISTTSListener;
import com.lge.aip.engine.servertts.STTSEngine;
import com.lge.aip.engine.servertts.STTSInput;
import com.lge.aip.engine.servertts.STTSResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.lge.aip.engine.base.AIEngineReturn.LGAI_STTS_SUCCESS;
import static com.lge.aip.engine.servertts.STTSEngine.TTS_LANGUAGE_ENGLISH;
import static com.lge.aip.engine.servertts.STTSEngine.TTS_LANGUAGE_KOREAN;


public class MainCookActivity extends AppCompatActivity {

    String documentName;

    ViewPager pager; // 뷰 페이저
    View view = null; // 넘길 뷰
    TextView textView;
    private Button button1; // 이전 페이지
    private Button button2; // 다음 페이지
    private Button button3; // 타이머 시작
    private Button button4; // 타이머 종료

    private FloatingActionButton mTtsStart; // TTS 시작
    Long page_num; // 총 페이지 수
    TextView textView2; // 숫자가 나오는 텍스트뷰
    MyTimer myTimer; // 타이머 객체 생성

    ProgressBar prog = null; // 타이머 바 객체
    int MAX_Timer = 60; // 타이머 할당 시간

    ArrayList<Long> CookTimeList = new ArrayList<>();
    ArrayList<String> ImgList = new ArrayList<>();
    ArrayList<String> CookContextList = new ArrayList<>();


    //TTS
    private static final String TAG = MainCookActivity.class.getSimpleName();

    private static final int MSG_RUN_TTS = 0;
    private static final int STTS_PERMISSION_REQUEST_LOAD_SAMPLES = 0;
    private static final Map<String, Integer> sMapSentence = createSentenceMap();
    private static final String pcmFileName = "result.pcm";

    private File mSampleFile;
    private STTSEngine mTts;
    private FileOutputStream mFos;

    // private FileRecyclerViewAdapter mFileAdapter;


    private HandlerThread mHandlerThread;
    private Handler mHandler;

    /**
     * Use this to check whether the settings have been changed in Settings.
     */
    private static final int REQUEST_CODE_SETTINGS = 5000;


    /**
     * Any changes to UI and config loaded from the json file are immediately written here.
     */
    private TtsConfig mConfig;

    private boolean mSentenceUpdated;

    private int mRequestedLanguage;
    private String mServerVersion;

    private static Map<String, Integer> createSentenceMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put(TTS_LANGUAGE_KOREAN, R.string.sentence_korean);
        map.put(TTS_LANGUAGE_ENGLISH, R.string.sentence_english);
        return map;
    }

    private void initEngine() {
        //tts 텍스트 전달 매개체 설정
        mTts = new STTSEngine();
        if (mTts.create() != LGAI_STTS_SUCCESS) {
            showErrorDialog(getString(R.string.msg_error_engine));
        }

        // 리스너 설정
        int result = mTts.setListener(sttsListener);
        if (result != LGAI_STTS_SUCCESS) {
            showErrorDialog(getString(R.string.msg_error_listener) + "error : " + result);
        }

    }

    private void showErrorDialog(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }

    /**
     * The result of requesting tts operation in process is received through listener.
     *
     */
    IEngineListener sttsListener = new ISTTSListener() {
        @Override
        public void onBufferReceived(STTSResult buffer) {
            int code = buffer.getResultCode();
            if (code == STTSResult.REQUEST_PROCESSED) {
                if (mServerVersion == null) {
                    mServerVersion = buffer.getMessage();
                    // updateMessage("Server ver. " + mServerVersion + "\n");
                }
                try {
                    Log.v(TAG, "onBufferReceived " + buffer.getLength());
                    mFos.write(buffer.getBuffer(), 0, buffer.getLength());
                    //updateMessage(".");
                } catch (IOException e) {
                    Log.w(TAG, "Exception onBufferReceived", e);
                }
            } else if (code == STTSResult.ERR_INVALID_PARAM) {
                //updateMessage(getString(R.string.msg_error_config));
            } else if (code == STTSResult.ERR_TEXT_LENGTH_EXCEED) {
                //updateMessage(getString(R.string.msg_error_sentence));
            } else {
                //updateMessage(String.format(Locale.getDefault(), getString(R.string.msg_error_etc),
                //      buffer.getResultCode(), buffer.getMessage()));
            }

            if (code != STTSResult.REQUEST_PROCESSED) {
                Log.w(TAG, "Error for request. code = " + buffer.getResultCode());
                resetUi();
            }
        }

        @Override
        public void onStateChanged(int state) {
            switch(state) {
                case AI_STTS_CB_INIT:
                    break;
                /**
                 * When you started delivering the requested sentence to the server
                 */
                case AI_STTS_CB_PROCESS:
                    // updateMessage(getString(R.string.msg_tts_working) + "\n");
                    fileReady();
                    try {
                        mFos = new FileOutputStream(mSampleFile);
                        Log.v(TAG, "File open as " + mSampleFile.getName());
                    } catch (FileNotFoundException e) {
                        Log.w(TAG, "Exception on create file", e);
                    }
                    break;

                /**
                 * When the TTS conversion operation ends
                 */
                case AI_STTS_CB_DONE:
                    if (mFos != null) {
                        try {
                            mFos.close();
                            mFos = null;
                            Log.v(TAG, "File closed as " + mSampleFile.getName());
                        } catch (IOException e) {
                            Log.w(TAG, "Exception on closing file", e);
                        }
                        //         updateMessage("\n" + getString(R.string.msg_download_done));
                        if (mSampleFile != null) {
                            //           updateMessage(" (" + mSampleFile.length() + " byte)\n");
                        }
                        //             updateMessage(getString(R.string.msg_start_playing_pcm) + "...");
                        playAudioFileViaAudioTrack(mSampleFile, mRequestedLanguage);
                        mRequestedLanguage = 0;
                        //           updateMessage(getString(R.string.msg_done_for_playing) + "\n");
                    }
                    resetUi();
                    break;

                case AI_STTS_CB_ERROR:

                    Log.e(TAG,"AI_STTS_CB_ERROR");
                    return;

                default:
                    Log.w(TAG, "unknown state : " + state);
                    return;
            }

        }
    };


    private void resetUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTtsStart.setEnabled(true);
            }
        });
        mServerVersion = null;
    }

    private void fileReady() {
        if (mSampleFile != null && mSampleFile.exists()) {
            if (!mSampleFile.delete()) {
                Log.w(TAG, "Could not delete previous simple file");
            }
        }
        try {
            File pcmPath = getExternalFilesDir(null).getAbsoluteFile();
            if (pcmPath != null) {
                mSampleFile = new File(pcmPath, pcmFileName);

                if (!mSampleFile.exists()) {
                    if (!mSampleFile.createNewFile()) {
                        Log.w(TAG, "Error : createNewFile()");
                    }
                }
                //           updateMessage("[pcm output]" + mSampleFile.getAbsolutePath() + "\n");
            } else {
                Log.w(TAG, "Error : External storage is not available.");
            }
        } catch (IOException e) {
            Log.w(TAG, "Could not create a pcm file", e);
        }
    }

    static final int SAMPLE_RATE_DEFAULT = 22050;
    static final int MAX_BUFFER_SIZE = 512 * 1024;

    private void playAudioFileViaAudioTrack(File file, int language) {
        if (file == null)
            return;

        int sampleRate = SAMPLE_RATE_DEFAULT;

        int intSize = android.media.AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                intSize, AudioTrack.MODE_STREAM);

        byte[] byteData = null;
        byteData = new byte[MAX_BUFFER_SIZE];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (in == null) {
            Log.w(TAG, "Could not read the file");
            return;
        }

        int bytesread = 0, ret = 0;
        at.play();
        try {
            while (bytesread < file.length()) {
                ret = in.read(byteData, 0, MAX_BUFFER_SIZE);
                if (ret != -1) { // Write the byte array to the track
                    at.write(byteData, 0, ret);
                    bytesread += ret;
                } else
                    break;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        at.stop();
        at.release();
    }

    // 메인
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cook);

        //Log.i(TAG, "Sample Version: " + BuildConfig.VERSION_NAME);
        Intent intent = getIntent();
        documentName = intent.getExtras().getString("recipeName");


        int position; // 현재 보여지는 아이템의 위치를 리턴
        pager= (ViewPager)findViewById(R.id.pager);

        button1 = (Button)findViewById(R.id.btn_previous); // 이전 버튼
        button2 = (Button)findViewById(R.id.btn_next); // 다음 버튼
        button3 = (Button)findViewById(R.id.btnStart); // 타이머 시작 버튼
        button4 = (Button)findViewById(R.id.btnReset); // 타이머 리셋 버튼

        mTtsStart = (FloatingActionButton) findViewById(R.id.TTSButton); // TTS 버튼
        mSentenceUpdated = false;


        textView = (TextView)findViewById(R.id.pageCount) ; // 페이지 세기

        prog = (ProgressBar) findViewById(R.id.TimerProgressBar); // 타이머 바
        textView2 = (TextView) findViewById(R.id.textView2); // 타이머 남은 시간 텍스트

        recipeDataInit(); // DB에서 데이터 가져오기

        // 데이터를 받아오는 동안 로딩 시간을 강제로 줌.
        try {
            Thread.sleep(1500); // 1.5초
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        initEngine(); // TTS 엔진 설정
        ConfigLoader configLoader = new ConfigLoader(this);
        mConfig = configLoader.loadConfig(TtsConfig.class);

        // 처음 시작시 총 몇페이지의 1페이지인지 반환
        position=pager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴

        //ViewPager에 설정할 Adapter 객체 생성
        //ListView에서 사용하는 Adapter와 같은 역할.
        //다만. ViewPager로 스크롤 될 수 있도록 되어 있다는 것이 다름
        //PagerAdapter를 상속받은 CustomAdapter 객체 생성
        //CustomAdapter에게 LayoutInflater 객체 전달




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

                textView.setText(String.valueOf(position + 1)+"/"+page_num+" 페이지");

                // 화면이 전환되었으니 기존에 진행되던 타이머가 있으면 멈춤.
                myTimer.cancel();

                // 해당 페이지의 Time 설정
                MAX_Timer = CookTimeList.get(position).intValue();

                textView2.setText(String.valueOf(MAX_Timer) + " 초");
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
                    myTimer = new MyTimer(MAX_Timer * 1000, 1000); // 타이머 주기 설정. - 이게 타이머 시간임.
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

    public void recipeDataInit() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("RecipeData").document(documentName); // 임시 경로, 데이터베이스 내용 구축 후 변경될 예정
        // FirebaseStorage storage = FirebaseStorage.getInstance();

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("DBInit", "DocumentSnapshot data: " + documentSnapshot.getData());

                CookTimeList = (ArrayList<Long>)documentSnapshot.get("COOK_TIME");
                ImgList = (ArrayList<String>)documentSnapshot.get("COOK_IMG");
                CookContextList = (ArrayList<String>)documentSnapshot.get("COOK_CONTEXT");
                page_num = Long.valueOf(CookTimeList.size());
                MAX_Timer = CookTimeList.get(0).intValue();
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
                textView.setText("1"+"/"+page_num+" 페이지");

                //TODO:여기에 다 넘겨줘야함
                CustomAdapter adapter= new CustomAdapter(getLayoutInflater(), ImgList, CookContextList, page_num);
                //ViewPager에 Adapter 설정
                pager.setAdapter(adapter);
            }
/*
            @Override
            public void onSuccess(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {  // DB 받는 것에 성공하면

                    } else {
                        Log.d("DBInit", "No such document");
                    }
                } else {
                    Log.d("DBInit", "get failed with ", task.getException());
                }
            }*/
        });
/*
       //이미지 저장
       FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

       final List<byte[]> imgList2 = null;
       for (int i = 0 ; i < page_num ; i++)
       {
           String ImgUrl = ImgList.get(i);
           StorageReference gsReference = firebaseStorage.getReferenceFromUrl(ImgUrl);
           Log.e("error:", gsReference.toString());
           final long ONE_MEGABYTE = 1024 * 1024;
           gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
               @Override
               public void onSuccess(byte[] bytes) {
                   // Data for "images/island.jpg" is returns, use this as needed
                   imgList2.add(bytes);
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception exception) {
                   // Handle any errors
               }
           });

       }*/

       /*
        GlideApp
                .with(view)
                .load(gsReference)
                .into(img);
*/
       /* docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                CookTimeList = (ArrayList<Integer>)documentSnapshot.get("COOK_TIME");
                Index_num = (Integer) documentSnapshot.get("RECIPE_INDEX_NUM");
            }
        });*/
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

                MAX_Timer = CookTimeList.get(position - 1).intValue();
                if (MAX_Timer == 0)
                {
                    button3.setVisibility(View.INVISIBLE);
                    button4.setVisibility(View.INVISIBLE);
                    prog.setVisibility(View.INVISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                }
                else{
                    myTimer = new MyTimer(MAX_Timer * 1000, 1000); // 타이머 주기 설정. - 이게 타이머 시간임.
                    textView2.setText(String.valueOf(MAX_Timer) + " 초");
                    initProg();
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    prog.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                }
                //charSequence = Integer.toString(position) + "/9 페이지";
                if(position-1 == 0) // 버튼 안보이게
                    button1.setVisibility(View.INVISIBLE);
                if(position-1 != 0)
                    button2.setVisibility(View.VISIBLE);
                textView.setText(String.valueOf(position)+"/"+page_num+" 페이지");

                button3.setEnabled(true);
                break;
            case R.id.btn_next://다음버튼 클릭
                position=pager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴

                //현재 위치(position)에서 +1 을 해서 다음 position으로 변경
                //다음 Item으로 현재의 아이템 변경 설정(가장 마지막이면 더이상 이동하지 않음)
                //첫번째 파라미터: 설정할 현재 위치
                //두번째 파라미터: 변경할 때 부드럽게 이동하는가? false면 팍팍 바뀜

                pager.setCurrentItem(position+1,true);

                MAX_Timer = CookTimeList.get(position + 1).intValue();
                if (MAX_Timer == 0)
                {
                    button3.setVisibility(View.INVISIBLE);
                    button4.setVisibility(View.INVISIBLE);
                    prog.setVisibility(View.INVISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                }
                else{
                    myTimer = new MyTimer(MAX_Timer * 1000, 1000); // 타이머 주기 설정. - 이게 타이머 시간임.
                    textView2.setText(String.valueOf(MAX_Timer) + " 초");
                    initProg();
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    prog.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                }

                if(position+2 == page_num) // 버튼 안보이게
                    button2.setVisibility(View.INVISIBLE);
                if(position+2 != page_num){ // 버튼 보이게
                    button1.setVisibility(View.VISIBLE);
                }
                /// charSequence = Integer.toString(position) + "/9 페이지";
                textView.setText(String.valueOf(position+2)+"/"+page_num+" 페이지");
                button3.setEnabled(true);
                break;
            case R.id.btnStart: // 타이머 시작 버튼
                myTimer.start();//
                //textView2.setText(String.valueOf(MAX_Timer - 1) + " 초");
                button3.setEnabled(false); // 중복 타이머를 막기 위한 비활성화
                break;
            case R.id.btnReset : // 타이머 리셋 버튼
                myTimer.cancel();
                textView2.setText(String.valueOf(MAX_Timer) + " 초"); // 이거 DB 연동 해야함.
                initProg();
                button3.setEnabled(true);
                break;
            case R.id.TTSButton:
                startTTS();
                break;

        }

    }


    // TTS

    private void startTTS() {
        if (mTts == null) {
            showErrorDialog(getString(R.string.msg_error_engine));
            return;
        }

        mHandlerThread = new HandlerThread("buffer IO");
        mHandlerThread.start();
        mHandler = new BufferHandler(mHandlerThread.getLooper());

        mHandler.sendMessage(mHandler.obtainMessage(MSG_RUN_TTS)); // 여기서 에러남. 메시지 전송하는 게 안됨... ##
        /*mTvResult.setText(String.format(Locale.getDefault(),
                getString(R.string.msg_request_sentence_with_length),
                mEtSentence.getText().length()) + "\n");*/
        mTtsStart.setEnabled(false);
    }


    private class BufferHandler extends Handler {

        public BufferHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_RUN_TTS:
                    /**
                     * Deliver configuration values to engine before execution.
                     */
                    String jsonConfig = new Gson().toJson(mConfig, TtsConfig.class);
                    Log.d(TAG, "jsonConfig: " + jsonConfig);
                    mTts.configure(jsonConfig); // READY로 바뀜 // 여기서 에러. jsonConfig = null;???

                    // 2019.01.22  State An explicit call to start/stop is required for state managing.
                    mTts.start(); // RUNNING으로 바뀜

                    /**
                     * Pass the input statement through process().
                     */
                    String sentence = CookContextList.get(pager.getCurrentItem()); // TTS 출력 부분
                    try {
                        mTts.process(new STTSInput(sentence), null);
                    } catch (IllegalStateException e) {
                        // TTS engine will throw IllegalStateException, if the right config is not set.
                        Log.w(TAG, "MSG_RUN_TTS: " + e.getMessage());
                        showErrorDialog(getString(R.string.msg_error_run));
                    }

                    // 2019.01.22 State An explicit call to start/stop is required for state managing.
                    mTts.stop();
                    break;

                default:
                    break;
            }
        }
    }

    // 타이머 함수

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
