set SRC_DIR=D:\Develop\AndroidSDK\docs\guide
set DST_DIR=D:\epub

xcopy google\*.* %DST_DIR%\google\ /S /F /R /Y /E
xcopy google-code-prettify\*.* %DST_DIR%\google-code-prettify\ /S /F /R /Y /E

call preprocess.bat %SRC_DIR% %DST_DIR%
call postprocess.bat %DST_DIR%
