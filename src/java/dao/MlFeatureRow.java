package dao;

public class MlFeatureRow {

    private double actualMl;
    private double durationS;
    private double targetMl;
    private double avgFlow;
    private double avgFlowSample;
    private double peakFlowSample;
    private double minFlowSample;
    private double stdFlowSample;
    private int flowSampleCount;
    private String startReason;
    private String stopReason;
    private String resultCode;

    // Getters and Setters
    public double getActualMl() {
        return actualMl;
    }

    public void setActualMl(double actualMl) {
        this.actualMl = actualMl;
    }

    public double getDurationS() {
        return durationS;
    }

    public void setDurationS(double durationS) {
        this.durationS = durationS;
    }

    public double getTargetMl() {
        return targetMl;
    }

    public void setTargetMl(double targetMl) {
        this.targetMl = targetMl;
    }

    public double getAvgFlow() {
        return avgFlow;
    }

    public void setAvgFlow(double avgFlow) {
        this.avgFlow = avgFlow;
    }

    public double getAvgFlowSample() {
        return avgFlowSample;
    }

    public void setAvgFlowSample(double avgFlowSample) {
        this.avgFlowSample = avgFlowSample;
    }

    public double getPeakFlowSample() {
        return peakFlowSample;
    }

    public void setPeakFlowSample(double peakFlowSample) {
        this.peakFlowSample = peakFlowSample;
    }

    public double getMinFlowSample() {
        return minFlowSample;
    }

    public void setMinFlowSample(double minFlowSample) {
        this.minFlowSample = minFlowSample;
    }

    public double getStdFlowSample() {
        return stdFlowSample;
    }

    public void setStdFlowSample(double stdFlowSample) {
        this.stdFlowSample = stdFlowSample;
    }

    public int getFlowSampleCount() {
        return flowSampleCount;
    }

    public void setFlowSampleCount(int flowSampleCount) {
        this.flowSampleCount = flowSampleCount;
    }

    public String getStartReason() {
        return startReason;
    }

    public void setStartReason(String startReason) {
        this.startReason = startReason;
    }

    public String getStopReason() {
        return stopReason;
    }

    public void setStopReason(String stopReason) {
        this.stopReason = stopReason;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
