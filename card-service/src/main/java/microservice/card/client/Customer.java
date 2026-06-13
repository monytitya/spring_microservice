package microservice.card.client;

import lombok.Data;

@Data
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String identityNumber;
    private String kycStatus;
}
