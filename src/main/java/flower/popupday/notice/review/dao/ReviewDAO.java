package flower.popupday.notice.review.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewDAO {
    public List selectAllReview(@Param("count") int count) throws DataAccessException;

    public int getNewReviewId() throws DataAccessException;

    public void insertNewReview(Map articleMap) throws DataAccessException;

    public void insertNewImages(Map articleMap) throws DataAccessException;

    public int selectToReview() throws DataAccessException;
}