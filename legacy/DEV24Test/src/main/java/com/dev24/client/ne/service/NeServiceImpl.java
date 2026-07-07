package com.dev24.client.ne.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dev24.client.ne.dao.NeDAO;
import com.dev24.client.ne.vo.NeVO;
import com.dev24.client.necmt.dao.NecmtDAO;
import com.dev24.common.file.FileUploadUtil;

import lombok.Setter;

@Service
public class NeServiceImpl implements NeService{

	@Setter(onMethod_ = @Autowired)
	private NeDAO neDAO;
	
	@Setter(onMethod_ = @Autowired)
	private NecmtDAO necmtDAO;
	
	@Override
	public List<NeVO> neList() {
		List<NeVO> neList = neDAO.neList();
		return neList;
	}
	
	@Override
	public NeVO neDetail(int ne_num) {
		NeVO nvo = neDAO.neDetail(ne_num);
		return nvo;
	}
	
	@Override
	@Transactional
	public int neDelete(int ne_num, int replyCnt) throws Exception {
		int deleteReply = 1;
		int result = -1;
		
		NeVO nevo = neDAO.neDetail(ne_num);
		
		if(replyCnt > 0) {
			deleteReply = necmtDAO.necmtDeleteNeNum(ne_num);
		}
		
		String img = nevo.getNe_imgurl();
		if(img != null) {
			FileUploadUtil.fileDelete(img);
		}
		
		if(deleteReply > 0) {
			result = neDAO.neDelete(ne_num);
		}
		
		return result;
	}
	
	@Override
	@Transactional
	public int neInsert(NeVO nevo) throws Exception{
		int result = 0;
		String ne_imgurl = null;
		
		MultipartFile imgFile = nevo.getImgFile();
		
		if (imgFile != null) {
			ne_imgurl = FileUploadUtil.fileUpload(imgFile, "neboard");
		}
		
		nevo.setNe_imgurl(ne_imgurl);
		result = neDAO.neInsert(nevo);
		return result;
	}
}
