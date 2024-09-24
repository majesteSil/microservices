package com.mindgarden.customer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public record CustomerService(CustomerRepository customerRepository)
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
    //todo: check if customer is fraudster
    customerRepository.save(customer);
    //todo: send notification
  }

  public void loginCustomer(final CustomerLoginRequest customerLoginRequest)
  {
    log.info("do Login");
  }
}
