package microservice.transaction.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionRequest {
    private String accountNumber;
    private BigDecimal amount;

    // Getters
    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    // Setters
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
