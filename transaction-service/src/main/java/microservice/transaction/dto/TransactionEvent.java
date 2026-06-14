package microservice.transaction.dto;

import java.math.BigDecimal;

public class TransactionEvent {

    // ─── Fields ────────────────────────────────────────────
    private String transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String type;
    private String status;
    private String timestamp;

    // ─── Constructors ───────────────────────────────────────

    // No Args Constructor
    public TransactionEvent() {
    }

    // All Args Constructor
    public TransactionEvent(
            String transactionId,
            String fromAccountNumber,
            String toAccountNumber,
            BigDecimal amount,
            String type,
            String status,
            String timestamp) {
        this.transactionId = transactionId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    // ─── Getters ────────────────────────────────────────────

    public String getTransactionId() {
        return transactionId;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // ─── Setters ────────────────────────────────────────────

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // ─── ToString ───────────────────────────────────────────

    @Override
    public String toString() {
        return "TransactionEvent{" +
                "transactionId='" + transactionId + '\'' +
                ", fromAccountNumber='" + fromAccountNumber + '\'' +
                ", toAccountNumber='" + toAccountNumber + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    // ─── Equals & HashCode ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TransactionEvent))
            return false;
        TransactionEvent that = (TransactionEvent) o;
        return java.util.Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(transactionId);
    }

    // ─── Builder Pattern ────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String transactionId;
        private String fromAccountNumber;
        private String toAccountNumber;
        private BigDecimal amount;
        private String type;
        private String status;
        private String timestamp;

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder fromAccountNumber(String fromAccountNumber) {
            this.fromAccountNumber = fromAccountNumber;
            return this;
        }

        public Builder toAccountNumber(String toAccountNumber) {
            this.toAccountNumber = toAccountNumber;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TransactionEvent build() {
            return new TransactionEvent(
                    transactionId,
                    fromAccountNumber,
                    toAccountNumber,
                    amount,
                    type,
                    status,
                    timestamp);
        }
    }
}