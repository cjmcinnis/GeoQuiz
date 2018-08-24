package com.example.craig.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {
    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;
    private boolean mIsCheater;

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_IS_CHEATER = "cheater";
    private static final String KEY_QUESTIONS_ANSWERED = "answered";
    private static final String KEY_QUESTIONS_ANSWERED_CORRECTLY = "answeredCorrectly";
    private static final String KEY_TIMES_CHEATED = "timesCheated";


    private static final int REQUEST_CODE_CHEAT = 0;

    private Question[] mQuestionBank = new Question[]
            {
                    new Question(R.string.question_australia, true),
                    new Question(R.string.question_africa, false),
                    new Question(R.string.question_americas, true),
                    new Question(R.string.question_asia, true),
                    new Question(R.string.question_oceans, true),
                    new Question(R.string.question_mideast, false)
            };

    private int mCurrentIndex;
    private int mQuestionsAnswered;
    private int mQuestionsAnsweredCorrectly;
    private int mTimesCheated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,  "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        //Retrieve data when retrieving state
        if(savedInstanceState != null)
        {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mIsCheater = savedInstanceState.getBoolean(KEY_IS_CHEATER, false);
            mQuestionsAnswered = savedInstanceState.getInt(KEY_QUESTIONS_ANSWERED, 0);
            mQuestionsAnsweredCorrectly = savedInstanceState.getInt(KEY_QUESTIONS_ANSWERED_CORRECTLY, 0);
            mTimesCheated = savedInstanceState.getInt(KEY_TIMES_CHEATED, 0);
        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                checkAnswer(true);
            }
        });
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                checkAnswer(false);
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });

        //if player cheats, create a new CheatActivity
        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //pass in the answer to the new activity
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].cheatCheckAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mCurrentIndex != 0) {
                    mCurrentIndex = Math.abs((mCurrentIndex - 1) % mQuestionBank.length);
                    updateQuestion();
                }
            }
        });

        updateQuestion();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_CODE_CHEAT){
            if(data == null){
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
            mTimesCheated++;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");

        //save current index, number of questions answered
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putInt(KEY_QUESTIONS_ANSWERED, mQuestionsAnswered);
        savedInstanceState.putInt(KEY_QUESTIONS_ANSWERED_CORRECTLY, mQuestionsAnsweredCorrectly);
        savedInstanceState.putInt(KEY_TIMES_CHEATED, mTimesCheated);
        savedInstanceState.putBoolean(KEY_IS_CHEATER, mIsCheater);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    private void updateQuestion(){
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        // if player exceeds # of times cheating is allowed, disable the button.
        if(mTimesCheated > 2)
        {
            mCheatButton.setEnabled(false);
        }

        updateButtonState();
    }

    // check answer and create toast for correct / incorrect
    private void checkAnswer(boolean userPressedTrue)
    {
        if(mQuestionBank[mCurrentIndex].isAnswered() == false) {
            boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
            int messageResId = 0;

            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                mQuestionsAnsweredCorrectly++;
            } else {
                messageResId = R.string.incorrect_toast;
            }

            if(mIsCheater) {
                //display cheater toast if player cheats
                messageResId = R.string.judgement_toast;
            }

            mQuestionsAnswered++;
            Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

        }
        updateButtonState();

        /*
         Check if we are finished, if so we can display the ending toast
         */
        if(mQuestionsAnswered == mQuestionBank.length)
        {
            String toastText = "You have answered " + mQuestionsAnsweredCorrectly + " out of " + mQuestionBank.length  + " questions correctly.";
            Toast.makeText(this,  toastText, Toast.LENGTH_LONG).show();
        }
    }

    private void updateButtonState()
    {
        if(mQuestionBank[mCurrentIndex].isAnswered())
        {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);

        }else{
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }
}
