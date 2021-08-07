set -x
set -e
timestamp=$(date +%s)

function rds() {
    filename=mysql-query-"${timestamp}".log
    mysql --login-path=root --database svp --table <mysql.sql >"${filename}"
}

$1