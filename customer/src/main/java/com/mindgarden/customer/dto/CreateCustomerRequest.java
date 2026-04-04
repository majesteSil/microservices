package com.mindgarden.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record CreateCustomerRequest(

        @NotBlank(message = "Vorname ist pflicht") String firstname,

        @NotBlank(message = "Nachname ist pflicht") String lastname,

        @Email(message = "Ungültige E-Mail-Adresse") @NotBlank(message = "E-Mail ist pflicht") String email,

        @Pattern(regexp = "^[+]?[0-9\\s\\-]{7,20}$", message = "Ungültige Telefonnummer") String phone,

        // @Valid erzwingt, dass die Validierungen in AddressRequest
        @Valid AddressRequest address) {
}
