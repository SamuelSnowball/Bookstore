package com.bookstore.order.repository;

import static com.example.database.generated.Tables.ORDERS;
import static com.example.database.generated.Tables.BOOK_ORDERS;
import static com.example.database.generated.Tables.ORDER_DETAIL_VW;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Repository;

import com.example.common.model.OrderStatus;
import com.example.database.generated.tables.daos.OrdersDao;
import com.example.database.generated.tables.pojos.OrderDetailVw;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class OrderRepository extends OrdersDao {

    private final DSLContext create;

    public OrderRepository(DSLContext dslContext, DefaultConfiguration configuration) {
        super(configuration);
        this.create = dslContext;
    }

    public int createOrder(int userId, BigDecimal totalPrice, OrderStatus status) {
        Integer orderId = create.insertInto(ORDERS)
                .set(ORDERS.USER_ID, userId)
                .set(ORDERS.TOTAL_PRICE, totalPrice)
                .set(ORDERS.STATUS, status.name())
                .returningResult(ORDERS.ID)
                .fetchOne()
                .value1();
        
        log.info("Created order {} for user: {} with status: {}", orderId, userId, status);
        
        return orderId;
    }

    public void updateOrderStatus(int orderId, OrderStatus status) {
        int updated = create.update(ORDERS)
                .set(ORDERS.STATUS, status.name())
                .where(ORDERS.ID.eq(orderId))
                .execute();
        
        if (updated > 0) {
            log.info("Updated order {} status to: {}", orderId, status);
        } else {
            log.warn("Failed to update order {} status - order not found", orderId);
        }
    }

    public void addBookToOrder(int orderId, int bookId, BigDecimal price, int quantity) {
        create.insertInto(BOOK_ORDERS)
                .set(BOOK_ORDERS.ORDER_ID, orderId)
                .set(BOOK_ORDERS.BOOK_ID, bookId)
                .set(BOOK_ORDERS.PRICE, price)
                .set(BOOK_ORDERS.QUANTITY, quantity)
                .execute();
        
        log.info("Added book {} to order {}", bookId, orderId);
    }

    public List<OrderDetailVw> getOrderDetailsByUserId(int userId) {
        return create.selectFrom(ORDER_DETAIL_VW)
                .where(ORDER_DETAIL_VW.USER_ID.eq(userId))
                .orderBy(ORDER_DETAIL_VW.CREATED_AT.desc())
                .fetch()
                .into(OrderDetailVw.class);
    }
}

