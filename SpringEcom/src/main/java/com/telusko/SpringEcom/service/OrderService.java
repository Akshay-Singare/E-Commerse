package com.telusko.SpringEcom.service;

import com.telusko.SpringEcom.model.Order;
import com.telusko.SpringEcom.model.OrderItem;
import com.telusko.SpringEcom.model.Product;
import com.telusko.SpringEcom.model.dto.OrderItemRequest;
import com.telusko.SpringEcom.model.dto.OrderItemResponse;
import com.telusko.SpringEcom.model.dto.OrderRequest;
import com.telusko.SpringEcom.model.dto.OrderResponse;
import com.telusko.SpringEcom.repository.OrderRepo;
import com.telusko.SpringEcom.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepo orderRepo;

    public OrderResponse placeOrder(OrderRequest orderRequest) {

        Order order = new Order();

        String orderId ="ORD"+ UUID.randomUUID().toString().substring(0,8).toUpperCase();
        order.setOrderId(orderId);
        order.setCustomerName(orderRequest.customerName());
        order.setEmail(orderRequest.email());
        order.setStatus("PLACED");
        order.setOrderDate(LocalDate.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemRequest orderItemReq : orderRequest.items()){

            Product product = productRepository.findById(orderItemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product Not Found"));

            product.setStockQuantity(product.getStockQuantity()-orderItemReq.quantity());
            productRepository.save(product);  //Update the Product

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(orderItemReq.quantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(orderItemReq.quantity())))
                    .order(order)
                    .build();

            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
       Order savedOrder = orderRepo.save(order);

       List<OrderItemResponse> itemResponses = new ArrayList<>();
       for (OrderItem item : order.getOrderItems()){
           OrderItemResponse orderItemResponse = new OrderItemResponse(
                   item.getProduct().getName(),
                   item.getQuantity(),
                 item.getTotalPrice());
           itemResponses.add(orderItemResponse);

       }

       OrderResponse orderResponse = new OrderResponse(savedOrder.getOrderId(),
               savedOrder.getCustomerName(),
               savedOrder.getEmail(),
               savedOrder.getStatus(),
               savedOrder.getOrderDate(),
               itemResponses
               );

        return orderResponse;
    }

    public List<OrderResponse> getAllOrdersResponses() {

        List<Order> orders = orderRepo.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order:orders){

            List<OrderItemResponse> itemResponses = new ArrayList<>();
            for (OrderItem item :order.getOrderItems()){
                OrderItemResponse orderItemResponse = new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getTotalPrice());
                itemResponses.add(orderItemResponse);
            }

            OrderResponse orderResponse = new OrderResponse(
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses);
            orderResponses.add(orderResponse);

        }

        return orderResponses;
    }
}
