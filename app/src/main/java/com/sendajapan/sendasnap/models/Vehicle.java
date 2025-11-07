package com.sendajapan.sendasnap.models;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Vehicle implements Serializable {

    @SerializedName(value = "id", alternate = { "vehicle_id" })
    private String id;

    @SerializedName(value = "serialNumber", alternate = { "chassis_number" })
    private String serialNumber;

    @SerializedName("make")
    private String make;

    @SerializedName("model")
    private String model;

    @SerializedName("chassisModel")
    private String chassisModel;

    @SerializedName(value = "cc", alternate = { "veh_cc" })
    private String cc;

    @SerializedName(value = "year", alternate = { "veh_year" })
    private String year;

    @SerializedName(value = "color", alternate = { "veh_color" })
    private String color;

    @SerializedName(value = "vehicleBuyDate", alternate = { "veh_buy_date" })
    private String vehicleBuyDate;

    @SerializedName(value = "auctionShipNumber", alternate = { "veh_auc_ship_number" })
    private String auctionShipNumber;

    @SerializedName(value = "netWeight", alternate = { "veh_net_weight" })
    private String netWeight;

    @SerializedName("area")
    private String area;

    @SerializedName(value = "length", alternate = { "veh_l" })
    private String length;

    @SerializedName(value = "width", alternate = { "veh_w" })
    private String width;

    @SerializedName(value = "height", alternate = { "veh_h" })
    private String height;

    @SerializedName("plateNumber")
    private String plateNumber;

    @SerializedName(value = "buyingPrice", alternate = { "veh_buy_price" })
    private String buyingPrice;

    @SerializedName(value = "expectedYardDate", alternate = { "yard_date_in" })
    private String expectedYardDate;

    @SerializedName(value = "riksoFrom", alternate = { "rikso_from_place_id" })
    private String riksoFrom;

    @SerializedName(value = "riksoTo", alternate = { "rikso_to_place_id" })
    private String riksoTo;

    @SerializedName(value = "riksoCost", alternate = { "rikso_cost" })
    private String riksoCost;

    @SerializedName(value = "riksoCompany", alternate = { "rikso_company" })
    private String riksoCompany;

    @SerializedName("images")
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

    // Custom setter to handle vehicle_id as integer from API
    public void setVehicleId(Object vehicleId) {
        if (vehicleId != null) {
            this.id = String.valueOf(vehicleId);
        }
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
        return TextUtils.isEmpty(chassisModel) ? "N/A" : chassisModel;
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
        return make + " " + " (" + year + ")";
    }

    public String getDimensions() {
        return length + " x " + width + " x " + height;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", chassisModel='" + chassisModel + '\'' +
                ", cc='" + cc + '\'' +
                ", year='" + year + '\'' +
                ", color='" + color + '\'' +
                ", vehicleBuyDate='" + vehicleBuyDate + '\'' +
                ", auctionShipNumber='" + auctionShipNumber + '\'' +
                ", netWeight='" + netWeight + '\'' +
                ", area='" + area + '\'' +
                ", length='" + length + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                ", plateNumber='" + plateNumber + '\'' +
                ", buyingPrice='" + buyingPrice + '\'' +
                ", expectedYardDate='" + expectedYardDate + '\'' +
                ", riksoFrom='" + riksoFrom + '\'' +
                ", riksoTo='" + riksoTo + '\'' +
                ", riksoCost='" + riksoCost + '\'' +
                ", riksoCompany='" + riksoCompany + '\'' +
                ", vehiclePhotos=" + vehiclePhotos +
                ", auctionSheet='" + auctionSheet + '\'' +
                ", tohonCopy='" + tohonCopy + '\'' +
                ", consigneeDetails=" + consigneeDetails +
                '}';
    }
}
