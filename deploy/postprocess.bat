@rem %1 -- dir

@rem 把指定目录下的html文件的javascript执行结果固化到静态html文件中

set SCRIPT_DIR=%~dp0

@echo off
for /r %1 %%i in (*.html) do (
    call %SCRIPT_DIR%\postprocessOneFile.bat %%i
	)
	
@echo on	
@rem pause

