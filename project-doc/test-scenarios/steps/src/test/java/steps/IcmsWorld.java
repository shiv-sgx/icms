package steps;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Shared "world" object — one instance per scenario (Cucumber creates a new
 * instance for every scenario, so state is automatically isolated).
 *
 * Provides a cookie-aware HTTP client that works against the running ICMS
 * Tomcat instance. The base URL and demo password are read from environment
 * variables so the specs never hardcode them.
 *
 * Environment variables (with defaults for local development):
 *   ICMS_BASE_URL       default: http://localhost:8080
 *   ICMS_DEMO_PASSWORD  default: Test@1234
 */
public class IcmsWorld {

    // -------------------------------------------------------------------------
    // Configuration — read from env, never hardcoded.
    // -------------------------------------------------------------------------

    public static final String BASE_URL;
    public static final String DEMO_PASSWORD;

    static {
        // Disable JVM HTTP connection pooling: the local IPv4->IPv6 bridge proxy
        // mishandles HTTP/1.1 keep-alive and can hand back a stale/mismatched
        // response on a reused connection. A fresh connection per request avoids it.
        System.setProperty("http.keepAlive", "false");
        String base = System.getenv("ICMS_BASE_URL");
        BASE_URL = (base != null && !base.isEmpty()) ? base.replaceAll("/+$", "") : "http://localhost:8080";
        String pw = System.getenv("ICMS_DEMO_PASSWORD");
        DEMO_PASSWORD = (pw != null && !pw.isEmpty()) ? pw : "Test@1234";
    }

    // -------------------------------------------------------------------------
    // Per-scenario state
    // -------------------------------------------------------------------------

    private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private HttpResponse lastResponse;

    /** The claim id captured from a redirect URL after a successful create/forward. */
    private long capturedClaimId = -1;

    // -------------------------------------------------------------------------
    // HTTP helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the absolute URL for a path.
     * Ensures scheme + host is always prepended exactly once.
     */
    public String url(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return BASE_URL + path;
    }

    /** GET request, follows redirects, returns final response. */
    public HttpResponse get(String path) throws Exception {
        return request("GET", url(path), null);
    }

    /** POST request with URL-encoded body params. */
    public HttpResponse post(String path, Map<String, String> params) throws Exception {
        String body = encodeForm(params);
        return request("POST", url(path), body);
    }

