@rem %1 -- file

@rem 把指定目录下的html文件的javascript执行结果固化到静态html文件中

@set SCRIPT_DIR=%~dp0


echo %1
pushd %~dp1
phantomjs  --output-encoding=utf8 --script-encoding=utf8 %SCRIPT_DIR%\url2src.js %~n1%~x1 %~n1.static.html
del %~n1%~x1
mv %~n1.static.html %~n1%~x1
popd


@rem pause

