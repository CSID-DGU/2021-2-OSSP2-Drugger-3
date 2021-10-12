# 데이터베이스 연결 관련 파일 
db = {
    'user'     : '',
    'password' : '',
    'host'     : '',
    'port'     : '',
    'database' : ''
}

DB_URL = f"mysql+mysqlconnector://{db['user']}:{db['password']}@{db['host']}:{db['port']}/{db['database']}?charset=utf8"