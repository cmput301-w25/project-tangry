package com.example.tangry.controllers;

import com.example.tangry.models.UserStats;
import com.example.tangry.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;

public class UserController {
    private final UserRepository repository;

    /**
     * Constructs a new UserController and initializes the UserRepository instance.
     */
    public UserController() {
        this.repository = UserRepository.getInstance();
    }

    /**
     * Retrieves the username associated with the specified email address.
     *
     * @param email     the email address for which to fetch the username
     * @param onSuccess callback invoked upon successful retrieval, returning the username as a String
     * @param onFailure callback invoked if the retrieval fails
     */
    public void getUsername(String email,
                            OnSuccessListener<String> onSuccess,
                            OnFailureListener onFailure) {
        repository.getUsernameFromEmail(email, onSuccess, onFailure);
    }

    /**
     * Increments the user's karma by the specified amount.
     *
     * @param email           the user's email address
     * @param onSuccess       callback invoked upon successful update
     * @param onFailure       callback invoked if the update fails
     * @param incrementAmount the amount to increment the user's karma
     */
    public void incrementKarma(String email,
                               OnSuccessListener<Void> onSuccess,
                               OnFailureListener onFailure, int incrementAmount) {
        repository.incrementKarmaByEmail(email, onSuccess, onFailure, incrementAmount);
    }

    /**
     * Retrieves the top users query for the leaderboard.
     *
     * @return a Query object to retrieve the top users
     */
    public Query getTopUsersQuery() {
        return repository.getTopUsersQuery();
    }

    /**
     * Retrieves the user's stats (karma and badges) for the specified email.
     *
     * @param email     the user's email address
     * @param onSuccess callback invoked with a UserStats object on success
     * @param onFailure callback invoked on failure
     */
    public void getUserStats(String email,
                             OnSuccessListener<UserStats> onSuccess,
                             OnFailureListener onFailure) {
        repository.getUserStats(email, onSuccess, onFailure);
    }

    public void getUserStatsByUsername(String username,
                                       OnSuccessListener<UserStats> onSuccess,
                                       OnFailureListener onFailure) {
        repository.getUserStatsByUsername(username, onSuccess, onFailure);
    }


    /**
     * Increments the user's post count and awards a gold badge for every 3 posts.
     *
     * @param email     the user's email address
     * @param onSuccess callback invoked upon successful update
     * @param onFailure callback invoked if the update fails
     */
    public void incrementPostCount(String email,
                                   OnSuccessListener<Void> onSuccess,
                                   OnFailureListener onFailure) {
        repository.updatePostCount(email, onSuccess, onFailure);
    }

    /**
     * Increments the user's comment count and awards a silver badge for every 3 comments.
     *
     * @param email     the user's email address
     * @param onSuccess callback invoked upon successful update
     * @param onFailure callback invoked if the update fails
     */
    public void incrementCommentCount(String email,
                                      OnSuccessListener<Void> onSuccess,
                                      OnFailureListener onFailure) {
        repository.updateCommentCount(email, onSuccess, onFailure);
    }

    /**
     * Awards a daily badge if the user hasn't received one for today.
     *
     * @param email     the user's email address
     * @param onSuccess callback invoked upon successful update
     * @param onFailure callback invoked if the update fails
     */
    public void updateDailyBadge(String email,
                                 OnSuccessListener<Void> onSuccess,
                                 OnFailureListener onFailure) {
        repository.updateDailyBadge(email, onSuccess, onFailure);
    }
}
