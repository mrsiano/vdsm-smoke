package org.ovirt.vdsm.test.client;

import static org.ovirt.vdsm.test.scenarios.Utils.removeQuotes;
import static org.ovirt.vdsm.test.scenarios.Utils.validate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;

public class EngineProvider extends ManagerProvider {

    public static final String TYPE = "engine";
    private static final String CONFIG_PATH = "/etc/ovirt-engine/engine.conf.d/10-setup-pki.conf";
    private static final String CERT_PATH = "ENGINE_PKI";
    private static final String KEYSTORE_FILE = "ENGINE_PKI_ENGINE_STORE";
    private static final String KEYSTORE_PASS = "ENGINE_PKI_ENGINE_STORE_PASSWORD";
    private static final String TRUSTSTORE_FILE = "ENGINE_PKI_TRUST_STORE";
    private static final String TRUSTSTORE_PASS = "ENGINE_PKI_TRUST_STORE_PASSWORD";
    private Properties properties;

    public EngineProvider(String path) {
        try (InputStream in = new FileInputStream(path == null ? CONFIG_PATH : path)) {
            this.properties = new Properties();
            this.properties.load(in);
        } catch (IOException ignored) {
            // checked path when loading a Manager
        }
    }

    @Override
    public KeyManager[] getKeyManagers() throws GeneralSecurityException {
        String path = this.properties.getProperty(CERT_PATH, null);
        validate(path);

        try {
            char[] pass = removeQuotes(this.properties.getProperty(KEYSTORE_PASS)).toCharArray();
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(getKeyStore(KEYSTORE_FILE, "PKCS12", pass), pass);
            return kmfactory.getKeyManagers();
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    @Override
    public TrustManager[] getTrustManagers() throws GeneralSecurityException {
        String path = this.properties.getProperty(CERT_PATH, null);
        validate(path);

        try {
            char[] pass = removeQuotes(this.properties.getProperty(TRUSTSTORE_PASS)).toCharArray();
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(getKeyStore(TRUSTSTORE_FILE, "JKS", pass));
            return tmfactory.getTrustManagers();
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private KeyStore getKeyStore(String fileProperty, String type, char[] password)
            throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
        String filepath = removeQuotes(this.properties.getProperty(fileProperty));

        try (final InputStream in = new FileInputStream(filepath)) {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(in, password);
            return keyStore;
        }
    }
}
