package tools

import aspects.testCaseSteps
import business.environmentURL
import com.google.gson.Gson
import model.TestCaseReport
import org.junit.*
import org.junit.rules.TestWatcher
import java.io.File


annotation class Jira(val id: String = "")
annotation class Description(val text: String = "")

lateinit var watcher: KotliniumTest.MyWatcher
private val reportsGenerated = mutableListOf<TestCaseReport>()

abstract class KotliniumTest {

    class MyWatcher : TestWatcher() {
        lateinit var annotations: Collection<Annotation>
        lateinit var methodName: String
        lateinit var testSetName: String
        override fun starting(description: org.junit.runner.Description?) {
            annotations = description?.annotations ?: emptyList()
            methodName = description?.methodName ?: ""
            //tests.RegressionSet -> RegressionSet
            testSetName = description?.className?.split(".")?.get(1) ?: ""
        }

        override fun succeeded(description: org.junit.runner.Description?) {
            super.succeeded(description)
            setStackTrace("")
        }

        override fun failed(e: Throwable?, description: org.junit.runner.Description?) {
            super.failed(e, description)
            e?.let {
                setStackTrace("<b>${e.message}</b><br>${e.stackTrace?.joinToString("<br>")}")
            }
        }

        private fun setStackTrace(message: String) {
            reportsGenerated.last().stackTrace = message
        }
    }

    @Before
    fun setUp() {
        File("./reports/${watcher.methodName}").deleteRecursively()
        openPage(environmentURL)
    }

    // set watcher globally - used in AspectJ for screenshot naming
    @Rule
    @JvmField
    val testName = MyWatcher().also { watcher = it }

    @After
    fun after() {
        val methodName = watcher.methodName
        val jiraId = watcher.annotations.findValue("Jira")?.let { (it as Jira).id } ?: "No Jira ID specified"
        val description = watcher.annotations.findValue("Description")?.let { (it as Description).text } ?: ""

        // copy of testCaseSteps has to be sent to avoid clearing it
        reportsGenerated.add(TestCaseReport(jiraId, methodName, description, testCaseSteps.toList()))
        // clear already added steps
        testCaseSteps.clear()
        //reset browser
        driver.manage().deleteAllCookies()
    }

    companion object {

        @BeforeClass
        @JvmStatic
        fun init() {
            File("./reports/").deleteRecursively()
        }

        @AfterClass
        @JvmStatic
        fun generateSummary() {
            println("Done all tests: ${watcher.testSetName}")
            driver.quit()

            val jsonOutput = File("./reports/${watcher.testSetName}.json").apply { delete() }
            jsonOutput.writeText(Gson().toJson(reportsGenerated))

            ReportBuilder.generateReportSummary(watcher.testSetName)
        }
    }
}