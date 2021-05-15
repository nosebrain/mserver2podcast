#!/bin/bash

DOWNLOAD_PATH=$1
wget -q https://verteiler1.mediathekview.de/Filmliste-akt.xz $DOWNLOAD_PATH/Filmliste-akt.xz
mv $DOWNLOAD_PATH/Filmliste-akt $DOWNLOAD_PATH/Filmliste-akt_alt
unxz $DOWNLOAD_PATH/Filmliste-akt.xz
