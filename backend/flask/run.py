from flask import Flask, request, jsonify, session 
from flask_cors import CORS
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from login import newMember, userlogin
from allergy import addAllergy, deleteAllergy, getUserInfo
from flask_swagger_ui import get_swaggerui_blueprint

# 데이터 베이스 불러오기
app = Flask(__name__,static_url_path='',static_folder="static") #html 폴더 경로 설정
app.config['JSON_AS_ASCII'] = False
app.config['SECRET_KEY'] = 'wcsfeufhwiquehfdx'
app.config.from_pyfile('config.py')
database = create_engine(app.config['DB_URL'], encoding = 'utf-8', max_overflow = 0)
Session = sessionmaker(database)
db_session = Session()

# swagger 설정 
CORS(app)
SWAGGER_URL = '/swagger'
API_URL = '/swagger.json'
swaggerui_blueprint = get_swaggerui_blueprint(

    SWAGGER_URL,
    API_URL,
    config = {
        'app_name':"Drugger"
    }
)

app.register_blueprint(swaggerui_blueprint, url_prefix=SWAGGER_URL)


@app.route('/')
def index():
    return "Hello World"

@app.route('/SignIn',methods=['POST'])
def signin():
    data=request.get_json()
    id=data['Id']
    pwd=data['Pw']
    result = newMember(id, pwd, db_session)
    return result

@app.route('/login', methods=['POST'])
def login():
    data=request.get_json()
    user_id = data['Id']
    pwd = data['Pw']
    valid = userlogin(user_id, pwd, db_session, session)
    return valid


@app.route('/logout', methods=['GET'])
def logout():
    session.pop('userID', None)
    return "로그아웃 완료"
        
@app.route('/main', methods=['GET'])
def main():
    result = getUserInfo(db_session, session)
    return result

@app.route('/edit/<int:option>', methods=['POST'])
def edit(option):
    data=request.get_json()
    info = data
    if option == 1 : # 약물 추가 
        result = addAllergy(info[0], db_session, session)
    else:  # 약물 삭제 
        result = deleteAllergy(info, db_session, session)
    return result


if __name__ == '__main__':
    app.run(debug=True)

    