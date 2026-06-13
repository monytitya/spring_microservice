package microservice.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", url = "http://localhost:8082") // Fallback url if not running Eureka
public interface AccountServiceClient {

    @GetMapping("/api/accounts/{accountNumber}")
    BankAccount getAccount(@PathVariable("accountNumber") String accountNumber);

    @PutMapping("/api/accounts/{accountNumber}/balance")
    void updateBalance(@PathVariable("accountNumber") String accountNumber, @RequestParam("amount") BigDecimal amount);
}
