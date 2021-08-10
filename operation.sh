set -x
set -e

timestamp=$(date +%s)

# remote
SERVER_URL=http://localhost:18605
REDIS_URI=redis://9e15-a7de0e9792a8@ecs:7108/0
MYSQL_CONN="mysqlsh -h ecs -u root -P 7107 --table --database svp "

# local
# SERVER_URL=http://192.168.50.174:18605/
# MYSQL_CONN="mysql --login-path=root --database svp --table <"

function resetDB() {
    ${MYSQL_CONN} --sql -e "show tables;"
    ${MYSQL_CONN} --sql -e "truncate table user;"
    redis-cli -u ${REDIS_URI} flushdb
}

function rds() {
    ${MYSQL_CONN} -f  mysql.sql
}

function redis() {
    redis-cli -u ${REDIS_URI} info
}

function curlPostNewSession() {
    curl -X POST ${SERVER_URL}/status -d @newSession.json
}

function curlPostUpdateProgress() {
    curl -X POST ${SERVER_URL}/status -d @updateProgress.json
}

function curlLogin() {
    curl -X POST ${SERVER_URL}/login -d @api-params/login.json
}

function apiGetMasterAccount() {
    queryString="action=masterAccount&account=wangkaixuan&token=6613b5ce-6b86-4058-98af-43987cdddbeb&progress=1"
    curl -X GET ${SERVER_URL}/status?"${queryString}"
}

function apiTest() {
    curlPostNewSession
    curlPostUpdateProgress
    apiGetMasterAccount
}

$1 > "$1.log" 2>&1
