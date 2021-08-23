package tools

import aspects.testCaseSteps
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sun.xml.internal.ws.api.ha.StickyFeature
import model.TestCaseReport
import model.TestStepReport
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


object ReportBuilder {

    private val jsonReport = File("./reports/DemoSet.json")
    private val listType = object : TypeToken<ArrayList<TestCaseReport?>?>() {}.type
    private val reportsList = Gson().fromJson<List<TestCaseReport>>(jsonReport.readText(), listType)

    private fun buildStepReport(testStepReport: TestStepReport): String {

        fun buildElement(testStepReport: TestStepReport): String {
            with(testStepReport) {
                val formattedParameters = stepParameters.joinToString(" ") { "[$it]" }
                val formattedTimestamp =
                    SimpleDateFormat("MMM-dd-yyyy HH:mm:ss.SSS", Locale.ENGLISH).format(timestamp) ?: ""
                return """
                <li> 
                    <div style="display: flex; align-items: center; justify-content: space-between">
                            <a style="height: 100%; padding-left: 10px;">
                                $stepName <span style="color: #164694">$formattedParameters</span>
                            </a>
                            <a href='$screenshotPath'>
                                <img src='$screenshotPath'/>
                            </a>

                        </div>
                        <div>
                            <a style="font-size: 12px">($formattedTimestamp)</a>
                     </div>

                </li>
            """.trimIndent()
            }
        }
        return buildElement(testStepReport)
    }

    private fun buildTestCaseReport(testCaseReport: TestCaseReport) {
        with(testCaseReport) {
            val destination = "$testCaseName/report.html"
            val reportTemplate = File("./src/test/resources/reportTemplate.html")
            val reportSummary = File("./reports/$destination").apply { delete() }
            reportTemplate.copyTo(reportSummary)

            reportSummary.readText().apply {
                val refactor = replace("%title%", testCaseName)
                    .replace("%jiraticket%", jiraID)
                    .replace("%description%", description)
                    .replace("%report%", stepsList.joinToString(separator = "\n") { buildStepReport(it) })
                    .replace("%stacktrace%", stackTrace)

                reportSummary.writeText(refactor)
            }
        }
    }

    fun generateReportSummary(testSetName: String) {
        val summaryTemplate = File("./src/test/resources/summaryTemplate.html")
        val reportSummary = File("./reports/$testSetName.html").apply { delete() }
        summaryTemplate.copyTo(reportSummary)

        reportSummary.readText().apply {
            val refactor = replace("%title%", testSetName)
                .replace("%summary%", reportsList.joinToString(separator = "\n") { buildSummaryTestCaseRow(it) })
            reportSummary.writeText(refactor)
        }
        reportsList.forEach { buildTestCaseReport(it) }
        println("Summary available at: ${reportSummary.absolutePath}")
    }

    private fun buildSummaryTestCaseRow(testCaseReport: TestCaseReport): String {
        with(testCaseReport) {
            val destination = "$testCaseName/report.html"
            return """
                <li onclick="location.href='$destination'"><a href='$destination'>$testCaseName</a></li>
            """.trimIndent()
        }
    }
}