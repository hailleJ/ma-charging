package com.partner.macharging;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ChargingExample {


    RestTemplate restTemplate = new RestTemplate();



    //this should be your subscriber list.
    List<String> msisdns = List.of("251936047430", "251914460985", "251910453900");


    String spId = "";
    String password = "";
    String timeStamp = "20231018021025";
    String serviceId = "";

    String price = "2";

    String spPassword = getMd5(spId + password + timeStamp);

    String url = "http://10.175.206.42/soap-payment-api/ws/AmountChargingService/services/chargeAmount";


    //@postConstruct will automatically run on start up
    @PostConstruct
    public void startCharging() {


        for (String msisdn : msisdns) {
            log.info("====================================");
            try {
                log.info("trying::{}",msisdn);
                ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(getPaymentBody(msisdn)), String.class);
                if (Objects.requireNonNull(response.getBody()).contains("ns1:chargeAmountResponse")
                        || response.getBody().contains("22007201")) {
                        log.info("response::{}",response);
                }
            } catch (Exception exception) {
                log.error("response::{}",exception.getMessage());

            }
            log.info("====================================");

        }
    }


    public String getPaymentBody(String msisdn) {


        return "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'" +
                " xmlns:loc='http://www.csapi.org/schema/parlayx/payment/amount_charging/v3_1/local' ><soapenv:Header>" +
                "<tns:RequestSOAPHeader xmlns:tns='http://www.huawei.com.cn/schema/common/v2_1'>" +
                "<tns:spId>" + spId + "</tns:spId>" +
                "<tns:spPassword>" + spPassword + "</tns:spPassword>" +
                "<tns:timeStamp>" + timeStamp + "</tns:timeStamp>" +
                "<tns:serviceId>" + serviceId + "</tns:serviceId>" +
                "<tns:OA>" + msisdn + "</tns:OA>" +
                "<tns:FA>" + msisdn + "</tns:FA>" +
                "</tns:RequestSOAPHeader> </soapenv:Header>" +
                "<soapenv:Body><loc:chargeAmount>" +
                "<loc:endUserIdentifier>" + msisdn + "</loc:endUserIdentifier>" +
                "<loc:charge>" +
                "<description>charged</description>" +
                "<currency>Birr</currency>" +
                "<amount>" + price + "00</amount>" +
                "<code>255</code>" +
                "</loc:charge>" +
                "<loc:referenceCode>225</loc:referenceCode>" +
                "</loc:chargeAmount></soapenv:Body></soapenv:Envelope>";
    }
    public static String getMd5(String input) {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            // of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
