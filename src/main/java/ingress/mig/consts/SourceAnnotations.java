package ingress.mig.consts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class SourceAnnotations {

    public static final String ALB_ID = "ingress.bluemix.net/ALB-ID";
    public static final String REDIRECT_TO_HTTPS = "ingress.bluemix.net/redirect-to-https";
    public static final String CLIENT_MAX_BODY_SIZE = "ingress.bluemix.net/client-max-body-size";
    public static final String PROXY_READ_TIMEOUT = "ingress.bluemix.net/proxy-read-timeout";
    public static final String PROXY_CONNECT_TIMEOUT = "ingress.bluemix.net/proxy-connect-timeout";
    public static final String PROXY_BUFFER_SIZE = "ingress.bluemix.net/proxy-buffer-size";
    public static final String PROXY_BUFFERS = "ingress.bluemix.net/proxy-buffers";
    public static final String SSL_SERVICES = "ingress.bluemix.net/ssl-services";
    
    
//    public static final Set<String> NONEED;
//    public static final String UPSTREAM_FAIL_TIMEOUT = "ingress.bluemix.net/upstream-fail-timeout";
//    public static final String UPSTREAM_KEEPALIVE_TIMEOUT = "ingress.bluemix.net/upstream-keepalive-timeout";
//    public static final String KEEPALIVE_TIMEOUT = "ingress.bluemix.net/keepalive-timeout";
    
    
    public static final Set<String> DELETE;
    public static final String PROXY_NEXT_UPSTREAM_CONFIG = "ingress.bluemix.net/proxy-next-upstream-config";
    public static final String KUBECTL_KUBERNETES_IO_LAST_APPLIED_CONFIGURATION = "kubectl.kubernetes.io/last-applied-configuration";
    public static final String KUBERNETES_IO_CHANGE_CAUSE = "kubernetes.io/change-cause";
    public static final String INGRESS_KUBERNETES_IO_ENABLE_CORS = "ingress.kubernetes.io/enable-cors";
    public static final String INGRESS_KUBERNETES_IO_REWRITE_TARGET = "ingress.kubernetes.io/rewrite-target";
    public static final String INGRESS_KUBERNETES_IO_PROXY_BODY_SIZE = "ingress.kubernetes.io/proxy-body-size";
    public static final String INGRESS_KUBERNETES_IO_SSL_REDIRECT = "ingress.kubernetes.io/ssl-redirect";
    public static final String NGINX_INGRESS_KUBERNETES_IO_SSL_REDIRECT = "nginx.ingress.kubernetes.io/ssl-redirect";
    public static final String NGINX_INGRESS_KUBERNETES_IO_REWRITE_TARGET = "nginx.ingress.kubernetes.io/rewrite-target";
    public static final String NGINX_INGRESS_KUBERNETES_IO_PROXY_BODY_SIZE = "nginx.ingress.kubernetes.io/proxy-body-size";
    public static final String CUSTOM_PORT = "ingress.bluemix.net/custom-port";
    public static final String PROXY_SEND_TIMEOUT = "ingress.bluemix.net/proxy-send-timeout";
    public static final String SSL_REDIRECT = "ingress.bluemix.net/ssl-redirect";
    public static final String PROXY_BODY_SIZE = "ingress.bluemix.net/proxy-body-size";
    public static final String DEFAULT_SERVER = "ingress.bluemix.net/default-server";
    public static final String CLIENT_HEADER_TIMEOUT = "ingress.bluemix.net/client-header-timeout";
    public static final String KUBERNETES_IO_INGRESS_CLASS = "kubernetes.io/ingress.class";
    
    static {
        DELETE = Sets.newHashSet(Arrays.asList(
                PROXY_NEXT_UPSTREAM_CONFIG,
                KUBECTL_KUBERNETES_IO_LAST_APPLIED_CONFIGURATION,
                KUBERNETES_IO_CHANGE_CAUSE,
                INGRESS_KUBERNETES_IO_ENABLE_CORS,
                INGRESS_KUBERNETES_IO_REWRITE_TARGET,
                INGRESS_KUBERNETES_IO_PROXY_BODY_SIZE,
                INGRESS_KUBERNETES_IO_SSL_REDIRECT,
//                NGINX_INGRESS_KUBERNETES_IO_SSL_REDIRECT,
//                NGINX_INGRESS_KUBERNETES_IO_REWRITE_TARGET,
//                NGINX_INGRESS_KUBERNETES_IO_PROXY_BODY_SIZE,
                CUSTOM_PORT,
                PROXY_SEND_TIMEOUT,
                SSL_REDIRECT,
                PROXY_BODY_SIZE,
                DEFAULT_SERVER,
                CLIENT_HEADER_TIMEOUT
//                KUBERNETES_IO_INGRESS_CLASS
                ));
    }
}
