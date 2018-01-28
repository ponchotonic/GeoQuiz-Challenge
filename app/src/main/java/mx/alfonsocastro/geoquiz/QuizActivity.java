package mx.alfonsocastro.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActiviy";
    private static final String KEY_INDEX = "index";
    private static final String KEY_SCORE = "score";
    private static final String KEY_CHEATER = "cheater";
    private static final String KEY_TOKENS = "tokens";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private TextView mQuestionTextView;
    private TextView mRemainingTokensTextView;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private boolean[] mIsCheater = new boolean[mQuestionBank.length];
    private int mCurrentIndex = 0;
    private int mCurrentScore = 0;
    private int mRemainingCheatTokens = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mCurrentScore = savedInstanceState.getInt(KEY_SCORE, 0);
            mRemainingCheatTokens = savedInstanceState.getInt(KEY_TOKENS, 3);
            mIsCheater = savedInstanceState.getBooleanArray(KEY_CHEATER);
        }

        mQuestionTextView = findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(l -> nextQuestion());

        mRemainingTokensTextView = findViewById(R.id.cheat_tokens_text_view);
        mRemainingTokensTextView.setText("Remaining Cheat Tokens: " + mRemainingCheatTokens);

        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(l -> checkAnswer(true));

        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(l -> checkAnswer(false));

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(l -> nextQuestion());

        mPreviousButton = findViewById(R.id.previous_button);
        mPreviousButton.setOnClickListener(l -> previousQuestion());

        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(view -> {
            //Start CheatActivity
            boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
            Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
            startActivityForResult(intent, REQUEST_CODE_CHEAT);
        });

        updateQuestion();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater[mCurrentIndex] = CheatActivity.wasAnswerShown(data);
            //Challenge: Limited to 3 cheats
            mRemainingCheatTokens--;
            mRemainingTokensTextView.setText("Remaining Cheat Tokens: " + mRemainingCheatTokens);
            if (mRemainingCheatTokens == 0) {
                mCheatButton.setEnabled(false);
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState() called");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putInt(KEY_SCORE, mCurrentScore);
        savedInstanceState.putInt(KEY_TOKENS, mRemainingCheatTokens);
        savedInstanceState.putBooleanArray(KEY_CHEATER, mIsCheater);
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

    private void previousQuestion() {
        if (mCurrentIndex != 0) {
            mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
        } else {
            mCurrentIndex = mQuestionBank.length - 1;
        }

        updateQuestion();
    }

    private void nextQuestion() {
        mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;

        updateQuestion();

        //Restart the score when they start over
        if (mCurrentIndex == 0) {
            mCurrentScore = 0;
        }
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
        toggleAnswerButtonsTo(true);
    }

    private void checkAnswer(boolean userPressedTrue) {

        toggleAnswerButtonsTo(false);

        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId;

        if (mIsCheater[mCurrentIndex]) {
            messageResId = R.string.judgment_toast;
            //Cheaters doesn't score
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                mCurrentScore += 1;
                Log.d(TAG, mCurrentScore + "");
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }

        Toast toast = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 20);
        toast.show();

        if (mCurrentIndex == mQuestionBank.length - 1) {
            showScore();
        }
    }

    private void showScore() {
        int percentage = (int) (((double)mCurrentScore/mQuestionBank.length)*100);
        String stringScore = "You got " + percentage + "% correct answers";
        Toast.makeText(this, stringScore, Toast.LENGTH_SHORT).show();
    }

    private void toggleAnswerButtonsTo(boolean b) {
        if (b == false) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        } else if (b == true) {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }
}
