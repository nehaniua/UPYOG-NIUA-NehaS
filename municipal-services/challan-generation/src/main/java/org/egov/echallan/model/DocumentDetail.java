package org.egov.echallan.model;

import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.echallan.validator.CreateApplicationGroup;
import org.springframework.validation.annotation.Validated;


import javax.validation.constraints.NotBlank;

/**
 * Document details of uploaded documents
 */
@ApiModel(description = "Document details of uploaded documents")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-19T11:17:29.419+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentDetail {
	
	private String documentDetailId;
	
	private String challanId;

	@NotBlank(groups = CreateApplicationGroup.class)
	private String documentType;
	
	@NotBlank(groups = CreateApplicationGroup.class)
	private String fileStoreId;

	private Double latitude;

	private Double longitude;

	private AuditDetails auditDetails;

}
