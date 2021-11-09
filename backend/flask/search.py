from elasticsearch import Elasticsearch

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
