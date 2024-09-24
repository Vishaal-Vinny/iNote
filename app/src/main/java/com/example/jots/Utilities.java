package com.example.jots;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Utilities {

    static CollectionReference getCollectionReference(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("Notes").document(currentUser.getUid())
                .collection("My_Notes");
    }

        public static CollectionReference getCollectionReference(String collectionName) {
            return FirebaseFirestore.getInstance().collection(collectionName);
        }
}
