# flask_web/app.py

import requests
from flask import Flask
from flask import request

app = Flask(__name__)

@app.route('/echo', methods=['GET', 'POST'])
def getrequest():
    response = requests.get('http://169.254.169.254/latest/meta-data/placement/availability-zone')
    res = '<h3>' + 'AZ' + ': ' + response.text + '</h3>'
    response = requests.get('http://169.254.169.254/latest/meta-data/instance-id')
    res += '<h3>' + 'Instance ID' + ': ' + response.text + '</h3>'
    res += '<h3>' + 'URL' + ':  ' + request.url + '</h3>'
    res += '<h3>' + 'Method' + ':  ' + request.method + '</h3>'
    for h in request.headers:
        res += '<h3>' + h[0] + ':  ' + h[1] + '</h3>'
    res += '<p>' + request.data.decode('utf-8') + '</p>'
    print(res)
    return res

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')

