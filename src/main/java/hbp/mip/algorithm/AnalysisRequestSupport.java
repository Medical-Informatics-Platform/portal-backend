package hbp.mip.algorithm;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

public final class AnalysisRequestSupport {

    private static final Gson gson = new Gson();

    private AnalysisRequestSupport() {
    }

    public static AnalysisRequestDTO fromStoredJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        JsonElement root = gson.fromJson(json, JsonElement.class);
        if (root == null || !root.isJsonObject()) {
            return null;
        }

        JsonElement algorithm = root.getAsJsonObject().get("algorithm");
        return algorithm != null && algorithm.isJsonObject()
                ? gson.fromJson(root, AnalysisRequestDTO.class)
                : fromLegacyAlgorithmExecution(gson.fromJson(root, LegacyAlgorithmExecutionDTO.class));
    }

    private static AnalysisRequestDTO fromLegacyAlgorithmExecution(LegacyAlgorithmExecutionDTO legacy) {
        if (legacy == null) {
            return null;
        }

        LegacyInputDataDTO inputdata = legacy.inputdata();
        AnalysisRequestDTO.AnalysisInputDataDTO data = null;
        List<String> x = null;
        List<String> y = null;
        if (inputdata != null) {
            data = new AnalysisRequestDTO.AnalysisInputDataDTO(
                    inputdata.data_model(),
                    inputdata.datasets(),
                    inputdata.validation_datasets(),
                    inputdata.filters(),
                    null);
            x = inputdata.x();
            y = inputdata.y();
        }

        return new AnalysisRequestDTO(
                null,
                data,
                toPreprocessingSteps(legacy.preprocessing()),
                new AnalysisRequestDTO.AnalysisAlgorithmDTO(legacy.name(), x, y, legacy.parameters()),
                null);
    }

    private static List<AnalysisRequestDTO.AnalysisPreprocessingStepDTO> toPreprocessingSteps(
            Map<String, Map<String, Object>> preprocessing) {
        if (preprocessing == null) {
            return null;
        }

        return preprocessing.entrySet().stream()
                .map(entry -> new AnalysisRequestDTO.AnalysisPreprocessingStepDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    record LegacyAlgorithmExecutionDTO(
            String name,
            LegacyInputDataDTO inputdata,
            Map<String, Object> parameters,
            Map<String, Map<String, Object>> preprocessing) {
    }

    record LegacyInputDataDTO(
            String data_model,
            List<String> datasets,
            List<String> x,
            List<String> y,
            List<String> validation_datasets,
            Map<String, Object> filters) {
    }
}
