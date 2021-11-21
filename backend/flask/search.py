import pytesseract
import shutil
import os
import cv2
import numpy as np
import random
from PIL import Image
import re


def recognizeMedicine(image):
    image = cv2.imread(image)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    gray = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]
    gray = cv2.medianBlur(gray,5)
    extractedInformation = pytesseract.image_to_string(gray, lang='kor')
    information = extractedInformation.split("\n")
    result = {}
    new_string = []
    for info in information:
        for i in range(0, len(info)) :
            if info[i] == '(':
                word = info[:i]
                break
        word = re.sub('[^A-Za-z0-9가-힣]', '', word)
        if word == "":
            continue
        new_string.append(word)
        word = ""
    result['medicine'] = new_string

    return result

def searchMedicine(es_session, mname):
    findMedicine = []
    for name in mname:
        name = '*' + name + '*'
        body = {
            "query": {
                "query_string": {
                    "fields": ["mname"],
                    "query": name
                }
            }
        }
        words = es_session.search(index="medicine", body=body)

        result = []
        for word in words['hits']['hits']:
            medicine = {}
            medicine['mmaterial'] = word['_source']['mmaterial']
            medicine['mname'] = word['_source']['mname']
            result.append(medicine)
        findMedicine.append(result)

    return findMedicine
