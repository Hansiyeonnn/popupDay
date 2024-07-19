package flower.popupday.notice.notice.controller;

import flower.popupday.login.dto.LoginDTO;
import flower.popupday.notice.notice.dto.NoticeDTO;
import flower.popupday.notice.notice.dto.NoticeimageDTO;
import flower.popupday.notice.notice.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.*;

@Controller("noticeController")
public class NoticeControllerImpl implements NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeDTO noticeDTO;


    //이미지 파일을 저장할 디렉토리 경로
    private static String ARRICLE_IMG_REPO = "C:\\Users\\성민석\\OneDrive\\바탕 화면\\popupsave";


    //공지사항 목륵을 보여주는 메서드와 페이징 처리
    @Override
    @RequestMapping("/notice/noticeList.do") //메인 화면엥서 이동했을 때 매핑이름
    public ModelAndView noticeList(@RequestParam(value = "section", required = false) String _section,
                                   @RequestParam(value = "pageNum", required = false) String _pageNum, //매개변수 Section,pageNum을 받으며 값이 없으면 기본적으로 null이 됨.
                                   HttpServletRequest request, HttpServletResponse response) throws DataAccessException {
        int section = Integer.parseInt((_section == null) ? "1" : _section); // '_section'이 null 이면 'section'을 1로 설정하고 그렇지 않으면 '_section'의 값을 정수로 변화하여 'section'에 저장
        int pageNum = Integer.parseInt((_pageNum == null) ? "1" : _pageNum); // 위와 동일한 내용

        Map<String, Integer> pagingMap = new HashMap<>(); // section,pageNum을 저장 할 맵을 저장

        pagingMap.put("section", section); // 1 맵에 seciton 값을 추가 함
        pagingMap.put("pageNum", pageNum); // 1 맵에 pageNum 값을 추가 함

        Map noticeMap = noticeService.noticeList(pagingMap); // 서비스에서 공지사항 글 목록 받아옴

        noticeMap.put("section", section); // noticeMap에 section 값을 추가 함
        noticeMap.put("pageNum", pageNum); // noticeMap에 pageNum 값을 추가 함

        // Debuggin 로그 추가(noticeMap(section,pageNum)이 잘 넘어오는지 확인)
        System.out.println("noticeMap: " + noticeMap);

        ModelAndView mav = new ModelAndView(); // ModelAndView 객체를 생성
        mav.setViewName("/notice/notice"); // 이 뷰로 이동
        mav.addObject("noticeMap", noticeMap); // notice을 mav에 추가하여 뷰로 전달(글 목록을 넘겨줌)

        return mav;
    }

    // 공지사항 폼으로 이동
    @Override
    @RequestMapping("/notice/noticeForm.do") // 공지사항 글쓰기 폼으로 이동
    public ModelAndView noticeForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("/notice/noticeForm"); //여러개 이미지 추가
        return mav;
    }

    // 글쓰기 + 이미지 여러개 추가
    @Override
    @RequestMapping("/notice/addNotice.do")
    public ModelAndView addNotice(MultipartHttpServletRequest multipartRequest, HttpServletResponse response) throws Exception {

        // 인코딩 설정 및 초기화
        String imageFileName = null; // 업로드 된 이미지 파일 이름을 저장 할 변수
        multipartRequest.setCharacterEncoding("utf-8");
        Map<String, Object> noticeMap = new HashMap<>(); // 글 정보를 저장할 맵을 생성
        Enumeration enu = multipartRequest.getParameterNames(); // 모든 매개변수 이름을 가져옴

        //요청 매개변수 읽기 및 저장
        while (enu.hasMoreElements()) { // 각 파라미터 이름에 대해 값을 가져와 noticeMap에 추가
            String name = (String) enu.nextElement();
            String value = multipartRequest.getParameter(name);
            noticeMap.put(name, value); // 파라미터들을 noticeMap에 추가
        }

        //파일 업로드 처리
        List<String> fileList = multiFileUpload(multipartRequest);
        List<NoticeimageDTO> imageFileList = new ArrayList<NoticeimageDTO>();

        //업로드 된 파일이 있을 경우
        if(fileList != null && fileList.size() != 0) {
            System.out.println("이미지있다");
            for(String fileName : fileList) { //업로드 된 파일 리스트를 반본
                NoticeimageDTO noticeimageDTO = new NoticeimageDTO(); // 각 파일마다 noticeimageDTO 객체를 생성
                noticeimageDTO.setImage_file_name(fileName);
                imageFileList.add(noticeimageDTO); // 이미지 파일 리스트에 추가
            }
            noticeMap.put("imageFileList", imageFileList); // 이미지 파일 리스트를 noticMap에 추가
        }
        HttpSession session=multipartRequest.getSession();
        LoginDTO loginDTO=(LoginDTO)session.getAttribute("loginDTO");
        Long id=loginDTO.getId();
        noticeMap.put("id", id);

        //게시글 추가 및 이미지 파일 이동
        try {
            int notice_id = noticeService.addNotice(noticeMap); // 글을 추가하고 작성자아이디를 받아옴
            if(imageFileList != null && imageFileList.size() != 0) { // 이미지 파일이 있을 경우
                for(NoticeimageDTO noticeimageDTO : imageFileList) { // 이미지 파일 리스트를 반복
                    imageFileName = noticeimageDTO.getImage_file_name();

                    //임시 폴더에서 실제 폴더로 파일 이동
                    File srcFile = new File(ARRICLE_IMG_REPO + "\\temp\\" + imageFileName);
                    File destFile = new File(ARRICLE_IMG_REPO + "\\" + notice_id);
                    FileUtils.moveFileToDirectory(srcFile, destFile, true);
                } // for end
            } // if end
        } catch (Exception e) { // 오류 발생 시
            if (imageFileList != null && imageFileList.size() != 0) {
                for (NoticeimageDTO noticeimageDTO : imageFileList) { //이미지 파일 리스트를 반복
                    imageFileName = noticeimageDTO.getImage_file_name();

                    // 임시 폴더의 파일 삭제
                    File srcFile = new File(ARRICLE_IMG_REPO + "\\temp\\" + imageFileName);
                    srcFile.delete(); // 오류 발생 시 temp 폴더의 이미지 삭제
                } //for end
            } //if end
            e.printStackTrace(); // 예외 출력
        } //catch end

        // 글 목록 페이지로 리다이렉트
        ModelAndView mav = new ModelAndView("redirect:/notice/noticeList.do");
        return mav;
    }

    // 여러개의 글과 이미지 상세 글보기
    @RequestMapping("/notice/noticeView.do")
    public ModelAndView noticeView(@RequestParam("notice_id") Long notice_id, HttpServletRequest request, HttpServletResponse response) throws Exception { // notice_id를 매개변수로 받아 공지사항 글을 조회
        Map noticeView = noticeService.noticeView(notice_id); // noticService에서 notice_id에 해당하는 공지사항 글을 조회하며 noticeMap에 조정

        ModelAndView mav = new ModelAndView(); // ModelAndView 객체 생성
        mav.setViewName("/notice/noticeView"); // 뷰 이름 설정
        mav.addObject("noticeView", noticeView); // "noticeMap"이라는 이름으로 ModelAndView 객체에 추가

        return mav; // ModelAndView 객체를 반환
    }

    // 여러개의 글과 이미지 수정
    @Override
    @RequestMapping("/notice/modNotice.do")
    public ModelAndView modNotice(MultipartHttpServletRequest multipartRequest, HttpServletResponse response) throws Exception {
        String imagefilename = null;
        multipartRequest.setCharacterEncoding("utf-8");
        Map<String, Object> noticeMap = new HashMap<>();

        Enumeration enu = multipartRequest.getParameterNames();

        while (enu.hasMoreElements()) {
            String name = (String) enu.nextElement();
            String value = multipartRequest.getParameter(name);
            System.out.println(name + " : " + value);
            noticeMap.put(name, value);
        } // while end
        List<String> fileList = multiFileUpload(multipartRequest);
        String notice_id = (String) noticeMap.get("notice_id");
        List<NoticeimageDTO> imageFileList = new ArrayList<>();
        int modityNumber = 0;

        if (fileList != null && fileList.size() != 0) {
            for (String fileName : fileList) {
                modityNumber++;
                NoticeimageDTO noticeimageDTO = new NoticeimageDTO();
                noticeimageDTO.setImage_file_name(fileName);

                imageFileList.add(noticeimageDTO);
            }
            noticeMap.put("imageFileList", imageFileList);
        }
        noticeMap.put("notice_id", notice_id);

        try {
            noticeService.modNotice(noticeMap);
            if (imageFileList != null && imageFileList.size() != 0) {
                int cnt = 0;
                for (NoticeimageDTO noticeimageDTO : imageFileList) {
                    cnt++;
                    imagefilename = noticeimageDTO.getImage_file_name();
                    if (imagefilename != null && imagefilename != "") {
                        File srcFile = new File(ARRICLE_IMG_REPO + "\\temp\\" + imagefilename);
                        File destDir = new File(ARRICLE_IMG_REPO + "\\" + notice_id);
                        FileUtils.moveFileToDirectory(srcFile, destDir, true);

                        String originalFileName = (String) noticeMap.get("originalFileName" + cnt); // 전의 이미지 , status 에 번호를 붙혀놓음
                        System.out.println("이전 이미지 " + originalFileName);
                        File oldFile = new File(ARRICLE_IMG_REPO + "\\" + notice_id + "\\" + originalFileName); // 파일 저장 위치(기존 이미지)
                        oldFile.delete(); // 기존 이미지 삭제
                    }
                }
            }
        } catch (Exception e) { // 글쓰기 하다 오류나면 여기로 옴
            if (imageFileList != null && imageFileList.size() != 0) {
                for (NoticeimageDTO noticeimageDTO : imageFileList) { // 이미지 전부
                    imagefilename = noticeimageDTO.getImage_file_name();
                    File srcFile = new File(ARRICLE_IMG_REPO + "\\temp\\" + imageFileList);
                    srcFile.delete(); // 오류 발생시 temp 이미지 삭제
                }
            }
            e.printStackTrace();
        }
        ModelAndView mav = new ModelAndView("redirect:/notice/noticeLis.do");
        return mav;
    }

    // 글, 이미지 삭제
    @Override
    @PostMapping("/notice/removeNotice.do") 		// 이미지까지 같이 삭제해야함
    public ModelAndView removeNotice(@RequestParam("notice_id") Long notice_id, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        noticeService.removeNotice(notice_id);
        File imgDir=new File(ARRICLE_IMG_REPO + "\\" + notice_id); // 파일 객체로 만듬
        if(imgDir.exists()) { // 이미지가 있는 글일때 수행
            FileUtils.deleteDirectory(imgDir); // 이 디렉토리(폴더)를 삭제
        }
        ModelAndView mav=new ModelAndView("redirect:/notice/noticeLis.do"); // 글 삭제 후 redirect 로 글목록 포워딩
        return mav;
    }

    // 한개의 이미지파일 업로드 , 글 수정시(이미지 선택안하면) null 이 들어가서 이미지가 사라짐 업로드폴더에는 남아있음.
    private String fileUpload(MultipartHttpServletRequest multipartrequest) throws Exception{
        String imagefilename=null;
        Iterator<String> fileNames=multipartrequest.getFileNames(); // 열거형 객체(여러개)
        while(fileNames.hasNext()) { // has.Next 파일 이름이 없을때 까지 돔
            String fileName=fileNames.next(); // 첨부한 이미지 파일 이름
            MultipartFile mFile=multipartrequest.getFile(fileName); // 파일 크기
            imagefilename=mFile.getOriginalFilename(); // 가져옴
            File file=new File(ARRICLE_IMG_REPO + "\\" + fileName); // 경로 저장
            if(mFile.getSize() != 0) { // 크기가 0인 이미지 거르기
                if(! file.exists()) { // exists 존재하는지(not 이라 존재 안할때) , EX ) 기존에 있던 이미지를 또 추가하면 안됨
                    if(file.getParentFile().mkdir()) { // mkdir 폴더 생성
                        file.createNewFile();
                    } // inner if end
                } // inner if end
                mFile.transferTo(new File(ARRICLE_IMG_REPO + "\\temp\\" + imagefilename)); // 파일 전달 (임시저장소에)
            } // if end
            //return fileList;
        } // while end
        return imagefilename;
    }

    // 여러개의 이미지파일 업로드
    private List<String> multiFileUpload(MultipartHttpServletRequest multipartrequest) throws Exception{
        List<String> fileList=new ArrayList<String>();
        Iterator<String> fileNames=multipartrequest.getFileNames(); // 열거형 객체(여러개)
        while(fileNames.hasNext()) { // has.Next 파일 이름이 없을때 까지 돔
            String fileName=fileNames.next(); // 첨부한 이미지 파일 이름
            MultipartFile mFile=multipartrequest.getFile(fileName); // 파일 크기
            String originalFileName=mFile.getOriginalFilename(); //  파일 name 얻어오기
            fileList.add(originalFileName); // 파일 이름 얻어온걸 하나씩 저장
            File file=new File(ARRICLE_IMG_REPO + "\\" + fileName); // 경로 저장
            if(mFile.getSize() != 0) { // 크기가 0인 이미지 거르기
                if(! file.exists()) { // exists 존재하는지(not이라 존재 안할때) , ex ) 기존에 있던 이미지를 또 추가하면 안됨
                    if(file.getParentFile().mkdir()) { // mkdir 폴더 생성
                        file.createNewFile();
                    } // inner if end
                } // inner if end
                mFile.transferTo(new File(ARRICLE_IMG_REPO + "\\temp\\" + originalFileName)); // 파일 전달 (임시저장소에)
            } // if end
        } // while end
        return fileList;
    }// class end








}