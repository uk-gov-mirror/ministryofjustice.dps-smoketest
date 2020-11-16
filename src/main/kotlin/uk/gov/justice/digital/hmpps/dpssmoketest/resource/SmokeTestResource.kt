package uk.gov.justice.digital.hmpps.dpssmoketest.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.dpssmoketest.resource.SmokeTestResource.TestStatus.TestProgress.FAIL
import uk.gov.justice.digital.hmpps.dpssmoketest.service.ptpu.PtpuTestProfiles
import uk.gov.justice.digital.hmpps.dpssmoketest.service.ptpu.SmokeTestServicePtpu
import javax.validation.constraints.NotNull

@Tag(name = "DPS Smoke Tests")
@RestController
class SmokeTestResource(private val smokeTestServicePtpu: SmokeTestServicePtpu) {

  @PostMapping("/smoke-test/prison-to-probation-update/{testProfile}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
  @PreAuthorize("hasRole('SMOKE_TEST')")
  @Operation(
    summary = "Start a new smoke test for prison to probation update",
    description =
      """
        This tests the prison-to-probation-update happy path, which means the following scenarios are working:
          Events are generated by Prison Offender Events
          hmpps-auth is providing tokens to access prison-api
          prison-api allows us to query Nomis
          prison-to-probation-update matching is working
          probation-offender-search is working
          hmpps-auth is providing tokens to access community-api
          communty-api allows us to update Delius
      """
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token"
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires role ROLE_SMOKE_TEST"
      )
    ]
  )
  fun smokeTestPtpu(
    @Parameter(
      name = "testProfile",
      description = "The profile that provides the test data",
      example = "PTPU_T3",
      required = true
    ) @NotNull @PathVariable(value = "testProfile") testProfile: String
  ): Flux<TestStatus> = runCatching { PtpuTestProfiles.valueOf(testProfile).profile }
    .map { smokeTestServicePtpu.runSmokeTest(it) }
    .getOrDefault(Flux.just(TestStatus("Unknown test profile $testProfile", FAIL)))

  @Schema(description = "One of a sequence test statuses. The last status should have progress SUCCESS or FAIL if the test concluded.")
  data class TestStatus(
    @Schema(description = "Human readable description of the latest test status")
    val description: String,
    @Schema(description = "The current progress of the test")
    val progress: TestProgress = TestProgress.INCOMPLETE
  ) {
    fun testComplete() = this.progress != TestProgress.INCOMPLETE
    fun hasResult() = this.progress == TestProgress.SUCCESS || this.progress == FAIL

    @Schema(description = "The current progress of a test")
    enum class TestProgress {
      INCOMPLETE, COMPLETE, SUCCESS, FAIL;
    }
  }
}
