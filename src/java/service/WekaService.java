package service;

import dao.MlFeatureRow;
import model.PourSession;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class WekaService {

    private static final String CHAMPION_MODEL = "Logistics.model";

    private static final String[] MODEL_FILES = new String[]{
        "Logistics.model",
        "NaivesBayes.model",
        "RandomTree.model",
        "Hoeffding.model"
    };

    /**
     * Định nghĩa cấu trúc dữ liệu Instances tương thích chính xác với View
     * PourSession_ML_Features
     */
    private Instances dataStructure;
    private final Map<String, Classifier> loadedModels = new LinkedHashMap<>();
    private boolean championLoaded = false;

    public WekaService(String webInfPath) {
        buildDataStructure();
        loadModels(webInfPath);
    }

    private void buildDataStructure() {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // Thuộc tính số học liên tục (Numeric) từ View
        attributes.add(new Attribute("actual_ml"));
        attributes.add(new Attribute("duration_s"));
        attributes.add(new Attribute("target_ml"));
        attributes.add(new Attribute("avg_flow"));
        attributes.add(new Attribute("avg_flow_sample"));
        attributes.add(new Attribute("peak_flow_sample"));
        attributes.add(new Attribute("min_flow_sample"));
        attributes.add(new Attribute("std_flow_sample"));
        attributes.add(new Attribute("flow_sample_count"));

        // Thuộc tính phân loại dạng chuỗi (Nominal)
        ArrayList<String> startReasons = new ArrayList<>();
        startReasons.add("MANUAL_BUTTON");
        startReasons.add("REMOTE_APP");
        attributes.add(new Attribute("start_reason", startReasons));

        ArrayList<String> stopReasons = new ArrayList<>();
        stopReasons.add("AUTO_PROFILE");
        stopReasons.add("MANUAL_BUTTON");
        stopReasons.add("REMOTE_APP");
        stopReasons.add("TANK_EMPTY");
        stopReasons.add("ERROR_ABORT");
        stopReasons.add("TIMEOUT_FAILSAFE");
        attributes.add(new Attribute("stop_reason", stopReasons));

        // Nhãn phân lớp mục tiêu dựa theo trường result_code của hệ thống
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("SUCCESS");
        classValues.add("UNDER_POUR");
        classValues.add("OVER_POUR");
        classValues.add("TIMEOUT");
        classValues.add("NO_CUP");
        classValues.add("ERROR");
        attributes.add(new Attribute("result_code", classValues));

        dataStructure = new Instances("PourSessionMLFeaturesRelation", attributes, 0);
        dataStructure.setClassIndex(attributes.size() - 1);
    }

    private void loadModels(String webInfPath) {
        for (String modelFile : MODEL_FILES) {
            try {
                String modelPath = resolveModelPath(webInfPath, modelFile);
                if (modelPath == null) {
                    System.err.println("WEKA: Model not found -> " + modelFile);
                    continue;
                }

                Classifier classifier = (Classifier) SerializationHelper.read(modelPath);
                loadedModels.put(modelFile, classifier);

                System.out.println("WEKA: Loaded model -> " + modelFile);

                if (CHAMPION_MODEL.equalsIgnoreCase(modelFile)) {
                    championLoaded = true;
                }
            } catch (Exception e) {
                System.err.println("WEKA: Failed to load model " + modelFile + " -> " + e.getMessage());
            }
        }

        if (!championLoaded) {
            System.err.println("WEKA: Champion model not loaded. Service will run in Simulation Mode.");
        }
    }

    private String resolveModelPath(String webInfPath, String fileName) {
        if (webInfPath == null || webInfPath.trim().isEmpty()) {
            return null;
        }
        if (webInfPath.endsWith(".model")) {
            webInfPath = webInfPath.substring(0, webInfPath.length() - 6);
        }

        File direct = new File(webInfPath, fileName);
        if (direct.exists() && direct.isFile()) {
            return direct.getAbsolutePath();
        }

        File underModels = new File(new File(webInfPath, "models"), fileName);
        if (underModels.exists() && underModels.isFile()) {
            return underModels.getAbsolutePath();
        }

        File underModel = new File(new File(webInfPath, "model"), fileName);
        if (underModel.exists() && underModel.isFile()) {
            return underModel.getAbsolutePath();
        }
        return null;
    }

    /**
     * Hàm chấm điểm rủi ro chính sử dụng DTO trích xuất từ View ML mới
     */
    public double analyzeFeaturesRisk(MlFeatureRow features) {
        if (!championLoaded || !loadedModels.containsKey(CHAMPION_MODEL)) {
            return simulateRiskFromFeatures(features);
        }

        try {
            Instance instance = new DenseInstance(dataStructure.numAttributes());
            instance.setDataset(dataStructure);

            // Mapping 9 trường dữ liệu liên tục
            instance.setValue(0, safe(features.getActualMl()));
            instance.setValue(1, safe(features.getDurationS()));
            instance.setValue(2, safe(features.getTargetMl()));
            instance.setValue(3, safe(features.getAvgFlow()));
            instance.setValue(4, safe(features.getAvgFlowSample()));
            instance.setValue(5, safe(features.getPeakFlowSample()));
            instance.setValue(6, safe(features.getMinFlowSample()));
            instance.setValue(7, safe(features.getStdFlowSample()));
            instance.setValue(8, safe(features.getFlowSampleCount()));

            // Mapping các trường nominal dạng chuỗi an toàn
            Attribute startReasonAttr = dataStructure.attribute("start_reason");
            if (features.getStartReason() != null && startReasonAttr.indexOfValue(features.getStartReason().trim().toUpperCase()) != -1) {
                instance.setValue(startReasonAttr, features.getStartReason().trim().toUpperCase());
            } else {
                instance.setMissing(startReasonAttr);
            }

            Attribute stopReasonAttr = dataStructure.attribute("stop_reason");
            if (features.getStopReason() != null && stopReasonAttr.indexOfValue(features.getStopReason().trim().toUpperCase()) != -1) {
                instance.setValue(stopReasonAttr, features.getStopReason().trim().toUpperCase());
            } else {
                instance.setMissing(stopReasonAttr);
            }

            instance.setMissing(dataStructure.classIndex());

            // Dự đoán phân phối xác suất lỗi
            double[] probabilities = loadedModels.get(CHAMPION_MODEL).distributionForInstance(instance);
            if (probabilities == null || probabilities.length == 0) {
                return 0.0;
            }

            // Tính tổng xác suất của các phân lớp bất thường (Bỏ qua index 0 - SUCCESS)
            double anomalyRisk = 0.0;
            for (int i = 1; i < probabilities.length; i++) {
                anomalyRisk += probabilities[i];
            }

            return clamp01(anomalyRisk);
        } catch (Exception e) {
            e.printStackTrace();
            return simulateRiskFromFeatures(features);
        }
    }

    private double simulateRiskFromFeatures(MlFeatureRow f) {
        if (f == null) {
            return 0.0;
        }
        if (f.getTargetMl() > 0 && f.getActualMl() > f.getTargetMl() * 1.15) {
            return 0.90;
        }
        if (f.getDurationS() > 25.0 && f.getActualMl() < 0.80 * f.getTargetMl()) {
            return 0.85;
        }
        if (f.getPeakFlowSample() > 65.0) {
            return 0.80;
        }
        return f.getResultCode() != null && !"SUCCESS".equalsIgnoreCase(f.getResultCode()) ? 0.70 : 0.15;
    }

    private double safe(double v) {
        return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : v;
    }

    private double clamp01(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v) || v < 0.0) {
            return 0.0;
        }
        return Math.min(v, 1.0);
    }

    // Giữ lại các hàm cũ tương thích ngược nếu các Controller khác vẫn gọi
    public double analyzeSessionRisk(PourSession session) {
        if (session == null) {
            return 0.0;
        }
        MlFeatureRow mock = new MlFeatureRow();
        mock.setTargetMl(session.getTargetMl());
        mock.setActualMl(session.getActualMl());
        mock.setDurationS(session.getDuration());
        mock.setAvgFlow(session.getAvgFlow());
        mock.setPeakFlowSample(session.getPeakFlow());
        return analyzeFeaturesRisk(mock);
    }
}
