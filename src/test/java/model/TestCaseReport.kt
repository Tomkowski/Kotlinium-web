package model

data class TestCaseReport(
    val jiraID: String,
    val testCaseName: String,
    val description: String,
    val stepsList: List<TestStepReport>,
    var stackTrace: String = "")
