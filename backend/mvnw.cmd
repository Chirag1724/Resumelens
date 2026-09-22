@REM
@REM Maven Wrapper startup batch script for Windows
@REM
@SETLOCAL
@SET DP0=%~dp0
@REM Strip trailing backslash from project dir
@SET PROJECT_DIR=%DP0:~0,-1%
@SET MAVEN_WRAPPER_JAR="%DP0%.mvn\wrapper\maven-wrapper.jar"
@java -Dmaven.multiModuleProjectDirectory="%PROJECT_DIR%" -cp %MAVEN_WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*
@ENDLOCAL
