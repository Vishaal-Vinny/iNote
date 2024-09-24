package com.example.jots;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class AIChat extends AppCompatActivity {
    private String OPENAI_API_KEY = API_Keys.OPENAI_API_KEY;

    private EditText editMessage;
    private LinearLayout chatContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aichat);

        editMessage = findViewById(R.id.editMessage);
        chatContainer = findViewById(R.id.chatContainer);

        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        sendMessage("Hello! How can I assist you?");
    }

    private void sendMessage(String message) {
        displayMessage("iBot: " + message, true);
    }

    private void sendMessage() {
        String message = editMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            editMessage.setText("");
            displayMessage("User: " + message, false);
            new OpenAiChatTask().execute(message);
        }
    }

    private void displayMessage(String message, boolean isAIResponse) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(18);
        textView.setTypeface(null, Typeface.BOLD);

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showCopyOption(message);
                return true;
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 8, 16, 8);
        textView.setLayoutParams(layoutParams);

        if (isAIResponse) {
            // Apply additional styling for AI responses
            textView.setBackgroundResource(R.drawable.message_background);
            textView.setPadding(16, 8, 16, 8);
        }

        chatContainer.addView(textView);
    }

    private void showCopyOption(final String message) {
        // Option 1: Display a copy icon
        TextView copyIcon = new TextView(this);
        copyIcon.setText("📋"); // Copy icon
        copyIcon.setTextSize(24);
        copyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(message);
            }
        });

        // Add the copy icon to the response container

        // Option 2: Display a context menu
        new AlertDialog.Builder(this)
                .setTitle("Copy Response")
                .setMessage("Do you want to copy the AI response?")
                .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        copyToClipboard(message);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void copyToClipboard(String message) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("AI Response", message);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private class OpenAiChatTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... messages) {
            String message = messages[0];
            // Initialize the ChatLanguageModel with your OpenAI API key
            ChatLanguageModel model = OpenAiChatModel.withApiKey(OPENAI_API_KEY);

            try {
                // Start interacting
                return model.generate(message);
            } catch (Exception e) {
                // Log any exceptions that occur
                e.printStackTrace();
                return "An error occurred: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Display the AI response in the chat
            displayMessage("iBot: " + result, true);
        }
    }
}