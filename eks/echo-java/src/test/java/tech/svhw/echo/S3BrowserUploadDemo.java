package tech.svhw.echo;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
/**
 https://docs.aws.amazon.com/zh_cn/AmazonS3/latest/API/sigv4-post-example.html
 */
public class S3BrowserUploadDemo {
    private static Logger logger = Logger.getLogger(S3BrowserUploadDemo.class.getName());
    String endpoint="http://s3.ap-northeast-1.amazonaws.com";
    String Region ="ap-northeast-1";
    // The existing bucket name
    String bucketName = "www.svhw.tech";
    public void postObject() throws Exception {
        String urlStr = endpoint.replace("http://", "http://" + bucketName + ".");
        ProfileCredentialsProvider c = new ProfileCredentialsProvider();
        String accessKeyId = c.getCredentials().getAWSAccessKeyId();
        String accessKeySecret = c.getCredentials().getAWSSecretKey();
        Date date = new Date();
        String currentDate = getDate(date);
        String credential = accessKeyId+"/"+currentDate+"/"+Region+"/s3/aws4_request";//AKIAIOSFODNN7EXAMPLE/20130728/us-east-1/s3/aws4_request.
        String  xAmzDate = getTimeStamp(date);
        //  Create Policy
        String expiration = "2024-10-31T00:00:00Z"; //Policy的过期时间，以ISO8601 GMT时间表示。例如2020-06-12T0:00:00Z指定了Post请求必须在2020年06月12日0点之前。
        PolicyPost policyPost = new PolicyPost();
        policyPost.setExpiration(expiration);
        HashMap Conditions = new HashMap();
        Conditions.put("bucket",bucketName);
        Conditions.put("x-amz-algorithm","AWS4-HMAC-SHA256"); //签名的版本V4，有效期最长7天。
        Conditions.put("x-amz-date",xAmzDate);
        Conditions.put("x-amz-credential", credential);
        Conditions.put("key", "1.txt");
        policyPost.setConditions(Conditions);
        JsonObject policyJson = policyPost.outPutPolicy();
        String encodePolicy = new String(Base64.encodeBase64(policyJson.toString().getBytes()));
        // Signature
        String signature = Signature.computeSignature(currentDate,Region,accessKeySecret,encodePolicy);
        logger.info("x-amz-Signature:"+signature);
        logger.info("policy:"+encodePolicy);
        logger.info("x-amz-date:"+xAmzDate);
        logger.info("x-amz-credential:"+credential);
        logger.info("url:"+urlStr);

        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody = RequestBody.create(MediaType.parse("text/plain"), "abcd");
        RequestBody formBody = new MultipartBody.Builder()
                .addFormDataPart("key", "1.txt")
                .addFormDataPart("x-amz-credential", credential)
                .addFormDataPart("x-amz-Signature", signature)
                .addFormDataPart("policy", encodePolicy)
                .addFormDataPart("x-amz-date", xAmzDate)
                .addFormDataPart("x-amz-algorithm", "AWS4-HMAC-SHA256")
                .addFormDataPart("file", "1.txt", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(urlStr)
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        logger.info(""+response.code()+": "+response.body().string());
    }
    private String getDate(Date date) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//server timezone
        return dateFormat.format(date);
    }
    private String getTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//server timezone
        return dateFormat.format(date);
    }
    public static void main(String[] args) throws Exception {
        S3BrowserUploadDemo PostObjectDemo = new S3BrowserUploadDemo();
        PostObjectDemo.postObject();
    }
}
class Signature{
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }
    public static  byte[] HmacSHA256(byte[] key, String data) throws Exception {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes());
    }
    public  static  String computeSignature(String currentDate,String Region, String accessKeySecret, String encodePolicy)
            throws Exception {
        String serviceName = "s3";
        byte[] kSecret = ("AWS4" + accessKeySecret).getBytes();
        byte[] kDate = Signature.HmacSHA256(kSecret, currentDate);
        byte[] kRegion = Signature.HmacSHA256(kDate, Region);
        byte[] kService = Signature.HmacSHA256(kRegion, serviceName);
        byte[] kSigning = Signature.HmacSHA256(kService, "aws4_request");
        byte[] signature = Signature.HmacSHA256(kSigning, encodePolicy);
        String strHexSignature = Signature.bytesToHex(signature);
        return  strHexSignature;
    }
}
class PolicyPost {
    JsonObject policyJson = new JsonObject();
    JsonArray arrayPlayer = new JsonArray();
    public  void setExpiration(String expiration) {
        policyJson.addProperty("expiration", expiration);
    }
    public  void setConditions(HashMap<String, String> conditions) {
        for (Entry<String, String> entry : conditions.entrySet()) {
            JsonObject tmp = new JsonObject();
            tmp.addProperty(entry.getKey(), entry.getValue());
            arrayPlayer.add(tmp);
        }
        policyJson.add("conditions", arrayPlayer);
    }
    public JsonObject outPutPolicy(){
        return  policyJson;
    }
}