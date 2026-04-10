package com.example.ddht.ui.manager.fragment;

import android.app.AlertDialog;
import android.util.DisplayMetrics;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;
import com.example.ddht.data.repository.CatalogRepository;
import com.example.ddht.ui.manager.adapter.CatalogAdapter;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogFragment extends Fragment {
    private CatalogRepository catalogRepository;
    private SessionManager sessionManager;

    private EditText edtSearch;
    private TextView tvError;
    private ProgressBar progressBar;
    private CatalogAdapter adapter;

    private final List<CatalogDto> allCatalogs = new ArrayList<>();
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        catalogRepository = new CatalogRepository();
        sessionManager = new SessionManager(requireContext());

        edtSearch = view.findViewById(R.id.edtCatalogSearch);
        ImageButton btnSearch = view.findViewById(R.id.btnSearchCatalogs);
        ImageButton btnAdd = view.findViewById(R.id.btnAddCatalog);
        tvError = view.findViewById(R.id.tvCatalogError);
        progressBar = view.findViewById(R.id.catalogProgress);
        RecyclerView rvCatalogs = view.findViewById(R.id.rvCatalogs);

        adapter = new CatalogAdapter(new CatalogAdapter.CatalogActionListener() {
            @Override
            public void onEdit(CatalogDto catalog) {
                showCatalogDialog(catalog);
            }

            @Override
            public void onDelete(CatalogDto catalog) {
                confirmDelete(catalog);
            }
        });

        rvCatalogs.setLayoutManager(new GridLayoutManager(requireContext(), getCatalogSpanCount()));
        rvCatalogs.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> applySearch());
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            applySearch();
            return true;
        });
        btnAdd.setOnClickListener(v -> showCatalogDialog(null));

        loadCatalogs();
    }

    private void applySearch() {
        currentQuery = edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim();
        renderFilteredCatalogs();
    }

    private void loadCatalogs() {
        setLoading(true);
        clearError();
        catalogRepository.getAllCatalogs().enqueue(new Callback<ApiResponse<List<CatalogDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CatalogDto>>> call,
                                   @NonNull Response<ApiResponse<List<CatalogDto>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allCatalogs.clear();
                    allCatalogs.addAll(response.body().getData());
                    renderFilteredCatalogs();
                } else {
                    showError(getString(R.string.manager_catalog_load_failed));
                    adapter.submit(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CatalogDto>>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
                adapter.submit(new ArrayList<>());
            }
        });
    }

    private void renderFilteredCatalogs() {
        List<CatalogDto> filtered = new ArrayList<>();
        for (CatalogDto catalog : allCatalogs) {
            if (catalog == null || TextUtils.isEmpty(catalog.getName())) {
                continue;
            }
            if (matchesQuery(catalog.getName(), currentQuery)) {
                filtered.add(catalog);
            }
        }
        adapter.submit(filtered);
        if (filtered.isEmpty()) {
            showError(TextUtils.isEmpty(currentQuery)
                    ? getString(R.string.manager_catalog_empty)
                    : getString(R.string.manager_catalog_not_found));
        } else {
            clearError();
        }
    }

    private boolean matchesQuery(String name, String query) {
        if (TextUtils.isEmpty(query)) {
            return true;
        }
        return name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private int getCatalogSpanCount() {
        DisplayMetrics metrics = requireContext().getResources().getDisplayMetrics();
        float widthDp = metrics.widthPixels / metrics.density;
        return widthDp >= 840 ? 3 : 2;
    }

    private void showCatalogDialog(@Nullable CatalogDto editingCatalog) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_catalog_form, null);
        EditText edtName = dialogView.findViewById(R.id.edtCatalogName);
        TextInputLayout tilName = dialogView.findViewById(R.id.tilCatalogName);

        boolean isEdit = editingCatalog != null;
        if (isEdit) {
            edtName.setText(editingCatalog.getName());
        }

        edtName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilName.setError(null);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? R.string.manager_catalog_edit_title : R.string.manager_catalog_add_title)
                .setView(dialogView)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = edtName.getText() == null ? "" : edtName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                tilName.setError(getString(R.string.error_required_field, getString(R.string.manager_catalog_name_hint)));
                return;
            }
            if (isDuplicateName(name, isEdit ? editingCatalog.getId() : null)) {
                tilName.setError(getString(R.string.manager_catalog_duplicate));
                return;
            }
            if (isEdit) {
                updateCatalog(editingCatalog.getId(), name, dialog);
            } else {
                createCatalog(name, dialog);
            }
        }));

        dialog.show();
    }

    private void createCatalog(String name, AlertDialog dialog) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        catalogRepository.createCatalog(name, token).enqueue(new Callback<ApiResponse<CatalogDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CatalogDto>> call,
                                   @NonNull Response<ApiResponse<CatalogDto>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), R.string.manager_catalog_create_success, Toast.LENGTH_SHORT).show();
                    loadCatalogs();
                } else {
                    showError(getString(R.string.manager_catalog_create_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CatalogDto>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void updateCatalog(Long catalogId, String name, AlertDialog dialog) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        catalogRepository.updateCatalog(catalogId, name, token).enqueue(new Callback<ApiResponse<CatalogDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CatalogDto>> call,
                                   @NonNull Response<ApiResponse<CatalogDto>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), R.string.manager_catalog_update_success, Toast.LENGTH_SHORT).show();
                    loadCatalogs();
                } else {
                    showError(getString(R.string.manager_catalog_update_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CatalogDto>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void confirmDelete(CatalogDto catalog) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.manager_catalog_delete_title)
                .setMessage(getString(R.string.manager_catalog_confirm_delete, catalog.getName()))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_delete, (dialog, which) -> deleteCatalog(catalog.getId()))
                .show();
    }

    private void deleteCatalog(Long catalogId) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        catalogRepository.deleteCatalog(catalogId, token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.manager_catalog_delete_success, Toast.LENGTH_SHORT).show();
                    loadCatalogs();
                } else {
                    showError(getString(R.string.manager_catalog_delete_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private boolean isDuplicateName(String candidateName, @Nullable Long editingId) {
        String normalizedCandidate = normalize(candidateName);
        for (CatalogDto catalog : allCatalogs) {
            if (catalog == null || catalog.getId() == null || TextUtils.isEmpty(catalog.getName())) {
                continue;
            }
            if (editingId != null && editingId.equals(catalog.getId())) {
                continue;
            }
            if (normalize(catalog.getName()).equals(normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
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

    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }
}
