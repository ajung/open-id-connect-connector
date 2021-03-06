/**
 * Copyright 2016 Moritz Möller, AOE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mule.modules.openidconnect.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * This class is responsible to build and provide meta data of an OpenID Connect
 * Identity-Provider. The data is provided manually with the parameters of the
 * ConnectorConfig or through HTTP from IdP. To obtain the configuration from
 * IdP the specified OpenID Configuration discovery is used.
 *
 * @author Moritz Möller, AOE GmbH
 * @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig">OpenID Connect spec</a>
 *
 */
public class MetaDataBuilder {
	
	private Client client;
	private URI providerUri;

    private static final Logger logger = LoggerFactory.getLogger(MetaDataBuilder.class);


    public MetaDataBuilder(URI providerUri) {
		this.providerUri = providerUri;
		client = ClientBuilder.newClient();
	}


    /**
     * Obtains the IdP configuration as JSON String via requestJsonString() to build and return the IdP-metadata
     *
     * @param configurationEndpoint Endpoint at the IdP to obtain configuration data
     * @return the IdP-metadata
     * @throws ParseException Thrown if the JSON from the response can not be parsed
     */
	public OIDCProviderMetadata provideMetadataFromServer(String configurationEndpoint) throws ParseException {
        URI metadataURI = uriBuilder(providerUri, configurationEndpoint);
        logger.debug("Sending HTTP request to retrieve metadata from identity provider");
        return OIDCProviderMetadata.parse(requestJsonString(metadataURI));
	}

    /**
     * Builds and returns the IdP-metadata manually from parameters passed to it.
     *
     * @param authEndpoint Authorization endpoint
     * @param tokenEndpoint Token endpoint
     * @param jwkSetEndpoint JSON Web Key endpoint
     * @return the IdP-metadata
     */
	public OIDCProviderMetadata provideMetadataManually(
            String authEndpoint, String tokenEndpoint, String jwkSetEndpoint) {
        Issuer issuer = new Issuer(providerUri);
        List<SubjectType> subjectTypes = new ArrayList<>();
        subjectTypes.add(SubjectType.PUBLIC);
        OIDCProviderMetadata metaData = new OIDCProviderMetadata(
                issuer, subjectTypes, uriBuilder(providerUri, jwkSetEndpoint)
        );
        metaData.setAuthorizationEndpointURI(uriBuilder(providerUri, authEndpoint));
        metaData.setTokenEndpointURI(uriBuilder(providerUri, tokenEndpoint));
        metaData.applyDefaults();
        return metaData;
	}

    /**
     * Parses a JSON string obtained from requestJsonString() to obtain the JSON Web Key configuration from the IdP
     * and returns the RSAPublicKey
     *
     * @param providerMetadata IdP metadata
     * @return The RSAPublicKey of the IdP
     * @throws ParseException
     * @throws JOSEException
     * @throws java.text.ParseException
     */
	public RSAPublicKey providePublicKeyFromJwkSet(OIDCProviderMetadata providerMetadata) throws
            ParseException, JOSEException, java.text.ParseException {
		URI jwkSetUri = providerMetadata.getJWKSetURI();
        logger.debug("Sending HTTP request to retrieve JWK set from identity provider");
        String metaDataResponse = requestJsonString(jwkSetUri);
        JSONObject json = JSONObjectUtils.parse(metaDataResponse);
        RSAPublicKey publicKey = null;
        JSONArray keyList = (JSONArray) json.get("keys");

        for (Object key : keyList) {
            JSONObject k = (JSONObject) key;
            if (k.get("use").equals("sig") && k.get("kty").equals("RSA")) {
                publicKey = RSAKey.parse(k).toRSAPublicKey();
            }
        }
        return publicKey;
	}

    /**
     * Provides a RSAPublicKey from a given string
     *
     * @param keyString Public key string
     * @return the RSAPublicKey from key string
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws UnsupportedEncodingException
     */
    public RSAPublicKey providePublicKeyFromString(String keyString) throws
            NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
        byte[] keyBytes = Base64.getDecoder().decode(keyString.getBytes("utf-8"));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey)keyFactory.generatePublic(spec);
    }


    /**
     * Helper method to extend and build URIs
     *
     * @param uri Base URI
     * @param path Path to be extend
     * @return New URI
     */
	private URI uriBuilder(URI uri, String path) {
		UriBuilder builder = UriBuilder.fromUri(uri).path(path);
        return builder.build();
	}

    /**
     * Requests JSON from given endpoint via HTTP and returns it as string
     *
     * @param uri Endpoint to obtain JSON from
     * @return JSON content as string
     */
	public String requestJsonString(URI uri) {
		WebTarget webTarget = client.target(uri);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class);
	}
}
