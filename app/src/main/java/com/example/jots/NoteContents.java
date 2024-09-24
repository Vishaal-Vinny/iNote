package com.example.jots;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Text;

import java.util.Locale;

import jp.wasabeef.richeditor.RichEditor;

public class NoteContents extends AppCompatActivity {

    EditText titleEditText;
    RichEditor contentEditor; // RichEditor
    ImageButton saveBtn;
    TextView titleView, deleteNotesView, cancelBtn, b1;
    String title, contents, docID;
    boolean editMode = false;
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_contents);

        titleEditText = findViewById(R.id.title_txt);
        contentEditor = findViewById(R.id.notes_content_txt); // Initialize RichEditor
        saveBtn = findViewById(R.id.save_note_btn);
        titleView = findViewById(R.id.title);
        deleteNotesView = findViewById(R.id.deleteBtn);
        cancelBtn = findViewById(R.id.cancel_btn);
        b1 = findViewById(R.id.icon_speech);

        title = getIntent().getStringExtra("title");
        contents = getIntent().getStringExtra("content");
        docID = getIntent().getStringExtra("docId");

        if (docID != null && !docID.isEmpty()) {
            editMode = true;
        }

        titleEditText.setText(title);
        if (!TextUtils.isEmpty(contents)) {
            contentEditor.setHtml(contents); // Set HTML content for RichEditor
        }
        if (editMode) {
            titleView.setText("Edit iNote");
            deleteNotesView.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.icon_ai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(NoteContents.this, AIChat.class);

                // Start the activity
                startActivity(intent);
            }
        });

        // Toolbar buttons functionality
        findViewById(R.id.btn_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.setBold();
            }
        });

        findViewById(R.id.btn_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.setItalic();
            }
        });

        findViewById(R.id.btn_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.setUnderline();
            }
        });

        findViewById(R.id.btn_bullet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.setBullets();
            }
        });

        findViewById(R.id.btn_number).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.setNumbers();
            }
        });

        findViewById(R.id.btn_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement color picker functionality here
            }
        });

        findViewById(R.id.btn_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.undo();
            }
        });

        findViewById(R.id.btn_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.redo();
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditor.removeFormat();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNotes();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteNotesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNotes();
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(NoteContents.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NoteContents.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tts.isSpeaking()) {
                    tts.stop();
                } else {
                    speakOut();
                }
            }
        });
    }

    private void speakOut() {
        String htmlContent = contentEditor.getHtml();
        if (TextUtils.isEmpty(htmlContent)) {
            tts.speak("No content available", TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            String plainText = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_COMPACT).toString();

            tts.speak(plainText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }


    @Override
    protected void onPause() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onPause();
    }

    private void saveNotes() {
        String noteTitle = titleEditText.getText().toString();
        String contents = contentEditor.getHtml(); // Get HTML content from RichEditor

        if (TextUtils.isEmpty(noteTitle)) {
            titleEditText.setError("Please Set a Title!");
            return;
        }

        Note noteObj = new Note();
        noteObj.setTitle(noteTitle);
        noteObj.setContents(contents);
        noteObj.setTimeStamp(Timestamp.now());

        saveToFirebase(noteObj);
    }

    private void saveToFirebase(Note note) {
        DocumentReference documentReference;

        if (editMode) {
            documentReference = Utilities.getCollectionReference().document(docID);
        } else {
            documentReference = Utilities.getCollectionReference().document();
        }
        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NoteContents.this, "Note Added Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(NoteContents.this, "Failed to Add Note!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void deleteNotes() {
        DocumentReference documentReference;

        if (editMode) {
            documentReference = Utilities.getCollectionReference().document(docID);
        } else {
            documentReference = Utilities.getCollectionReference().document();
        }
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NoteContents.this, "Note Deleted Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(NoteContents.this, "Failed to Delete Note!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
