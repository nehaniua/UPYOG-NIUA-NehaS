package org.egov.echallan.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchCriteria {

	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("ids")
	private List<String> ids;

	@JsonProperty("challanNo")
	private String challanNo;
	
	@JsonProperty("accountId")
	private String accountId;

	@JsonProperty("mobileNumber")
	private String mobileNumber;
	
	@JsonProperty("businessService")
	private String businessService;
	
	@JsonProperty("userIds")
	private List<String> userIds;
	
	@JsonProperty("offset")
	private Integer offset;

	@JsonProperty("limit")
	private Integer limit;
	
	@JsonProperty("status")
    private String status;
 
	@JsonProperty("receiptNumber")
	private String receiptNumber;
	
	@JsonProperty("offenceTypeName")
	private String offenceTypeName;
	
	@JsonProperty("offenceCategoryName")
	private String offenceCategoryName;
	
	@JsonProperty("offenceSubCategoryName")
	private String offenceSubCategoryName;
	
	public boolean isEmpty() {
        return (this.tenantId == null && this.ids == null  && this.mobileNumber == null 
        );
    }

}
