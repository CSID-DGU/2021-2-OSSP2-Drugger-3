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

    

def newMember(id, password, session):
    isDuplicate = bool(session.query(MEMBER).filter_by(id=id).first())
    
    if isDuplicate :
        return
    else :
        user = MEMBER(id, password)
        session.add(user)
        session.commit()


