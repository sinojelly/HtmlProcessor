@rem %1 -- dir

@rem ��ָ��Ŀ¼�µ�html�ļ���javascriptִ�н���̻�����̬html�ļ���

set SCRIPT_DIR=%~dp0

@echo off
for /r %1 %%i in (*.html) do (
    call %SCRIPT_DIR%\postprocessOneFile.bat %%i
	)
	
@echo on	
@rem pause

