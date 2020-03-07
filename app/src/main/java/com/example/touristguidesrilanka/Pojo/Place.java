package com.example.touristguidesrilanka.Pojo;

public class Place {

    private Integer imgPlace;
    private String placeName;
    private String PlaceAddress;
    private String placeDetails;
    private Double latitude = 0.0;
    private Double longitude = 0.0;

    public Place(Integer imgPlace, String placeName, String placeAddress, String placeDetails, Double latitude, Double longitude) {
        this.imgPlace = imgPlace;
        this.placeName = placeName;
        PlaceAddress = placeAddress;
        this.placeDetails = placeDetails;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Place(Integer imgPlace, String placeName, String placeAddress) {
        this.imgPlace = imgPlace;
        this.placeName = placeName;
        PlaceAddress = placeAddress;
    }

    public Integer getImgPlace() {
        return imgPlace;
    }

    public void setImgPlace(Integer imgPlace) {
        this.imgPlace = imgPlace;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return PlaceAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        PlaceAddress = placeAddress;
    }

    public String getPlaceDetails() {
        return placeDetails;
    }

    public void setPlaceDetails(String placeDetails) {
        this.placeDetails = placeDetails;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
