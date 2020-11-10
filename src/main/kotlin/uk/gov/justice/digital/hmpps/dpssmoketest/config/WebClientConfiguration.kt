package uk.gov.justice.digital.hmpps.dpssmoketest.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration(@Value("\${community.endpoint.url}") private val communityRootUri: String,
                             @Value("\${prisonapi.endpoint.url}") private val prisonapiRootUri: String,
                             @Value("\${oauth.endpoint.url}") private val oauthRootUri: String,
                             private val webClientBuilder: WebClient.Builder) {




  @Bean
  fun communityApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("community-api")


    return webClientBuilder
        .baseUrl(communityRootUri)
        .apply(oauth2Client.oauth2Configuration())
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs { configurer ->
              configurer.defaultCodecs()
                  .maxInMemorySize(-1)
            }
            .build())
        .build()
  }

  @Bean
  fun prisonApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("prison-api")

    return webClientBuilder
        .baseUrl(prisonapiRootUri)
        .apply(oauth2Client.oauth2Configuration())
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs { configurer ->
              configurer.defaultCodecs()
                  .maxInMemorySize(-1)
            }
            .build())
        .build()
  }


  @Bean
  fun communityApiHealthWebClient(): WebClient {
    return webClientBuilder.baseUrl(communityRootUri).build()
  }


  @Bean
  fun prisonApiHealthWebClient(): WebClient {
    return webClientBuilder.baseUrl(prisonapiRootUri).build()
  }

  @Bean
  fun oauthApiHealthWebClient(): WebClient {
    return webClientBuilder.baseUrl(oauthRootUri).build()
  }

  @Bean
  fun authorizedClientManager(clientRegistrationRepository: ClientRegistrationRepository?,
                              oAuth2AuthorizedClientService: OAuth2AuthorizedClientService?): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }


  fun beans(authorizedClientManager: OAuth2AuthorizedClientManager,
            clientRegistrationRepository: ClientRegistrationRepository,
            oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ) =
      beans {
        bean<WebClient>("communityApiWebClient") {
          val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
          oauth2Client.setDefaultClientRegistrationId("community-api")

          webClientBuilder
              .baseUrl(communityRootUri)
              .apply(oauth2Client.oauth2Configuration())
              .exchangeStrategies(org.springframework.web.reactive.function.client.ExchangeStrategies.builder()
                  .codecs { configurer ->
                    configurer.defaultCodecs()
                        .maxInMemorySize(-1)
                  }
                  .build())
              .build()
        }
        bean<WebClient>("prisonApiWebClient") {
          val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
          oauth2Client.setDefaultClientRegistrationId("prison-api")

          webClientBuilder
              .baseUrl(prisonapiRootUri)
              .apply(oauth2Client.oauth2Configuration())
              .exchangeStrategies(ExchangeStrategies.builder()
                  .codecs { configurer ->
                    configurer.defaultCodecs()
                        .maxInMemorySize(-1)
                  }
                  .build())
              .build()

        }
        bean<WebClient>("communityApiHealthWebClient") { webClientBuilder.baseUrl(communityRootUri).build() }
        bean<WebClient>("prisonApiHealthWebClient") { webClientBuilder.baseUrl(prisonapiRootUri).build() }
        bean<WebClient>("oauthApiHealthWebClient") { webClientBuilder.baseUrl(oauthRootUri).build() }
        bean<OAuth2AuthorizedClientManager>("authorizedClientManager") {
          AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
              .apply { setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()) }
        }
      }

}