package com.trainning.ordersystem.service;


import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ReportService {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public byte[] generateBill(OrderDetailResponse order) throws Exception {

        InputStream inputStream = getClass()
                .getResourceAsStream("/reports/bills.jrxml");
        if (inputStream == null) {
            inputStream = getClass().getResourceAsStream("/reports/Bills.jrxml");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("Khong tim thay file mau bao cao /reports/bills.jrxml hoac /reports/Bills.jrxml");
        }

        JasperReport jasperReport =
                JasperCompileManager.compileReport(inputStream);

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("orderCode", order.getOrderCode());
        parameters.put("customerName", removeDiacritics(order.getCustomerName()));
        parameters.put("customerPhone", order.getCustomerPhone());
        parameters.put("totalAmount", order.getTotalAmount());

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(order.getItems());

        JasperPrint jasperPrint =
                JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        dataSource
                );

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private String removeDiacritics(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String result = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        // Xử lý thêm các ký tự đặc biệt tiếng Việt: đ, Đ
        result = result.replace('\u0111', 'd').replace('\u0110', 'D');
        return result;
    }
}
