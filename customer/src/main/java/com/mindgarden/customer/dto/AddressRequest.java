package com.mindgarden.customer.dto;

public record AddressRequest(String street, String houseNumber, String zipCode, String city, String country) {
}
