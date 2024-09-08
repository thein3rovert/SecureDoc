package com.in3rovert_so.securedoc.utils;

import org.apache.catalina.util.StringUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class DocumentUtils {
    public static String getDocumentUri(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(String.format("/documents/%s", filename)).toUriString();
    }

    public static String setIcon(String fileExtension) {
        String extension = StringUtils.trimAllWhitespace(fileExtension);
        if(extension.equalsIgnoreCase("DOC") || extension.equalsIgnoreCase("DOCX")) {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/word-icon.svg";
        }
        if(extension.equalsIgnoreCase("XLS") || extension.equalsIgnoreCase("XLSX")) {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/excel-icon.svg"
        }
        if(extension.equalsIgnoreCase("PDF")) {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/pdf-icon.svg"
        } else {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/word-icon.svg";
        }
    }
}
