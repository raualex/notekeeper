package android.alexkotlinandroidprojects.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    //Name this String with the package name Prepended on it, as I am going to send this
    //as an Extra with my Intent in NoteListActivity, and Extras can come from 3rd parties,
    //so the package name will indicate this is from me!
    public static final String NOTE_POSITION = "android.alexkotlinandroidprojects.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private Boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private Boolean mIsCanceling;
    private NoteActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Boilerplate code!!!
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        //Boilerplate code!!

        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);
//        ^^ 'get's an Instance of the "ViewModel" class ^^^ and saves it to the designated variable

        if (mViewModel.mIsNewlyCreated && savedInstanceState != null)
            mViewModel.restoreState(savedInstanceState);

        mViewModel.mIsNewlyCreated = false;

        mSpinnerCourses = findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // simple_spinner_dropdown_item ^^^ is a built-in layout that comes from Android <- hence, the: 'android.R' reference
        mSpinnerCourses.setAdapter(adapterCourses);
        // ^^^ This is what takes the Adapter stuff I've set up and puts them in the ListView I specify on line: 29

//        VVV This function is what checks to see which note to display on the screen
//        VVV (this Activity is for when a user taps to see a specific Note)
        readDisplayStateValues();
        saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote)
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

        Log.d(TAG, "onCreate");
    }

    private void saveOriginalNoteValues() {
        if(!mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
            mViewModel.saveState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsCanceling && mIsNewNote) {
            Log.i(TAG, "Canceling note at position: " + mNotePosition);
            DataManager.getInstance().removeNote(mNotePosition);
        } else if (mIsCanceling && !mIsNewNote) {
            Log.i(TAG, "Canceling note at position: " + mNotePosition);
            storePreviousNoteValues();
        } else {
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
//        VVV This gets the Intent that was sent over with the command to start this Activity
        Intent intent = getIntent();
//        VVV This gets the position sent over in the Intent to look in the
//        VVV DataManager singleton and find the specific Note the user has selected
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
//        VVV This is a Boolean (empty var declared above) checking if
//        VVV this Note exists within the DataManager, or if we got null or something
        mIsNewNote = mNotePosition == POSITION_NOT_SET;
//        VVV As long as the above Boolean is not false, we will get the selected
//        VVV Note info from the DataManager here
        if (mIsNewNote) {
            createNewNote();
        }
        Log.i(TAG, "mNotePosition: " + mNotePosition);
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        ^^^ This get called whenever the user selects a menu option

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) { //<-- If you selected the "Send as email" menu button
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) { //<-- If you selected the "Cancel" menu button
            mIsCanceling = true;
//            VVV This is a function all Activities have,
//            VVV which will signify this Activity is finishing and going to be destroyed!
//            VVV "finish()', because it is closing this Activity, the "onPause()"
//            VVV method for THIS ACTIVITY will be called before the destroy happens (above)
//            VVV "onPause" (above) has the function call to save the user Note, or not
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText();
//        VVV Putting in "Intent.ACTION_SEND" in the Intent constructor gives you
//        VVV the implied (or "implicit") intent that you are sending an Action
        Intent intent = new Intent(Intent.ACTION_SEND);
//        VVV THIS and THIS ^^^ identifies the Implicit target
        intent.setType("message/rfc2822"); //<-- "message/rfc2822" is a common mime type to indicate sending an email
//        VVV "Intent.EXTRA_SUBJECT" implies this is the subject of an email
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//        VVV "Intent.EXTRA_TEXT" implies this is text
        intent.putExtra(Intent.EXTRA_TEXT, text);

        startActivity(intent);
    }
}