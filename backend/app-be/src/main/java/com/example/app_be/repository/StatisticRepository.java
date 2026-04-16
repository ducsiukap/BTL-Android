package com.example.app_be.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.app_be.model.Order;
import com.example.app_be.repository.projection.CatalogStatisticProjection;
import com.example.app_be.repository.projection.ProductStatisticProjection;
import com.example.app_be.repository.projection.StaffStatisticProjection;
import com.example.app_be.repository.projection.StatusCountProjection;
import com.example.app_be.repository.projection.TimeSeriesProjection;

public interface StatisticRepository extends JpaRepository<Order, Long>  {
    @Query(value = """
            select coalesce(sum(o.total_price), 0)
            from orders o
            where o.is_paid = 1
            and o.payment_time >= :from
            and o.payment_time <= :to
            """, nativeQuery = true)
    BigDecimal sumRevenue(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select count(*)
            from orders o
            where o.created_at >= :from
            and o.created_at <= :to
            """, nativeQuery = true)
    Long countAllOrders(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select count(*)
            from orders o
            where o.is_paid = 1
            and o.payment_time >= :from
            and o.payment_time <= :to
            """, nativeQuery = true)
    Long countPaidOrdes(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select count(*)
            from orders o
            where o.status = 'COMPLETED'
            and o.created_at >= :from
            and o.created_at <= :to
            """, nativeQuery = true)
    Long countCompletedOrders (@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select count(*)
            from orders o
            where o.status = 'CANCELED'
            and o.created_at >= :from
            and o.created_at <= :to
            """, nativeQuery = true)
    Long countCanceledOrders (@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select date_format(o.payment_time, '%Y-%m-%d') as bucket,
                    sum(o.total_price) as revenue,
                    count(*) as orderCount
            from orders o
            where o.is_paid = 1
            and o.payment_time >= :from
            and o.payment_time <= :to
            group by dat_format(o.payment_time, '%Y-%m-%d') 
            order by bucket
            """, nativeQuery = true)
    List<TimeSeriesProjection> revenueSeriesByDay(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select concat(year(o.payment_time), '-w', LPAD(week(o.payment_time, 3), 2, '0')) as bucket,
                    sum(o.total_price) as revenue,
                    count(*) as orderCount
            from orders o
            where o.is_paid = 1
            and o.payment_time >= :from
            and o.payment_time <= :to
            group by year(o.payment_time), week(o.payment_time, 3)
            order by year(o.payment_time), week(o.payment_time, 3)
            """, nativeQuery = true)
    List<TimeSeriesProjection> revenueSeriesByWeek(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select date_format(o.payment_time, '%Y-%m') as bucket,
                   sum(o.total_price) as revenue,
                   count(*) as orderCount
            from orders o
            where o.is_paid = 1
              and o.payment_time >= :from
              and o.payment_time < :to
            group by dat_format(o.payment_time, '%Y-%m')
            order by bucket
            """, nativeQuery = true)
    List<TimeSeriesProjection> revenueSeriesByMonth(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select concat(year(o.payment_time), '-Q', quarter(o.payment_time)) as bucket,
                   sum(o.total_price) as revenue,
                   count(*) as orderCount
            from orders o
            where o.is_paid = 1
              and o.payment_time >= :from
              and o.payment_time < :to
            group by year(o.payment_time), quarter(o.payment_time)
            order by year(o.payment_time), quarter(o.payment_time)
            """, nativeQuery = true)
    List<TimeSeriesProjection> revenueSeriesByQuarter(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select date_format(o.payment_time, '%Y') as bucket,
                   sum(o.total_price) as revenue,
                   count(*) as orderCount
            from orders o
            where o.is_paid = 1
              and o.payment_time >= :from
              and o.payment_time < :to
            group by date_format(o.payment_time, '%Y')
            order by bucket
            """, nativeQuery = true)
    List<TimeSeriesProjection> revenueSeriesByYear(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select date_format(o.created_at, '%Y-%m-%d') as bucket,
                   cast(0 as decimal(15,2)) as revenue,
                   count(*) as orderCount
            from orders o
            where o.created_at >= :from
              and o.created_at < :to
            group by date_format(o.created_at, '%Y-%m-%d')
            order by bucket
            """, nativeQuery = true)
    List<TimeSeriesProjection> orderSeriesByDay(@Param("from") Instant from, @Param("to") Instant to);

