package android.alexkotlinandroidprojects.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    private ArrayAdapter<NoteInfo> mAdapterNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });

        initializeDisplayContent();
    }

    @Override
    protected void onResume(){
        super.onResume();
//        VVV 'notifyDataSetChanged' function is from Android
        mAdapterNotes.notifyDataSetChanged();
    }

    private void initializeDisplayContent() {
        // Marking this ListView variable VVV as "final" makes you able to reference it within this anonymous class!!
        final ListView listNotes = findViewById(R.id.list_notes);
        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mAdapterNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notes);
        // simple_list_item_1 is a built-in layout that comes from Android <- hence, the: 'android.R' reference
        listNotes.setAdapter(mAdapterNotes);
        // ^^^ This is what takes the Adapter stuff I've set up and puts them in the ListView I specify on line: 39

        // THIS VVV is creating an anonymous, nested class that overrides 'onItemClick()'
        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // use 'NoteListActivity.this' instead of just 'this' because 'this' is an anonymous class referencing 'setOnItemClickListener',
                // and I want to specifically reference the 'setOnClickListener' that is associated with the 'NoteListActivity' here
                // This VVV creates the Intent
                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
//                NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);
                // This VVV is sending the Extra with my Intent!
                intent.putExtra(NoteActivity.NOTE_POSITION, position);

                //This VVV launches that activity WITH that intent
                startActivity(intent);
            }
        });

    }


}