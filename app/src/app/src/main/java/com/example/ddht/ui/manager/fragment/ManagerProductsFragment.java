package com.example.ddht.ui.manager.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.repository.CatalogRepository;
import com.example.ddht.data.repository.ProductRepository;
import com.example.ddht.ui.manager.ProductDetailActivity;
import com.example.ddht.ui.manager.adapter.ManagerProductAdapter;
import com.example.ddht.ui.manager.adapter.SelectedImageUriAdapter;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerProductsFragment extends Fragment {
    private static final int MAX_PRODUCT_IMAGES = 3;

    private ProductRepository productRepository;
    private CatalogRepository catalogRepository;
    private SessionManager sessionManager;

    private EditText edtSearch;
    private TextView tvError;
    private ProgressBar progressBar;
    private LinearLayout layoutCatalogFilters;
    private ManagerProductAdapter adapter;

    private final List<CatalogDto> catalogs = new ArrayList<>();
    private final List<Uri> createProductImageUris = new ArrayList<>();
    private Long selectedCatalogId = null;
    private String currentQuery = "";
    private ActivityResultLauncher<String> pickImagesLauncher;
    private SelectedImageUriAdapter selectedImageUriAdapter;
    private TextView tvSelectedImageCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_products, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris == null || uris.isEmpty()) {
                return;
            }
            int before = createProductImageUris.size();
            List<Uri> merged = mergeUris(createProductImageUris, uris, MAX_PRODUCT_IMAGES);
            createProductImageUris.clear();
            createProductImageUris.addAll(merged);
            if (selectedImageUriAdapter != null) {
                selectedImageUriAdapter.submit(new ArrayList<>(createProductImageUris));
            }
            if (before + uris.size() > MAX_PRODUCT_IMAGES) {
                Toast.makeText(requireContext(), getString(R.string.manager_product_image_limit_reached, MAX_PRODUCT_IMAGES), Toast.LENGTH_SHORT).show();
            }
            updateSelectedImageCount();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productRepository = new ProductRepository();
        catalogRepository = new CatalogRepository();
        sessionManager = new SessionManager(requireContext());

        edtSearch = view.findViewById(R.id.edtManagerProductSearch);
        ImageButton btnSearch = view.findViewById(R.id.btnManagerSearchProducts);
        ImageButton btnAdd = view.findViewById(R.id.btnManagerAddProduct);
        tvError = view.findViewById(R.id.tvManagerProductsError);
        progressBar = view.findViewById(R.id.managerProductsProgress);
        layoutCatalogFilters = view.findViewById(R.id.layoutManagerCatalogFilters);
        RecyclerView rvProducts = view.findViewById(R.id.rvManagerProducts);

        adapter = new ManagerProductAdapter(new ManagerProductAdapter.ProductActionListener() {
            @Override
            public void onOpenDetail(ProductDto product) {
                if (product.getId() == null) {
                    return;
                }
                Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
                startActivity(intent);
            }

            @Override
            public void onToggleSelling(ProductDto product, boolean isSelling) {
                updateSelling(product, isSelling);
            }
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProducts.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> applySearch());
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                applySearch();
                return true;
            }
            return false;
        });
        btnAdd.setOnClickListener(v -> showAddProductDialog());

        renderCatalogChips();
        loadCatalogs();
        loadProducts(currentQuery);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            loadProducts(currentQuery);
        }
    }

    private void applySearch() {
        currentQuery = edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim();
        loadProducts(currentQuery);
    }

    private void loadCatalogs() {
        catalogRepository.getAllCatalogs().enqueue(new Callback<ApiResponse<List<CatalogDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CatalogDto>>> call,
                                   @NonNull Response<ApiResponse<List<CatalogDto>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    return;
                }
                catalogs.clear();
                catalogs.addAll(response.body().getData());
                renderCatalogChips();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CatalogDto>>> call, @NonNull Throwable throwable) {
                // Keep "Tất cả" filter when catalog API is unavailable.
            }
        });
    }

    private void loadProducts(String query) {
        setLoading(true);
        clearError();
        productRepository.searchProducts(query, selectedCatalogId, 0, 100)
                .enqueue(new Callback<ApiResponse<List<ProductDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<ProductDto>>> call,
                                           @NonNull Response<ApiResponse<List<ProductDto>>> response) {
                        setLoading(false);
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            adapter.submit(new ArrayList<>());
                            showError(getString(R.string.manager_product_load_failed));
                            return;
                        }
                        List<ProductDto> products = response.body().getData();
                        adapter.submit(products);
                        if (products.isEmpty()) {
                            showError(getString(R.string.manager_product_empty));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<ProductDto>>> call, @NonNull Throwable throwable) {
                        setLoading(false);
                        adapter.submit(new ArrayList<>());
                        showError(getString(R.string.network_error, throwable.getMessage()));
                    }
                });
    }

    private void renderCatalogChips() {
        if (layoutCatalogFilters == null) {
            return;
        }
        layoutCatalogFilters.removeAllViews();
        addCatalogChip(getString(R.string.home_catalog_all), null, selectedCatalogId == null);
        for (CatalogDto catalog : catalogs) {
            if (catalog == null || catalog.getId() == null || TextUtils.isEmpty(catalog.getName())) {
                continue;
            }
            addCatalogChip(catalog.getName(), catalog.getId(), catalog.getId().equals(selectedCatalogId));
        }
    }

    private void addCatalogChip(String label, Long catalogId, boolean selected) {
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setSelected(selected);
        chip.setBackgroundResource(R.drawable.bg_catalog_chip);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        chip.setTextColor(requireContext().getColor(selected ? android.R.color.white : R.color.brand_primary));
        chip.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            selectedCatalogId = catalogId;
            renderCatalogChips();
            loadProducts(currentQuery);
        });

        layoutCatalogFilters.addView(chip);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    private void showAddProductDialog() {
        if (catalogs.isEmpty()) {
            Toast.makeText(requireContext(), R.string.manager_product_catalog_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manager_product_form, null);
        TextInputLayout tilCatalog = dialogView.findViewById(R.id.tilManagerProductCatalog);
        TextInputLayout tilName = dialogView.findViewById(R.id.tilManagerProductName);
        TextInputLayout tilPrice = dialogView.findViewById(R.id.tilManagerProductPrice);
        MaterialAutoCompleteTextView actCatalog = dialogView.findViewById(R.id.actManagerProductCatalog);
        EditText edtName = dialogView.findViewById(R.id.edtManagerProductName);
        EditText edtDescription = dialogView.findViewById(R.id.edtManagerProductDescription);
        EditText edtPrice = dialogView.findViewById(R.id.edtManagerProductPrice);
        SwitchCompat switchSelling = dialogView.findViewById(R.id.switchManagerProductFormSelling);
        View layoutUploadLoading = dialogView.findViewById(R.id.layoutManagerProductUploadLoading);
        Button btnPickImages = dialogView.findViewById(R.id.btnManagerPickProductImages);
        RecyclerView rvSelectedImages = dialogView.findViewById(R.id.rvManagerProductSelectedImages);
        tvSelectedImageCount = dialogView.findViewById(R.id.tvManagerProductImageCount);

        createProductImageUris.clear();
        selectedImageUriAdapter = new SelectedImageUriAdapter(position -> {
            if (position < 0 || position >= createProductImageUris.size()) {
                return;
            }
            createProductImageUris.remove(position);
            selectedImageUriAdapter.submit(new ArrayList<>(createProductImageUris));
            updateSelectedImageCount();
        });
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        rvSelectedImages.setAdapter(selectedImageUriAdapter);
        updateSelectedImageCount();

        btnPickImages.setOnClickListener(v -> pickImagesLauncher.launch("image/*"));

        List<String> labels = new ArrayList<>();
        for (CatalogDto catalog : catalogs) {
            if (catalog != null && !TextUtils.isEmpty(catalog.getName())) {
                labels.add(catalog.getName());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_material_dropdown, labels);
        adapter.setDropDownViewResource(R.layout.item_material_dropdown);
        actCatalog.setAdapter(adapter);
        actCatalog.setKeyListener(null);
        actCatalog.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actCatalog.showDropDown();
            }
        });
        actCatalog.setOnClickListener(v -> actCatalog.showDropDown());
        final CatalogDto[] selectedCatalogRef = new CatalogDto[]{null};
        actCatalog.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < labels.size()) {
                selectedCatalogRef[0] = findCatalogByName(labels.get(position));
            }
            tilCatalog.setError(null);
        });

        CatalogDto preSelectedCatalog = findCatalogById(selectedCatalogId);
        if (preSelectedCatalog != null) {
            actCatalog.setText(preSelectedCatalog.getName(), false);
            selectedCatalogRef[0] = preSelectedCatalog;
        } else if (!catalogs.isEmpty()) {
            CatalogDto first = catalogs.get(0);
            if (first != null && !TextUtils.isEmpty(first.getName())) {
                actCatalog.setText(first.getName(), false);
                selectedCatalogRef[0] = first;
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.manager_product_add)
                .setView(dialogView)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilCatalog.setError(null);
            tilName.setError(null);
            tilPrice.setError(null);

            CatalogDto selectedCatalog = selectedCatalogRef[0];
            if (selectedCatalog == null) {
                selectedCatalog = findCatalogByName(actCatalog.getText() == null ? "" : actCatalog.getText().toString().trim());
            }
            String name = edtName.getText() == null ? "" : edtName.getText().toString().trim();
            String description = edtDescription.getText() == null ? "" : edtDescription.getText().toString().trim();
            String rawPrice = edtPrice.getText() == null ? "" : edtPrice.getText().toString().trim();
            Double price = parsePrice(rawPrice);

            if (selectedCatalog == null || selectedCatalog.getId() == null) {
                tilCatalog.setError(getString(R.string.error_required_field, getString(R.string.manager_product_catalog_hint)));
                return;
            }
            if (TextUtils.isEmpty(name)) {
                tilName.setError(getString(R.string.error_required_field, getString(R.string.manager_product_name_hint)));
                return;
            }
            if (price == null || price <= 0) {
                tilPrice.setError(getString(R.string.manager_product_price_invalid));
                return;
            }

            setCreateDialogLoading(dialog, layoutUploadLoading, true);
            createProduct(selectedCatalog.getId(), name, description, price, switchSelling.isChecked(),
                    new ArrayList<>(createProductImageUris), dialog, () -> setCreateDialogLoading(dialog, layoutUploadLoading, false));
        }));

        dialog.show();
    }

    private void createProduct(Long catalogId,
                               String name,
                               String description,
                               Double price,
                               boolean isSelling,
                               List<Uri> imageUris,
                               AlertDialog dialog,
                               Runnable onFinished) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        List<MultipartBody.Part> imageParts = buildImageParts(imageUris);

        setLoading(true);
        clearError();
        productRepository.addProductToCatalog(catalogId, name, description, price, isSelling, imageParts, token)
                .enqueue(new Callback<ApiResponse<ProductDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ProductDto>> call,
                                           @NonNull Response<ApiResponse<ProductDto>> response) {
                        setLoading(false);
                        if (onFinished != null) {
                            onFinished.run();
                        }
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), R.string.manager_product_create_success, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadProducts(currentQuery);
                        } else {
                            showError(getString(R.string.manager_product_create_failed));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Throwable throwable) {
                        setLoading(false);
                        if (onFinished != null) {
                            onFinished.run();
                        }
                        showError(getString(R.string.network_error, throwable.getMessage()));
                    }
                });
    }

    private void setCreateDialogLoading(AlertDialog dialog, View loadingView, boolean loading) {
        if (loadingView != null) {
            loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (dialog != null) {
            if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!loading);
            }
            if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!loading);
            }
        }
    }

    private List<MultipartBody.Part> buildImageParts(List<Uri> uris) {
        List<MultipartBody.Part> parts = new ArrayList<>();
        if (uris == null || uris.isEmpty()) {
            return parts;
        }
        for (int i = 0; i < uris.size() && i < MAX_PRODUCT_IMAGES; i++) {
            Uri uri = uris.get(i);
            byte[] data = readBytes(uri);
            if (data == null || data.length == 0) {
                continue;
            }
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), data);
            String fileName = "product_" + System.currentTimeMillis() + "_" + i + ".jpg";
            parts.add(MultipartBody.Part.createFormData("images", fileName, body));
        }
        return parts;
    }

    private byte[] readBytes(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void updateSelectedImageCount() {
        if (tvSelectedImageCount == null) {
            return;
        }
        tvSelectedImageCount.setText(getString(
                R.string.manager_product_selected_images_count,
                createProductImageUris.size(),
                MAX_PRODUCT_IMAGES
        ));
    }

    private List<Uri> limitUris(List<Uri> uris, int limit) {
        List<Uri> result = new ArrayList<>();
        for (Uri uri : uris) {
            if (uri == null) {
                continue;
            }
            result.add(uri);
            if (result.size() >= limit) {
                break;
            }
        }
        if (uris.size() > limit) {
            Toast.makeText(requireContext(), getString(R.string.manager_product_image_limit_reached, limit), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private List<Uri> mergeUris(List<Uri> current, List<Uri> incoming, int limit) {
        List<Uri> merged = new ArrayList<>();
        if (current != null) {
            merged.addAll(limitUris(current, limit));
        }
        if (incoming == null) {
            return merged;
        }
        for (Uri uri : incoming) {
            if (uri == null) {
                continue;
            }
            if (merged.size() >= limit) {
                break;
            }
            if (!merged.contains(uri)) {
                merged.add(uri);
            }
        }
        return merged;
    }

    private void updateSelling(ProductDto product, boolean isSelling) {
        if (product.getId() == null) {
            return;
        }

        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            loadProducts(currentQuery);
            return;
        }

        String name = TextUtils.isEmpty(product.getName())
                ? getString(R.string.manager_product_default_name)
                : product.getName();
        String description = product.getDescription() == null ? "" : product.getDescription();
        Double price = product.getOriginalPrice() == null ? 0 : product.getOriginalPrice();

        productRepository.updateProduct(product.getId(), name, description, price, isSelling, token)
                .enqueue(new Callback<ApiResponse<ProductDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ProductDto>> call,
                                           @NonNull Response<ApiResponse<ProductDto>> response) {
                        if (response.isSuccessful()) {
                            loadProducts(currentQuery);
                        } else {
                            showError(getString(R.string.manager_product_update_failed));
                            loadProducts(currentQuery);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Throwable throwable) {
                        showError(getString(R.string.network_error, throwable.getMessage()));
                        loadProducts(currentQuery);
                    }
                });
    }

    private CatalogDto findCatalogByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        for (CatalogDto catalog : catalogs) {
            if (catalog != null && catalog.getName() != null && catalog.getName().equals(name)) {
                return catalog;
            }
        }
        return null;
    }

    private CatalogDto findCatalogById(Long id) {
        if (id == null) {
            return null;
        }
        for (CatalogDto catalog : catalogs) {
            if (catalog != null && id.equals(catalog.getId())) {
                return catalog;
            }
        }
        return null;
    }

    private Double parsePrice(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (tvError != null) {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void clearError() {
        if (tvError != null) {
            tvError.setText("");
            tvError.setVisibility(View.GONE);
        }
    }
}
