package com.adaptris.core.jwt;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

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
  private String id;

  @Getter
  @Setter
  @NotNull
  @Valid
  @InputFieldHint(expression=true)
  private String issuer;

  @Getter
  @Setter
  @NotNull
  @Valid
  @InputFieldHint(expression=true)
  private String subject;

  @Getter
  @Setter
  @NotNull
  @Valid
  @InputFieldHint(expression=true)
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

  @Getter
  @Setter
  @NotNull
  @Valid
  @InputFieldHint(expression=true)
  private String secret;

  @Getter
  @Setter
  @Valid
  @AdvancedConfig
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
  public void doService(AdaptrisMessage message)
  {
    JwtBuilder builder = Jwts.builder()
        .setSubject(message.resolve(subject))
        .setAudience(message.resolve(audience))
        .setNotBefore(notBefore)
        .setIssuer(message.resolve(issuer))
        .setExpiration(expiration)
        .setIssuedAt(issuedAt != null ? issuedAt : new Date())
        .setId(id != null ? id : UUID.randomUUID().toString())

        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(message.resolve(secret))));

    if (customClaims != null)
    {
      for (KeyValuePair claim : customClaims)
      {
        builder.claim(claim.getKey(), claim.getValue());
      }
    }

    message.setContent(builder.compact(), message.getContentEncoding());
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
