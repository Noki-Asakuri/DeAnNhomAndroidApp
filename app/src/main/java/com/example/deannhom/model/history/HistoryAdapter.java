package com.example.deannhom.model.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deannhom.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    ArrayList<History> historyArrayList;
    Context context;
    UserCallback userCallback;

    public HistoryAdapter(ArrayList<History> historyArrayList, UserCallback userCallback) {
        this.historyArrayList = historyArrayList;
        this.userCallback = userCallback;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView = inflater.inflate(R.layout.layout_history_item, parent, false);

        return new HistoryViewHolder(userView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History item = historyArrayList.get(position);

        holder.textHistoryWord.setText(item.getWord());
        holder.layoutHistory.setOnClickListener(view -> userCallback.onItemClicked(item, position));
        holder.btnDeleteHistoryWord.setOnClickListener(view -> userCallback.onItemDelete(item, position));
    }

    @Override
    public int getItemCount() {
        return historyArrayList.size();
    }

    public interface UserCallback {
        void onItemDelete(History history, int position);

        void onItemClicked(History history, int position);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textHistoryWord;
        FlexboxLayout layoutHistory;
        MaterialButton btnDeleteHistoryWord;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutHistory = itemView.findViewById(R.id.layoutHistory);

            textHistoryWord = itemView.findViewById(R.id.textHistoryWord);
            btnDeleteHistoryWord = itemView.findViewById(R.id.btnDeleteHistoryWord);
        }
    }
}