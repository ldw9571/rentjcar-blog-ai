package com.rentjcar.blogai.dto;

public class BlogRequestDto {

    private String carModel;       // 차량 모델명
    private String monthlyFee;     // 월 렌탈료
    private String contractPeriod; // 계약 기간
    private String deposit;        // 보증금
    private String mileage;        // 약정 주행거리
    private String insuranceType;  // 보험 종류
    private String extraOptions;   // 추가 옵션 (선택)

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(String monthlyFee) { this.monthlyFee = monthlyFee; }

    public String getContractPeriod() { return contractPeriod; }
    public void setContractPeriod(String contractPeriod) { this.contractPeriod = contractPeriod; }

    public String getDeposit() { return deposit; }
    public void setDeposit(String deposit) { this.deposit = deposit; }

    public String getMileage() { return mileage; }
    public void setMileage(String mileage) { this.mileage = mileage; }

    public String getInsuranceType() { return insuranceType; }
    public void setInsuranceType(String insuranceType) { this.insuranceType = insuranceType; }

    public String getExtraOptions() { return extraOptions; }
    public void setExtraOptions(String extraOptions) { this.extraOptions = extraOptions; }
}