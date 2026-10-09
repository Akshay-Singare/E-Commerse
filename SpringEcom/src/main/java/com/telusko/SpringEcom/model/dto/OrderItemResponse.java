package com.telusko.SpringEcom.model.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    String ProductName,
    int quantity,
    BigDecimal totalPrice
) {}
