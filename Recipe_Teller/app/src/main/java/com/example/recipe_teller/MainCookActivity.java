package com.example.recipe_teller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.recipe_teller.configASR.AsrConfig;
import com.example.recipe_teller.configASR.SpeechConfig;
import com.example.recipe_teller.configHTWD.HybridTwdConfig;
import com.example.recipe_teller.modelHTWD.TwdModelLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.lge.aip.engine.base.IEngineListener;
import com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI;
import com.lge.aip.engine.hybridtwd.BuildConfig;
import com.lge.aip.engine.servertts.ISTTSListener;
import com.lge.aip.engine.servertts.STTSEngine;
import com.lge.aip.engine.servertts.STTSInput;
import com.lge.aip.engine.servertts.STTSResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.lge.aip.engine.base.AIEngineReturn.LGAI_ASR_SUCCESS;
import static com.lge.aip.engine.base.AIEngineReturn.LGAI_STTS_SUCCESS;
import static com.lge.aip.engine.servertts.STTSEngine.TTS_LANGUAGE_ENGLISH;
import static com.lge.aip.engine.servertts.STTSEngine.TTS_LANGUAGE_KOREAN;
import static com.lge.aip.engine.speech.util.MyDevice.isNetworkConnection;


public class MainCookActivity extends AppCompatActivity implements AsrManager.UpdateResultListener, TriggerWordDetectionManager.UpdateResultListener{

    String documentName;
    Boolean timerFlag;

    ViewPager pager; // 뷰 페이저
    View view = null; // 넘길 뷰
    TextView textView;
    private Button button1; // 이전 페이지
    private Button button2; // 다음 페이지
    private Button button3; // 타이머 시작
    private Button button4; // 타이머 종료

    //for HTWD
    private TriggerWordDetectionManager mHTWDEngineManager;
    private static final int REQUEST_CODE_HTWD = 42;
    private TwdModelLoader mModelLoader;
    private HybridTwdConfig mConfigHTWD;
    private boolean mInitialized = false;
    final static int KEYWORD_HILG=1;
    final static int HTWD_SENSITIVITY=10;

    //for ASR
    private ToggleButton mButtonStartOnOff;
    private AsrManager mASREngineManager;

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
    private TtsConfig ttsConfig;
    private SpeechConfig speechConfig;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS=1234;
    private boolean mSentenceUpdated;

    private int mRequestedLanguage;
    private String mServerVersion;

