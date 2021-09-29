package model

annotation class Jira(val id: String = "")
annotation class Description(val text: String = "")

/**
 * Specifies WebElement which does not change during test case execution.
 * It will be evaluated only once.
 */
annotation class Static

/**
 * WebElement annotated with this will drop all access rules.
 * Reference to it will be instantly returned without waiting for it to be displayed etc.
 */
annotation class Invisible

/**
 * Defines how annotated step will be presented in summary report.
 * Each '%s' will be replaced with consecutive function parameters
 * e.g.: "Lorem %s ipsum %s" for arguments ("some", 10.0) --> Lorem some ipsum 10.0
 */
annotation class DisplayName(val name: String)