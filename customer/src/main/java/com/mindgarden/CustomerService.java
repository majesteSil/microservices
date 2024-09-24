package com.mindgarden;

public class CustomerService
{

  public void registerCustomer(CustomerRegistrationRequest request)
  {
    Customer customer = Customer.builder()
                                .firstname(request.firstName())
                                .lastname(request.lastName())
                                .email(request.email())
                                .build();

    //todo: check if email is valid
    //todo: check if email is not taken
    //todo: store customer in db
  }
}
