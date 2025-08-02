package com.hotelbooking.Config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationInfo {
    private String ip;
    private String country;
    private String region;
    private String city;
    private String org;
    private String timezone;
}
