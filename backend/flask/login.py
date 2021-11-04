from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, String, Integer
from sqlalchemy.orm import session

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
        return "duplicate"
    else :
        try : 
            user = MEMBER(id, password)
            db_session.add(user)
            db_session.commit()
        except : 
            db_session.rollback()
            return "fail"
        finally:
            db_session.close()
        return "success"

def userlogin(id, pwd, db_session, session):
    try :
        data = db_session.query(MEMBER).filter_by(id=id).first()
    except:
        db_session.rollback()
        return "fail"
    finally:
        db_session.close()
    if data is not None:
        if data.pw != pwd :
            return "check pwd"
    else :             
        return "check id"

    session['userID'] = data.id_num
    return "success" 



