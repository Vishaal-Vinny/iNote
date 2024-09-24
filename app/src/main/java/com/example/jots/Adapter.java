package com.example.jots;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;

public class Adapter extends FirestoreRecyclerAdapter<Note, Adapter.NoteViewHolder> {
    private Context context;

    public Adapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int position, @NonNull Note note) {
        noteViewHolder.titleTxt.setText(note.title);
        noteViewHolder.contentTxt.setText(Html.fromHtml(note.contents, Html.FROM_HTML_MODE_COMPACT));
        noteViewHolder.timeTxt.setText(new SimpleDateFormat("MM/dd/yy").format(note.timeStamp.toDate()));

        noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = noteViewHolder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(context, NoteContents.class);
                    intent.putExtra("title", note.title);
                    intent.putExtra("content", note.contents);
                    String documentID = getSnapshots().getSnapshot(adapterPosition).getId();
                    intent.putExtra("docId", documentID);
                    context.startActivity(intent);
                }
            }
        });
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, contentTxt, timeTxt;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.note_title_txt);
            contentTxt = itemView.findViewById(R.id.note_content_txt);
            timeTxt = itemView.findViewById(R.id.note_time);
        }
    }
}