package lt.rimkus.paymentService.utilities;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IpAddressUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (StringUtils.hasText(ipList) && !"unknown".equalsIgnoreCase(ipList)) {
                // Get the first IP if there are multiple (comma-separated)
                String ip = ipList.split(",")[0].trim();
                if (isValidIpAddress(ip)) {
                    return ip;
                }
            }
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    private static boolean isValidIpAddress(String ip) {
        return StringUtils.hasText(ip) &&
                !"unknown".equalsIgnoreCase(ip) &&
                !"0:0:0:0:0:0:0:1".equals(ip);
    }
}
