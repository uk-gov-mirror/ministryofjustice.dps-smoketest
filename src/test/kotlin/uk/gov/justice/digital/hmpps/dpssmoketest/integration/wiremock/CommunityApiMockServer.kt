package uk.gov.justice.digital.hmpps.dpssmoketest.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class CommunityApiExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    val communityApi = CommunityApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    communityApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    communityApi.resetAll()
  }

  override fun afterAll(context: ExtensionContext) {
    communityApi.stop()
  }
}

class CommunityApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8096
  }

  fun stubHealthPing(status: Int) {
    stubFor(get("/health/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

  }
}