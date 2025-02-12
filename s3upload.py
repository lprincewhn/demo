import os
import requests
import hashlib
import math
import time
import hmac

# AWS S3 bucket details
AWS_ACCESS_KEY_ID = ''
AWS_SECRET_ACCESS_KEY = ''
BUCKET_NAME = ''
FILE_PATH = ''
OBJECT_KEY = ''

# Multipart upload parameters
PART_SIZE = 5 * 1024 * 1024  # 5 MB

def upload_file_to_s3(file_path, object_key):
    # Get file size
    file_size = os.path.getsize(file_path)

    # Initiate multipart upload
    upload_id = initiate_multipart_upload(object_key)

    # Upload parts
    parts = []
    for part_num in range(1, math.ceil(file_size / PART_SIZE) + 1):
        start = (part_num - 1) * PART_SIZE
        end = min(start + PART_SIZE, file_size)
        part_data = read_file_part(file_path, start, end)
        part_etag = upload_part(object_key, upload_id, part_num, part_data)
        parts.append({'PartNumber': part_num, 'ETag': part_etag})

    # Complete multipart upload
    complete_multipart_upload(object_key, upload_id, parts)

    print(f"File '{object_key}' uploaded successfully to S3 bucket '{BUCKET_NAME}'.")

def initiate_multipart_upload(object_key):
    url = f"https://{BUCKET_NAME}.s3.amazonaws.com/{object_key}?uploads"
    headers = {
        'Authorization': f'AWS {AWS_ACCESS_KEY_ID}:{sign_request("POST", object_key, "")}'
    }
    response = requests.post(url, headers=headers)
    print(response)
    return response.json()['UploadId']

def upload_part(object_key, upload_id, part_number, part_data):
    url = f"https://{BUCKET_NAME}.s3.amazonaws.com/{object_key}?partNumber={part_number}&uploadId={upload_id}"
    headers = {
        'Content-Length': str(len(part_data)),
        'Content-MD5': calculate_md5(part_data),
        'Authorization': f'AWS {AWS_ACCESS_KEY_ID}:{sign_request("PUT", object_key, part_number, upload_id)}'
    }
    response = requests.put(url, data=part_data, headers=headers)
    return response.headers['ETag']

def complete_multipart_upload(object_key, upload_id, parts):
    url = f"https://{BUCKET_NAME}.s3.amazonaws.com/{object_key}?uploadId={upload_id}"
    data = {
        'Parts': parts
    }
    headers = {
        'Content-Type': 'application/xml',
        'Authorization': f'AWS {AWS_ACCESS_KEY_ID}:{sign_request("POST", object_key, upload_id)}'
    }
    response = requests.post(url, json=data, headers=headers)

def read_file_part(file_path, start, end):
    with open(file_path, 'rb') as file:
        file.seek(start)
        return file.read(end - start)

def calculate_md5(data):
    md5 = hashlib.md5()
    md5.update(data)
    return md5.hexdigest()

def sign_request(method, object_key, upload_id, content_type=''):
    string_to_sign = f"{method}\n\n{content_type}\n\nx-amz-date:{time.strftime('%Y%m%dT%H%M%SZ', time.gmtime())}\n/{BUCKET_NAME}/{object_key}?uploadId={upload_id}"
    signature = hmac.new(AWS_SECRET_ACCESS_KEY.encode(), string_to_sign.encode(), hashlib.sha1).hexdigest()
    return signature

if __name__ == "__main__":
    upload_file_to_s3(FILE_PATH, OBJECT_KEY)