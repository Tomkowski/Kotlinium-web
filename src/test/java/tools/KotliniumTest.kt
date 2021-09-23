package tools

import aspects.getScreenshot
import aspects.testCaseSteps
import business.environmentURL
import com.google.gson.Gson
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

//name of currently run test
//used in AspectJ for screenshot naming
lateinit var testName: String
private val reportsGenerated = mutableListOf<TestCaseReport>()

@ExtendWith(KotliniumTest.KotliniumWatcher::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class KotliniumTest() {

    class KotliniumWatcher : TestWatcher {
        override fun testSuccessful(context: ExtensionContext?) {
            super.testSuccessful(context)
            setStackTrace("")
        }

        override fun testFailed(context: ExtensionContext?, cause: Throwable?) {
            super.testFailed(context, cause)
            cause?.let {
                setStackTrace("<b>${cause.message}<br>${cause.cause}</b><br>${cause.stackTrace?.joinToString("<br>")}")
            }
            if(System.getProperty("screenshot.strategy") == "on-fail"){
                with(reportsGenerated.last().stepsList.last()){
                    screenshotPath = getScreenshot(testName, stepName)
                }
            }
        }


        private fun setStackTrace(message: String) {
            reportsGenerated.last().stackTrace = message
        }
    }

    @BeforeEach
    fun setUp(testInfo: TestInfo) {
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
    }


    @BeforeAll
    fun init() {
        driver = run {
            //get all driver options specified in driver.properties file. Ignore lines starting with '#'
            val driverOptions = with(File("./src/test/resources/driver.properties")) {
                if (exists()) readLines().filter { !it.startsWith("#") }
                else emptyList()
            }
            logger.info("$driverOptions")
            //return web driver as specified in driver.properties file. Default is Chrome.
            return@run when (System.getProperty("webdriver.type")) {
                "chrome" -> ChromeDriver(ChromeOptions().addArguments(driverOptions))
                "firefox" -> FirefoxDriver(FirefoxOptions().addArguments(driverOptions))
                "opera" -> OperaDriver(OperaOptions().addArguments(driverOptions))
                else -> ChromeDriver(ChromeOptions().addArguments(driverOptions))
            }
        }
    }

    @AfterAll
    fun generateSummary(testInfo: TestInfo) {
        //split by '.' and get last "java.test.SmokeTests" -> "SmokeTests"
        val testSetName = testInfo.testClass.get().name.split(".").last()
        logger.info("Done all tests: $testSetName")
        driver.quit()
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