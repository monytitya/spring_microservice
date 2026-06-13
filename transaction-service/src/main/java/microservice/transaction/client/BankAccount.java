package microservice.transaction.client;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BankAccount {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String accountType;
    private BigDecimal balance;
    private String status;
}
