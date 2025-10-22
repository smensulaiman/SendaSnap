package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Vehicle implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("serialNumber")
    private String serialNumber;

    @SerializedName("make")
    private String make;

    @SerializedName("model")
    private String model;

    @SerializedName("chassisModel")
    private String chassisModel;

    @SerializedName("cc")
    private String cc;

    @SerializedName("year")
    private String year;

    @SerializedName("color")
    private String color;

    @SerializedName("vehicleBuyDate")
    private String vehicleBuyDate;

    @SerializedName("auctionShipNumber")
    private String auctionShipNumber;

    @SerializedName("netWeight")
    private String netWeight;

    @SerializedName("area")
    private String area;

    @SerializedName("length")
    private String length;

    @SerializedName("width")
    private String width;

    @SerializedName("height")
    private String height;

    @SerializedName("plateNumber")
    private String plateNumber;

    @SerializedName("buyingPrice")
    private String buyingPrice;

    @SerializedName("expectedYardDate")
    private String expectedYardDate;

    @SerializedName("riksoFrom")
    private String riksoFrom;

    @SerializedName("riksoTo")
    private String riksoTo;

    @SerializedName("riksoCost")
    private String riksoCost;

    @SerializedName("riksoCompany")
    private String riksoCompany;

    @SerializedName("vehiclePhotos")
    private List<String> vehiclePhotos;

    @SerializedName("auctionSheet")
    private String auctionSheet;

    @SerializedName("tohonCopy")
    private String tohonCopy;

    @SerializedName("consigneeDetails")
    private List<ConsigneeDetail> consigneeDetails;

    // Constructor
    public Vehicle() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getChassisModel() {
        return chassisModel;
    }

    public void setChassisModel(String chassisModel) {
        this.chassisModel = chassisModel;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getVehicleBuyDate() {
        return vehicleBuyDate;
    }

    public void setVehicleBuyDate(String vehicleBuyDate) {
        this.vehicleBuyDate = vehicleBuyDate;
    }

    public String getAuctionShipNumber() {
        return auctionShipNumber;
    }

    public void setAuctionShipNumber(String auctionShipNumber) {
        this.auctionShipNumber = auctionShipNumber;
    }

    public String getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(String netWeight) {
        this.netWeight = netWeight;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(String buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public String getExpectedYardDate() {
        return expectedYardDate;
    }

    public void setExpectedYardDate(String expectedYardDate) {
        this.expectedYardDate = expectedYardDate;
    }

    public String getRiksoFrom() {
        return riksoFrom;
    }

    public void setRiksoFrom(String riksoFrom) {
        this.riksoFrom = riksoFrom;
    }

    public String getRiksoTo() {
        return riksoTo;
    }

    public void setRiksoTo(String riksoTo) {
        this.riksoTo = riksoTo;
    }

    public String getRiksoCost() {
        return riksoCost;
    }

    public void setRiksoCost(String riksoCost) {
        this.riksoCost = riksoCost;
    }

    public String getRiksoCompany() {
        return riksoCompany;
    }

    public void setRiksoCompany(String riksoCompany) {
        this.riksoCompany = riksoCompany;
    }

    public List<String> getVehiclePhotos() {
        return vehiclePhotos;
    }

    public void setVehiclePhotos(List<String> vehiclePhotos) {
        this.vehiclePhotos = vehiclePhotos;
    }

    public String getAuctionSheet() {
        return auctionSheet;
    }

    public void setAuctionSheet(String auctionSheet) {
        this.auctionSheet = auctionSheet;
    }

    public String getTohonCopy() {
        return tohonCopy;
    }

    public void setTohonCopy(String tohonCopy) {
        this.tohonCopy = tohonCopy;
    }

    public List<ConsigneeDetail> getConsigneeDetails() {
        return consigneeDetails;
    }

    public void setConsigneeDetails(List<ConsigneeDetail> consigneeDetails) {
        this.consigneeDetails = consigneeDetails;
    }

    // Helper methods
    public String getDisplayName() {
        return make + " " + model + " (" + year + ")";
    }

    public String getDimensions() {
        return length + " x " + width + " x " + height;
    }
}
