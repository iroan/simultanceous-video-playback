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
    curl -X POST ${SERVER_URL}/status -d @api-params/newSession.json
}

function curlPostUpdateProgress() {
    curl -X POST ${SERVER_URL}/status -d @api-params/updateProgress.json
}

function curlLogin() {
    curl -X POST ${SERVER_URL}/login -d @api-params/login.json
}

function joinSession() {
    curl -X POST ${SERVER_URL}/status -d @api-params/joinSession.json
}

function apiGet() {
    queryString="action=masterAccount&account=wangkaixuan&token=045ba410-ae0c-41d4-b8d9-b7c9a3f20337"
    curl -X GET ${SERVER_URL}/status?"${queryString}"

    queryString="action=sessionProgress&account=wangkaixuan&token=045ba410-ae0c-41d4-b8d9-b7c9a3f20337&master=wangkaixuan"
    curl -X GET ${SERVER_URL}/status?"${queryString}"
}

function apiTest() {
    curlPostNewSession
    curlPostUpdateProgress
    apiGetMasterAccount
}

$1 > "$1.log" 2>&1
