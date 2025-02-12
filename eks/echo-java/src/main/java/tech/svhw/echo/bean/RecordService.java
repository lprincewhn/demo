package tech.svhw.echo.bean;

public interface RecordService {

    Record getRecordById(Record r);

    int putRecord(Record r);

    int updateRecord(Record r);
}