    private static Map<String, Integer> createSentenceMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put(TTS_LANGUAGE_KOREAN, R.string.sentence_korean);
        map.put(TTS_LANGUAGE_ENGLISH, R.string.sentence_english);
        return map;
    }
    private void initEngineHTWD() {
        // Set full path of model file needed for start-up operation
        //mEditPath.setText(TEST_COPY_BASE_PATH + ASSET_NAME_PCM);

        //mButtonStartOnOff.setEnabled(true);

        loadConfigHTWD();

        // Creating a startup engine
        mHTWDEngineManager = new TriggerWordDetectionManager(this);
        mHTWDEngineManager.create(this);
    }

    private void initEngineTTS() {
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

        timerFlag = false;

        //for audio permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }


        /*
        //for ASR and HTWD test
        mButtonStartOnOff = (ToggleButton) findViewById(R.id.mButtonStartOnOff);
        mButtonStartOnOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                View currentFocus = getCurrentFocus();
                if (currentFocus != null)
                    currentFocus.clearFocus();
                if (mHTWDEngineManager == null) {
                    Log.w(TAG, "StartButton: EngineManager is not created.");
                    return;
                }
                if (mASREngineManager == null) {
                    Log.d(TAG, "StartButton: EngineManager is not created.");
                    mASREngineManager = new AsrManager(MainCookActivity.this, MainCookActivity.this);
                }
                //HTWD part
                if (mButtonStartOnOff.isChecked()) {
                    startHTWD();
                } else {
                    stopHTWD();
                }
                */
                /*
                //ASR part
                if (mButtonStartOnOff.isChecked()) {
                    startASR();
                } else {
                    stopASR();
                }

            }
        });
         */

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

        ConfigLoaderTTS configLoaderTTS = new ConfigLoaderTTS(this);
        ttsConfig = configLoaderTTS.loadConfig(TtsConfig.class);

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
                if(position + 1 != page_num) // 다음 버튼 보이게
                    button2.setVisibility(View.VISIBLE);
                if(position + 1 == page_num) // 마지막에는 다음 버튼 안보이게
                    button2.setVisibility(View.INVISIBLE);
                if(position + 1 != 1){ // 이전 버튼 보이게
                    button1.setVisibility(View.VISIBLE);
                }

                textView.setText(String.valueOf(position + 1)+"/"+page_num+" 페이지");

                // 화면이 전환되었으니 기존에 진행되던 타이머가 있으면 멈춤.
                myTimer.cancel();
                timerFlag=false;
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

                //TTS
                startTTS();
                //HTWD
                startHTWD();
            }

            @Override // 페이지 상태
            public void onPageScrollStateChanged(int state) {
                Log.d("ITPANGPANG","onPageScrollStateChanged : "+state);

            }
        });
    }
    private void stopHTWD() {
        Log.d(TAG, "StartButton: STOP");
        mHTWDEngineManager.stopListening();
        //setEnabledViewsForStart(true);
        //scLog.append(R.string.stopped_by_user);
    }
    private void startHTWD(){
        Log.d(TAG, "StartButton: START");

        // Sensitivity value check, UI validation check is through the listener of the view.
        String sensitivity = String.valueOf(HTWD_SENSITIVITY); // sensitivity = 10;
        if (TextUtils.isEmpty(sensitivity) || Integer.valueOf(sensitivity) <= 0) {
            Toast.makeText(getApplicationContext(), R.string.sens_not_valid, Toast.LENGTH_LONG).show();
            //mButtonStartOnOff.setChecked(false);
            return;
        }

        // Check file information in file mode
        boolean isFileMode = false; // must false

        int keywordId = KEYWORD_HILG; // HILG = 1
        if (keywordId >= AI_HybridTWDEngineAPI.AI_VA_KEYWORD_UNREGISTERED) {
            // If you use Additional Trigger Word,
            // You should set triggerWord of config to "UNREGISTERED"
            mConfigHTWD.embeddedConfig.triggerWord = "UNREGISTERED";
        }

        boolean isHybridMode = false; // must false
        // Create Config in Json format. In this case, we use Gson to dynamically configure
        // to change the setting by the UI, but it is also possible to read from a fixed
        // file or to use a hard-coded string.
        //Log.e("test", getEncryptionKeyHTWD());
        mConfigHTWD.serverConfig.encryptionKey = getEncryptionKeyHTWD();
        String jsonConfig = new Gson().toJson(mConfigHTWD, HybridTwdConfig.class);
        Log.d(TAG, "onClick: " + jsonConfig);
        mHTWDEngineManager.configure(jsonConfig);

        // Passing enableHybrid to the Manager is for turning off the microphone and handling
        // the UI. The processing of the engine is sufficient to be passed through configure.
        if (mConfigHTWD != null) {
            mHTWDEngineManager.enableHybrid(mConfigHTWD.enableHybrid);
        }

        // The model path can be set via the configure function, but only absolute paths
        // are possible.
        // However, on Android, you cannot get the absolute paths from the asset.
        // So, you can specify it after copying it to the sdcard, or you can read the files
        // and pass the model data to the engine using injectModels function.
        if (mModelLoader == null) {
            showJsonErrorDialog();
            return;
        }
        try {
            mHTWDEngineManager.injectModels(mModelLoader.readAmAsset(), mModelLoader.readNetAsset());
        } catch (IllegalStateException e) {
            showJsonErrorDialog(e.getMessage());
            return;
        }

        // Update on UI
        //scLog.clear();
        //setEnabledViewsForStart(false);

        //mTimeFull = System.currentTimeMillis();

        // Start thread that retrieve audio data.
        //mic mode
        //scLog.append(R.string.common_speak);
        mHTWDEngineManager.startListening(new MicAudioSourceHTWD());
    }

    private void stopASR() {
        Log.e("ASR", "stopbyuser");
        stopListening(getString(R.string.stopped_by_user));
    }

    private void startASR() {
        Log.e("ASR", "StartButton: START");

        if (!isNetworkConnection(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.network_not_available, Toast.LENGTH_SHORT).show();
            //mButtonStartOnOff.setChecked(false);
            return;
        }
                    /*
                    // Check file information in file mode
                    boolean isFileMode = false;
                    if (mRadioGroupInputType.getCheckedRadioButtonId() == R.id.vr_radio_file) {
                        isFileMode = true;
                    }
                    if (isFileMode && TextUtils.isEmpty(mSelectedFilePath)) {
                        Toast.makeText(getApplicationContext(), R.string.file_not_valid, Toast.LENGTH_LONG).show();
                        mButtonStartOnOff.setChecked(false);
                        return;
                    }

                    Log.d(TAG, "StartButton: lang:" + mSpnLanguage.getSelectedItemId()
                            + " workingMode:" + mSpnWorkingMode.getSelectedItemId()
                            + " CompleteMode:" + mSpnCompleteMode.getSelectedItemId()
                            + " isFileMode:" + isFileMode
                            + " path:" + mSelectedFilePath);

                    updateDeviceTime();
                    */
        mASREngineManager.create();

        // Create Config in Json format. In this case, we use Gson to dynamically configure
        // to change the setting by the UI, but it is also possible to read from a fixed
        // file or to use a hard-coded string.
        if (!speechConfig.enableHttp2) {
            speechConfig.asrConfig.encryptionKey = getEncryptionKeyASR();
        }
        String jsonConfigASR = new Gson().toJson(speechConfig, SpeechConfig.class);
        mASREngineManager.configure(jsonConfigASR);
                    /*
                        mScLog.clear();
                    setEnabledViewsForStart(false);
                    */
        Log.e("ASR", "Speak");
        int ret = mASREngineManager.startListening(new MicAudioSourceASR());
        if (LGAI_ASR_SUCCESS != ret) {
            stopListening("Unable to start. error = " + ret);
        }

    }

    private void initEngineASR() {
        //mEditPath.setText(mTestPath + ASSET_NAME_PCM);

        // When using location information
        //mLocationHelper.getLocationInfo();

        loadConfigASR();

        mASREngineManager = new AsrManager(this, this);
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

                CustomAdapter adapter= new CustomAdapter(getLayoutInflater(), ImgList, CookContextList, page_num);
                //ViewPager에 Adapter 설정
                pager.setAdapter(adapter);
            }
        });
    }


    public void goPreviousPage(){
        int position;
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
    }

    public void goNextPage(){
        int position;
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
    }
    //onClick속성이 지정된 View를 클릭했을때 자동으로 호출되는 메소드
    public void mOnClick(View v){
        switch( v.getId() ){
            case R.id.btn_previous://이전버튼 클릭
                goPreviousPage();
                break;
            case R.id.btn_next://다음버튼 클릭
                goNextPage();
                break;
            case R.id.btnStart: // 타이머 시작 버튼
                myTimer.start();//
                timerFlag=true;
                //textView2.setText(String.valueOf(MAX_Timer - 1) + " 초");
                button3.setEnabled(false); // 중복 타이머를 막기 위한 비활성화
                break;
            case R.id.btnReset : // 타이머 리셋 버튼
                myTimer.cancel();
                timerFlag=false;
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

    public void analysisASR(final String str){
        if(str.contains("다음")){
            goNextPage();
        }
        else if(str.contains("이전")){
            goPreviousPage();
        }
        else if(str.contains("다시")){
            startTTS();
        }
        else if(str.contains("타이머")){
            if(timerFlag){
                myTimer.cancel();
                timerFlag=false;
                textView2.setText(String.valueOf(MAX_Timer) + " 초"); // 이거 DB 연동 해야함.
                initProg();
                button3.setEnabled(true);
            }
            else{
                myTimer.start();
                timerFlag=true;
            }
        }
    }

    @Override
    public void updateResult(final String str) {
        //ASR callback function
        stopASR();
        Log.e("MainCookActivity", "updateResult(str) = "+str);
        this.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                if (str != null && !str.isEmpty()) {
                    Log.e("updateResult", str);
                    //mScLog.append(str);
                    analysisASR(str);
                }
                //mButtonStartOnOff.setChecked(false);
                //setEnabledViewsForStart(true);
            }
        });
        startHTWD();
    }

    @Override
    public void updateKeyword(final String str) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (str != null && !str.isEmpty()) {
                    Log.e("updateKeyword", str);
                    //mScLog.updateKeyword(str);
                }
            }
        });
    }

    @Override
    public void updateResult(final String str, final boolean detected, final boolean fromServer, final boolean stopped) {
        //htwd result callback function
        stopHTWD();
        Log.e("MainCookActivity", "updateResult() start");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("MainCookActivity", "updateResult() run!");
                startASR();
                if (str != null && !str.isEmpty()) {
                    int msgId;
                    if (detected) {
                        if (fromServer) {
                            msgId = R.string.recognized_by_server;
                        } else {
                            msgId = R.string.detected;
                        }
                    } else {
                        if (fromServer) {
                            msgId = R.string.not_recognized_by_server;
                        } else {
                            msgId = R.string.error;
                        }
                    }
                    //scLog.append(String.format(HtwdMainActivity.this.getString(msgId), str));

                    //printResultTime(fromServer);

                    if (stopped) {
                        //mButtonStartOnOff.setChecked(false);
                        //setEnabledViewsForStart(true);
                        //scLog.append("\n" + HtwdMainActivity.this.getString(R.string.common_press_start));
                    }
                }
            }
        });
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
                    String jsonConfigTTS = new Gson().toJson(ttsConfig, TtsConfig.class);
                    Log.d(TAG, "jsonConfig: " + jsonConfigTTS);
                    mTts.configure(jsonConfigTTS); // READY로 바뀜 // 여기서 에러. jsonConfig = null;???

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
    private void stopListening(String reason) {
        Log.d(TAG, "StartButton: STOP");
        mASREngineManager.stopListening();
    }
    private String getEncryptionKeyHTWD() {
        return getBuildConfigFieldHTWD("ENCRYPTION_KEY");
    }
    private String getEncryptionKeyASR() {
        return getBuildConfigFieldASR("ENCRYPTION_KEY");
    }
    private String getBuildConfigFieldHTWD(String name) {
        String key = null;
        try {
            Field f = BuildConfig.class.getField(name); // 여기 buildConfig는 asr꺼로 사용
            key = (String)f.get(null);
        } catch (Exception e) {
            Log.w(TAG, "Cannot found on BuildConfig " + name);
        }
        return key;
    }
    private String getBuildConfigFieldASR(String name) {
        String key = null;
        try {
            Field f = com.lge.aip.engine.speech.BuildConfig.class.getField(name); // 여기 buildConfig는 asr꺼로 사용
            key = (String)f.get(null);
        } catch (Exception e) {
            Log.w(TAG, "Cannot found on BuildConfig " + name);
        }
        return key;
    }
    private void loadConfigASR() {
        ConfigLoaderASR configLoader = new ConfigLoaderASR(this);
        speechConfig = configLoader.loadConfig(SpeechConfig.class);

        if (speechConfig == null) {
            showJsonErrorDialog();
            return;
        }

        //enableSpinners(!speechConfig.enableHttp2);
        if (speechConfig.enableHttp2) {
            return;
        }

        AsrConfig asrConfig = speechConfig.asrConfig;
        if (asrConfig == null) {
            showJsonErrorDialog();
            return;
        }
        /*
        if (mSpnLanguage != null) {
            ArrayAdapter adapter = (ArrayAdapter)mSpnLanguage.getAdapter();
            int position = adapter.getPosition(asrConfig.language);
            mSpnLanguage.setSelection(position);
        }
        if (mSpnWorkingMode != null) {
            ArrayAdapter adapter = (ArrayAdapter)mSpnWorkingMode.getAdapter();
            int position = adapter.getPosition(speechConfig.opMode);
            mSpnWorkingMode.setSelection(position);
        }
        if (mSpnCompleteMode != null) {
            mSpnCompleteMode.setSelection(asrConfig.enableCompleteMode? 0 : 1);
        }
        */
    }
    private void loadConfigHTWD() {
        ConfigLoaderHTWD configLoader = new ConfigLoaderHTWD(this);
        mConfigHTWD = configLoader.loadConfig(HybridTwdConfig.class);

        if (mConfigHTWD == null) {
            showJsonErrorDialog();
            return;
        }

        // This is a simple example of generating a value to identify the device. It changes every
        // time it runs. Find and apply the appropriate method.
        mConfigHTWD.serverConfig.deviceId = UUID.randomUUID().toString();

        if (mModelLoader == null) {
            mModelLoader = new TwdModelLoader(this);
        }
        try {
            mModelLoader.load(mConfigHTWD.embeddedConfig.triggerWord, mConfigHTWD.language);
        } catch (IllegalArgumentException e) {
            showJsonErrorDialog();
            mModelLoader = null;
            return;
        }

        if (!(mConfigHTWD.embeddedConfig.cm > 0)) {
            mConfigHTWD.embeddedConfig.cm = mModelLoader.getCm();
        }
        if (mConfigHTWD.embeddedConfig.weight == 0) {
            mConfigHTWD.embeddedConfig.weight = mModelLoader.getWeight();
        }
        if (mConfigHTWD.embeddedConfig.sensitivity == 0) {
            mConfigHTWD.embeddedConfig.sensitivity = mModelLoader.getSensitivity();
        }
        /*
        if (mSpinnerKeyword != null) {
            mSpinnerKeyword.setSelection(mModelLoader.getKeywordIndex(mConfig.embeddedConfig.triggerWord));
        }
        if (mSpinnerLanguage != null) {
            mSpinnerLanguage.setSelection(mModelLoader.getLanguageIndex(mConfig.language));
        }
        if (mEditSensitivity != null) {
            mEditSensitivity.setText(Integer.toString(mConfig.embeddedConfig.sensitivity));
        }
        if (mRadioGroupModule != null) {
            if (mConfig.enableHybrid) {
                mRadioGroupModule.check(R.id.radio_hybrid);
            } else {
                mRadioGroupModule.check(R.id.radio_embedded);
            }
        }
         */
    }
    private void showJsonErrorDialog() {
        Toast.makeText(this, R.string.popup_msg_config_error, Toast.LENGTH_LONG).show();
        /*
        if (mScLog != null) {
            mScLog.clear();
            mScLog.append(R.string.popup_msg_config_error);
        }
         */
    }
    private void showJsonErrorDialog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        /*
        if (scLog != null) {
            scLog.clear();
            scLog.append(message);
        }
         */
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            speechConfig = null;
            //removeListenerOnViews();
            loadConfigASR();
            //setListenerOnViews();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initEngineASR();

                } else {
                    Log.d("TAG", "permission denied by user");
                }
                return;
            }
            case REQUEST_CODE_HTWD:{
                if (grantResults.length == 0) {
                    return;
                }

                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    // If you do not have the following two permissions,
                    // it will not work properly and you need to end the app.
                    if ((permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || permission.equals(Manifest.permission.READ_PHONE_STATE))
                            && grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainCookActivity.this,
                                R.string.permission_notice, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onRequestPermissionsResult: Mandatory permissions are not granted.");
                        finish();
                        return;
                    }

                    if (!mInitialized && !checkCopyNeeded()) {
                        // Init engine
                        initEngineHTWD();

                        mInitialized = true;
                    }

                    // Allow only file testing if microphone permissions are not acquired
                    if (permission.equals(Manifest.permission.RECORD_AUDIO)
                            && grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainCookActivity.this,
                                "RECORD_ADUIO PERMISSION REQUIRED", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        // Check permissions and whether files are copied.
        if (!mInitialized && !requestPermission() && !checkCopyNeeded()) {
            // Perform engine initialization
            initEngineHTWD();
            initEngineASR();
            initEngineTTS();
            mInitialized = true;
        }
    }
    private boolean requestPermission() {
        ArrayList<String> permissions = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissions.size() > 0) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_HTWD);
            return true;
        }
        return false;
    }
    public boolean checkCopyNeeded() {
        Log.d(TAG, "checkCopyNeeded: exist");
        return false;
    }
}