    /**
     * Performs the HTTP request.  Follows Location redirects up to 5 hops so
     * assertions can inspect the *final* page, but still tracks the original
     * redirect target for "redirects to X" assertions.
     */
    private HttpResponse request(String method, String urlStr, String body) throws Exception {
        String originalLocation = null;
        int hops = 0;
        String currentUrl = urlStr;

        while (hops++ < 6) {
            URL url = new URL(currentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestMethod(method);
            // Use a fresh connection per request. The local IPv4->IPv6 bridge proxy
            // does not reliably handle HTTP/1.1 keep-alive, so reusing a pooled
            // connection can return a mismatched (stale) response body.
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            // Inject cookies
            String cookieHeader = buildCookieHeader(url.getHost());
            if (!cookieHeader.isEmpty()) {
                conn.setRequestProperty("Cookie", cookieHeader);
            }

            if (body != null) {
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyBytes);
                }
            }

            conn.connect();
            int status = conn.getResponseCode();

            // Collect Set-Cookie headers
            Map<String, List<String>> headers = conn.getHeaderFields();
            storeCookies(url, headers);

            String location = conn.getHeaderField("Location");

            InputStream is;
            try {
                is = conn.getInputStream();
            } catch (IOException e) {
                is = conn.getErrorStream();
            }
            String responseBody = is == null ? "" : readAll(is);
            conn.disconnect();

            if (originalLocation == null && (status == 301 || status == 302 || status == 303)) {
                originalLocation = location;
            }

            lastResponse = new HttpResponse(status, location, responseBody, originalLocation);

            if ((status == 301 || status == 302 || status == 303) && location != null) {
                // Resolve relative Location
                if (location.startsWith("http")) {
                    currentUrl = location;
                } else {
                    URL base = new URL(currentUrl);
                    currentUrl = base.getProtocol() + "://" + base.getHost()
                            + (base.getPort() > 0 ? ":" + base.getPort() : "")
                            + (location.startsWith("/") ? location : "/" + location);
                }
                method = "GET";
                body = null;
                continue;
            }
            break;
        }
        return lastResponse;
    }

    // -------------------------------------------------------------------------
    // Authentication helpers
    // -------------------------------------------------------------------------

    /**
     * Logs in as the given user and stores the session cookie.
     * The step definitions call this in Background / Given steps.
     */
    public HttpResponse login(String username, String password) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("username", username);
        params.put("password", password);
        return post("/doLogin", params);
    }

    /**
     * Logs out the current session.
     */
    public HttpResponse logout() throws Exception {
        return get("/logout");
    }

    /**
     * Clears all stored cookies (call in After hooks if needed, but normally
     * a new IcmsWorld instance per scenario handles isolation).
     */
    public void clearCookies() {
        cookieManager.getCookieStore().removeAll();
    }

    // -------------------------------------------------------------------------
    // Cookie management (replaces HttpURLConnection's broken cookie support)
    // -------------------------------------------------------------------------

    private void storeCookies(URL url, Map<String, List<String>> headers) {
        try {
            cookieManager.put(url.toURI(), headers);
        } catch (Exception ignored) {
        }
    }

    private String buildCookieHeader(String host) {
        List<String> parts = new ArrayList<>();
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            parts.add(cookie.getName() + "=" + cookie.getValue());
        }
        return String.join("; ", parts);
    }

    // -------------------------------------------------------------------------
    // Multipart file upload (simplified — sends a real multipart body)
    // -------------------------------------------------------------------------

    /**
     * Uploads a file (supplied as byte content) via multipart/form-data.
     * Additional text fields (e.g. docType) are included as text parts.
     */
    public HttpResponse uploadFile(String path, Map<String, String> textFields,
                                    String fileField, String fileName, byte[] fileContent,
                                    String mimeType) throws Exception {
        String boundary = "----BDDTestBoundary" + System.nanoTime();
        URL urlObj = new URL(url(path));
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Connection", "close");

        String cookieHeader = buildCookieHeader(urlObj.getHost());
        if (!cookieHeader.isEmpty()) {
            conn.setRequestProperty("Cookie", cookieHeader);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), true);

        for (Map.Entry<String, String> entry : textFields.entrySet()) {
            pw.append("--").append(boundary).append("\r\n");
            pw.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append("\r\n\r\n");
            pw.append(entry.getValue() == null ? "" : entry.getValue()).append("\r\n");
        }

        if (fileContent != null) {
            pw.append("--").append(boundary).append("\r\n");
            pw.append("Content-Disposition: form-data; name=\"").append(fileField)
              .append("\"; filename=\"").append(fileName).append("\"").append("\r\n");
            pw.append("Content-Type: ").append(mimeType != null ? mimeType : "application/octet-stream").append("\r\n\r\n");
            pw.flush();
            baos.write(fileContent);
            baos.write(("\r\n").getBytes(StandardCharsets.UTF_8));
        }

        pw.append("--").append(boundary).append("--").append("\r\n");
        pw.flush();
        byte[] multipart = baos.toByteArray();

        conn.setRequestProperty("Content-Length", String.valueOf(multipart.length));
        conn.getOutputStream().write(multipart);
        conn.getOutputStream().flush();

        int status = conn.getResponseCode();
        Map<String, List<String>> headers = conn.getHeaderFields();
        storeCookies(urlObj, headers);
        String location = conn.getHeaderField("Location");
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        String body = is == null ? "" : readAll(is);
        conn.disconnect();

        // Follow the POST-Redirect-GET: the app sets a flash message and redirects
        // to the claim detail, so the success/error text is only visible on the
        // landed page. Without this, a rejected upload returns an empty 302 body
        // and the rejection message is never seen. Mirrors get()/post() behaviour.
        if (status >= 300 && status < 400 && location != null && !location.isEmpty()) {
            String followPath = new URL(urlObj, location).getFile(); // path + query
            HttpResponse landed = get(followPath);
            lastResponse = new HttpResponse(landed.status, location, landed.body, location);
            return lastResponse;
        }

        lastResponse = new HttpResponse(status, location, body, location);
        return lastResponse;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public HttpResponse getLastResponse() { return lastResponse; }

    public long getCapturedClaimId() { return capturedClaimId; }
    public void setCapturedClaimId(long id) { this.capturedClaimId = id; }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static String readAll(InputStream is) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = is.read(chunk)) != -1) {
            buf.write(chunk, 0, n);
        }
        return buf.toString("UTF-8");
    }

    public static String encodeForm(Map<String, String> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), "UTF-8"))
              .append('=')
              .append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), "UTF-8"));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Inner response holder
    // -------------------------------------------------------------------------

    /**
     * Immutable HTTP response snapshot.
     * {@code originalRedirectTarget} is the first Location header seen before
     * the client followed any redirect — used for "redirects to X" assertions.
     */
    public static final class HttpResponse {
        public final int status;
        public final String location;
        public final String body;
        public final String originalRedirectTarget;

        public HttpResponse(int status, String location, String body, String originalRedirectTarget) {
            this.status = status;
            this.location = location;
            this.body = body == null ? "" : body;
            this.originalRedirectTarget = originalRedirectTarget;
        }

        /** True if the original (pre-follow) response was a 3xx. */
        public boolean wasRedirect() {
            return originalRedirectTarget != null;
        }

        /**
         * True if the final landed page or the redirect target URL contains the given path segment.
         */
        public boolean redirectsTo(String path) {
            String target = originalRedirectTarget != null ? originalRedirectTarget : location;
            return target != null && target.contains(path);
        }

        public boolean bodyContains(String text) {
            return body.contains(text);
        }
    }
}
