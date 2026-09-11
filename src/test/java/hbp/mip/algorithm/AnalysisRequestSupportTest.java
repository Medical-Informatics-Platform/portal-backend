package hbp.mip.algorithm;

import com.fasterxml.jackson.databind.ObjectMapper;
import hbp.mip.experiment.ExperimentExecutionDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisRequestSupportTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fromStoredJson_deserializesNewAnalysisShape() {
        String payload = """
                {
                  "request_id": "11111111-1111-1111-1111-111111111111",
                  "inputdata": {
                    "data_model": "dm:1",
                    "datasets": ["ds1"],
                    "variables": ["age"]
                  },
                  "preprocessing": [
                    { "name": "winsorize", "parameters": { "limit": 0.05 } }
                  ],
                  "algorithm": {
                    "name": "histogram",
                    "y": ["age"],
                    "parameters": {}
                  }
                }
                """;

        AnalysisRequestDTO analysis = AnalysisRequestSupport.fromStoredJson(payload);

        assertThat(analysis.algorithm().name()).isEqualTo("histogram");
        assertThat(analysis.preprocessing()).hasSize(1);
        assertThat(analysis.inputdata().datasets()).containsExactly("ds1");
    }

    @Test
    void fromStoredJson_convertsLegacyAlgorithmExecutionShape() {
        String payload = """
                {
                  "name": "histogram",
                  "inputdata": {
                    "data_model": "dm:1",
                    "datasets": ["ds1"],
                    "x": ["sex"],
                    "y": ["age"],
                    "filters": { "condition": "AND", "rules": [] }
                  },
                  "parameters": { "bins": 10 },
                  "preprocessing": {
                    "winsorize": { "limit": 0.05 }
                  }
                }
                """;

        AnalysisRequestDTO analysis = AnalysisRequestSupport.fromStoredJson(payload);

        assertThat(analysis.algorithm().name()).isEqualTo("histogram");
        assertThat(analysis.algorithm().x()).containsExactly("sex");
        assertThat(analysis.algorithm().y()).containsExactly("age");
        assertThat(analysis.algorithm().parameters()).containsEntry("bins", 10.0);
        assertThat(analysis.preprocessing()).extracting("name").containsExactly("winsorize");
        assertThat(analysis.inputdata().filters()).containsEntry("condition", "AND");
    }

    @Test
    void experimentExecutionDeserializer_acceptsLegacyAlgorithmField() throws Exception {
        String payload = """
                {
                  "name": "legacy experiment",
                  "algorithm": {
                    "name": "histogram",
                    "inputdata": {
                      "data_model": "dm:1",
                      "datasets": ["ds1"],
                      "y": ["age"]
                    },
                    "parameters": {}
                  }
                }
                """;

        ExperimentExecutionDTO execution = objectMapper.readValue(payload, ExperimentExecutionDTO.class);

        assertThat(execution.analysis()).isNotNull();
        assertThat(execution.analysis().algorithm().name()).isEqualTo("histogram");
        assertThat(execution.analysis().inputdata().datasets()).containsExactly("ds1");
    }
}
