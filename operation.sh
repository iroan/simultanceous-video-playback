set -x
set -e

timestamp=$(date +%s)

# SERVER_URL=http://192.168.50.174:18605/
SERVER_URL=http://localhost:18605

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
    curl -X POST ${SERVER_URL}/status -d @newSession.json
}

function curlPostUpdateProgress() {
    curl -X POST ${SERVER_URL}/status -d @updateProgress.json
}

function curlLogin() {
    curl -X POST ${SERVER_URL}/login -d @api-params/login.json
}
function apiGetMasterAccount() {
    queryString="action=masterAccount&account=wangkaixuan&token=122EE42F-FA57-4779-96D9-EA3821DFE4DE&progress=1"
    curl -v -X GET ${SERVER_URL}/status?"${queryString}"
}

$1