package com.proj.webprojrct.payment;

import com.proj.webprojrct.order.repository.OrderRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;

    public VnpayDTO createPayment(VnPayBody body) {
        var vnpTxnRef = String.valueOf(System.currentTimeMillis());
        var vnpParams = buildParamVnPay(body, vnpTxnRef, VnpayUtils.VN_PAY_RETURN_URL);

        var cld = Calendar.getInstance(TimeZone.getTimeZone(VnpayUtils.TIME_ZONE_DEFAULT));
        var formatter = new SimpleDateFormat(VnpayUtils.DATE_FORMAT);
        var vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put(VnpayUtils.VNP_CREATE_DATE_KEY, vnpCreateDate);
        cld.add(Calendar.MINUTE, VnpayUtils.DEFAULT_TIME_END);

        var vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put(VnpayUtils.VNP_EXPIRE_DATE_KEY, vnpExpireDate);
        var fieldNames = vnpParams.keySet().stream().sorted().toList();
        var hashData = new StringBuilder();
        var query = new StringBuilder();
        var itr = fieldNames.iterator();
        while (itr.hasNext()) {
            var fieldName = itr.next();
            var fieldValue = vnpParams.get(fieldName);
            if (StringUtils.isNotBlank(fieldValue)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        var secureHash = VnpayUtils.hmacSHA512(VnpayUtils.VNP_SECRET_KEY_VALUE, hashData.toString());
        var paymentUrl = MessageFormat.format(
                "{0}?{1}&{2}={3}",
                VnpayUtils.VNP_PAY_URL,
                query,
                VnpayUtils.VNP_SECURE_HASH_TYPE_KEY,
                secureHash
        );
        return new VnpayDTO(0L, paymentUrl, vnpTxnRef);
    }

    public Map<String, Object> createConfirm(String vnpTxnRef, String transDate) {
        var vnpParams = buildParamQuery(vnpTxnRef, transDate);
        return Map.of("param", vnpParams, "url", VnpayUtils.VNP_QUERY_URL);
    }


    @NotNull
    private HashMap<String, String> buildParamVnPay(
            VnPayBody body,
            String vnpTxnRef,
            String vnpReturnUrl
    ) {
        var vnpParams = new HashMap<String, String>();
        vnpParams.put(VnpayUtils.VNP_VERSION_KEY, VnpayUtils.VNP_VERSION_VALUE);
        vnpParams.put(VnpayUtils.VNP_COMMAND_KEY, VnpayUtils.VNP_COMMAND_VALUE);
        vnpParams.put(VnpayUtils.VNP_TMN_CODE_KEY, VnpayUtils.VNP_TMN_CODE_VALUE);
        vnpParams.put(VnpayUtils.VNP_AMOUNT_KEY, String.valueOf(body.amountVnpPay()));
        vnpParams.put(VnpayUtils.VNP_CURR_CODE_KEY, VnpayUtils.VNP_CURR_CODE_VALUE);
        vnpParams.put(VnpayUtils.VNP_BANK_CODE_KEY, StringUtils.EMPTY);
        vnpParams.put(VnpayUtils.VNP_TXN_REF_KEY, vnpTxnRef);
        vnpParams.put(VnpayUtils.VNP_ORDER_INFO_KEY, body.orderInfo());
        vnpParams.put(VnpayUtils.VNP_RETURN_URL_KEY, vnpReturnUrl);
        vnpParams.put(VnpayUtils.VNP_IP_ADDR_KEY, ServerHelper.getClientIp());
        vnpParams.put(VnpayUtils.VNP_ORDER_TYPE_KEY, VnpayUtils.VNP_ORDER_TYPE_VALUE);
        vnpParams.put(VnpayUtils.VNP_LOCALE_KEY, VnpayUtils.VNP_LOCALE_VALUE);
        return vnpParams;
    }

    private HashMap<String, String> buildParamQuery(String vnpTxnRef, String transDate) {
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        var vnp_CreateDate = formatter.format(cld.getTime());
        var vnpParams = new HashMap<String, String>();
        var requestId = VnpayUtils.getRandomNumber(8);
        var vnIpAddr = ServerHelper.getClientIp();
        vnpParams.put(VnpayUtils.VNP_REQUEST_ID, requestId);
        vnpParams.put(VnpayUtils.VNP_VERSION_KEY, VnpayUtils.VNP_VERSION_VALUE);
        vnpParams.put(VnpayUtils.VNP_COMMAND_KEY, VnpayUtils.VNP_COMMAND_QUERY_VALUE);
        vnpParams.put(VnpayUtils.VNP_TMN_CODE_KEY, VnpayUtils.VNP_TMN_CODE_VALUE);
        vnpParams.put(VnpayUtils.VNP_TXN_REF_KEY, vnpTxnRef);
        vnpParams.put(VnpayUtils.VNP_ORDER_INFO_KEY, vnpTxnRef);
        vnpParams.put(VnpayUtils.VNP_TRANSACTION_DATE_KEY, transDate);
        vnpParams.put(VnpayUtils.VNP_CREATE_DATE_KEY, vnp_CreateDate);
        vnpParams.put(VnpayUtils.VNP_IP_ADDR_KEY, vnIpAddr);
        var hash_Data = String.join(
                "|",
                requestId,
                VnpayUtils.VNP_VERSION_VALUE,
                VnpayUtils.VNP_COMMAND_QUERY_VALUE,
                VnpayUtils.VNP_TMN_CODE_VALUE,
                vnpTxnRef,
                transDate,
                vnp_CreateDate,
                vnIpAddr,
                vnpTxnRef
        );
        var vnp_SecureHash = VnpayUtils.hmacSHA512(VnpayUtils.VNP_SECRET_KEY_VALUE, hash_Data);
        vnpParams.put(VnpayUtils.VNP_SECURE_HASH_TYPE_KEY, vnp_SecureHash);
        return vnpParams;
    }


    public record VnPayBody(Double amount, String orderInfo) {
        public long amountVnpPay() {
            return Math.round(amount * 100);  // remove decimal point
        }
    }

    public record QueryVnPayBody(String txnRef, String transDate) {
    }

    public HttpHeaders createHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
