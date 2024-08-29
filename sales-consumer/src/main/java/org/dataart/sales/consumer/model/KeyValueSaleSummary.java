package org.dataart.sales.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyValueSaleSummary {

  private String key;
  private SalesmanSalesSummary summary;
}
