package dropshipping_project.demo.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipOrderRequest {

    @NotBlank(message = "trackingNumber is required")
    private String trackingNumber;

    @NotBlank(message = "shippingCompany is required")
    private String shippingCompany;
}