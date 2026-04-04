package com.mindgarden.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank(message = "Vorname ist pflicht") String firstname,

        @NotBlank(message = "Nachname ist pflicht") String lastname,
        @NotBlank(message = "Nachname ist pflicht") String email,

        @NotBlank(message = "Telefon ist pflicht") @Size(min = 6, message = "Telefonnummer zu kurz") String phone,

        @Valid AddressRequest address  // optional, null = Adresse bleibt unverändert
) {
}
