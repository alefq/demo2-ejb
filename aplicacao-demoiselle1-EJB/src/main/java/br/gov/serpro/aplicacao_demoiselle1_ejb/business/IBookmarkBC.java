package br.gov.serpro.aplicacao_demoiselle1_ejb.business;

import java.util.List;

import br.gov.framework.demoiselle.core.layer.IBusinessController;
import br.gov.serpro.aplicacao_demoiselle1_ejb.bean.Bookmark;


public interface IBookmarkBC extends IBusinessController {

	public void insert(Bookmark bookmark);
	
	public List<Bookmark> getBookmarks();
	
}
