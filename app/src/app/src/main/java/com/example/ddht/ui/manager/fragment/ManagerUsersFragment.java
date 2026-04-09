package com.example.ddht.ui.manager.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.UserDto;
import com.example.ddht.data.repository.AuthRepository;
import com.example.ddht.ui.manager.adapter.ManagerUserAdapter;
import com.example.ddht.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerUsersFragment extends Fragment {
    private AuthRepository authRepository;
    private SessionManager sessionManager;

    private EditText edtSearch;
    private TextView tvError;
    private ProgressBar progressBar;
    private ManagerUserAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authRepository = new AuthRepository();
        sessionManager = new SessionManager(requireContext());

        edtSearch = view.findViewById(R.id.edtUserSearch);
        Button btnSearch = view.findViewById(R.id.btnSearchUsers);
        Button btnAddUser = view.findViewById(R.id.btnAddUser);
        tvError = view.findViewById(R.id.tvManagerUsersError);
        progressBar = view.findViewById(R.id.managerUsersProgress);
        RecyclerView rvUsers = view.findViewById(R.id.rvManagerUsers);

        adapter = new ManagerUserAdapter(new ManagerUserAdapter.UserActionListener() {
            @Override
            public void onEdit(UserDto user) {
                showUserFormDialog(user);
            }

            @Override
            public void onResetPassword(UserDto user) {
                confirmResetPassword(user);
            }

            @Override
            public void onDelete(UserDto user) {
                confirmDeleteUser(user);
            }
        });

        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsers.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> loadUsers(edtSearch.getText().toString().trim()));
        btnAddUser.setOnClickListener(v -> showUserFormDialog(null));

        loadUsers("");
    }

    private void loadUsers(String query) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }

        setLoading(true);
        clearError();
        authRepository.searchUsers(query, token).enqueue(new Callback<ApiResponse<List<UserDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<UserDto>>> call,
                                   @NonNull Response<ApiResponse<List<UserDto>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    adapter.submit(response.body().getData());
                } else {
                    showError(getString(R.string.manager_users_load_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<UserDto>>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void showUserFormDialog(@Nullable UserDto editingUser) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_form, null);
        EditText edtFullName = dialogView.findViewById(R.id.edtDialogFullName);
        EditText edtEmail = dialogView.findViewById(R.id.edtDialogEmail);
        Spinner spnRole = dialogView.findViewById(R.id.spnDialogRole);
        View tilRole = dialogView.findViewById(R.id.tilDialogRole);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"STAFF", "MANAGER"}
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRole.setAdapter(roleAdapter);

        boolean isEdit = editingUser != null;
        if (isEdit) {
            edtFullName.setText(editingUser.getFullName());
            edtEmail.setText(editingUser.getEmail());
            tilRole.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? R.string.manager_users_edit : R.string.manager_users_add)
                .setView(dialogView)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            if (TextUtils.isEmpty(fullName)) {
                showError(getString(R.string.error_required_field, getString(R.string.profile_fullname)));
                return;
            }
            if (TextUtils.isEmpty(email)) {
                showError(getString(R.string.error_required_field, getString(R.string.profile_email)));
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(getString(R.string.error_invalid_email));
                return;
            }

            if (isEdit) {
                updateUser(editingUser.getId(), fullName, email, dialog);
            } else {
                String role = (String) spnRole.getSelectedItem();
                createUser(fullName, email, role, dialog);
            }
        }));

        dialog.show();
    }

    private void createUser(String fullName, String email, String role, AlertDialog dialog) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        authRepository.createUser(fullName, email, role, token).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDto>> call,
                                   @NonNull Response<ApiResponse<UserDto>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), R.string.manager_users_create_success, Toast.LENGTH_SHORT).show();
                    loadUsers(edtSearch.getText().toString().trim());
                } else {
                    showError(getString(R.string.manager_users_create_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDto>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void updateUser(String userId, String fullName, String email, AlertDialog dialog) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        authRepository.updateUser(userId, fullName, email, token).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDto>> call,
                                   @NonNull Response<ApiResponse<UserDto>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), R.string.manager_users_update_success, Toast.LENGTH_SHORT).show();
                    loadUsers(edtSearch.getText().toString().trim());
                } else {
                    showError(getString(R.string.manager_users_update_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDto>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void confirmResetPassword(UserDto user) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.manager_users_reset_password)
                .setMessage(getString(R.string.manager_users_confirm_reset_password, user.getFullName()))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_reset_password, (dialog, which) -> resetPassword(user.getId()))
                .show();
    }

    private void resetPassword(String userId) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        authRepository.resetPassword(userId, token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.manager_users_reset_password_success, Toast.LENGTH_SHORT).show();
                } else {
                    showError(getString(R.string.manager_users_reset_password_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void confirmDeleteUser(UserDto user) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.manager_users_delete)
                .setMessage(getString(R.string.manager_users_confirm_delete, user.getFullName()))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_delete, (dialog, which) -> deleteUser(user.getId()))
                .show();
    }

    private void deleteUser(String userId) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        authRepository.deleteUser(userId, token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.manager_users_delete_success, Toast.LENGTH_SHORT).show();
                    loadUsers(edtSearch.getText().toString().trim());
                } else {
                    showError(getString(R.string.manager_users_delete_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }
}
