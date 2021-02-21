package com.example.lbstest;

public class AddressBean {
    public static double longitude;//经度
    public static double latitude;//纬度
    private String title;//信息标题
    private String text;//信息内容
    public AddressBean(double lon, double lat, String title, String text){
        this.longitude = lon;
        this.latitude = lat;
        this.title = title;
        this.text = text;
    }
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public String getTitle() {
        return title;
    }
    public String getText(){
        return text;
    }
}