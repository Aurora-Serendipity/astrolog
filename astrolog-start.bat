@echo off
chcp 65001 >nul
cd /d D:\AstroLog-Design
mvn exec:java -Dexec.mainClass="com.astrolog.AppMain"
