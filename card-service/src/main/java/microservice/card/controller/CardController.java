package microservice.card.controller;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import microservice.card.client.Customer;
import microservice.card.client.CustomerServiceClient;
import microservice.card.entity.Card;
import microservice.card.repository.CardRepository;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody Card card) {
        try {
            Customer customer = customerServiceClient.getCustomer(card.getCustomerId());
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

        // Generate a random unique 16-digit card number
        String cardNumber;
        Random random = new Random();
        do {
            long number1 = 10000000L + (long) (random.nextDouble() * 90000000L);
            long number2 = 10000000L + (long) (random.nextDouble() * 90000000L);
            cardNumber = String.valueOf(number1) + String.valueOf(number2);
        } while (cardRepository.findByCardNumber(cardNumber).isPresent());

        card.setCardNumber(cardNumber);
        Card savedCard = cardRepository.save(card);
        return ResponseEntity.ok(savedCard);
    }

    @GetMapping
    public ResponseEntity<List<Card>> getAllCards() {
        return ResponseEntity.ok(cardRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getCardById(@PathVariable Long id) {
        return cardRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Card>> getCardsByCustomerId(@PathVariable Long customerId) {
        List<Card> cards = cardRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Card> updateCard(@PathVariable Long id, @RequestBody Card cardDetails) {
        return cardRepository.findById(id)
                .map(card -> {
                    if (cardDetails.getCardType() != null) card.setCardType(cardDetails.getCardType());
                    if (cardDetails.getStatus() != null) card.setStatus(cardDetails.getStatus());
                    if (cardDetails.getCreditLimit() != null) card.setCreditLimit(cardDetails.getCreditLimit());
                    Card updatedCard = cardRepository.save(card);
                    return ResponseEntity.ok(updatedCard);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        return cardRepository.findById(id)
                .map(card -> {
                    cardRepository.delete(card);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
