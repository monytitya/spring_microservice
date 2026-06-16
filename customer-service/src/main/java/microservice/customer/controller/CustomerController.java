package microservice.customer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import microservice.customer.entity.Customer;
import microservice.customer.entity.KycStatus;
import microservice.customer.repository.CustomerRepository;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "APIs for managing customer profiles and KYC status")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Creates a new customer with KYC status set to PENDING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Email or identity number already in use")
    })
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }
        if (customerRepository.findByIdentityNumber(customer.getIdentityNumber()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Identity number is already registered!");
        }
        customer.setKycStatus(KycStatus.PENDING); // Initialize as pending
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer details", description = "Retrieves customer profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/kyc")
    @Operation(summary = "Update KYC status", description = "Updates the KYC (Know Your Customer) status for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KYC status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<?> updateKyc(@PathVariable Long id, @RequestParam KycStatus status) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setKycStatus(status);
                    customerRepository.save(customer);
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieves all customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    })
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details", description = "Updates details of a specific customer by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        return customerRepository.findById(id)
                .map(customer -> {
                    if (customerDetails.getFirstName() != null) customer.setFirstName(customerDetails.getFirstName());
                    if (customerDetails.getLastName() != null) customer.setLastName(customerDetails.getLastName());
                    if (customerDetails.getEmail() != null) customer.setEmail(customerDetails.getEmail());
                    if (customerDetails.getPhone() != null) customer.setPhone(customerDetails.getPhone());
                    if (customerDetails.getIdentityNumber() != null) customer.setIdentityNumber(customerDetails.getIdentityNumber());
                    Customer updatedCustomer = customerRepository.save(customer);
                    return ResponseEntity.ok(updatedCustomer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer", description = "Deletes a customer by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customerRepository.delete(customer);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
