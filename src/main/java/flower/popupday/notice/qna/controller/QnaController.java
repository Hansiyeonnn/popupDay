package flower.popupday.notice.qna.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

public interface QnaController {

    public ModelAndView addQna(HttpServletRequest request, HttpServletResponse response) throws Exception;

    // 폼
    public ModelAndView qnaForm (HttpServletRequest request, HttpServletResponse response) throws Exception;

    // 리스트
    public ModelAndView qnaList (@RequestParam(value = "section", required = false) String _section, @RequestParam(value = "pageNum", required = false)
    String _pageNum,HttpServletRequest request, HttpServletResponse response) throws Exception;

    public ModelAndView modQna(HttpServletRequest request, HttpServletResponse response) throws Exception;


    public ModelAndView removeQna(@RequestParam("qna_id") int qna_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
