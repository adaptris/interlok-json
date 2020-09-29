package com.adaptris.core.jwt;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.jwt.secrets.SecretConfigurator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * This service provides a way to create a JSON Web Token from the given data.
 *
 * <pre>{@code
 *    <jwt-creator>
 *      <unique-id>jwt-create</unique-id>
 *      <issuer>ashley</issuer>
 *      <subject>test</subject>
 *      <audience>everyone</audience>
 *      <expiration>2040-12-31 00:00:00.0 UTC</expiration>
 *      <not-before>2020-01-01 00:00:00.0 UTC</not-before>
 *      <secret class="base64-encoded-secret">
 *        <secret>c64975ba3cf3f9cd58459710b0a42369f34b0759c9967fb5a47eea488e8bea79</secret>
 *      </secret>
 *      <custom-claims>
 *        <key-value-pair>
 *          <key>payload</key>
 *          <value>%message{%payload}</value>
 *        </key-value-pair>
 *      </custom-claims>
 *    </jwt-creator>
 * }</pre>
 *
 * @author aanderson
 * @config jwt-create
 */
@XStreamAlias("jwt-creator")
@AdapterComponent
@ComponentProfile(summary = "Create a JSON Web Token", tag = "jwt,create,json,web,token", since="3.11.1")
@DisplayOrder(order = { "id", "issuer", "subject", "audience", "issuedAt", "expiration", "notBefore", "secret", "customClaims" })
public class JWTCreator extends ServiceImp
{
  @Getter
  @Setter
  @Valid
  @AdvancedConfig(rare = true)
  @InputFieldHint(expression = true)
  private String id;

  @Getter
  @Setter
  @NotNull
  @Valid
  @InputFieldHint(expression = true)
  private String issuer;

  @Getter
  @Setter
  @NotNull
  @Valid
  @InputFieldHint(expression = true)
  private String subject;

  @Getter
  @Setter
  @NotNull
  @Valid
  @InputFieldHint(expression = true)
  private String audience;

  @Getter
  @Setter
  @Valid
  @AdvancedConfig(rare = true)
  private Date issuedAt;

  @Getter
  @Setter
  @NotNull
  @Valid
  private Date expiration;

  @Getter
  @Setter
  @NotNull
  @Valid
  private Date notBefore;

  @NotNull
  @Valid
  @Getter
  @Setter
  private SecretConfigurator secret;

  @Getter
  @Setter
  @Valid
  @AdvancedConfig
  @InputFieldHint(expression = true)
  private KeyValuePairSet customClaims;

  /**
   * <p>
   * Apply the service to the message.
   * </p>
   *
   * @param message the <code>AdaptrisMessage</code> to process
   * @throws ServiceException wrapping any underlying <code>Exception</code>s
   */
  @Override
  public void doService(AdaptrisMessage message) throws ServiceException
  {
    try
    {
      JwtBuilder builder = Jwts.builder()
              .setSubject(message.resolve(subject))
              .setAudience(message.resolve(audience))
              .setNotBefore(notBefore)
              .setIssuer(message.resolve(issuer))
              .setExpiration(expiration)
              .setIssuedAt(issuedAt != null ? issuedAt : new Date())
              .setId(id != null ? id : UUID.randomUUID().toString());

      builder = secret.configure(builder);

      if (customClaims != null)
      {
        for (KeyValuePair claim : customClaims)
        {
          builder.claim(claim.getKey(), message.resolve(claim.getValue()));
        }
      }

      message.setContent(builder.compact(), message.getContentEncoding());
    }
    catch (Exception e)
    {
      log.error("Could not create JSON Web Token", e);
      throw new ServiceException(e);
    }
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void initService()
  {
    /* unused */
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void closeService()
  {
    /* unused */
  }

  /**
   * Prepare for initialisation.
   */
  @Override
  public void prepare()
  {
    /* unused */
  }
}
