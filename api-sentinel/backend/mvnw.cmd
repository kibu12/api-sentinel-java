@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Batch Script for API Sentinel
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "" @ECHO OFF

SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

IF EXIST "%USERPROFILE%\.maven\apache-maven-3.9.9\bin\mvn.cmd" (
    "%USERPROFILE%\.maven\apache-maven-3.9.9\bin\mvn.cmd" %*
) ELSE (
    mvn %*
)
