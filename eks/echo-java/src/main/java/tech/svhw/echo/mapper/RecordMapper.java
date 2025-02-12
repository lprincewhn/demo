package tech.svhw.echo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tech.svhw.echo.bean.Record;

@Mapper
public interface RecordMapper {

    @Select("select * from record where id = #{id,jdbcType=INTEGER}")
    Record getRecordById(Record record);

    @Insert("insert into record ( id, description)" +
            "values ( #{id,jdbcType=INTEGER}, #{description,jdbcType=VARCHAR})")
    int putRecord(Record record);

    @Update("update record set description = #{description,jdbcType=VARCHAR} where id = #{id,jdbcType=INTEGER}")
    int updateRecordById(Record id);
}
