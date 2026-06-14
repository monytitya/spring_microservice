package microservice.account.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import microservice.account.client.Customer;
import microservice.account.client.CustomerServiceClient;
import microservice.account.entity.BankAccount;
import microservice.account.repository.AccountRepository;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Bank Account Management", description = "APIs for managing bank accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @PostMapping
    @Operation(summary = "Create a new bank account", description = "Creates a new bank account for an approved customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or customer not approved")
    })
    public ResponseEntity<?> createAccount(@RequestBody BankAccount account) {
        try {
            Customer customer = customerServiceClient.getCustomer(account.getCustomerId());
            if (customer == null) {
                return ResponseEntity.badRequest().body("Error: Customer not found!");
            }
            if (!"APPROVED".equalsIgnoreCase(customer.getKycStatus())) {
                return ResponseEntity.badRequest().body(
                        "Error: Customer's KYC status is not APPROVED! Current status: " + customer.getKycStatus());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error communicating with Customer Service: " + e.getMessage());
        }

        // Generate a random unique 10-digit account number
        String accountNumber;
        Random random = new Random();
        do {
            long number = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        account.setAccountNumber(accountNumber);
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        BankAccount savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details", description = "Retrieves bank account details by account number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<BankAccount> getAccount(@PathVariable String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer accounts", description = "Retrieves all accounts for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    public ResponseEntity<List<BankAccount>> getCustomerAccounts(@PathVariable Long customerId) {
        List<BankAccount> accounts = accountRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{accountNumber}/balance")
    @Operation(summary = "Update account balance", description = "Updates the balance of a bank account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance updated successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<?> updateBalance(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        Optional<BankAccount> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BankAccount account = accountOpt.get();
        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Error: Insufficient funds in account!");
        }
        account.setBalance(newBalance);
        accountRepository.save(account);
        return ResponseEntity.ok(account);
    }
}
