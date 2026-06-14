package microservice.card.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "expiry_date", nullable = false)
    private String expiryDate;

    @Column(nullable = false)
    private String cvv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.INACTIVE;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    // Getters
    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public CardType getCardType() {
        return cardType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public CardStatus getStatus() {
        return status;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
