from flask import Flask, request, jsonify
from flask_cors import CORS
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from login import newMember
from flask_swagger_ui import get_swaggerui_blueprint

# 데이터 베이스 불러오기
app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False
app.config.from_pyfile('config.py')
database = create_engine(app.config['DB_URL'], encoding = 'utf-8', max_overflow = 0)
Session = sessionmaker(database)
session = Session()

# swagger 설정 
app = Flask(__name__,static_url_path='',static_folder="static") #html 폴더 경로 설정
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
    result = newMember(id, pwd, session)
    return "OK"

if __name__ == '__main__':
    app.run(debug=True)