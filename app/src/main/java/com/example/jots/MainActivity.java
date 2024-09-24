package com.example.jots;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addNoteBtn;
    ImageButton profileBtn;
    RecyclerView recyclerView;
    Adapter noteAdapter;
    SearchView searchView;

    private Query defaultQuery;
    private FirestoreRecyclerOptions<Note> defaultOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteBtn = findViewById(R.id.add_note_btn);
        recyclerView = findViewById(R.id.recycler_view);
        searchView = findViewById(R.id.search_view);
        profileBtn = findViewById(R.id.profile_btn);

        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NoteContents.class));
            }
        });

        initiateRecyclerView();

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userEmail = currentUser.getEmail();
                    String username = currentUser.getDisplayName();

                    Intent intent = new Intent(MainActivity.this, ProfilePage.class);
                    intent.putExtra("userEmail", userEmail);
                    startActivity(intent);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotes(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    noteAdapter.updateOptions(defaultOptions);
                } else {
                    searchNotes(newText);
                }
                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isViewInBounds(searchView, (int) event.getX(), (int) event.getY())) {
            searchView.clearFocus();
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean isViewInBounds(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        return (x >= viewX && x < (viewX + viewWidth)) && (y >= viewY && y < (viewY + viewHeight));
    }

    private void initiateRecyclerView() {
        defaultQuery = Utilities.getCollectionReference().orderBy("timeStamp", Query.Direction.DESCENDING);
        defaultOptions = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(defaultQuery, Note.class)
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new Adapter(defaultOptions, this);
        recyclerView.setAdapter(noteAdapter);
    }

    private void searchNotes(String query) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference myNotesRef = Utilities.getCollectionReference("Notes").document(userId).collection("My_Notes");

        Query searchQuery = myNotesRef
                .whereEqualTo("title", query)
                .orderBy("title")
                .orderBy("timeStamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> searchOptions = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(searchQuery, Note.class)
                .build();

        noteAdapter.updateOptions(searchOptions);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }
}