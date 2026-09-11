package hbp.mip.experiment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import hbp.mip.algorithm.AnalysisRequestSupport;

import java.io.IOException;

public class ExperimentExecutionDTODeserializer extends JsonDeserializer<ExperimentExecutionDTO> {

    @Override
    public ExperimentExecutionDTO deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        JsonNode analysis = node.hasNonNull("analysis") ? node.get("analysis") : node.get("algorithm");

        return new ExperimentExecutionDTO(
                node.hasNonNull("name") ? node.get("name").asText() : null,
                analysis == null ? null : AnalysisRequestSupport.fromStoredJson(analysis.toString()));
    }
}
