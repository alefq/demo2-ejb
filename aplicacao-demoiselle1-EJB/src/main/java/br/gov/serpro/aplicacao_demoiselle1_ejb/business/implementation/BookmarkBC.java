package br.gov.serpro.aplicacao_demoiselle1_ejb.business.implementation;

import java.util.List;

import br.gov.framework.demoiselle.core.layer.integration.Injection;
import br.gov.serpro.aplicacao_demoiselle1_ejb.bean.Bookmark;
import br.gov.serpro.aplicacao_demoiselle1_ejb.business.IBookmarkBC;
import br.gov.serpro.aplicacao_demoiselle1_ejb.persistence.dao.IBookmarkDAO;


public class BookmarkBC implements IBookmarkBC{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Injection
	private IBookmarkDAO bookmarkDAO;
	
	public void insert(Bookmark bookmark) {
		bookmarkDAO.insert(bookmark);
	}
	
	public List<Bookmark> getBookmarks(){
		return bookmarkDAO.findAll();
	}
	
}
