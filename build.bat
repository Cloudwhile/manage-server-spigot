@echo off
echo 正在构建 ManageServer 插件...
echo.

REM 检查是否安装了 Maven
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo 错误: 未找到 Maven。请确保已安装 Maven 并添加到系统 PATH 中。
    goto :end
)

REM 检查是否安装了 Java
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo 错误: 未找到 Java。请确保已安装 Java 并添加到系统 PATH 中。
    goto :end
)

REM 设置 MAVEN_OPTS 以增加内存
set MAVEN_OPTS=-Xmx512m

REM 执行 Maven 构建
echo 正在下载依赖并构建插件...
call mvn clean package -U

if %ERRORLEVEL% neq 0 (
    echo.
    echo 构建失败! 请检查上面的错误信息。
) else (
    echo.
    echo 构建成功! 插件 JAR 文件位于 target 目录中。
    echo 文件名: manage-server-1.0-SNAPSHOT.jar
)

:end
echo.
pause 