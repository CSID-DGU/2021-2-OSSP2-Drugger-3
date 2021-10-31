from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, String, Integer

Base = declarative_base()

class Allergy(Base):
    __tablename__ ='ALLERGY'

    id_num = Column(Integer, nullable=False, primary_key=True)
    Mname = Column(String(100), nullable=False, primary_key=True)
    Mmaterial = Column(String(100), nullable=False, primary_key=True)
    Symptom = Column(String(100), nullable=False, primary_key=True)

    def __init__(self, id_num, Mname, Mmaterial, Symptom):
        self.id_num = id_num
        self.Mname = Mname
        self.Mmaterial = Mmaterial
        self.Symptom = Symptom


def getUserInfo(db_session, session):
    allData = db_session.query(Allergy).filter_by(id_num=session['userID'])
    
    result = {}
    for data in allData :
        medicine = []
        medicine['Mname'] = data['Mname']
        medicine['Mmaterial'] = data['Mmaterial']
        medicine['Symptom'] = data['Symptom']
        result.append(medicine)
    
    return result

def addAllergy(info, db_session, session):
    newInfo = Allergy(session['userID'],info['Mname'], info['Mmaterial'], info['Symptom'])
    db_session.add(newInfo)
    db_session.commit()
    return "알러지 항목 추가가 완료되었습니다."

def deleteAllergy(infos, db_session, session):

    for info in infos :
        deleteData = db_session.query(Allergy).filter_by(id_num=session['userID'],
                                        Mname=info['Mname'], Mmaterial=info['Mmaterial'], Symptom=info['Symptom']).first()
        db_session.delete(deleteData)
    db_session.commit()
    return "알러지 항목 삭제가 완료되었습니다."



