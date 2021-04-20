package ingress.mig.consts;

public class NginxAnnotations {

    public static final String INGRESS_CLASS = "kubernetes.io/ingress.class";
    public static final String SSL_REDIRECT = "nginx.ingress.kubernetes.io/ssl-redirect";
    public static final String PROXY_BODY_SIZE = "nginx.ingress.kubernetes.io/proxy-body-size";
    public static final String PROXY_READ_TIMEOUT = "nginx.ingress.kubernetes.io/proxy-read-timeout";
    public static final String PROXY_CONNECT_TIMEOUT = "nginx.ingress.kubernetes.io/proxy-connect-timeout";
    public static final String BACKEND_PROTOCOL = "nginx.ingress.kubernetes.io/backend-protocol";
}
