import requests
from requests_auth_aws_sigv4 import AWSSigV4

r = requests.request('POST', 'https://sts.us-east-1.amazonaws.com', 
    data=dict(Version='2011-06-15', Action='GetCallerIdentity'), 
    auth=AWSSigV4('sts', region='us-east-1'))
print(r.text)