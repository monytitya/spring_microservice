package microservice.customer.controller;

import microservice.customer.entity.Customer;
import microservice.customer.entity.KycStatus;
import microservice.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
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
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/kyc")
    public ResponseEntity<?> updateKyc(@PathVariable Long id, @RequestParam KycStatus status) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setKycStatus(status);
                    customerRepository.save(customer);
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
