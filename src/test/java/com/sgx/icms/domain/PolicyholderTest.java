package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PolicyholderTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Policyholder p = new Policyholder();
        LocalDate dob = LocalDate.of(1990, 5, 1);

        p.setId(1L);
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setDob(dob);
        p.setEmail("john@example.com");
        p.setMobile("9999999999");
        p.setAddress("123 Main St");
        p.setCity("Pune");
        p.setState("MH");
        p.setPinCode("411001");

        assertEquals(1L, p.getId());
        assertEquals("John", p.getFirstName());
        assertEquals("Doe", p.getLastName());
        assertEquals(dob, p.getDob());
        assertEquals("john@example.com", p.getEmail());
        assertEquals("9999999999", p.getMobile());
        assertEquals("123 Main St", p.getAddress());
        assertEquals("Pune", p.getCity());
        assertEquals("MH", p.getState());
        assertEquals("411001", p.getPinCode());
    }

    @Test
    void fullNameConcatenatesFirstAndLastName() {
        Policyholder p = new Policyholder();
        p.setFirstName("John");
        p.setLastName("Doe");
        assertEquals("John Doe", p.getFullName());
    }
}
