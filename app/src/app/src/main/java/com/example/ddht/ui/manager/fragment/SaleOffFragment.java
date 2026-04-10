package com.example.ddht.ui.manager.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.SaleOffDto;
import com.example.ddht.data.repository.ProductRepository;
import com.example.ddht.data.repository.SaleOffRepository;
import com.example.ddht.ui.manager.adapter.SaleOffAdapter;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaleOffFragment extends Fragment {
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
    private SaleOffRepository saleOffRepository;
    private ProductRepository productRepository;
    private SessionManager sessionManager;

    private TextView tvError;
    private ProgressBar progressBar;
    private SaleOffAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_saleoff, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saleOffRepository = new SaleOffRepository();
        productRepository = new ProductRepository();
        sessionManager = new SessionManager(requireContext());

        tvError = view.findViewById(R.id.tvSaleOffError);
        progressBar = view.findViewById(R.id.saleOffProgress);
        RecyclerView rvSaleOffs = view.findViewById(R.id.rvSaleOffs);

        adapter = new SaleOffAdapter(new SaleOffAdapter.SaleOffActionListener() {
            @Override
            public void onOpenDetail(SaleOffDto saleOff) {
                showDetailDialog(saleOff);
            }

            @Override
            public void onDelete(SaleOffDto saleOff) {
                confirmDelete(saleOff);
            }

            @Override
            public void onToggleActive(SaleOffDto saleOff, boolean isActive) {
                updateSaleOff(saleOff, saleOff.getDiscount(), saleOff.getStartDate(), saleOff.getEndDate(), isActive, false);
            }
        });

        rvSaleOffs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSaleOffs.setAdapter(adapter);

        loadSaleOffs();
    }

    private void loadSaleOffs() {
        setLoading(true);
        clearError();
        saleOffRepository.getAllSaleOffs().enqueue(new Callback<ApiResponse<List<SaleOffDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SaleOffDto>>> call,
                                   @NonNull Response<ApiResponse<List<SaleOffDto>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    adapter.submit(response.body().getData());
                    if (response.body().getData().isEmpty()) {
                        showError(getString(R.string.manager_saleoff_empty));
                    }
                } else {
                    adapter.submit(new ArrayList<>());
                    showError(getString(R.string.manager_saleoff_load_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SaleOffDto>>> call, @NonNull Throwable throwable) {
                setLoading(false);
                adapter.submit(new ArrayList<>());
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void showDetailDialog(SaleOffDto saleOff) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_saleoff_detail, null);

        TextView tvProductName = dialogView.findViewById(R.id.tvDetailSaleOffProductName);
        TextInputLayout tilDiscount = dialogView.findViewById(R.id.tilSaleOffDiscount);
        TextInputLayout tilStartDate = dialogView.findViewById(R.id.tilSaleOffStartDate);
        EditText edtDiscount = dialogView.findViewById(R.id.edtSaleOffDiscount);
        EditText edtStartDate = dialogView.findViewById(R.id.edtSaleOffStartDate);
        EditText edtEndDate = dialogView.findViewById(R.id.edtSaleOffEndDate);
        SwitchCompat switchActive = dialogView.findViewById(R.id.switchSaleOffDetailActive);

        tvProductName.setText(getString(R.string.manager_saleoff_detail_for_product, saleOff.getProductName()));
        edtDiscount.setText(String.valueOf(saleOff.getDiscount() == null ? 0 : saleOff.getDiscount()));
        setDateTimeField(edtStartDate, saleOff.getStartDate());
        setDateTimeField(edtEndDate, saleOff.getEndDate());
        switchActive.setChecked(Boolean.TRUE.equals(saleOff.getActive()));

        edtStartDate.setOnClickListener(v -> openDateTimePicker(edtStartDate));
        edtEndDate.setOnClickListener(v -> openDateTimePicker(edtEndDate));
        edtEndDate.setOnLongClickListener(v -> {
            edtEndDate.setText("");
            edtEndDate.setTag(null);
            return true;
        });

        TextWatcher clearWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilDiscount.setError(null);
                tilStartDate.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        edtDiscount.addTextChangedListener(clearWatcher);
        edtStartDate.addTextChangedListener(clearWatcher);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.manager_saleoff_detail_title)
                .setView(dialogView)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String discountRaw = edtDiscount.getText() == null ? "" : edtDiscount.getText().toString().trim();
            String startDate = tagToString(edtStartDate.getTag());
            String endDate = tagToString(edtEndDate.getTag());
            boolean isActive = switchActive.isChecked();

            Double discount = parseDiscount(discountRaw);
            if (discount == null || discount <= 0) {
                tilDiscount.setError(getString(R.string.manager_saleoff_discount_invalid));
                return;
            }
            if (TextUtils.isEmpty(startDate)) {
                tilStartDate.setError(getString(R.string.error_required_field, getString(R.string.manager_saleoff_start_hint)));
                return;
            }

            updateSaleOff(saleOff, discount, startDate, TextUtils.isEmpty(endDate) ? null : endDate, isActive, true);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private Double parseDiscount(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String tagToString(Object tag) {
        return tag instanceof String ? (String) tag : "";
    }

    private void setDateTimeField(EditText editText, String isoValue) {
        if (TextUtils.isEmpty(isoValue)) {
            editText.setText("");
            editText.setTag(null);
            return;
        }
        LocalDateTime dateTime = parseDateTime(isoValue);
        if (dateTime == null) {
            editText.setText(isoValue);
            editText.setTag(isoValue);
            return;
        }
        editText.setText(DISPLAY_FORMATTER.format(dateTime));
        editText.setTag(toIsoUtc(dateTime));
    }

    private void openDateTimePicker(EditText target) {
        LocalDateTime current = null;
        String tagValue = tagToString(target.getTag());
        if (!TextUtils.isEmpty(tagValue)) {
            current = parseDateTime(tagValue);
        }
        if (current == null) {
            current = LocalDateTime.now();
        }

        final LocalDateTime seed = current;
        DatePickerDialog dateDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timeDialog = new TimePickerDialog(
                            requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                LocalDateTime selected = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute);
                                target.setText(DISPLAY_FORMATTER.format(selected));
                                target.setTag(toIsoUtc(selected));
                            },
                            seed.getHour(),
                            seed.getMinute(),
                            true
                    );
                    timeDialog.show();
                },
                seed.getYear(),
                seed.getMonthValue() - 1,
                seed.getDayOfMonth()
        );
        dateDialog.show();
    }

    private LocalDateTime parseDateTime(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        try {
            return Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(raw, DISPLAY_FORMATTER);
        } catch (Exception ignored) {
        }
        return null;
    }

    private String toIsoUtc(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
    }

    private void confirmDelete(SaleOffDto saleOff) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.manager_saleoff_delete_title)
                .setMessage(getString(R.string.manager_saleoff_confirm_delete, saleOff.getProductName()))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_delete, (dialog, which) -> deleteSaleOff(saleOff))
                .show();
    }

    private void deleteSaleOff(SaleOffDto saleOff) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        if (saleOff.getProductId() == null || saleOff.getId() == null) {
            showError(getString(R.string.manager_saleoff_delete_failed));
            return;
        }

        setLoading(true);
        clearError();
        productRepository.deleteSaleOffFromProduct(saleOff.getProductId(), saleOff.getId(), token)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                           @NonNull Response<ApiResponse<Void>> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), R.string.manager_saleoff_delete_success, Toast.LENGTH_SHORT).show();
                            loadSaleOffs();
                        } else {
                            showError(getString(R.string.manager_saleoff_delete_failed));
                            loadSaleOffs();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable throwable) {
                        setLoading(false);
                        showError(getString(R.string.network_error, throwable.getMessage()));
                        loadSaleOffs();
                    }
                });
    }

    private void updateSaleOff(SaleOffDto source,
                               Double discount,
                               String startDate,
                               String endDate,
                               Boolean isActive,
                               boolean showSuccessToast) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            loadSaleOffs();
            return;
        }

        setLoading(true);
        clearError();
        saleOffRepository.updateSaleOff(source.getId(), discount, startDate, endDate, isActive, token)
                .enqueue(new Callback<ApiResponse<SaleOffDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<SaleOffDto>> call,
                                           @NonNull Response<ApiResponse<SaleOffDto>> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            if (showSuccessToast) {
                                Toast.makeText(requireContext(), R.string.manager_saleoff_update_success, Toast.LENGTH_SHORT).show();
                            }
                            loadSaleOffs();
                        } else {
                            showError(getString(R.string.manager_saleoff_update_failed));
                            loadSaleOffs();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<SaleOffDto>> call, @NonNull Throwable throwable) {
                        setLoading(false);
                        showError(getString(R.string.network_error, throwable.getMessage()));
                        loadSaleOffs();
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
