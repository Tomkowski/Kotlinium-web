package tools

import aspects.testCaseSteps
import business.environmentURL
import com.google.gson.Gson
import model.TestCaseReport
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File


annotation class Jira(val id: String = "")
annotation class Description(val text: String = "")

//name of currently run test
//used in AspectJ for screenshot naming
lateinit var testName: String
private val reportsGenerated = mutableListOf<TestCaseReport>()

abstract class KotliniumTest {

    class MyWatcher : TestWatcher {
        override fun testSuccessful(context: ExtensionContext?) {
            super.testSuccessful(context)
            setStackTrace("")
        }

        override fun testFailed(context: ExtensionContext?, cause: Throwable?) {
            super.testFailed(context, cause)
            cause?.let {
                setStackTrace("<b>${cause.message}</b><br>${cause.stackTrace?.joinToString("<br>")}")
            }
        }

        private fun setStackTrace(message: String) {
            reportsGenerated.last().stackTrace = message
        }
    }

    @BeforeEach
    fun setUp(testInfo: TestInfo) {
        testName = testInfo.testMethod.get().name
        openPage(environmentURL)
    }

    @AfterEach
    fun after(testInfo: TestInfo) {
        val methodName = testInfo.testMethod.get().name
        val jiraId =
            testInfo.testMethod.get().annotations.findValue("Jira")?.let { (it as Jira).id } ?: "No Jira ID specified"
        val description =
            testInfo.testMethod.get().annotations.findValue("Description")?.let { (it as Description).text } ?: methodName

        // copy of testCaseSteps has to be sent to avoid clearing it
        reportsGenerated.add(TestCaseReport(jiraId, methodName, description, testCaseSteps.toList()))
        // clear already added steps
        testCaseSteps.clear()
        //reset browser
        driver.manage().deleteAllCookies()
    }

    companion object {

        @BeforeAll
        @JvmStatic
        fun init() {
            driver = run {
                val driverOptions = with(File("./src/test/resources/driver.properties")) {
                    if (exists()) readLines().filter { !it.startsWith("#") }
                    else emptyList()
                }
                val options = ChromeOptions().addArguments(driverOptions)
                println(driverOptions)
                ChromeDriver(options)
            }
            //File("./reports/").deleteRecursively()
        }

        @AfterAll
        @JvmStatic
        fun generateSummary(testInfo: TestInfo) {
            //split by '.' and get last "java.test.SmokeTests" -> "SmokeTests"
            val testSetName = testInfo.testClass.get().name.split(".").last()
            println("Done all tests: $testSetName")
            driver.quit()

            val jsonOutput = File("./reports/json/$testSetName.json").apply{
                delete()
                mkSubDirs()
            }
            jsonOutput.writeText(Gson().toJson(reportsGenerated))

            ReportBuilder.generateReportSummary(testSetName)
            reportsGenerated.clear()
        }
    }
}