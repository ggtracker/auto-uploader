@echo off
cd "%~dp0"
set PATH=%PATH%;c:\Program Files (x86)\Java\jre6\bin\;c:\Program Files\Java\jre6\bin\
java -cp lib/ggtracker-uploader.jar;lib/jna.jar;lib/platform.jar com/ggtracker/uploader/GgtrackerUploader %*
