import argparse
import logging
import boto3
from botocore.exceptions import ClientError
import requests

logger = logging.getLogger(__name__)


def generate_presigned_post(s3_client, method_parameters, expires_in):
    """
    Generate a presigned Amazon S3 URL that can be used to perform an action.

    :param s3_client: A Boto3 Amazon S3 client.
    :param method_parameters: The parameters of the specified client method.
    :param expires_in: The number of seconds the presigned URL is valid for.
    :return: The presigned URL.
    """
    try:
        post = s3_client.generate_presigned_post(Bucket=method_parameters['Bucket'], Key='${filename}', ExpiresIn=expires_in)
        logger.info("Got presigned post: %s", post)
    except ClientError:
        logger.exception(
            "Couldn't get a presigned POST."
        )
        raise
    return post 


def usage_demo():
    logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

    print("-" * 88)
    print("Welcome to the Amazon S3 presigned URL demo.")
    print("-" * 88)

    parser = argparse.ArgumentParser()
    parser.add_argument("bucket", help="The name of the bucket.")
    parser.add_argument(
        "file",
        help="Local file to upload",
    )
    args = parser.parse_args()

    s3_client = boto3.client("s3")
    response = generate_presigned_post(
        s3_client, {"Bucket": args.bucket}, 1000
    )

    print("Using the Requests package to send a request to the S3.")
    print("Posting data.")
    try:
        with open(f'./{args.file}', "r") as object_file:
            response = requests.post(response['url'], data=response['fields'], files={'file': object_file})
    except FileNotFoundError:
        print(
            f"Couldn't find {args.file}."
            f"name of a file that exists on your computer."
        )

    if response is not None:
        print("Got response:")
        print(f"Status: {response.status_code}")
        print(response.text)

    print("-" * 88)


if __name__ == "__main__":
    usage_demo()


