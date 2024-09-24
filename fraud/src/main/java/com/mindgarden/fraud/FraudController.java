package com.mindgarden.fraud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v1/fraud-check")
public record FraudController(FraudCheckService fraudCheckService)
{

  @GetMapping(path = "{customerId}")
  public FraudCheckResponse isFraudster(@PathVariable("customerId") Integer CustomerID)
  {
    boolean isFraudulentCustomer = fraudCheckService.isFraudulentCustomer(CustomerID);
    return new FraudCheckResponse(isFraudulentCustomer);
  }
}
