package br.gov.serpro.aplicacao_demoiselle1_ejb.persistence.dao;

import java.util.List;

import br.gov.framework.demoiselle.core.layer.IDAO; 
import br.gov.serpro.aplicacao_demoiselle1_ejb.bean.Bookmark;

public interface IBookmarkDAO extends IDAO<Bookmark>{

	public List<Bookmark> findAll();
	
}
