package hbp.mip.experiment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hbp.mip.algorithm.AnalysisRequestDTO;

@JsonDeserialize(using = ExperimentExecutionDTODeserializer.class)
public record ExperimentExecutionDTO(
        String name,
        AnalysisRequestDTO analysis) {
}
