from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, String, Integer

Base = declarative_base()

class MEMBER(Base):
    __tablename__='USER'

    id = Column(String(30), nullable=False)
    pw = Column(String(30), nullable=False)
    id_num = Column(Integer, nullable=False, primary_key=True)

    def __init__(self, id, password):
        self.id = id
        self.pw = password

    

def newMember(id, password, db_session):
    isDuplicate = bool(db_session.query(MEMBER).filter_by(id=id).first())
    
    if isDuplicate :
        return "중복된 아이디입니다."
    else :
        user = MEMBER(id, password)
        db_session.add(user)
        db_session.commit()
        return "정상적으로 회원가입 완료하였습니다."

def userlogin(id, pwd, db_session, session):
    data = db_session.query(MEMBER).filter_by(id=id).first()
    if data is not None:
        if data.pw != pwd :
            return "비밀번호를 확인해 주세요"
    else :             
        return "아이디를 확인해 주세요"

    session['userID'] = data.id_num
    return "로그인 성공" 



            
