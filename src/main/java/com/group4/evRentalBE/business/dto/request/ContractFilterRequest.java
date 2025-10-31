package     com.group4.evRentalBE.business.dto.request;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractFilterRequest {
    private Long stationId;
    private Long vehicleTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
}
