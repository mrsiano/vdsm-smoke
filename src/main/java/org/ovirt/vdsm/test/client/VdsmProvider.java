package org.ovirt.vdsm.test.client;

import static org.ovirt.vdsm.test.scenarios.Utils.validate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;

public class VdsmProvider extends ManagerProvider {

    public static final String TYPE = "vdsm";
    private static final String CONFIG_PATH = "/usr/lib/python2.7/site-packages/vdsm/config.py";
    private String path;

    public VdsmProvider(String path) {
        Pattern pattern = Pattern.compile("'trust_store_path', '(.*)'");
        try (BufferedReader reader = new BufferedReader(new FileReader(path == null ? CONFIG_PATH : path))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    this.path = matcher.group(1);
                    break;
                }
            }
        } catch (IOException ignored) {
            // checked path when loading a Manager
        }
    }

    @Override
    public TrustManager[] getTrustManagers() throws GeneralSecurityException {
        validate(this.path);
        try {
            Path certPath = Paths.get(this.path + File.separator + "certs" + File.separator + "cacert.pem");
            byte[] certData = Files.readAllBytes(certPath);

            X509Certificate cert = (X509Certificate) generateCertificateFromPEM(certData);

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            return tmf.getTrustManagers();
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    @Override
    public KeyManager[] getKeyManagers() throws GeneralSecurityException {
        validate(this.path);
        try {
            Path keyPath = Paths.get(this.path + File.separator + "keys" + File.separator + "vdsmkey.pem");
            byte[] keyData = Files.readAllBytes(keyPath);

            Path certPath = Paths.get(this.path + File.separator + "certs" + File.separator + "vdsmcert.pem");
            byte[] certData = Files.readAllBytes(certPath);

            X509Certificate cert = (X509Certificate) generateCertificateFromPEM(certData);
            PrivateKey key = (PrivateKey) generatePrivateKeyFromPEM(keyData);

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);
            keystore.setKeyEntry("key-alias", key, "changeit".toCharArray(), new Certificate[] { cert });

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, "changeit".toCharArray());
            return kmf.getKeyManagers();
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    protected static PrivateKey generatePrivateKeyFromPEM(byte[] keyBytes)
            throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        try (ByteArrayInputStream pemByteIn = new ByteArrayInputStream(keyBytes);
                PEMReader reader = new PEMReader(new InputStreamReader(pemByteIn))) {
            KeyPair keyPair = (KeyPair) reader.readObject();
            return keyPair.getPrivate();
        }
    }

    protected static X509Certificate generateCertificateFromPEM(byte[] certBytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(
                new ByteArrayInputStream(certBytes));
    }
}
