#!/bin/bash
cd "${0%/*}"
java -cp lib/ggtracker-uploader.jar:lib/jna.jar:lib/platform.jar com/ggtracker/uploader/GgtrackerUploader "$@"