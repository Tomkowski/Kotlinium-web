package tools

import aspects.getScreenshot
import business.environmentURL
import com.google.gson.Gson
import io.github.bonigarcia.wdm.WebDriverManager
import model.Description
import model.Jira
import model.TestCaseReport
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.opera.OperaDriver
import org.openqa.selenium.opera.OperaOptions
import java.io.File

private val reportsGenerated = mutableListOf<TestCaseReport>()

@ExtendWith(
    KotliniumTest.KotliniumWatcher::class
)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class KotliniumTest {

    class KotliniumWatcher : TestWatcher {
        override fun testSuccessful(context: ExtensionContext?) {
            super.testSuccessful(context)
            logger.info("Test passed for ${context?.uniqueId}")
        }

        override fun testFailed(context: ExtensionContext?, cause: Throwable?) {
            super.testFailed(context, cause)
            logger.warning("Test failed for $testName")
            logger.warning(reportsGenerated.joinToString{"$it\n"})

            cause?.let {
                setStackTrace("<b>${cause.message}<br>${cause.cause}</b><br>${cause.stackTrace?.joinToString("<br>")}")
            }
            if (System.getProperty("screenshot.strategy") == "on-fail") {
                with(reportsGenerated.find { it.testCaseName == testName }?.stepsList!!.last()) {
                    screenshotPath = getScreenshot(testName, stepName)
                }
            }
        }


        private fun setStackTrace(message: String) {
            if (reportsGenerated.isEmpty()) return
            reportsGenerated.find { it.testCaseName == testName }?.stackTrace = message
        }
    }

    @BeforeEach
    fun setUp() {
        testCaseStepsMap[Thread.currentThread().name] = mutableListOf()
        driverMap[Thread.currentThread().name] = run {
            //get all driver options specified in driver.properties file. Ignore lines starting with '#'
            val driverOptions = with(File("./src/test/resources/driver.properties")) {
                if (exists()) readLines().filter { !it.startsWith("#") }
                else emptyList()
            }
            logger.info("$driverOptions")
            //return web driver as specified in driver.properties file. Default is Chrome.
            return@run when (System.getProperty("webdriver.type")) {
                "chrome" -> {
                    WebDriverManager.chromedriver().setup()
                    ChromeDriver(ChromeOptions().addArguments(driverOptions))
                }
                "firefox" ->{
                    WebDriverManager.firefoxdriver().setup()
                    FirefoxDriver(FirefoxOptions().addArguments(driverOptions))
                } "opera" -> {
                    WebDriverManager.operadriver().setup()
                    OperaDriver(OperaOptions().addArguments(driverOptions))
                }
                else -> {
                    logger.warning("Unsupported browser: ${System.getProperty("webdriver.type")}")
                    ChromeDriver(ChromeOptions().addArguments(driverOptions))
                }
            }
        }
        openPage(environmentURL)
    }

    @AfterEach
    fun after(testInfo: TestInfo) {
        val jiraId =
            testInfo.testMethod.get().annotations.findValue(Jira::class)?.let { (it as Jira).id } ?: "No Jira ID specified"
        val description =
            testInfo.testMethod.get().annotations.findValue(Description::class)?.let { (it as Description).text }
                ?: testName

        // copy of testCaseSteps has to be sent to avoid clearing it
        reportsGenerated.add(TestCaseReport(jiraId, testName, description, testCaseSteps.toList()))
        // clear already added steps
        testCaseSteps.clear()
        //reset browser
        driver.manage().deleteAllCookies()
        driver.quit()
        logger.info("Saved data in @AfterEach for $testName")
    }

    companion object {
        @AfterAll
        @JvmStatic
        fun generateSummary(testInfo: TestInfo) {
            //split by '.' and get last "java.test.SmokeTests" -> "SmokeTests"
            val testSetName = testInfo.testClass.get().name.split(".").last()
            logger.info("Done all tests: $testSetName")
            // delete existing one (from previous Test Set execution) and create new in the same place.
            val jsonOutput = File("./reports/json/$testSetName.json").apply {
                delete()
                mkSubDirs()
            }
            jsonOutput.writeText(Gson().toJson(reportsGenerated))

            ReportBuilder.generateReportSummary(testSetName)
            reportsGenerated.clear()
        }
    }
}