set -x
set -e
timestamp=$(date +%s)

function rds() {
    filename=mysql-query-"${timestamp}".log
    mysql --login-path=root --database svp --table <mysql.sql >"${filename}"
}

function redis() {
    set +x
    redis-cli -h amd -p 12052 -a 9e15-a7de0e9792a8
    set -x
}

function curlPostNewSession() {
    curl -X POST http://192.168.50.174:18605/status -d @newSession.json
}

function curlPostUpdateProgress() {
    curl -X POST http://192.168.50.174:18605/status -d @updateProgress.json
}

$1