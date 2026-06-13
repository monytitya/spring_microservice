package microservice.loan.controller;

import microservice.loan.client.Customer;
import microservice.loan.client.CustomerServiceClient;
import microservice.loan.entity.Loan;
import microservice.loan.entity.LoanStatus;
import microservice.loan.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLoan(@RequestBody Loan loan) {
        try {
            Customer customer = customerServiceClient.getCustomer(loan.getCustomerId());
            if (customer == null) {
                return ResponseEntity.badRequest().body("Error: Customer not found!");
            }
            if (!"APPROVED".equalsIgnoreCase(customer.getKycStatus())) {
                return ResponseEntity.badRequest().body("Error: Customer's KYC status is not APPROVED! Current status: " + customer.getKycStatus());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error communicating with Customer Service: " + e.getMessage());
        }

        loan.setStatus(LoanStatus.APPLIED);
        Loan savedLoan = loanRepository.save(loan);
        return ResponseEntity.ok(savedLoan);
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable Long loanId, @RequestParam LoanStatus status) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Loan loan = loanOpt.get();
        if (status != LoanStatus.APPROVED && status != LoanStatus.REJECTED) {
            return ResponseEntity.badRequest().body("Error: Status must be APPROVED or REJECTED!");
        }
        loan.setStatus(status);
        loanRepository.save(loan);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Loan>> getCustomerLoans(@PathVariable Long customerId) {
        List<Loan> loans = loanRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(loans);
    }
}
