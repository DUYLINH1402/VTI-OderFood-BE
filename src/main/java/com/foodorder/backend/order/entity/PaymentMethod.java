package com.foodorder.backend.order.entity;

public enum PaymentMethod {
    COD, // Cash on Delivery
    MOMO, // MoMo Wallet
    ZALOPAY, // ZaloPay Wallet
    ATM, // ATM Card via ZaloPay Gateway
    VISA // Credit Card (Visa/Master/JCB) via ZaloPay Gateway
}
