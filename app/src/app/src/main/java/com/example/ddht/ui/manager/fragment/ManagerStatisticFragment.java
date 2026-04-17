package com.example.ddht.ui.manager.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.ddht.data.remote.dto.CatalogStatisticResponse;
import com.example.ddht.data.remote.dto.ProductStatisticResponse;
import com.example.ddht.data.remote.dto.StaffStatisticResponse;
import com.example.ddht.data.remote.dto.StatisticOverviewResponse;
import com.example.ddht.data.remote.dto.StatusStatisticResponse;
import com.example.ddht.data.remote.dto.TimeSeriesPointResponse;
import com.example.ddht.data.repository.StatisticRepository;
import com.example.ddht.ui.manager.adapter.StatisticCatalogAdapter;
import com.example.ddht.ui.manager.adapter.StatisticProductAdapter;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerStatisticFragment extends Fragment {

    private SessionManager sessionManager;
    private StatisticRepository statisticRepository;

    private TextView tvFromDate, tvToDate;
    private Spinner spinnerGroupBy;
    private Button btnFilter;
    private TabLayout tabStatisticType;

    private TextView tvTotalRevenue, tvTotalOrders;
    private View layoutTimeSeries, layoutStatus, layoutProducts, layoutCatalogs, layoutStaff;
    private RecyclerView rvTimeSeries, rvStatusDistribution, rvTopStaff, rvTopProducts, rvTopCatalogs;

    private StatisticProductAdapter productAdapter;
    private StatisticCatalogAdapter catalogAdapter;
    private TimeSeriesAdapter timeSeriesAdapter;
    private StatusDistributionAdapter statusAdapter;
    private TopStaffAdapter staffAdapter;

    private Calendar calFrom, calTo;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager_statistic, container, false);

        sessionManager = new SessionManager(requireContext());
        statisticRepository = new StatisticRepository();

        calFrom = Calendar.getInstance();
        calFrom.add(Calendar.DAY_OF_MONTH, -30);
        calTo = Calendar.getInstance();

        initViews(view);
        setupRecyclerViews();
        setupFilters();
        setupTabs();

        fetchData();

        return view;
    }

    private void initViews(View view) {
        tvFromDate = view.findViewById(R.id.tvFromDate);
        tvToDate = view.findViewById(R.id.tvToDate);
        spinnerGroupBy = view.findViewById(R.id.spinnerGroupBy);
        btnFilter = view.findViewById(R.id.btnFilter);
        tabStatisticType = view.findViewById(R.id.tabStatisticType);

        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);

        layoutTimeSeries = view.findViewById(R.id.layoutTimeSeries);
        layoutStatus = view.findViewById(R.id.layoutStatus);
        layoutProducts = view.findViewById(R.id.layoutProducts);
        layoutCatalogs = view.findViewById(R.id.layoutCatalogs);
        layoutStaff = view.findViewById(R.id.layoutStaff);

        rvTimeSeries = view.findViewById(R.id.rvTimeSeries);
        rvStatusDistribution = view.findViewById(R.id.rvStatusDistribution);
        rvTopStaff = view.findViewById(R.id.rvTopStaff);
        rvTopProducts = view.findViewById(R.id.rvTopProducts);
        rvTopCatalogs = view.findViewById(R.id.rvTopCatalogs);
    }

    private void setupFilters() {
        tvFromDate.setText(sdf.format(calFrom.getTime()));
        tvToDate.setText(sdf.format(calTo.getTime()));

        tvFromDate.setOnClickListener(v -> showDatePicker(calFrom, tvFromDate, true));
        tvToDate.setOnClickListener(v -> showDatePicker(calTo, tvToDate, false));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.group_by_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroupBy.setAdapter(adapter);

        btnFilter.setOnClickListener(v -> fetchData());
    }

    private void setupTabs() {
        tabStatisticType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabVisibility(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void updateTabVisibility(int position) {
        layoutTimeSeries.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        layoutStatus.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        layoutProducts.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        layoutCatalogs.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
        layoutStaff.setVisibility(position == 4 ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker(Calendar targetCal, TextView targetView, boolean isFromDate) {
        DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar tempCal = (Calendar) targetCal.clone();
                    tempCal.set(year, month, dayOfMonth);

                    if (isFromDate) {
                        if (tempCal.after(calTo)) {
                            Toast.makeText(requireContext(), "Ngày bắt đầu phải nhỏ hơn ngày kết thúc", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        if (tempCal.before(calFrom)) {
                            Toast.makeText(requireContext(), "Ngày kết thúc phải lớn hơn ngày bắt đầu", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    targetCal.set(year, month, dayOfMonth);
                    targetView.setText(sdf.format(targetCal.getTime()));
                },
                targetCal.get(Calendar.YEAR),
                targetCal.get(Calendar.MONTH),
                targetCal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void setupRecyclerViews() {
        rvTopProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        productAdapter = new StatisticProductAdapter();
        rvTopProducts.setAdapter(productAdapter);

        rvTopCatalogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        catalogAdapter = new StatisticCatalogAdapter();
        rvTopCatalogs.setAdapter(catalogAdapter);

        rvTimeSeries.setLayoutManager(new LinearLayoutManager(requireContext()));
        timeSeriesAdapter = new TimeSeriesAdapter();
        rvTimeSeries.setAdapter(timeSeriesAdapter);

        rvStatusDistribution.setLayoutManager(new LinearLayoutManager(requireContext()));
        statusAdapter = new StatusDistributionAdapter();
        rvStatusDistribution.setAdapter(statusAdapter);

        rvTopStaff.setLayoutManager(new LinearLayoutManager(requireContext()));
        staffAdapter = new TopStaffAdapter();
        rvTopStaff.setAdapter(staffAdapter);
    }

    private String getSelectedGroupBy() {
        int pos = spinnerGroupBy.getSelectedItemPosition();
        switch (pos) {
            case 0:
                return "DAY";
            case 1:
                return "WEEK";
            case 2:
                return "MONTH";
            case 3:
                return "YEAR";
            default:
                return "DAY";
        }
    }

    private Instant getStartOfDay(Calendar cal) {
        return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
    }

    private Instant getEndOfDay(Calendar cal) {
        return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                .atTime(23, 59, 59)
                .toInstant(ZoneOffset.UTC);
    }

    private void fetchData() {
        String token = sessionManager.getAccessToken();
        if (token == null) return;

        Instant from = getStartOfDay(calFrom);
        Instant to = getEndOfDay(calTo);
        String groupBy = getSelectedGroupBy();

        loadOverview(token, from, to);

        int currentTab = tabStatisticType.getSelectedTabPosition();
        switch (currentTab) {
            case 0:
                loadTimeSeries(token, from, to, groupBy);
                break;
            case 1:
                loadStatusDistribution(token, from, to);
                break;
            case 2:
                // Theo yêu cầu: hiển thị theo doanh thu (REVENUE) mặc định cho tab sản phẩm
                loadTopProducts(token, from, to, 10, "REVENUE");
                break;
            case 3:
                loadByCatalog(token, from, to);
                break;
            case 4:
                loadTopStaff(token, from, to);
                break;
        }
    }

    private void loadOverview(String token, Instant from, Instant to) {
        statisticRepository.getStatisticOverview(token, from, to)
                .enqueue(new Callback<ApiResponse<StatisticOverviewResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<StatisticOverviewResponse>> call,
                                           Response<ApiResponse<StatisticOverviewResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            StatisticOverviewResponse data = response.body().getData();
                            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                            if (data.getTotalRevenue() != null)
                                tvTotalRevenue.setText(format.format(data.getTotalRevenue()));
                            if (data.getTotalOrders() != null)
                                tvTotalOrders.setText(String.valueOf(data.getTotalOrders()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<StatisticOverviewResponse>> call, Throwable t) {
                    }
                });
    }

    private void loadTimeSeries(String token, Instant from, Instant to, String groupBy) {
        statisticRepository.getRevenueSeries(token, from, to, groupBy)
                .enqueue(new Callback<ApiResponse<List<TimeSeriesPointResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<TimeSeriesPointResponse>>> call,
                                           Response<ApiResponse<List<TimeSeriesPointResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            timeSeriesAdapter.setData(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<TimeSeriesPointResponse>>> call, Throwable t) {
                    }
                });
    }

    private void loadStatusDistribution(String token, Instant from, Instant to) {
        statisticRepository.getStatusDistribution(token, from, to)
                .enqueue(new Callback<ApiResponse<List<StatusStatisticResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<StatusStatisticResponse>>> call,
                                           Response<ApiResponse<List<StatusStatisticResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            statusAdapter.setData(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<StatusStatisticResponse>>> call, Throwable t) {
                    }
                });
    }

    private void loadTopStaff(String token, Instant from, Instant to) {
        statisticRepository.getByStaff(token, from, to)
                .enqueue(new Callback<ApiResponse<List<StaffStatisticResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<StaffStatisticResponse>>> call,
                                           Response<ApiResponse<List<StaffStatisticResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            staffAdapter.setData(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<StaffStatisticResponse>>> call, Throwable t) {
                    }
                });
    }

    private void loadTopProducts(String token, Instant from, Instant to, Integer limit, String sortBy) {
        statisticRepository.getTopProducts(token, from, to, limit, sortBy)
                .enqueue(new Callback<ApiResponse<List<ProductStatisticResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ProductStatisticResponse>>> call,
                                           Response<ApiResponse<List<ProductStatisticResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            productAdapter.setProductList(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ProductStatisticResponse>>> call, Throwable t) {
                    }
                });
    }

    private void loadByCatalog(String token, Instant from, Instant to) {
        statisticRepository.getByCatalog(token, from, to)
                .enqueue(new Callback<ApiResponse<List<CatalogStatisticResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<CatalogStatisticResponse>>> call,
                                           Response<ApiResponse<List<CatalogStatisticResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            catalogAdapter.setCatalogList(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CatalogStatisticResponse>>> call, Throwable t) {
                    }
                });
    }

    // Local Adapters
    private static class TimeSeriesAdapter extends RecyclerView.Adapter<TimeSeriesAdapter.ViewHolder> {
        private List<TimeSeriesPointResponse> list = new ArrayList<>();

        public void setData(List<TimeSeriesPointResponse> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistic_time_series, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTimeBucket, tvOrderAmount, tvRevenue;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTimeBucket = itemView.findViewById(R.id.tvTimeBucket);
                tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
                tvRevenue = itemView.findViewById(R.id.tvRevenue);
            }

            public void bind(TimeSeriesPointResponse item) {
                tvTimeBucket.setText(item.getBucket() != null ? item.getBucket() : "-");
                tvOrderAmount.setText((item.getOrderAmount() != null ? item.getOrderAmount() : 0) + " đơn");
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvRevenue.setText(item.getRevenue() != null ? format.format(item.getRevenue()) : "0 đ");
            }
        }
    }

    private static class StatusDistributionAdapter extends RecyclerView.Adapter<StatusDistributionAdapter.ViewHolder> {
        private List<StatusStatisticResponse> list = new ArrayList<>();

        public void setData(List<StatusStatisticResponse> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistic_status, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStatusName, tvOrderCount, tvPercent;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStatusName = itemView.findViewById(R.id.tvStatusName);
                tvOrderCount = itemView.findViewById(R.id.tvOrderCount);
                tvPercent = itemView.findViewById(R.id.tvPercent);
            }

            public void bind(StatusStatisticResponse item) {
                if (item.getStatus() != null) tvStatusName.setText(item.getStatus().name());
                tvOrderCount.setText((item.getCount() != null ? item.getCount() : 0) + " đơn");
                tvPercent.setText((item.getRatioPercent() != null ? item.getRatioPercent() : 0) + "%");
            }
        }
    }

    private static class TopStaffAdapter extends RecyclerView.Adapter<TopStaffAdapter.ViewHolder> {
        private List<StaffStatisticResponse> list = new ArrayList<>();

        public void setData(List<StaffStatisticResponse> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistic_staff, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStaffName, tvOrders, tvRevenue;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStaffName = itemView.findViewById(R.id.tvStaffName);
                tvOrders = itemView.findViewById(R.id.tvOrders);
                tvRevenue = itemView.findViewById(R.id.tvRevenue);
            }

            public void bind(StaffStatisticResponse item) {
                tvStaffName.setText(item.getStaffName() != null ? item.getStaffName() : "-");
                tvOrders.setText(String.format("Giao: %d | Đã TT: %d",
                        item.getAssignedOrders() != null ? item.getAssignedOrders() : 0,
                        item.getPaidOrders() != null ? item.getPaidOrders() : 0));
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvRevenue.setText(item.getRevenue() != null ? format.format(item.getRevenue()) : "0 đ");
            }
        }
    }
}
