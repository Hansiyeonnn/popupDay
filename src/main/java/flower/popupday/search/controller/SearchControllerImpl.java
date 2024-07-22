package flower.popupday.search.controller;

import flower.popupday.search.dto.SearchDTO;
import flower.popupday.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class SearchControllerImpl implements SearchController {

    @Autowired
    private SearchService searchService;

    @Override
    @GetMapping("/search")
    public ModelAndView search(@RequestParam("query") String query, Model model) {
        List<SearchDTO> results = searchService.searchHashTags(query);
        model.addAttribute("results", results);
        return new ModelAndView("/popup/searchList"); // 수정된 뷰 이름
    }
}
