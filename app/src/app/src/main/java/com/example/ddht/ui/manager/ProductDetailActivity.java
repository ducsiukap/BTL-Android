package com.example.ddht.ui.manager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.remote.dto.ProductImageDto;
import com.example.ddht.data.repository.ProductRepository;
import com.example.ddht.ui.manager.adapter.ManageProductImageAdapter;
import com.example.ddht.ui.manager.adapter.ProductImagePagerAdapter;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    private static final int MAX_PRODUCT_IMAGES = 3;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm",
            Locale.getDefault());

    private ProductRepository productRepository;
    private SessionManager sessionManager;

    private ProgressBar progressBar;
    private TextView tvError;
    private ViewPager2 vpImages;
    private TextView tvImageIndicator;
    private TextView tvName;
    private TextView tvDescription;
    private TextView tvOriginalPrice;
    private TextView tvDiscountedPrice;
    private TextView tvStatus;
    private SwitchCompat switchSelling;
    private ProductImagePagerAdapter imagePagerAdapter;
    private ActivityResultLauncher<String> pickDetailImagesLauncher;
    private AlertDialog manageImagesDialog;
    private TextView tvManageImagesHint;
    private ManageProductImageAdapter manageProductImageAdapter;
    private final List<ProductImageDto> manageImages = new ArrayList<>();
    private Button btnManageImagesAdd;
    private CharSequence manageImagesAddDefaultText;

    private Long productId;
    private ProductDto currentProduct;
    private boolean bindingSwitch = false;
    // Manager Views
    private View layoutManagerActions;
    private ImageButton btnEdit;
    private ImageButton btnDelete;
    private ImageButton btnEditImages;
    private Button btnAddSaleOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_manager);

        productRepository = new ProductRepository();
        sessionManager = new SessionManager(this);

        progressBar = findViewById(R.id.productDetailProgress);
        tvError = findViewById(R.id.tvProductDetailError);
        vpImages = findViewById(R.id.vpProductDetailImages);
        tvImageIndicator = findViewById(R.id.tvProductImagePagerIndicator);
        tvName = findViewById(R.id.tvProductDetailName);
        tvDescription = findViewById(R.id.tvProductDetailDescription);
        tvOriginalPrice = findViewById(R.id.tvProductDetailOriginalPrice);
        tvDiscountedPrice = findViewById(R.id.tvProductDetailDiscountedPrice);
        tvStatus = findViewById(R.id.tvProductDetailStatus);
        switchSelling = findViewById(R.id.switchProductDetailSelling);

        // Manager Views
        layoutManagerActions = findViewById(R.id.layoutProductDetailManagerActions);
        btnEdit = findViewById(R.id.btnProductDetailEdit);
        btnDelete = findViewById(R.id.btnProductDetailDelete);
        btnEditImages = findViewById(R.id.btnProductDetailEditImages);
        btnAddSaleOff = findViewById(R.id.btnProductDetailAddSaleOff);

        ImageButton btnBack = findViewById(R.id.btnProductDetailBack);

        pickDetailImagesLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(),
                this::handlePickedDetailImages);

        imagePagerAdapter = new ProductImagePagerAdapter();
        vpImages.setAdapter(imagePagerAdapter);
        vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int total = imagePagerAdapter.getItemCount();
                if (total <= 1) {
                    tvImageIndicator.setText("");
                } else {
                    tvImageIndicator
                            .setText(getString(R.string.manager_product_image_pager_indicator, position + 1, total));
                }
            }
        });

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == null || productId <= 0) {
            Toast.makeText(this, R.string.manager_product_invalid_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        System.out.println("ProductDetailActivity - Loading productId " + productId);
        btnBack.setOnClickListener(v -> finish());

        btnBack.setOnClickListener(v -> finish());

        // Manager logic
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnEditImages.setOnClickListener(v -> showManageImagesDialog());
        btnAddSaleOff.setOnClickListener(v -> showAddSaleOffDialog());
        switchSelling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (bindingSwitch || currentProduct == null) {
                return;
            }
            updateProduct(currentProduct.getName(), currentProduct.getDescription(), currentProduct.getOriginalPrice(),
                    isChecked, false);
        });

        loadProduct();
    }


    private void loadProduct() {
        setLoading(true);
        clearError();
        System.out.println("Calling productRepository.getProductById with ID " + productId);
        productRepository.getProductById(productId).enqueue(new Callback<ApiResponse<ProductDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ProductDto>> call,
                    @NonNull Response<ApiResponse<ProductDto>> response) {
                setLoading(false);
                System.out.println(
                        "Product load API response: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showError(getString(R.string.manager_product_detail_load_failed));
                    return;
                }
                currentProduct = response.body().getData();
                System.out.println("Product loaded successfully " + currentProduct.getName());
                bindProduct(currentProduct);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Throwable throwable) {
                setLoading(false);
                System.out.println("Product load API error: " + throwable.getMessage());
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void bindProduct(ProductDto product) {
        String name = TextUtils.isEmpty(product.getName()) ? getString(R.string.manager_product_default_name)
                : product.getName();
        String description = TextUtils.isEmpty(product.getDescription())
                ? getString(R.string.manager_product_no_description)
                : product.getDescription();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        double originalPrice = product.getOriginalPrice() == null ? 0 : product.getOriginalPrice();
        double discountedPrice = product.getDiscountedPrice() == null ? originalPrice : product.getDiscountedPrice();

        tvName.setText(name);
        tvDescription.setText(description);
        tvOriginalPrice.setText(getString(R.string.manager_product_original_price, formatter.format(originalPrice)));
        tvDiscountedPrice
                .setText(getString(R.string.manager_product_discounted_price, formatter.format(discountedPrice)));
        tvOriginalPrice.setTextColor(getColor(R.color.brand_text_primary));

        boolean hasSale = Boolean.TRUE.equals(product.getSaleOff()) && discountedPrice < originalPrice;
        if (hasSale) {
            tvDiscountedPrice.setTextColor(getColor(R.color.brand_error));
        } else {
            tvDiscountedPrice.setTextColor(getColor(R.color.brand_text_primary));
        }

        bindingSwitch = true;
        boolean isSelling = product.getSelling() == null || product.getSelling();
        switchSelling.setChecked(isSelling);
        bindingSwitch = false;

        tvStatus.setText(isSelling
                ? R.string.manager_product_status_selling
                : R.string.manager_product_status_stopped);
        tvStatus.setTextColor(getColor(isSelling ? R.color.brand_primary : R.color.brand_error));

        List<String> imageUrls = resolveImageUrls(product);
        imagePagerAdapter.submit(imageUrls);
        if (imageUrls.size() <= 1) {
            tvImageIndicator.setText("");
        } else {
            tvImageIndicator.setText(getString(R.string.manager_product_image_pager_indicator, 1, imageUrls.size()));
        }
        vpImages.setCurrentItem(0, false);
    }

    private List<String> resolveImageUrls(ProductDto product) {
        java.util.ArrayList<String> urls = new java.util.ArrayList<>();
        List<ProductImageDto> images = product.getImages();
        if (images != null) {
            for (ProductImageDto image : images) {
                if (image != null && !TextUtils.isEmpty(image.getUrl())) {
                    urls.add(image.getUrl());
                }
            }
        }
        if (urls.isEmpty()) {
            urls.add(null);
        }
        return urls;
    }

    private void showManageImagesDialog() {
        if (currentProduct == null) {
            return;
        }
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_product_images, null);
        tvManageImagesHint = dialogView.findViewById(R.id.tvManageProductImagesHint);
        btnManageImagesAdd = dialogView.findViewById(R.id.btnManageProductImagesAdd);
        RecyclerView rvImages = dialogView.findViewById(R.id.rvManageProductImages);

        manageImages.clear();
        manageImages.addAll(getValidImages(currentProduct));
        updateManageImagesHint();

        manageProductImageAdapter = new ManageProductImageAdapter(image -> {
            if (image == null || image.getId() == null) {
                return;
            }
            confirmDeleteImage(image.getId());
        });
        rvImages.setLayoutManager(new LinearLayoutManager(this));
        rvImages.setAdapter(manageProductImageAdapter);
        manageProductImageAdapter.submit(new ArrayList<>(manageImages));

        manageImagesAddDefaultText = btnManageImagesAdd.getText();
        btnManageImagesAdd.setOnClickListener(v -> {
            if (manageImages.size() >= MAX_PRODUCT_IMAGES) {
                Toast.makeText(this, getString(R.string.manager_product_image_limit_reached, MAX_PRODUCT_IMAGES),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            pickDetailImagesLauncher.launch("image/*");
        });

        manageImagesDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.manager_product_manage_images)
                .setView(dialogView)
                .setNegativeButton(R.string.common_close, null)
                .show();
        manageImagesDialog.setOnDismissListener(dialog -> {
            manageImagesDialog = null;
            tvManageImagesHint = null;
            manageProductImageAdapter = null;
            btnManageImagesAdd = null;
            manageImagesAddDefaultText = null;
            manageImages.clear();
        });
    }

    private void confirmDeleteImage(Long imageId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.manager_product_image_delete_title)
                .setMessage(R.string.manager_product_image_delete_confirm)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_delete, (dialog, which) -> deleteProductImage(imageId))
                .show();
    }

    private void deleteProductImage(Long imageId) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }

        setLoading(true);
        productRepository.deleteImageFromProduct(productId, imageId, token)
                .enqueue(new Callback<ApiResponse<ProductDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ProductDto>> call,
                            @NonNull Response<ApiResponse<ProductDto>> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(ProductDetailActivity.this, R.string.manager_product_image_delete_success,
                                    Toast.LENGTH_SHORT).show();
                            if (response.body() != null && response.body().getData() != null) {
                                currentProduct = response.body().getData();
                                bindProduct(currentProduct);
                                refreshManageImagesFromCurrentProduct();
                            } else {
                                loadProduct();
                                refreshManageImagesFromCurrentProduct();
                            }
                        } else {
                            showError(getString(R.string.manager_product_image_delete_failed));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Throwable throwable) {
                        setLoading(false);
                        showError(getString(R.string.network_error, throwable.getMessage()));
                    }
                });
    }

    private void handlePickedDetailImages(List<Uri> uris) {
        if (uris == null || uris.isEmpty() || currentProduct == null) {
            return;
        }
        int currentCount = getValidImages(currentProduct).size();
        int remaining = MAX_PRODUCT_IMAGES - currentCount;
        if (remaining <= 0) {
            Toast.makeText(this, getString(R.string.manager_product_image_limit_reached, MAX_PRODUCT_IMAGES),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<Uri> accepted = new ArrayList<>();
        for (Uri uri : uris) {
            if (uri == null) {
                continue;
            }
            accepted.add(uri);
            if (accepted.size() >= remaining) {
                break;
            }
        }
        if (uris.size() > accepted.size()) {
            Toast.makeText(this, getString(R.string.manager_product_image_limit_reached, MAX_PRODUCT_IMAGES),
                    Toast.LENGTH_SHORT).show();
        }

        List<MultipartBody.Part> imageParts = buildImageParts(accepted);
        if (imageParts.isEmpty()) {
            showError(getString(R.string.manager_product_image_upload_failed));
            return;
        }

        uploadProductImages(imageParts);
    }

    private void uploadProductImages(List<MultipartBody.Part> imageParts) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setManageImagesAddLoading(true);
        productRepository.addImagesToProduct(productId, imageParts, token)
                .enqueue(new Callback<ApiResponse<ProductDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ProductDto>> call,
                            @NonNull Response<ApiResponse<ProductDto>> response) {
                        setManageImagesAddLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(ProductDetailActivity.this, R.string.manager_product_image_upload_success,
                                    Toast.LENGTH_SHORT).show();
                            if (response.body() != null && response.body().getData() != null) {
                                currentProduct = response.body().getData();
                                bindProduct(currentProduct);
                                refreshManageImagesFromCurrentProduct();
                            } else {
                                loadProduct();
                                refreshManageImagesFromCurrentProduct();
                            }
                        } else {
                            showError(getString(R.string.manager_product_image_upload_failed));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Throwable throwable) {
                        setManageImagesAddLoading(false);
                        showError(getString(R.string.network_error, throwable.getMessage()));
                    }
                });
    }

    private void setManageImagesAddLoading(boolean loading) {
        if (btnManageImagesAdd == null) {
            return;
        }
        btnManageImagesAdd.setEnabled(!loading);
        btnManageImagesAdd.setText(loading
                ? getString(R.string.manager_product_image_uploading)
                : (manageImagesAddDefaultText == null ? getString(R.string.manager_product_image_add)
                        : manageImagesAddDefaultText));
    }

    private List<ProductImageDto> getValidImages(ProductDto product) {
        List<ProductImageDto> valid = new ArrayList<>();
        if (product == null || product.getImages() == null) {
            return valid;
        }
        for (ProductImageDto image : product.getImages()) {
            if (image != null && image.getId() != null && !TextUtils.isEmpty(image.getUrl())) {
                valid.add(image);
            }
        }
        return valid;
    }

    private void refreshManageImagesFromCurrentProduct() {
        if (manageProductImageAdapter == null || currentProduct == null) {
            return;
        }
        manageImages.clear();
        manageImages.addAll(getValidImages(currentProduct));
        manageProductImageAdapter.submit(new ArrayList<>(manageImages));
        updateManageImagesHint();
    }

    private void updateManageImagesHint() {
        if (tvManageImagesHint == null) {
            return;
        }
        tvManageImagesHint.setText(getString(
                R.string.manager_product_selected_images_count,
                manageImages.size(),
                MAX_PRODUCT_IMAGES));
    }

    private List<MultipartBody.Part> buildImageParts(List<Uri> uris) {
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (int i = 0; i < uris.size(); i++) {
            byte[] bytes = readBytes(uris.get(i));
            if (bytes == null || bytes.length == 0) {
                continue;
            }
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), bytes);
            parts.add(MultipartBody.Part.createFormData("images",
                    "product_detail_" + System.currentTimeMillis() + "_" + i + ".jpg", body));
        }
        return parts;
    }

    private byte[] readBytes(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void showEditDialog() {
        if (currentProduct == null) {
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manager_product_form, null);
        TextInputLayout tilCatalog = dialogView.findViewById(R.id.tilManagerProductCatalog);
        TextInputLayout tilName = dialogView.findViewById(R.id.tilManagerProductName);
        TextInputLayout tilPrice = dialogView.findViewById(R.id.tilManagerProductPrice);
        View layoutImageActions = dialogView.findViewById(R.id.layoutManagerProductImageActions);
        View tvImageCount = dialogView.findViewById(R.id.tvManagerProductImageCount);
        View rvImagePreview = dialogView.findViewById(R.id.rvManagerProductSelectedImages);
        EditText edtName = dialogView.findViewById(R.id.edtManagerProductName);
        EditText edtDescription = dialogView.findViewById(R.id.edtManagerProductDescription);
        EditText edtPrice = dialogView.findViewById(R.id.edtManagerProductPrice);
        SwitchCompat switchFormSelling = dialogView.findViewById(R.id.switchManagerProductFormSelling);

        View catalogLayout = tilCatalog;
        if (catalogLayout != null) {
            catalogLayout.setVisibility(View.GONE);
        }
        if (layoutImageActions != null) {
            layoutImageActions.setVisibility(View.GONE);
        }
        if (tvImageCount != null) {
            tvImageCount.setVisibility(View.GONE);
        }
        if (rvImagePreview != null) {
            rvImagePreview.setVisibility(View.GONE);
        }

        edtName.setText(currentProduct.getName());
        edtDescription.setText(currentProduct.getDescription());
        edtPrice.setText(
                String.valueOf(currentProduct.getOriginalPrice() == null ? 0 : currentProduct.getOriginalPrice()));
        switchFormSelling.setChecked(currentProduct.getSelling() == null || currentProduct.getSelling());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.manager_product_edit_title)
                .setView(dialogView)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilName.setError(null);
            tilPrice.setError(null);

            String name = edtName.getText() == null ? "" : edtName.getText().toString().trim();
            String description = edtDescription.getText() == null ? "" : edtDescription.getText().toString().trim();
            String rawPrice = edtPrice.getText() == null ? "" : edtPrice.getText().toString().trim();
            Double price = parseDouble(rawPrice);

            if (TextUtils.isEmpty(name)) {
                tilName.setError(
                        getString(R.string.error_required_field, getString(R.string.manager_product_name_hint)));
                return;
            }
            if (price == null || price <= 0) {
                tilPrice.setError(getString(R.string.manager_product_price_invalid));
                return;
            }

            updateProduct(name, description, price, switchFormSelling.isChecked(), true);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void confirmDelete() {
        if (currentProduct == null) {
            return;
        }
        String name = TextUtils.isEmpty(currentProduct.getName())
                ? getString(R.string.manager_product_default_name)
                : currentProduct.getName();
        new AlertDialog.Builder(this)
                .setTitle(R.string.manager_product_delete_title)
                .setMessage(getString(R.string.manager_product_confirm_delete, name))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_delete, (dialog, which) -> deleteProduct())
                .show();
    }

    private void deleteProduct() {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }
        setLoading(true);
        clearError();
        productRepository.deleteProduct(productId, token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, R.string.manager_product_delete_success,
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(getString(R.string.manager_product_delete_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void updateProduct(String name, String description, Double price, boolean isSelling, boolean showToast) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }

        setLoading(true);
        clearError();
        productRepository.updateProduct(productId, name, description, price, isSelling, token)
                .enqueue(new Callback<ApiResponse<ProductDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ProductDto>> call,
                            @NonNull Response<ApiResponse<ProductDto>> response) {
                        setLoading(false);
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            showError(getString(R.string.manager_product_update_failed));
                            loadProduct();
                            return;
                        }
                        currentProduct = response.body().getData();
                        bindProduct(currentProduct);
                        if (showToast) {
                            Toast.makeText(ProductDetailActivity.this, R.string.manager_product_update_success,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Throwable throwable) {
                        setLoading(false);
                        showError(getString(R.string.network_error, throwable.getMessage()));
                        loadProduct();
                    }
                });
    }

    private void showAddSaleOffDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_saleoff, null);
        TextInputLayout tilDiscount = dialogView.findViewById(R.id.tilAddSaleOffDiscount);
        TextInputLayout tilStartDate = dialogView.findViewById(R.id.tilAddSaleOffStartDate);
        EditText edtDiscount = dialogView.findViewById(R.id.edtAddSaleOffDiscount);
        EditText edtStartDate = dialogView.findViewById(R.id.edtAddSaleOffStartDate);
        EditText edtEndDate = dialogView.findViewById(R.id.edtAddSaleOffEndDate);
        SwitchCompat switchActive = dialogView.findViewById(R.id.switchAddSaleOffActive);

        edtStartDate.setOnClickListener(v -> openDateTimePicker(edtStartDate));
        edtEndDate.setOnClickListener(v -> openDateTimePicker(edtEndDate));
        edtEndDate.setOnLongClickListener(v -> {
            edtEndDate.setText("");
            edtEndDate.setTag(null);
            return true;
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.manager_product_add_saleoff)
                .setView(dialogView)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilDiscount.setError(null);
            tilStartDate.setError(null);

            String rawDiscount = edtDiscount.getText() == null ? "" : edtDiscount.getText().toString().trim();
            Double discount = parseDouble(rawDiscount);
            String startDate = tagToString(edtStartDate.getTag());
            String endDate = tagToString(edtEndDate.getTag());
            boolean active = switchActive.isChecked();

            if (discount == null || discount <= 0) {
                tilDiscount.setError(getString(R.string.manager_saleoff_discount_invalid));
                return;
            }
            if (TextUtils.isEmpty(startDate)) {
                tilStartDate.setError(
                        getString(R.string.error_required_field, getString(R.string.manager_saleoff_start_hint)));
                return;
            }

            addSaleOff(discount, startDate, TextUtils.isEmpty(endDate) ? null : endDate, active);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void addSaleOff(Double discount, String startDate, String endDate, boolean active) {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            showError(getString(R.string.session_expired));
            return;
        }

        setLoading(true);
        clearError();
        productRepository.addSaleOffToProduct(productId, discount, startDate, endDate, active, token)
                .enqueue(new Callback<ApiResponse<com.example.ddht.data.remote.dto.SaleOffDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<com.example.ddht.data.remote.dto.SaleOffDto>> call,
                            @NonNull Response<ApiResponse<com.example.ddht.data.remote.dto.SaleOffDto>> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(ProductDetailActivity.this, R.string.manager_product_add_saleoff_success,
                                    Toast.LENGTH_SHORT).show();
                            loadProduct();
                        } else {
                            showError(getString(R.string.manager_product_add_saleoff_failed));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<com.example.ddht.data.remote.dto.SaleOffDto>> call,
                            @NonNull Throwable throwable) {
                        setLoading(false);
                        showError(getString(R.string.network_error, throwable.getMessage()));
                    }
                });
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

        LocalDateTime seed = current;
        DatePickerDialog dateDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timeDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                LocalDateTime selected = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay,
                                        minute);
                                target.setText(DISPLAY_FORMATTER.format(selected));
                                target.setTag(toIsoUtc(selected));
                            },
                            seed.getHour(),
                            seed.getMinute(),
                            true);
                    timeDialog.show();
                },
                seed.getYear(),
                seed.getMonthValue() - 1,
                seed.getDayOfMonth());
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

    private String tagToString(Object tag) {
        return tag instanceof String ? (String) tag : "";
    }

    private Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
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
