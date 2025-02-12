package tech.svhw.echo.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.svhw.echo.mapper.RecordMapper;

@Service("RecordService")
public class RecordServiceImpl implements RecordService {

    @Autowired
    RecordMapper recordMapper;


    @Override
    public Record getRecordById(Record r) {
        return recordMapper.getRecordById(r);
    }

    @Override
    public int putRecord(Record r) {
        return recordMapper.putRecord(r);
    }

    @Override
    public int updateRecord(Record r) {
        return recordMapper.updateRecordById(r);
    }
}
