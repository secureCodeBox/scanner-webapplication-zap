package io.securecodebox.zap.zap.api;

import io.securecodebox.zap.zap.model.Scans;
import io.securecodebox.zap.zap.model.Status;
import io.securecodebox.zap.zap.model.ZapApiCall;
import io.securecodebox.zap.zap.model.scanner.Alerts;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class ZapScannerApi extends ZapBaseApi {

    private static final Logger LOG = LoggerFactory.getLogger(ZapScannerApi.class);

    /**
     * Starts the OWASP Zap scanner without authentication.
     */
    public int startScanner(String targetUrl, boolean inScopeOnly) {
        Map<String, String> parameters = new HashMap<>(3);
        parameters.put("url", targetUrl);
        parameters.put("recurse", "true");
        parameters.put("inScopeOnly", String.valueOf(inScopeOnly));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(ScannerApi.START_SCANNER, parameters), String.class, parameters);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.debug("Successfully started ZAP scan for '{}' and  with inScopeOnly '{}'.", targetUrl, inScopeOnly);
            Map<String, Object> map = convertJsonStringToMap(response);
            return Integer.parseInt(map.get("scan").toString());
        } else {
            throw new IllegalStateException(String.format("Couldn't start ZAP scan for '%s' and  with inScopeOnly '%s', failed with response '%s' and HTTP-Code '%s'", targetUrl, inScopeOnly, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Starts OWASP Zap scanner as (authenticated) user.
     */
    public int startScannerAsUser(int contextId, String targetUrl, int userId) {
        Map<String, String> parameters = new HashMap<>(4);
        parameters.put("url", targetUrl);
        parameters.put("contextId", String.valueOf(contextId));
        parameters.put("userId", String.valueOf(userId));
        parameters.put("recurse", "true");

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(ScannerApi.START_SCANNER_AS_USER, parameters), String.class, parameters);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.debug("Successfully started ZAP scan for '{}' with context '{}'.", targetUrl, contextId);
            Map<String, Object> map = convertJsonStringToMap(response);
            return Integer.parseInt(map.get("scanAsUser").toString());

        } else {
            throw new IllegalStateException(String.format("Couldn't start ZAP scan for '%s' with context '%s', failed with response '%s' and HTTP-Code '%s'", targetUrl, contextId, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Gets the scan alert results (without false/positive handling and special filtering).
     */
    public Alerts getScanResults(String host) {
        return zapApiCall(ScannerApi.VIEW_SCAN_ALERTS, "baseurl", host, Alerts.class);
    }

    /**
     * Returns a list of running and finished scans.
     */
    public Scans getScans() {
        return zapApiCall(ScannerApi.VIEW_SCAN_LIST, Scans.class);
    }

    public Status getStatus(int scanId) {
        return zapApiCall(ScannerApi.VIEW_SCAN_STATUS, "scanId", String.valueOf(scanId), Status.class);
    }

    public void optionEnableAllScanners() {
        zapApiCall(ScannerApi.OPTION_ENABLE_ALL_SCANNER);
    }

    public void optionHandleAntiCfrs(boolean enable) {
        zapApiCall(ScannerApi.OPTION_HANDLE_ANTI_CFRS, "Boolean", String.valueOf(enable));
    }

    public void stopScanner(int scanId) {
        zapApiCall(ScannerApi.STOP_SCANNER, "scanId", String.valueOf(scanId));
    }

    public void stopAllScanners() {
        zapApiCall(ScannerApi.STOP_ALL_SCANNER);
    }

    public void removeScanner(int scanId) {
        zapApiCall(ScannerApi.REMOVE_SCANNER, "scanId", String.valueOf(scanId));
    }

    public void removeAllScanners() {
        zapApiCall(ScannerApi.REMOVE_ALL_SCANNER);
    }

    private enum ScannerApi implements ZapApiCall {
        VIEW_SCAN_LIST{
            @Override
            public String getUrl() {
                return "ascan/view/scans/";
            }
        },
        VIEW_SCAN_STATUS{
            @Override
            public String getUrl() {
                return "ascan/view/status/";
            }
        },
        VIEW_SCAN_ALERTS{
            @Override
            public String getUrl() {
                return "core/view/alerts";
            }
        },
        OPTION_ENABLE_ALL_SCANNER{
            @Override
            public String getUrl() {
                return "ascan/action/enableAllScanners/";
            }
        },
        OPTION_HANDLE_ANTI_CFRS{
            @Override
            public String getUrl() {
                return "ascan/action/setOptionHandleAntiCSRFTokens/";
            }
        },
        START_SCANNER{
            @Override
            public String getUrl() {
                return "ascan/action/scan/";
            }
        },
        START_SCANNER_AS_USER{
            @Override
            public String getUrl() {
                return "ascan/action/scanAsUser/";
            }
        },
        STOP_SCANNER{
            @Override
            public String getUrl() {
                return "ascan/action/stop/";
            }
        },
        STOP_ALL_SCANNER{
            @Override
            public String getUrl() {
                return "ascan/action/stopAllScans/";
            }
        },
        REMOVE_SCANNER{
            @Override
            public String getUrl() {
                return "ascan/action/removeScan/";
            }
        },
        REMOVE_ALL_SCANNER{
            @Override
            public String getUrl() {
                return "ascan/action/removeAllScans/";
            }
        };


        private final String url = null;

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
