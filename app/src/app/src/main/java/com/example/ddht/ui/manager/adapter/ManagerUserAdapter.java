package com.example.ddht.ui.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

public class ManagerUserAdapter extends RecyclerView.Adapter<ManagerUserAdapter.ViewHolder> {
    public interface UserActionListener {
        void onEdit(UserDto user);
        void onResetPassword(UserDto user);
        void onDelete(UserDto user);
    }

    private final List<UserDto> users = new ArrayList<>();
    private final UserActionListener listener;

    public ManagerUserAdapter(UserActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<UserDto> data) {
        users.clear();
        if (data != null) {
            users.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDto user = users.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(holder.itemView.getContext().getString(R.string.manager_users_role, user.getRole()));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnResetPassword.setOnClickListener(v -> listener.onResetPassword(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        TextView tvRole;
        ImageButton btnEdit;
        ImageButton btnResetPassword;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnResetPassword = itemView.findViewById(R.id.btnResetPasswordUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
