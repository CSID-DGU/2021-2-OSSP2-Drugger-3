from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, String, Integer
from sqlalchemy.sql.sqltypes import TIMESTAMP
from sqlalchemy.dialects.mysql import BIGINT

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

class Medicine(Base):
    __tablename__ = 'MEDICINE'

    id = Column(BIGINT(unsigned=True))
    Mname = Column(String(100), nullable=False, primary_key=True)
    Mmaterial = Column(String(100), nullable=False, primary_key=True)
    modification_time = Column(TIMESTAMP, nullable=False)
    insertion_time = Column(TIMESTAMP, nullable=False)

    def __init__(self, id_num, Mname, modification_time, insertion_time):
        self.id_num = id_num
        self.Mname = Mname
        self.Mmaterial = Mmaterial
        self.modification_time = modification_time
        self.insertion_time = insertion_time

def getUserInfo(db_session, session):
    result = []
    try :
        allData = db_session.query(Allergy).filter_by(id_num=session['userID'])
    except:
        db_session.rollback()
        return result, "fail"
    finally:
        db_session.close()
    
    for data in allData :
        medicine = {}
        medicine['Mname'] = data.Mname
        medicine['Mmaterial'] = data.Mmaterial
        medicine['Symptom'] = data.Symptom
        result.append(medicine)
    return result, "success"

def addAllergy(info, db_session, session):
    newInfo = Allergy(session['userID'],info['Mname'], info['Mmaterial'], info['Symptom'])

    try :
        db_session.add(newInfo)
        db_session.commit()
    except:
        db_session.rollback()
        return "fail"
    finally:
        db_session.close()
   
    return "success"


def deleteAllergy(infos, db_session, session):

    try : 
        for info in infos :
            deleteData = db_session.query(Allergy).filter_by(id_num=session['userID'],
                                            Mname=info['Mname'], Mmaterial=info['Mmaterial'], Symptom=info['Symptom']).first()
            db_session.delete(deleteData)
        db_session.commit()
    except:
        db_session.rollback()
        return "fail"
    finally:
        db_session.close()
    return "success"

def medicineAnalysis(mname, db_session, session):
    result = []

    for name in mname:
        analysis = {}
        try :
            medicine = db_session.query(Medicine).filter_by(Mname=name).first()
            if medicine is None:
                analysis['material'] = 'None'
                analysis['status'] = 'FAIL'
                result.append(analysis)
            else : 
                duplicate = db_session.query(Allergy).filter_by(Mmaterial=medicine.Mmaterial, id_num=session['userID']).first()
                if duplicate is not None:
                    analysis['material'] = medicine.Mmaterial
                    analysis['status'] = 'DUPLICATE'
                    result.append(analysis)
                else :
                    analysis['material'] = medicine.Mmaterial
                    analysis['status'] = 'OK'
                    result.append(analysis)

        except:
            db_session.rollback()
            return result, "fail"
        finally:
            db_session.close()
    return result


