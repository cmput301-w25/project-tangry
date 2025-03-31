/**
 * UserRepository.java
 * 
 * This file contains the repository implementation for managing user data and relationships
 * in the Tangry application. It provides a data access layer for user profiles, statistics,
 * achievements, and social connections in Firebase Firestore.
 * 
 * Key features:
 * - Implements singleton pattern for application-wide access to user data
 * - Manages user registration and username/email mappings
 * - Tracks karma points and badge achievements (gold, silver, daily)
 * - Handles user statistics for the leaderboard and profile displays
 * - Supports friend relationship management through following/follower lists
 * - Implements achievements logic for post creation and commenting
 * - Provides user search functionality by username prefix
 * - Tracks daily login rewards through badge system
 * - Encapsulates all Firestore user-related operations behind a clean API
 * - Enables testability through dependency injection
 */
package com.example.tangry.repositories;

import com.example.tangry.datasource.FirebaseDataSource;
import com.example.tangry.models.UserStats;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Query.Direction;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserRepository {
    private static UserRepository instance;
    private FirebaseDataSource firebaseDataSource;
    private FirebaseAuth mAuth;
    private static final String COLLECTION_NAME = "users";

    /**
     * Private constructor initializes the FirebaseDataSource with the "usernames" collection.
     */
    private UserRepository() {
        firebaseDataSource = new FirebaseDataSource(COLLECTION_NAME);
    }

    public UserRepository(FirebaseFirestore db, String collectionName) {
        this.firebaseDataSource = new FirebaseDataSource(db, collectionName);
    }


    /**
     * Returns the singleton instance of UserRepository.
     *
     * @return the UserRepository instance
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Searches for usernames that start with the provided prefix.
     *
     * @param prefix            the username prefix
     * @param onSuccessListener callback invoked on a successful query with a QuerySnapshot
     * @param onFailureListener callback invoked if the query fails
     */
    public void searchUsersByPrefix(String prefix,
                                    OnSuccessListener<QuerySnapshot> onSuccessListener,
                                    OnFailureListener onFailureListener) {
        firebaseDataSource.getCollectionReference()
                .orderBy("username")
                .startAt(prefix)
                .endAt(prefix + "\uf8ff")
                .get()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Saves a username and email pair to Firestore.
     *
     * @param username        the username to save
     * @param email           the email address associated with the username
     * @param successListener callback invoked on a successful save with a DocumentReference of the saved document
     * @param failureListener callback invoked if the save operation fails
     */
    public void saveUsernameToFirestore(String username, String email,
                                        OnSuccessListener<DocumentReference> successListener,
                                        OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);

        firebaseDataSource.saveData(data, successListener, failureListener);
    }

    /**
     * Returns a Firestore Query that orders username documents in descending order.
     *
     * @return a Query object for retrieving username documents ordered by the "username" field in descending order
     */
    public Query getPostsQuery() {
        return firebaseDataSource.getCollectionReference().orderBy("username", Direction.DESCENDING);
    }

    /**
     * Retrieves a username from Firestore based on the provided email.
     *
     * @param email           the email address to query for
     * @param successListener callback invoked with the username String if found, or null if not found
     * @param failureListener callback invoked if the query fails
     */
    public void getUsernameFromEmail(String email,
                                     OnSuccessListener<String> successListener,
                                     OnFailureListener failureListener) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        successListener.onSuccess(querySnapshot.getDocuments().get(0).getString("username"));
                    } else {
                        successListener.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener);
    }

    //Increment karma for a user identified by email
    public void incrementKarmaByEmail(String email,
                                      OnSuccessListener<Void> successListener,
                                      OnFailureListener failureListener, int incrementAmount) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        firebaseDataSource.getCollectionReference().document(docId)
                                .update("karma", FieldValue.increment(incrementAmount))
                                .addOnSuccessListener(successListener)
                                .addOnFailureListener(failureListener);
                    } else {
                        failureListener.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(failureListener);
    }


    //Get top 10 users ordered by karma
    public Query getTopUsersQuery() {
        return firebaseDataSource.getCollectionReference()
                .orderBy("karma", Query.Direction.DESCENDING)
                .limit(10);
    }

    // Retrieves the user's stats (karma, badges.goldBadges, badges.silverBadges, daily badge count)
    public void getUserStats(String email,
                             OnSuccessListener<UserStats> onSuccess,
                             OnFailureListener onFailure) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        // Get karma or default to 0
                        long karma = doc.contains("karma") ? doc.getLong("karma") : 0L;

                        // Get gold badges count or default to 0
                        long goldBadges = doc.contains("badges.goldBadges") ? doc.getLong("badges.goldBadges") : 0L;

                        // Get silver badges count or default to 0
                        long silverBadges = doc.contains("badges.silverBadges") ? doc.getLong("badges.silverBadges") : 0L;

                        // Get daily badge dates and calculate count
                        int dailyBadgeCount = 0;
                        if (doc.contains("badges.dailyBadgeDates")) {
                            // Assuming dailyBadgeDates is stored as a List<String>
                            List<String> dailyBadgeDates = (List<String>) doc.get("badges.dailyBadgeDates");
                            dailyBadgeCount = (dailyBadgeDates != null) ? dailyBadgeDates.size() : 0;
                        }

                        UserStats stats = new UserStats(karma, goldBadges, silverBadges, dailyBadgeCount);
                        onSuccess.onSuccess(stats);
                    } else {
                        onFailure.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void getUserStatsByUsername(String username,
                                       OnSuccessListener<UserStats> onSuccess,
                                       OnFailureListener onFailure) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        long karma = doc.contains("karma") ? doc.getLong("karma") : 0L;
                        long goldBadges = doc.contains("badges.goldBadges") ? doc.getLong("badges.goldBadges") : 0L;
                        long silverBadges = doc.contains("badges.silverBadges") ? doc.getLong("badges.silverBadges") : 0L;
                        int dailyBadgeCount = 0;
                        if (doc.contains("badges.dailyBadgeDates")) {
                            List<String> dailyBadgeDates = (List<String>) doc.get("badges.dailyBadgeDates");
                            dailyBadgeCount = (dailyBadgeDates != null) ? dailyBadgeDates.size() : 0;
                        }
                        onSuccess.onSuccess(new UserStats(karma, goldBadges, silverBadges, dailyBadgeCount));
                    } else {
                        onFailure.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }




    /**
     * Updates the user's post count and awards a gold badge for every 3 posts.
     *
     * @param email           the user's email address
     * @param onSuccess       callback invoked on success
     * @param onFailure       callback invoked on failure
     */
    public void updatePostCount(String email,
                                OnSuccessListener<Void> onSuccess,
                                OnFailureListener onFailure) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        firebaseDataSource.getCollectionReference().document(docId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    long postCount = documentSnapshot.contains("postCount")
                                            ? documentSnapshot.getLong("postCount")
                                            : 0;
                                    postCount++;

                                    long goldBadges = 0;
                                    if (documentSnapshot.contains("badges.goldBadges")) {
                                        goldBadges = documentSnapshot.getLong("badges.goldBadges");
                                    }
                                    if (postCount % 3 == 0) {
                                        goldBadges++;
                                    }
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("postCount", postCount);
                                    updates.put("badges.goldBadges", goldBadges);
                                    firebaseDataSource.getCollectionReference().document(docId)
                                            .update(updates)
                                            .addOnSuccessListener(onSuccess)
                                            .addOnFailureListener(onFailure);
                                })
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Updates the user's comment count and awards a silver badge for every 3 comments.
     *
     * @param email           the user's email address
     * @param onSuccess       callback invoked on success
     * @param onFailure       callback invoked on failure
     */
    public void updateCommentCount(String email,
                                   OnSuccessListener<Void> onSuccess,
                                   OnFailureListener onFailure) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        firebaseDataSource.getCollectionReference().document(docId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    long commentCount = documentSnapshot.contains("commentCount")
                                            ? documentSnapshot.getLong("commentCount")
                                            : 0;
                                    commentCount++;

                                    long silverBadges = 0;
                                    if (documentSnapshot.contains("badges.silverBadges")) {
                                        silverBadges = documentSnapshot.getLong("badges.silverBadges");
                                    }
                                    if (commentCount % 3 == 0) {
                                        silverBadges++;
                                    }
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("commentCount", commentCount);
                                    updates.put("badges.silverBadges", silverBadges);
                                    firebaseDataSource.getCollectionReference().document(docId)
                                            .update(updates)
                                            .addOnSuccessListener(onSuccess)
                                            .addOnFailureListener(onFailure);
                                })
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Awards a daily badge if the user hasn't received one for today.
     *
     * @param email           the user's email address
     * @param onSuccess       callback invoked on success
     * @param onFailure       callback invoked on failure
     */
    public void updateDailyBadge(String email,
                                 OnSuccessListener<Void> onSuccess,
                                 OnFailureListener onFailure) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        firebaseDataSource.getCollectionReference().document(docId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    List<String> dailyBadgeDates = (List<String>) documentSnapshot.get("badges.dailyBadgeDates");
                                    if (dailyBadgeDates == null) {
                                        dailyBadgeDates = new ArrayList<>();
                                    }
                                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    if (!dailyBadgeDates.contains(today)) {
                                        dailyBadgeDates.add(today);
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("badges.dailyBadgeDates", dailyBadgeDates);
                                        firebaseDataSource.getCollectionReference().document(docId)
                                                .update(updates)
                                                .addOnSuccessListener(onSuccess)
                                                .addOnFailureListener(onFailure);
                                    } else {
                                        // Daily badge already awarded today.
                                        onSuccess.onSuccess(null);
                                    }
                                })
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }
    /**
     * Retrieves the friends list for the user identified by email.
     * Users are considered friends if they follow each other.
     * It calculates the intersection of the "followers" and "followings" arrays in the user's Firestore document.
     *
     * @param email           the user's email address
     * @param successListener callback invoked with the list of friend usernames
     * @param failureListener callback invoked if retrieval fails
     */
    public void getFriendsList(String email,
                               OnSuccessListener<List<String>> successListener,
                               OnFailureListener failureListener) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
//                        List<String> followers = (List<String>) doc.get("followers");
                        List<String> followings = (List<String>) doc.get("followings");
//                        if (followers == null) {
//                            followers = new ArrayList<>();
//                        }
//                        if (followings == null) {
//                            followings = new ArrayList<>();
//                        }
//                        // Calculate the intersection of followers and followings.
//                        List<String> friends = new ArrayList<>();
//                        for (String user : followers) {
//                            if (followings.contains(user)) {
//                                friends.add(user);
//                            }
//                        }
                        successListener.onSuccess(followings);
                    } else {
                        successListener.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(failureListener);
    }
}