     @Query(value = """
            select concat(year(o.created_at), '-W', LPAD(week(o.created_at, 3), 2, '0')) as bucket,
                   cast(0 as decimal(15,2)) as revenue,
                   count(*) as orderCount
            from orders o
            where o.created_at >= :from
              and o.created_at < :to
            group by year(o.created_at), week(o.created_at, 3)
            order by year(o.created_at), week(o.created_at, 3)
            """, nativeQuery = true)
    List<TimeSeriesProjection> orderSeriesByWeek(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select date_format(o.created_at, '%Y-%m') as bucket,
                   cast(0 AS decimal(15,2)) as revenue,
                   count(*) as orderCount
            from orders o
            where o.created_at >= :from
              and o.created_at < :to
            group by date_format(o.created_at, '%Y-%m')
            order by bucket
            """, nativeQuery = true)
    List<TimeSeriesProjection> orderSeriesByMonth(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select concat(year(o.created_at), '-Q', quarter(o.created_at)) as bucket,
                   cast(0 as decimal(15,2)) as revenue,
                   count(*) AS orderCount
            from orders o
            where o.created_at >= :from
              and o.created_at < :to
            group by year(o.created_at), quarter(o.created_at)
            order by year(o.created_at), quarter(o.created_at)
            """, nativeQuery = true)
    List<TimeSeriesProjection> orderSeriesByQuarter(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select date_format(o.created_at, '%Y') as bucket,
                   cast(0 as decimal(15,2)) as revenue,
                   count(*) as orderCount
            from orders o
            where o.created_at >= :from
              and o.created_at < :to
            group by date_format(o.created_at, '%Y')
            order by bucket
            """, nativeQuery = true)
    List<TimeSeriesProjection> orderSeriesByYear(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select p.id as productId,
                   p.name as productName,
                   c.id as catalogId,
                   c.name as catalogName,
                   sum(oi.quantity) as soldQuantity,
                   sum(oi.quantity * oi.price) as revenue
            from orders o
            join order_items oi on oi.order_id = o.id
            join products p on p.id = oi.product_id
            left join catalog c on c.id = p.catalog_id
            where o.is_paid = 1
              and o.payment_time >= :from
              and o.payment_time < :to
            group by p.id, p.name, c.id, c.name
            order by revenue desc, soldQuantity desc
            limit :limitValue
            """, nativeQuery = true)
    List<ProductStatisticProjection> topProductsByRevenue(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("limitValue") int limitValue
    );

    @Query(value = """
            select p.id as productId,
                   p.name as productName,
                   c.id as catalogId,
                   c.name as catalogName,
                   sum(oi.quantity) as soldQuantity,
                   sum(oi.quantity * oi.price) as revenue
            from orders o
            join order_items oi on oi.order_id = o.id
            JOIN products p on p.id = oi.product_id
            left join catalog c on c.id = p.catalog_id
            where o.is_paid = 1
              and o.payment_time >= :from
              and o.payment_time < :to
            group by p.id, p.name, c.id, c.name
            order by soldQuantity desc, revenue desc
            limit :limitValue
            """, nativeQuery = true)
    List<ProductStatisticProjection> topProductsByQuantity(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("limitValue") int limitValue
    );


    @Query(value = """
            select c.id as catalogId,
                   c.name as catalogName,
                   sum(oi.quantity) as soldQuantity,
                   sum(oi.quantity * oi.price) as revenue
            from orders o
            join order_items oi on oi.order_id = o.id
            join products p on p.id = oi.product_id
            left join catalog c on c.id = p.catalog_id
            where o.is_paid = 1
              and o.payment_time >= :from
              and o.payment_time < :to
            group by c.id, c.name
            order by revenue desc
            """, nativeQuery = true)
    List<CatalogStatisticProjection> catalogStatistics(
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query(value = """
            select bin_to_uuid(u.id) as staffId,
                   u.full_name as staffName,
                   count(o.id) as assignedOrders,
                   sum(case when o.is_paid = 1 then 1 else 0 end) as paidOrders,
                   coalesce(sum(case when o.is_paid = 1 then o.total_price else 0 end), 0) as revenue
            from orders o
            join users u on u.id = o.user_id
            where o.created_at >= :from
              and o.created_at < :to
            group by u.id, u.full_name
            order by assignedOrders desc
            """, nativeQuery = true)
    List<StaffStatisticProjection> staffStatistics(
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query(value = """
            select o.status as status,
                   count(*) as count
            from orders o
            where o.created_at >= :from
              and o.created_at < :to
            group by o.status
            order by count desc
            """, nativeQuery = true)
    List<StatusCountProjection> statusDistribution(
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}