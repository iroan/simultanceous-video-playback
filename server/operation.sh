set -x

function clean() {
    rm *.class
}

function db() {
    mysqlsh --host ecs --user root --port 7107 --table --database svp --file svp.sql
}

function api() {
    # curl 'http://localhost:8000/login?fName=Wang&lName=Kaixuan'
    curl -X POST 'http://localhost:8000/login' -d @params.json
}

function run() {
    export SVP_CONF_FILE=dev.yaml
    gradle run
}

if [ "$1" == "run" ]; then
    run
    elif [ "$1" == "api" ]; then
    api
    elif [ "$1" == "clean" ]; then
    clean
    elif [ "$1" == "db" ]; then
    db
fi
