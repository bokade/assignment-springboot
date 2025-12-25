package com.example.utils;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class i11Utils {

    private i11Utils() {}

    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static final String MOBILE_PATTERN = "^[6-9][0-9]{9}$";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String EMAIL_PATTERN_PROD = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
}
