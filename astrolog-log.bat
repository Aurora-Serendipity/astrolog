@echo off
chcp 65001 >nul
mysql -u root -p -e "SELECT log_id, user_id, operation, detail, create_time FROM astrolog.operation_logs ORDER BY create_time DESC LIMIT 20;"
