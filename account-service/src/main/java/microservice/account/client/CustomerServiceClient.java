package microservice.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "http://localhost:8081") // Fallback url if not running Eureka/Ribbon
public interface CustomerServiceClient {
    
    @GetMapping("/api/customers/{id}")
    Customer getCustomer(@PathVariable("id") Long id);
}
