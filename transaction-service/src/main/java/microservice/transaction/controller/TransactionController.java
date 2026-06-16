package microservice.transaction.controller;

import microservice.transaction.client.AccountServiceClient;
import microservice.transaction.client.BankAccount;
import microservice.transaction.dto.TransactionEvent;
import microservice.transaction.dto.TransactionRequest;
import microservice.transaction.dto.TransferRequest;
import microservice.transaction.entity.Transaction;
import microservice.transaction.entity.TransactionStatus;
import microservice.transaction.entity.TransactionType;
import microservice.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "transaction-events";

    @PostMapping("/transfer")
    @Transactional
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        String txId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setTransactionId(txId);
        transaction.setFromAccount(request.getFromAccountNumber());
        transaction.setToAccount(request.getToAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);

        try {
            // Verify from account and balance
            BankAccount fromAcc = accountServiceClient.getAccount(request.getFromAccountNumber());
            if (fromAcc == null) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Error: Source account not found!");
            }
            if (fromAcc.getBalance().compareTo(request.getAmount()) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Error: Insufficient funds!");
            }

            // Verify to account
            BankAccount toAcc = accountServiceClient.getAccount(request.getToAccountNumber());
            if (toAcc == null) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Error: Destination account not found!");
            }

            // Update balances via Feign
            accountServiceClient.updateBalance(request.getFromAccountNumber(), request.getAmount().negate());
            accountServiceClient.updateBalance(request.getToAccountNumber(), request.getAmount());

            // Save transaction
            transaction.setStatus(TransactionStatus.SUCCESS);
            Transaction savedTx = transactionRepository.save(transaction);

            // Publish event to Kafka
            TransactionEvent event = new TransactionEvent(
                    txId,
                    request.getFromAccountNumber(),
                    request.getToAccountNumber(),
                    request.getAmount(),
                    "TRANSFER",
                    "SUCCESS",
                    LocalDateTime.now().toString()
            );
            kafkaTemplate.send(TOPIC, event);

            return ResponseEntity.ok(savedTx);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Transaction Failed: " + e.getMessage());
        }
    }

    @PostMapping("/deposit")
    @Transactional
    public ResponseEntity<?> deposit(@RequestBody TransactionRequest request) {
        String txId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setTransactionId(txId);
        transaction.setToAccount(request.getAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.DEPOSIT);

        try {
            BankAccount acc = accountServiceClient.getAccount(request.getAccountNumber());
            if (acc == null) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Error: Account not found!");
            }

            accountServiceClient.updateBalance(request.getAccountNumber(), request.getAmount());

            transaction.setStatus(TransactionStatus.SUCCESS);
            Transaction savedTx = transactionRepository.save(transaction);

            TransactionEvent event = new TransactionEvent(
                    txId,
                    null,
                    request.getAccountNumber(),
                    request.getAmount(),
                    "DEPOSIT",
                    "SUCCESS",
                    LocalDateTime.now().toString()
            );
            kafkaTemplate.send(TOPIC, event);

            return ResponseEntity.ok(savedTx);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Deposit Failed: " + e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    @Transactional
    public ResponseEntity<?> withdraw(@RequestBody TransactionRequest request) {
        String txId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setTransactionId(txId);
        transaction.setFromAccount(request.getAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.WITHDRAWAL);

        try {
            BankAccount acc = accountServiceClient.getAccount(request.getAccountNumber());
            if (acc == null) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Error: Account not found!");
            }
            if (acc.getBalance().compareTo(request.getAmount()) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Error: Insufficient funds!");
            }

            accountServiceClient.updateBalance(request.getAccountNumber(), request.getAmount().negate());

            transaction.setStatus(TransactionStatus.SUCCESS);
            Transaction savedTx = transactionRepository.save(transaction);

            TransactionEvent event = new TransactionEvent(
                    txId,
                    request.getAccountNumber(),
                    null,
                    request.getAmount(),
                    "WITHDRAWAL",
                    "SUCCESS",
                    LocalDateTime.now().toString()
            );
            kafkaTemplate.send(TOPIC, event);

            return ResponseEntity.ok(savedTx);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Withdrawal Failed: " + e.getMessage());
        }
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable String accountNumber) {
        List<Transaction> history = transactionRepository.findByFromAccountOrToAccountOrderByTimestampDesc(accountNumber, accountNumber);
        return ResponseEntity.ok(history);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction txDetails) {
        return transactionRepository.findById(id)
                .map(tx -> {
                    if (txDetails.getAmount() != null) tx.setAmount(txDetails.getAmount());
                    if (txDetails.getType() != null) tx.setType(txDetails.getType());
                    if (txDetails.getStatus() != null) tx.setStatus(txDetails.getStatus());
                    if (txDetails.getFromAccount() != null) tx.setFromAccount(txDetails.getFromAccount());
                    if (txDetails.getToAccount() != null) tx.setToAccount(txDetails.getToAccount());
                    Transaction updatedTx = transactionRepository.save(tx);
                    return ResponseEntity.ok(updatedTx);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(tx -> {
                    transactionRepository.delete(tx);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
