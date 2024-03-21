package org.dataart.sales.procesor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PipelineMetadata {

    private String host;
    private Set<String> partitions;
}
