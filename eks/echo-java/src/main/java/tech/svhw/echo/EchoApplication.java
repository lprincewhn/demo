package tech.svhw.echo;

import io.opentelemetry.api.common.Attributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;
import tech.svhw.echo.bean.Record;
import tech.svhw.echo.bean.RecordService;
import java.util.logging.Logger;
import io.opentelemetry.api.trace.Span;

@SpringBootApplication
@RestController
public class EchoApplication {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	RecordService recordService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	static Logger log = Logger.getLogger(EchoApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(EchoApplication.class, args);
	}

	@GetMapping("/echo")
	public String echo(@RequestHeader() Map<String, String> headers) {
		Span.current().setAttribute("TestKey", "TestValue");
		String traceId = Span.current().getSpanContext().getTraceId();
		String spanId = Span.current().getSpanContext().getSpanId();
		String prettyTraceId = "1-" + traceId.substring(0, 8) + "-" + traceId.substring(8);
		log.info("AWS-XRAY-TRACE-ID: " + prettyTraceId + "@" + spanId + ": Received request on /echo");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		response = restTemplate.getForEntity("http://169.254.169.254/latest/meta-data/placement/availability-zone", String.class);
		String az = response.getBody();
		response = restTemplate.getForEntity("http://169.254.169.254/latest/meta-data/instance-id", String.class);
		String instanceId = response.getBody();
		String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI();
		String method = request.getMethod();
		StringBuffer result = new StringBuffer(String.format("<h3>TraceId: %s</h3><h3>AZ: %s</h3><h3>Instance ID: %s</h3><h3>URL: %s</h3><h3>Method: %s</h3>",
				prettyTraceId, az, instanceId, url, method));
		for (Map.Entry entry : headers.entrySet()) {
			result.append(new StringBuffer(String.format("<h3>%s: %s</h3>", entry.getKey(), entry.getValue())));
		}
		result.append(new StringBuffer(String.format("<h3>DynamoDb: %s</h3>", accessDynamoDb())));
		result.append(new StringBuffer(String.format("<h3>Redis: %s</h3>", accessRedis())));
		result.append(new StringBuffer(String.format("<h3>RDS: %s</h3>", accessRDS())));
		return result.toString();
	}

	private boolean accessDynamoDb() {
		boolean isSuc = true;
		StsClient stsClient = StsClient.builder().build();
		GetCallerIdentityResponse result = stsClient.getCallerIdentity(GetCallerIdentityRequest.builder().build());
		log.info("Caller ARN: " + result.arn());

		DynamoDbClient dynamoDB = DynamoDbClient.builder().build();
		String tableName = "Movies";

		try {
			log.info("Attempting to create table; please wait...");
			CreateTableResponse tableResponse = dynamoDB.createTable(CreateTableRequest.builder()
					.tableName(tableName)
					.billingMode(BillingMode.PROVISIONED)
					.provisionedThroughput(ProvisionedThroughput.builder()
							.readCapacityUnits(1L)
							.writeCapacityUnits(2L)
							.build())
					.attributeDefinitions(
							AttributeDefinition.builder()
									.attributeName("year")
									.attributeType(ScalarAttributeType.N)
									.build(),
							AttributeDefinition.builder()
									.attributeName("title")
									.attributeType(ScalarAttributeType.S)
									.build())
					.keySchema(
							KeySchemaElement.builder()
									.attributeName("year")
									.keyType(KeyType.HASH)
									.build(),
							KeySchemaElement.builder()
									.attributeName("title")
									.keyType(KeyType.RANGE)
									.build())
					.build());
			log.info("Success.  Table status: " + tableResponse.tableDescription().tableStatus());
		}
		catch (Exception e) {
			log.severe("Unable to create table: ");
			log.severe("Error in accessDynamoDb:" + e.getMessage());
		}

		final Map<String, AttributeValue> infoMap = new HashMap<>();
		infoMap.put("year", AttributeValue.builder().n("2016").build());
		infoMap.put("title", AttributeValue.builder().s("The Big New Movie").build());
		infoMap.put("plot", AttributeValue.builder().s("Nothing happens at all.").build());
		infoMap.put("rating", AttributeValue.builder().s("0").build());

		;

		try {
			log.info("Adding a new item...");
			PutItemResponse itemResponse = dynamoDB.putItem(
					PutItemRequest.builder()
							.tableName(tableName)
							.item(infoMap)
							.returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
							.build());
			log.info("PutItem succeeded, consume WCU: \n" + itemResponse.consumedCapacity().capacityUnits());

		}
		catch (Exception e) {
			isSuc = false;
			log.severe("Unable to add item: " + "2016" + " " + "The Big New Movie");
			log.severe("Error in accessDynamoDb:" + e.getMessage());
		}
		return isSuc;
	}

	private boolean accessRDS() {
		try {
			Record record = new Record();
			record.setId(1);
			record.setDescription("Test");
			Record result = recordService.getRecordById(record);
			if(result == null) {
				recordService.putRecord(record);
			} else {
				recordService.updateRecord(record);
			}
			return true;
		}
		catch (Exception e) {
			log.severe("Error in accessRDS:" + e.getMessage());
			return false;
		}
	}

	private boolean accessRedis() {
		try {
			redisTemplate.opsForValue().set("1", "Test");
			return true;
		}
		catch (Exception e) {
			log.severe("Error in accessRedis:" + e.getMessage());
			return false;
		}
	}
}
