package com.example.tangry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;
import com.example.tangry.models.Comment;
import com.example.tangry.utils.TimeUtils;

import java.util.List;

/**
 * Adapter for displaying a list of comments in a RecyclerView.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> commentList;

    /**
     * Creates a new CommentAdapter.
     *
     * @param commentList The list of comments to display.
     */
    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.usernameText.setText(comment.getUsername());
        holder.bodyText.setText(comment.getText());

        if (comment.getTimestamp() != null) {
            String timeAgo = TimeUtils.getTimeAgo(comment.getTimestamp().toDate());
            holder.timestampText.setText(timeAgo);
        } else {
            holder.timestampText.setText("Just now");
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /**
     * ViewHolder class for comment items.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, bodyText, timestampText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.comment_username);
            bodyText = itemView.findViewById(R.id.comment_body);
            timestampText = itemView.findViewById(R.id.comment_timestamp);
        }
    }
}
