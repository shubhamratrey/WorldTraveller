package com.example.shubh.worldtraveller.Worldtraveller.models;

/**
 * Created by Shubh on 06/04/2018.
 */

public class Camera  {
    private String camera_name;
    private String manufacturer;
    private String camera_lens;
    private String iso;
    private String shutter_speed;
    private String flash;
    private String aperture;
    private String focal_length;

    public Camera() {};

    public Camera(String camera_name, String manufacturer, String camera_lens, String iso, String shutter_speed, String flash, String aperture, String focal_length) {
        this.camera_name = camera_name;
        this.manufacturer = manufacturer;
        this.camera_lens = camera_lens;
        this.iso = iso;
        this.shutter_speed = shutter_speed;
        this.flash = flash;
        this.aperture = aperture;
        this.focal_length = focal_length;
    }

    public String getCamera_name() {
        return camera_name;
    }

    public void setCamera_name(String camera_name) {
        this.camera_name = camera_name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCamera_lens() {
        return camera_lens;
    }

    public void setCamera_lens(String camera_lens) {
        this.camera_lens = camera_lens;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getShutter_speed() {
        return shutter_speed;
    }

    public void setShutter_speed(String shutter_speed) {
        this.shutter_speed = shutter_speed;
    }

    public String getFlash() {
        return flash;
    }

    public void setFlash(String flash) {
        this.flash = flash;
    }

    public String getAperture() {
        return aperture;
    }

    public void setAperture(String aperture) {
        this.aperture = aperture;
    }

    public String getFocal_length() {
        return focal_length;
    }

    public void setFocal_length(String focal_length) {
        this.focal_length = focal_length;
    }
}
