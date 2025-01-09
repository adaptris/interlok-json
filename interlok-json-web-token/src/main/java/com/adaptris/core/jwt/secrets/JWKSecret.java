package com.adaptris.core.jwt.secrets;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.ProtectedHeader;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@XStreamAlias("jwk-secret")
public class JWKSecret implements SecretConfigurator {

    /**
     * A configurator for encoding JWT
     */
    private SecretConfigurator encoderSecret;

    /**
     * This could be a json representing the JWKS or a domain that hosts the JWKS
     */
    private String source;

    /**
     * Handles domain based JWKS. This uses the Auth0 library as it supports caching of
     * JWKS to minimise requests.
     */
    private transient JwkProvider urlJwkSet;

    /**
     * Handles explicitly conifigured JWKS
     */
    private transient JwkSet stringJwkSet;

    /**
     * Implements KeyLocator interface to get public key for decoding
     */
    private transient KeyLocator keyLocator = new KeyLocator();


    /**
     * Encoding could occur with different secrets
     * @return
     */
    public SecretConfigurator getEncoderSecret() {
        return encoderSecret;
    }

    public void setEncoderSecret(SecretConfigurator encoderSecret) {
        this.encoderSecret = encoderSecret;
    }

    @Override
    public JwtBuilder configure(JwtBuilder builder) throws InvalidSecretException {
        return encoderSecret.configure(builder);
    }

    @Override
    public JwtParserBuilder configure(JwtParserBuilder builder) throws InvalidSecretException {
        try {
            return builder.keyLocator(keyLocator);
        } catch (Exception e) {
            log.error("Error loading public key.", e);
            throw new InvalidSecretException(e);
        }
    }

    private void initJwkSet() {
        if (!isJwkSetInitialized()) {
            try {
                initFromUrl(source);
            } catch (IllegalArgumentException  ex) {
                initFromString(source);
            }
        }
    }

    private boolean isJwkSetInitialized() {
        return urlJwkSet != null || stringJwkSet != null;
    }

    private void clearJwkSet() {
        urlJwkSet = null;
        stringJwkSet = null;
    }

    public boolean getStringSource() {
        return stringJwkSet != null;
    }

    public boolean getUrlSource() {
        return urlJwkSet != null;
    }

    /**
     * Depending on whether the source is a domain or explicit JWK json, extract the
     * public key and return.
     * @param keyId
     * @return the public key from the key defined by keyId
     * @throws Exception
     */
    protected Key locatePublicKey(String keyId) throws Exception {
        initJwkSet();
        assert getUrlSource() || getStringSource() : "No valid sources configured";
        if (getUrlSource()) {
            Jwk jwk = urlJwkSet.get(keyId);
            return jwk.getPublicKey();
        } else if (getStringSource()) {
            Optional<io.jsonwebtoken.security.Jwk<?>> maybeJwk = stringJwkSet.getKeys().stream().filter(jwk -> jwk.getId().equals(keyId)).findFirst();
            if (!maybeJwk.isPresent()) throw new IllegalArgumentException("Jwk not found: " + keyId);
            else {
                Key k = maybeJwk.get().toKey();
                // if it is RSA, then extract the public key and return
                if (k instanceof RSAPrivateCrtKey privateKey) {
                    return publicFromPrivate(privateKey);
                } else if (k instanceof ECPrivateKey privateKey) {
                    return publicFromPrivate(privateKey);
                }
                return maybeJwk.get().toKey();
            }
        }
        throw new IllegalArgumentException("Jwk not found: " + keyId);
    }

    public String getSource() {
        return source;
    }

    /**
     * Set the source for decoding a JWT. This should either be a domain which hosts
     * the JWKS or the JWKS in string form.
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
        clearJwkSet();
    }

    protected void initFromUrl(String url) {
        urlJwkSet = new JwkProviderBuilder(url)
                .cached(10, 24, TimeUnit.HOURS)
                .build();
    }

    protected void initFromString(String json) {
        stringJwkSet = Jwks.setParser().build().parse(json);
    }

    private class KeyLocator extends LocatorAdapter<Key> {
        @Override
        protected Key locate(ProtectedHeader header) {
            String keyId = header.getKeyId();
            try {
                return locatePublicKey(keyId);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static PublicKey publicFromPrivate(RSAPrivateCrtKey privateKey) throws Exception {
        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return publicKey;
    }

    public static PublicKey publicFromPrivate(final ECPrivateKey privateKey) throws Exception {
        ECParameterSpec params = privateKey.getParams();
        org.bouncycastle.jce.spec.ECParameterSpec bcSpec = org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util
                .convertSpec(params);
        org.bouncycastle.math.ec.ECPoint q = bcSpec.getG().multiply(privateKey.getS());
        org.bouncycastle.math.ec.ECPoint bcW = bcSpec.getCurve().decodePoint(q.getEncoded(false));
        ECPoint w = new ECPoint(
                bcW.getAffineXCoord().toBigInteger(),
                bcW.getAffineYCoord().toBigInteger());
        ECPublicKeySpec keySpec = new ECPublicKeySpec(w, tryFindNamedCurveSpec(params));
        return KeyFactory
                .getInstance("EC", org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME)
                .generatePublic(keySpec);
    }

    @SuppressWarnings("unchecked")
    public static ECParameterSpec tryFindNamedCurveSpec(ECParameterSpec params) {
        org.bouncycastle.jce.spec.ECParameterSpec bcSpec
                = org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util.convertSpec(params);
        for (Object name : Collections.list(org.bouncycastle.jce.ECNamedCurveTable.getNames())) {
            org.bouncycastle.jce.spec.ECNamedCurveParameterSpec bcNamedSpec
                    = org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec((String) name);
            if (bcNamedSpec.getN().equals(bcSpec.getN())
                    && bcNamedSpec.getH().equals(bcSpec.getH())
                    && bcNamedSpec.getCurve().equals(bcSpec.getCurve())
                    && bcNamedSpec.getG().equals(bcSpec.getG())) {
                return new org.bouncycastle.jce.spec.ECNamedCurveSpec(
                        bcNamedSpec.getName(),
                        bcNamedSpec.getCurve(),
                        bcNamedSpec.getG(),
                        bcNamedSpec.getN(),
                        bcNamedSpec.getH(),
                        bcNamedSpec.getSeed());
            }
        }
        return params;
    }
}
